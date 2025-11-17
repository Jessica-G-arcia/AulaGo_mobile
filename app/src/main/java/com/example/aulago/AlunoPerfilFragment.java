package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Importe o Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar; // Importe o RatingBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Importe
import androidx.recyclerview.widget.RecyclerView; // Importe

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Importe
import com.google.firebase.firestore.QueryDocumentSnapshot; // Importe

import java.util.ArrayList; // Importe
import java.util.List; // Importe
import java.util.Locale; // Importe

public class AlunoPerfilFragment extends Fragment {

    // --- VARIÁVEIS DE VIEW (Corrigidas e Completas) ---
    private TextView tvNomeAluno, tvStatusSolicitacao;
    private TextView tvNivelAluno, tvModalidadeAluno, tvObjetivosAluno;
    private Button btnEditarPerfilAluno;
    private ImageView ivAvatarAluno;
    private TabLayout tabLayoutAluno;
    private LinearLayout groupBioAluno, groupAvaliacoesAluno;
    private TextView tvBioAluno;

    // --- VARIÁVEIS QUE FALTAVAM ---
    private LinearLayout groupRating;
    private TextView tvAlunoRatingMedia;
    private RatingBar rbAlunoRating;
    private RecyclerView rvAvaliacoes;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList = new ArrayList<>();
    private TextView tvEmptyReviews;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aluno_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser == null) {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }

        initViews(view);
        configurarListeners();
        setupReviewRecyclerView(); // <-- CHAME O SETUP DO RECYCLER
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarDadosAluno();
        fetchReviewsForAluno(); // <-- CARREGUE AS AVALIAÇÕES
    }

    private void initViews(View view) {
        // --- SEÇÃO DO CABEÇALHO ---
        ivAvatarAluno = view.findViewById(R.id.ivAvatarAluno);
        btnEditarPerfilAluno = view.findViewById(R.id.btnEditar);

        // --- SEÇÃO DO NOME ---
        tvNomeAluno = view.findViewById(R.id.tvNomeAluno);
        tvStatusSolicitacao = view.findViewById(R.id.tvStatusSolicitacao);

        // --- SEÇÃO DO RATING (Corrigido) ---
        groupRating = view.findViewById(R.id.groupRating);
        tvAlunoRatingMedia = view.findViewById(R.id.tvAlunoRatingMedia);
        rbAlunoRating = view.findViewById(R.id.rbAlunoRating);

        // --- SEÇÃO DE INFORMAÇÕES ---
        tvNivelAluno = view.findViewById(R.id.tvNivelAluno);
        tvModalidadeAluno = view.findViewById(R.id.tvModalidadeAluno);
        tvObjetivosAluno = view.findViewById(R.id.tvObjetivosAluno);

        // --- SEÇÃO DAS ABAS (TABS) ---
        tabLayoutAluno = view.findViewById(R.id.tabLayoutAluno);

        // --- CONTEÚDO DA ABA "SOBRE MIM" ---
        groupBioAluno = view.findViewById(R.id.groupBio);
        tvBioAluno = view.findViewById(R.id.tvBio); // ID de dentro do bio_card.xml

        // --- CONTEÚDO DA ABA "AVALIAÇÕES" ---
        groupAvaliacoesAluno = view.findViewById(R.id.groupAvaliacoesAluno);
        rvAvaliacoes = view.findViewById(R.id.rvAvaliacoes);
        tvEmptyReviews = view.findViewById(R.id.tvEmptyReviews);
    }

    /**
     * CORRIGIDO: Adicionado o listener do TabLayout
     */
    private void configurarListeners() {
        if (btnEditarPerfilAluno != null) {
            btnEditarPerfilAluno.setOnClickListener(v -> {
                if (getActivity() instanceof ToolbarActivity) {
                    ((ToolbarActivity) getActivity()).replaceFragment(new EditarPerfilAlunoFragment());
                } else {
                    Log.e("AlunoPerfilFragment", "Não foi possível abrir a tela de edição.");
                }
            });
        }

        // --- LÓGICA DAS ABAS (FALTAVA) ---
        tabLayoutAluno.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // "Sobre Mim"
                    groupBioAluno.setVisibility(View.VISIBLE);
                    groupAvaliacoesAluno.setVisibility(View.GONE);
                } else { // "Minhas Avaliações"
                    groupBioAluno.setVisibility(View.GONE);
                    groupAvaliacoesAluno.setVisibility(View.VISIBLE);
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

    /**
     * ATUALIZADO: Agora também carrega a foto e a NOTA MÉDIA.
     */
    private void carregarDadosAluno() {
        String uid = currentUser.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    if (documentSnapshot.exists()) {

                        tvNomeAluno.setText(documentSnapshot.getString("nome"));

                        String urlFotoPerfil = documentSnapshot.getString("urlFotoPerfil");
                        Glide.with(requireContext())
                                .load(urlFotoPerfil)
                                .placeholder(R.drawable.img_avatar_circle)
                                .error(R.drawable.img_avatar_circle)
                                .into(ivAvatarAluno);

                        String bio = documentSnapshot.getString("bio");
                        tvBioAluno.setText((bio != null && !bio.isEmpty()) ? bio : "Nenhuma bio disponível.");

                        String nivel = documentSnapshot.getString("nivel");
                        if (nivel != null && !nivel.isEmpty()) {
                            tvNivelAluno.setText("Nível: " + nivel);
                            tvNivelAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvNivelAluno.setVisibility(View.GONE);
                        }

                        String modalidade = documentSnapshot.getString("preferenciaModalidade");
                        if (modalidade != null && !modalidade.isEmpty()) {
                            tvModalidadeAluno.setText("Modalidade: " + modalidade);
                            tvModalidadeAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvModalidadeAluno.setVisibility(View.GONE);
                        }

                        String objetivos = documentSnapshot.getString("objetivos");
                        if (objetivos != null && !objetivos.isEmpty()) {
                            tvObjetivosAluno.setText("Objetivos: " + objetivos);
                            tvObjetivosAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvObjetivosAluno.setVisibility(View.GONE);
                        }

                        String status = documentSnapshot.getString("statusSolicitacao");
                        controlarStatusSolicitacao(status);

                        // --- LÓGICA DE RATING (FALTAVA) ---
                        if (documentSnapshot.contains("ratingMedia") && documentSnapshot.contains("ratingCount")) {
                            double media = documentSnapshot.getDouble("ratingMedia");
                            long contagem = documentSnapshot.getLong("ratingCount");

                            if (contagem > 0) {
                                tvAlunoRatingMedia.setText(String.format(Locale.US, "%.1f", media));
                                rbAlunoRating.setRating((float) media);
                                groupRating.setVisibility(View.VISIBLE);
                            } else {
                                groupRating.setVisibility(View.GONE);
                            }
                        } else {
                            groupRating.setVisibility(View.GONE);
                        }

                    } else {
                        Toast.makeText(requireContext(), "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void controlarStatusSolicitacao(String status) {
        if (status == null) status = "nenhum";

        switch (status) {
            case "pendente_analise":
                tvStatusSolicitacao.setText("Solicitação: Em Análise");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "rejeitado":
                tvStatusSolicitacao.setText("Solicitação: Rejeitada");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "aprovado":
                tvStatusSolicitacao.setText("Status: Professor Aprovado");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "nenhum":
            default:
                tvStatusSolicitacao.setVisibility(View.GONE);
                break;
        }
    }

    private void setupReviewRecyclerView() {
        // No perfil do Aluno, vemos as avaliações dos PROFESSORES
        reviewAdapter = new ReviewAdapter(getContext(), reviewList, ReviewAdapter.MODO_EXIBIR_PROFESSOR);
        rvAvaliacoes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvaliacoes.setAdapter(reviewAdapter);
    }

    private void fetchReviewsForAluno() {
        String uid = currentUser.getUid();

        db.collection("avaliacoes")
                .whereEqualTo("alunoId", uid) // Avaliações DESTE aluno
                .whereEqualTo("escritoPor", "professor") // Escritas PELO professor
                .orderBy("dataAvaliacao", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    reviewList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        reviewList.add(document.toObject(ReviewModel.class));
                    }
                    reviewAdapter.updateList(reviewList);

                    if (reviewList.isEmpty()) {
                        tvEmptyReviews.setVisibility(View.VISIBLE);
                        rvAvaliacoes.setVisibility(View.GONE);
                    } else {
                        tvEmptyReviews.setVisibility(View.GONE);
                        rvAvaliacoes.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("AlunoPerfilFragment", "Erro ao buscar avaliações.", e);
                });
    }
}