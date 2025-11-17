package com.example.aulago;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlunoPerfilPublicoFragment extends Fragment {

    // ID do aluno cujo perfil estamos VENDO
    private String alunoId;

    // Views do XML
    private TextView tvNomeAluno, tvNivelAluno, tvModalidadeAluno, tvObjetivosAluno, tvBioAluno;
    private TextView tvAlunoRatingMedia, tvEmptyReviews;
    private RatingBar rbAlunoRating;
    private ImageView ivAvatar;
    private MaterialButton btnSolicitarContato; // <-- Corresponde ao btnEditar no XML
    private TabLayout tabLayout;
    private LinearLayout groupBio, groupAvaliacoes, groupRating;

    // Firebase
    private FirebaseFirestore db;

    // RecyclerView de Avaliações
    private RecyclerView rvAvaliacoes;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList = new ArrayList<>();

    /**
     * Método de fábrica OBRIGATÓRIO.
     *
     * @param alunoId O ID do aluno que você quer ver.
     */
    public static AlunoPerfilPublicoFragment newInstance(String alunoId) {
        AlunoPerfilPublicoFragment fragment = new AlunoPerfilPublicoFragment();
        Bundle args = new Bundle();
        args.putString("ALUNO_ID", alunoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            alunoId = getArguments().getString("ALUNO_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o SEU NOVO XML de perfil público
        return inflater.inflate(R.layout.fragment_aluno_perfil_publico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Verifica se o ID foi passado
        if (alunoId == null) {
            Toast.makeText(getContext(), "Erro: ID do aluno não fornecido.", Toast.LENGTH_SHORT).show();
            if (isAdded() && getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        initViews(view);
        configurarListeners();
        carregarDadosAluno();
        setupReviewRecyclerView(view);
        fetchReviewsForAluno(alunoId);
    }

    private void initViews(View view) {
        // IDs do seu novo XML
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvNomeAluno = view.findViewById(R.id.inputNome);

        // Rating
        groupRating = view.findViewById(R.id.groupRating);
        tvAlunoRatingMedia = view.findViewById(R.id.tvAlunoRatingMedia);
        rbAlunoRating = view.findViewById(R.id.rbAlunoRating);

        // Botão (ID "btnEditar" no XML, mas é de "solicitar")
        btnSolicitarContato = view.findViewById(R.id.btnEditar);

        // Campos de Informação
        tvNivelAluno = view.findViewById(R.id.tvNivelAluno);
        tvModalidadeAluno = view.findViewById(R.id.tvModalidadeAluno);
        tvObjetivosAluno = view.findViewById(R.id.tvObjetivosAluno);

        // Tabs
        tabLayout = view.findViewById(R.id.tabLayout);
        groupBio = view.findViewById(R.id.groupBio);
        tvBioAluno = view.findViewById(R.id.tvBio);
        groupAvaliacoes = view.findViewById(R.id.groupAvaliacoes);

        // Reviews
        rvAvaliacoes = view.findViewById(R.id.rvAvaliacoes);
        tvEmptyReviews = view.findViewById(R.id.tvEmptyReviews);
    }

    private void configurarListeners() {
        // 1. Botão de Solicitar Contato
        btnSolicitarContato.setOnClickListener(v -> {
            mostrarDialogoSolicitar();
        });

        // 2. Listener do TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // "Sobre Mim"
                    groupBio.setVisibility(View.VISIBLE);
                    groupAvaliacoes.setVisibility(View.GONE);
                } else { // "Avaliações"
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

    private void carregarDadosAluno() {
        // Carrega os dados do ALUNO_ID, e não do currentUser
        db.collection("users").document(alunoId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null || !documentSnapshot.exists()) return;

                    // Nome
                    tvNomeAluno.setText(documentSnapshot.getString("nome"));

                    // Avatar
                    String urlFotoPerfil = documentSnapshot.getString("urlFotoPerfil");
                    Glide.with(requireContext()).load(urlFotoPerfil)
                            .placeholder(R.drawable.img_avatar_circle)
                            .error(R.drawable.img_avatar_circle)
                            .into(ivAvatar);

                    // Bio
                    String bio = documentSnapshot.getString("bio");
                    tvBioAluno.setText((bio != null && !bio.isEmpty()) ? bio : "Nenhuma bio disponível.");

                    // Nível
                    String nivel = documentSnapshot.getString("nivel");
                    if (nivel != null && !nivel.isEmpty()) {
                        tvNivelAluno.setText("Nível: " + nivel);
                        tvNivelAluno.setVisibility(View.VISIBLE);
                    } else {
                        tvNivelAluno.setVisibility(View.GONE);
                    }

                    // Modalidade
                    String modalidade = documentSnapshot.getString("preferenciaModalidade");
                    if (modalidade != null && !modalidade.isEmpty()) {
                        tvModalidadeAluno.setText("Modalidade: " + modalidade);
                        tvModalidadeAluno.setVisibility(View.VISIBLE);
                    } else {
                        tvModalidadeAluno.setVisibility(View.GONE);
                    }

                    // Objetivos
                    String objetivos = documentSnapshot.getString("objetivos");
                    if (objetivos != null && !objetivos.isEmpty()) {
                        tvObjetivosAluno.setText("Objetivos: " + objetivos);
                        tvObjetivosAluno.setVisibility(View.VISIBLE);
                    } else {
                        tvObjetivosAluno.setVisibility(View.GONE);
                    }

                    // Carrega Média de Rating
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
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Configura o RecyclerView
     */
    private void setupReviewRecyclerView(View view) {
        if (getContext() == null || rvAvaliacoes == null) return;

        // No perfil do Aluno, queremos ver as reviews dos PROFESSORES
        reviewAdapter = new ReviewAdapter(getContext(), reviewList, ReviewAdapter.MODO_EXIBIR_PROFESSOR);
        rvAvaliacoes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvaliacoes.setAdapter(reviewAdapter);
    }

    /**
     * Busca as avaliações feitas por professores para este aluno
     */
    private void fetchReviewsForAluno(String idDoAluno) {
        db.collection("avaliacoes")
                // Filtra por avaliações ONDE o alunoId é o do perfil
                .whereEqualTo("alunoId", idDoAluno)
                // Filtra apenas por reviews escritas por "professor"
                .whereEqualTo("escritoPor", "professor")
                .orderBy("dataAvaliacao", Query.Direction.DESCENDING)
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) return;
                    reviewList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        reviewList.add(document.toObject(ReviewModel.class));
                    }
                    if (reviewAdapter != null) {
                        reviewAdapter.updateList(reviewList);
                    }

                    // Mostra ou esconde a mensagem de "sem avaliações"
                    if (reviewList.isEmpty()) {
                        tvEmptyReviews.setVisibility(View.VISIBLE);
                        rvAvaliacoes.setVisibility(View.GONE);
                    } else {
                        tvEmptyReviews.setVisibility(View.GONE);
                        rvAvaliacoes.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("AlunoPerfilPublico", "Erro ao buscar avaliações.", e);
                });
    }

    /**
     * Diálogo de solicitação
     */
    private void mostrarDialogoSolicitar() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Solicitar Contato")
                .setMessage("Solicitação de contato enviada!") // TODO: Implementar lógica
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}