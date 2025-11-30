package com.example.aulago;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide; // Importe o Glide
import com.example.aulago.databinding.FragmentAvaliacaoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Objects;

public class AvaliacaoFragment extends Fragment {

    // --- ARGUMENTO SIMPLIFICADO ---
    private static final String ARG_AVALIADO_ID = "avaliado_id";
    private static final String ARG_DATA_HORA = "data_hora";
    private static final String ARG_MODALIDADE = "modalidade";
    private static final String ARG_AULA_ID = "aula_id";

    private FragmentAvaliacaoBinding binding;
    private RatingManager ratingManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Variáveis locais para armazenar os dados recebidos
    private String idDoUsuarioAvaliado;
    private String textoDataHora;
    private String textoModalidade;
    private String idDaAula;

    /**
     * MÉTODO newInstance ATUALIZADO (Agora só precisa do ID)
     */
    public static AvaliacaoFragment newInstance(String avaliadoId, String dataHora, String modalidade, String aulaId) {
        AvaliacaoFragment fragment = new AvaliacaoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AVALIADO_ID, avaliadoId);
        args.putString(ARG_DATA_HORA, dataHora);
        args.putString(ARG_MODALIDADE, modalidade);
        args.putString(ARG_AULA_ID, aulaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            idDoUsuarioAvaliado = getArguments().getString(ARG_AVALIADO_ID);
            textoDataHora = getArguments().getString(ARG_DATA_HORA);
            textoModalidade = getArguments().getString(ARG_MODALIDADE);
            idDaAula = getArguments().getString(ARG_AULA_ID);
        }

        ratingManager = new RatingManager();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAvaliacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (textoDataHora != null) {
            binding.tvDetalhesData.setText(textoDataHora);
        } else {
            binding.tvDetalhesData.setText("--/--");
        }

        if (textoModalidade != null) {
            binding.tvDetalhesModalidade.setText(textoModalidade);

            // Lógica visual para o ícone da modalidade
            if (textoModalidade.toLowerCase().contains("presencial")) {
                binding.ivModalidadeIcon.setImageResource(R.drawable.ic_loc); // Ícone de Localização
            } else {
                binding.ivModalidadeIcon.setImageResource(R.drawable.ic_camera); // Ícone de Câmera
            }
        }

        // Configura Botão Voltar
        binding.btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Busca os dados do perfil (Nome, Foto e Tipo)
        loadAvaliadoData();

        // Botão Enviar
        binding.btnEnviarAvaliacao.setOnClickListener(v -> submeterAvaliacao());
    }

    /**
     * Busca os dados do usuário (avaliado) para exibir na tela
     */
    private void loadAvaliadoData() {
        if (idDoUsuarioAvaliado == null) return;

        db.collection("users").document(idDoUsuarioAvaliado).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding == null) return; // Fragmento foi destruído
                    if (documentSnapshot.exists()) {
                        String nome = documentSnapshot.getString("nome");
                        String urlFotoPerfil = documentSnapshot.getString("urlFotoPerfil");
                        String userType = documentSnapshot.getString("userType");


                        // Atualiza a UI com os dados do usuário

                        binding.tvAvaliacaoTitulo.setText("Avalie " + nome);

                        if ("professor".equalsIgnoreCase(userType)) {
                            binding.tvUserBadge.setText("Professor");
                        } else if ("aluno".equalsIgnoreCase(userType)) {
                            binding.tvUserBadge.setText("Aluno");
                        } else {
                            binding.tvUserBadge.setText("Usuário");
                        }

                        Glide.with(requireContext())
                                .load(urlFotoPerfil)
                                .placeholder(R.drawable.ic_person_placeholder)
                                .error(R.drawable.ic_perfil)
                                .into(binding.ivAvaliacaoProfile);
                    }
                });
    }

    /**
     * MÉTODO submeterAvaliacao ATUALIZADO
     */
    private void submeterAvaliacao() {
        // Validações
        FirebaseUser currentUser = auth.getCurrentUser();
        if (idDoUsuarioAvaliado == null || idDoUsuarioAvaliado.isEmpty() || currentUser == null) {
            Toast.makeText(getContext(), "Erro: Não foi possível identificar os usuários.", Toast.LENGTH_SHORT).show();
            return;
        }

        String idDoAvaliador = currentUser.getUid();
        double nota = binding.ratingBarNota.getRating();
        String comentario = binding.etComentario.getText().toString();

        if (nota == 0) {
            Toast.makeText(getContext(), "Por favor, selecione uma nota.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnEnviarAvaliacao.setEnabled(false);
        binding.btnEnviarAvaliacao.setText("Enviando...");

        // Busca os dados dos perfis antes de salvar
        fetchUserDataAndSubmit(idDoAvaliador, idDoUsuarioAvaliado, nota, comentario);
    }

    /**
     * Busca os dados dos perfis (Avaliador e Avaliado)
     * e então salva a avaliação.
     */
    private void fetchUserDataAndSubmit(String idDoAvaliador, String idDoAvaliado, double nota, String comentario) {

        // 1. Busca o documento do Avaliador (usuário logado)
        db.collection("users").document(idDoAvaliador).get().addOnSuccessListener(avaliadorDoc -> {
            if (!avaliadorDoc.exists()) {
                handleError(new Exception("Documento do avaliador não encontrado."));
                return;
            }

            // 2. Busca o documento do Avaliado (perfil)
            db.collection("users").document(idDoAvaliado).get().addOnSuccessListener(avaliadoDoc -> {
                if (!avaliadoDoc.exists()) {
                    handleError(new Exception("Documento do avaliado não encontrado."));
                    return;
                }

                // 3. Extrai TODOS os dados
                String tipoAvaliador = avaliadorDoc.getString("userType"); // "aluno" ou "professor"
                String nomeAvaliador = avaliadorDoc.getString("nome");
                String avatarAvaliador = avaliadorDoc.getString("urlFotoPerfil");

                String nomeAvaliado = avaliadoDoc.getString("nome");
                String avatarAvaliado = avaliadoDoc.getString("urlFotoPerfil");

                // 4. Monta o objeto ReviewModel COMPLETO
                ReviewModel review = new ReviewModel();
                review.setRating(nota);
                review.setComentario(comentario);
                review.setDataAvaliacao(new Date()); // @ServerTimestamp vai sobrescrever
                review.setEscritoPor(tipoAvaliador);
                review.setAulaId(idDaAula);

                if ("aluno".equals(tipoAvaliador)) {
                    // Aluno (avaliador) avaliando Professor (avaliado)
                    review.setAlunoId(idDoAvaliador);
                    review.setAlunoNome(nomeAvaliador);
                    review.setAlunoAvatarUrl(avatarAvaliador);
                    review.setProfessorId(idDoAvaliado);
                    review.setProfessorNome(nomeAvaliado);
                    review.setProfessorAvatarUrl(avatarAvaliado);
                } else {
                    // Professor (avaliador) avaliando Aluno (avaliado)
                    review.setProfessorId(idDoAvaliador);
                    review.setProfessorNome(nomeAvaliador);
                    review.setProfessorAvatarUrl(avatarAvaliador);
                    review.setAlunoId(idDoAvaliado);
                    review.setAlunoNome(nomeAvaliado);
                    review.setAlunoAvatarUrl(avatarAvaliado);
                }

                // 5. Envia o objeto COMPLETO para o RatingManager
                ratingManager.submitRating(review, idDoAvaliado, new RatingManager.RatingCallback() {
                    @Override
                    public void onSuccess() {
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), "Avaliação enviada com sucesso!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }

                    @Override
                    public void onError(Exception e) {
                        handleError(e);
                    }
                });

            }).addOnFailureListener(this::handleError);
        }).addOnFailureListener(this::handleError);
    }

    // Função auxiliar para tratar erros
    private void handleError(Exception e) {
        if (getContext() == null) return;
        Log.e("AvaliacaoFragment", "Erro ao submeter avaliação", e);
        Toast.makeText(getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        if (binding != null) {
            binding.btnEnviarAvaliacao.setEnabled(true);
            binding.btnEnviarAvaliacao.setText("Enviar Avaliação");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}