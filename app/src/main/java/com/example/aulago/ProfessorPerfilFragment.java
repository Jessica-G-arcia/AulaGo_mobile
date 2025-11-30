package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // <-- MUDOU
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfessorPerfilFragment extends Fragment {

    // Views da UI
    private TextView inputNome, inputIdioma, inputEspecialidade, inputModalidade, inputValorPresencial, inputValorOnline, tvBio;
    private ImageView ivAvatar;
    private Button btnEditar;
    private Chip statusProfessor, chipPlanoPro, chipPlanoPremium;
    private TabLayout tabLayout;
    private LinearLayout groupBio, groupAvaliacoes;
    private LinearLayout groupValorPresencial, groupValorOnline;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Lista de Avaliações
    private RecyclerView rvAvaliacoes;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> listaDeAvaliacoes;
    private TextView tvEmptyReviews;
    private LinearLayout groupRating;
    private TextView tvProfessorRatingMedia;
    private RatingBar rbProfessorRating;

    // O 'onCreate' de um Fragmento é para dados, não para Views
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    // O 'onCreateView' é para carregar o XML
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Carrega o seu ficheiro XML renomeado
        return inflater.inflate(R.layout.fragment_professor_perfil, container, false);
    }

    // O 'onViewCreated' é para configurar as Views (o seu 'onCreate' antigo)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser == null) {
            // MUDOU: Usa requireActivity() para o contexto
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }

        // Encontrar todas as Views (agora precisa do 'view.')
        initViews(view);

        // Configurar os cliques
        configurarListeners();

        // Configurar o RecyclerView de avaliações
        setupReviewRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Carrega (ou recarrega) os dados do perfil e as avaliações
        carregarDadosProfessor();
        carregarAvaliacoes();
    }

    // MUDOU: Este método agora precisa da 'view'
    private void initViews(View view) {
        // Banner
        btnEditar = view.findViewById(R.id.btnEditar);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        // Info Principal
        inputNome = view.findViewById(R.id.inputNome);
        statusProfessor = view.findViewById(R.id.statusProfessor);
        chipPlanoPro = view.findViewById(R.id.chipPlanoPro);
        chipPlanoPremium = view.findViewById(R.id.chipPlanoPremium);

        // Bloco de Detalhes
        inputIdioma = view.findViewById(R.id.inputIdioma);
        inputEspecialidade = view.findViewById(R.id.inputEspecialidade);
        inputModalidade = view.findViewById(R.id.inputModalidade);
        inputValorPresencial = view.findViewById(R.id.inputValorPresencial);
        inputValorOnline = view.findViewById(R.id.inputValorOnline);
        groupValorPresencial = view.findViewById(R.id.groupValorPresencial);
        groupValorOnline = view.findViewById(R.id.groupValorOnline);

        // TabLayout
        tabLayout = view.findViewById(R.id.tabLayout);
        groupBio = view.findViewById(R.id.groupBio);
        groupAvaliacoes = view.findViewById(R.id.groupAvaliacoes);
        tvBio = view.findViewById(R.id.tvBio);

        // Views da aba Avaliações
        rvAvaliacoes = view.findViewById(R.id.rvAvaliacoes);
        tvEmptyReviews = view.findViewById(R.id.tvEmptyReviews);
        groupRating = view.findViewById(R.id.groupRating);
        tvProfessorRatingMedia = view.findViewById(R.id.tvProfessorRatingMedia);
        rbProfessorRating = view.findViewById(R.id.rbProfessorRating);
    }

    private void configurarListeners() {

        // Botão para abrir a tela de Edição de Perfil PÚBLICO
        btnEditar.setOnClickListener(v -> {

            // Pede para a Activity "pai" (ToolbarActivity) fazer a troca.
            if (getActivity() instanceof ToolbarActivity) {
                ((ToolbarActivity) getActivity()).replaceFragment(new EditarPerfilProfessorFragment());
            }
        });

        // Listener para o TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    groupBio.setVisibility(View.VISIBLE);
                    groupAvaliacoes.setVisibility(View.GONE);
                } else {
                    groupBio.setVisibility(View.GONE);
                    groupAvaliacoes.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void carregarDadosProfessor() {
        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {

                        // Nome
                        inputNome.setText(document.getString("nome"));

                        // --- LÓGICA DE STATUS E PLANOS ---

                        // 1. Lógica do Selo "Verificado" (baseado na APROVAÇÃO)
                        if ("aprovado".equals(document.getString("statusSolicitacao"))) {
                            statusProfessor.setVisibility(View.VISIBLE);
                        } else {
                            statusProfessor.setVisibility(View.GONE);
                        }

                        // 2. --- NOVO: Lógica do Selo de PLANO (baseado em "statusVerificacao") ---
                        String plano = document.getString("statusVerificacao");

                        // Primeiro, garantimos que os dois chips de plano estão escondidos
                        chipPlanoPro.setVisibility(View.GONE);
                        chipPlanoPremium.setVisibility(View.GONE);

                        // Agora, mostramos o chip correto de acordo com o plano
                        if (plano != null) {
                            switch (plano) {
                                case "plano_pro":
                                    chipPlanoPro.setVisibility(View.VISIBLE);
                                    statusProfessor.setVisibility(View.GONE);
                                    break;
                                case "plano_premium":
                                    chipPlanoPremium.setVisibility(View.VISIBLE);
                                    statusProfessor.setVisibility(View.GONE);
                                    break;
                                case "plano_gratuito":
                                case "nenhum": // Cobre o valor antigo também
                                default:
                                    // Não faz nada, os chips já estão escondidos
                                    break;
                            }
                        }

                        // --- FIM DA LÓGICA DE PLANOS ---


                        String especialidade = document.getString("especialidade");
                        String modalidade = document.getString("preferenciaModalidade");
                        String bio = document.getString("bio");
                        String idioma = document.getString("idioma");

                        if (idioma != null && !idioma.isEmpty()) {
                            inputIdioma.setText(idioma);
                        } else {
                            inputIdioma.setText("Idioma não definido");
                        }

                        if (especialidade != null && !especialidade.isEmpty()) {
                            inputEspecialidade.setText(especialidade);
                        } else {
                            inputEspecialidade.setText("Especialidade não definida");
                        }

                        if (modalidade != null && !modalidade.isEmpty()) {
                            inputModalidade.setText(modalidade);
                        } else {
                            inputModalidade.setText("Modalidade não definida");
                        }

                        if (bio != null && !bio.isEmpty()) {
                            tvBio.setText(bio);
                        } else {
                            tvBio.setText("O professor ainda não escreveu uma bio.");
                        }

                        // Preenche os valores formatados
                        Double valorP = document.getDouble("valorPresencial");
                        Double valorO = document.getDouble("valorOnline");

                        // Lógica para Valor Presencial
                        if (valorP != null && valorP > 0) {
                            inputValorPresencial.setText(String.format(Locale.getDefault(), "R$ %.2f", valorP));
                            groupValorPresencial.setVisibility(View.VISIBLE);
                        } else {
                            groupValorPresencial.setVisibility(View.GONE);
                        }

                        // Lógica para Valor Online
                        if (valorO != null && valorO > 0) {
                            inputValorOnline.setText(String.format(Locale.getDefault(), "R$ %.2f", valorO));
                            groupValorOnline.setVisibility(View.VISIBLE);
                        } else {
                            groupValorOnline.setVisibility(View.GONE);
                        }

                        // Carregar foto do avatar
                        String urlFotoPerfil = document.getString("urlFotoPerfil");
                        if (urlFotoPerfil != null && !urlFotoPerfil.isEmpty()) {
                            // MUDOU: 'this' para 'requireContext()'
                            Glide.with(requireContext())
                                    .load(urlFotoPerfil)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .error(R.drawable.img_avatar_circle)
                                    .into(ivAvatar);
                        }
                        if (document.contains("ratingMedia") && document.contains("ratingCount")) {
                            double media = document.getDouble("ratingMedia");
                            long contagem = document.getLong("ratingCount");

                            if (contagem > 0) {
                                // Exibe a nota (ex: "4.8")
                                tvProfessorRatingMedia.setText(String.format(Locale.US, "%.1f", media));
                                // Preenche as estrelas
                                rbProfessorRating.setRating((float) media);
                                // Mostra o grupo
                                groupRating.setVisibility(View.VISIBLE);
                            } else {
                                // Se não tem avaliações, esconde o grupo
                                groupRating.setVisibility(View.GONE);
                            }
                        } else {
                            // Se os campos não existem, esconde o grupo
                            groupRating.setVisibility(View.GONE);
                        }
                    } else {
                        // MUDOU: 'this' para 'requireContext()'
                        Toast.makeText(requireContext(), "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // MUDOU: 'this' para 'requireContext()'
                    Toast.makeText(requireContext(), "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupReviewRecyclerView() {
        listaDeAvaliacoes = new ArrayList<>();
        // MUDOU: 'this' para 'requireContext()'
        reviewAdapter = new ReviewAdapter(requireContext(), listaDeAvaliacoes, ReviewAdapter.MODO_EXIBIR_ALUNO);
        rvAvaliacoes.setLayoutManager(new LinearLayoutManager(requireContext())); // <-- MUDOU
        rvAvaliacoes.setAdapter(reviewAdapter);
    }

    private void carregarAvaliacoes() {
        String uid = currentUser.getUid();

        db.collection("avaliacoes")
                .whereEqualTo("professorId", uid) // Avaliações DESTE professor
                .whereEqualTo("escritoPor", "aluno") // Escritas PELO aluno
                .orderBy("dataAvaliacao", Query.Direction.DESCENDING) // Mais recentes primeiro
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Converte os documentos do Firestore em objetos ReviewModel
                        listaDeAvaliacoes = task.getResult().toObjects(ReviewModel.class);
                        reviewAdapter.updateList(listaDeAvaliacoes);

                        // Lógica para mostrar/esconder o estado vazio
                        if (listaDeAvaliacoes.isEmpty()) {
                            rvAvaliacoes.setVisibility(View.GONE);
                            tvEmptyReviews.setVisibility(View.VISIBLE);
                            tvEmptyReviews.setText("Você ainda não recebeu avaliações.");
                        } else {
                            rvAvaliacoes.setVisibility(View.VISIBLE);
                            tvEmptyReviews.setVisibility(View.GONE);
                        }

                    } else {
                        Log.w("Firestore", "Erro ao buscar avaliações.", task.getException());
                        // Mostra o estado vazio em caso de erro também
                        rvAvaliacoes.setVisibility(View.GONE);
                        tvEmptyReviews.setVisibility(View.VISIBLE);
                        tvEmptyReviews.setText("Erro ao carregar avaliações.");
                    }
                });
    }
}