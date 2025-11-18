package com.example.aulago;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ProfessorPerfilPublicoFragment extends Fragment {

    // ID do professor cujo perfil estamos VENDO (Chave: "PROFESSOR_ID")
    private String professorId;

    // --- VIEWS DO XML ---
    private TextView tvNomeProfessor, tvEspecialidade, tvModalidade, tvIdioma;
    private TextView tvValorPresencial, tvValorOnline, tvBio, tvEmptyReviews;
    private TextView tvProfessorRatingMedia;
    private RatingBar rbProfessorRating;
    private ImageView ivAvatar;
    private MaterialButton btnContratar; // Mapeado para R.id.btnEditar no XML
    private TabLayout tabLayout;
    private LinearLayout groupBio, groupAvaliacoes, groupRating;
    private LinearLayout groupValorPresencial, groupValorOnline;

    // Chips
    private View chipStatusProfessor, chipPlanoPro, chipPlanoPremium;

    // Firebase e Dados
    private FirebaseFirestore db;
    private RecyclerView rvAvaliacoes;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList = new ArrayList<>();

    // -------------------------------------------------------------------
    // METODOLOGIA DE CRIAÇÃO (Factory Pattern)
    // -------------------------------------------------------------------
    public static ProfessorPerfilPublicoFragment newInstance(String professorId) {
        ProfessorPerfilPublicoFragment fragment = new ProfessorPerfilPublicoFragment();
        Bundle args = new Bundle();
        args.putString("PROFESSOR_ID", professorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            professorId = getArguments().getString("PROFESSOR_ID"); // Lendo a chave
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o XML que você enviou
        return inflater.inflate(R.layout.fragment_professor_perfil_publico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Verifica se o ID foi passado
        if (professorId == null) {
            Toast.makeText(getContext(), "Erro: ID do professor não fornecido.", Toast.LENGTH_SHORT).show();
            if (isAdded() && getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        initViews(view);
        configurarListeners();
        carregarDadosProfessor();
        setupReviewRecyclerView(view);
        fetchReviewsForProfessor(professorId);
    }

    // -------------------------------------------------------------------
    // INICIALIZAÇÃO E LISTENERS
    // -------------------------------------------------------------------

    private void initViews(View view) {
        // Header e Avatar
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvNomeProfessor = view.findViewById(R.id.inputNome);

        // Chips de Status
        chipStatusProfessor = view.findViewById(R.id.statusProfessor);
        chipPlanoPro = view.findViewById(R.id.chipPlanoPro);
        chipPlanoPremium = view.findViewById(R.id.chipPlanoPremium);

        // Rating
        groupRating = view.findViewById(R.id.groupRating);
        tvProfessorRatingMedia = view.findViewById(R.id.tvProfessorRatingMedia);
        rbProfessorRating = view.findViewById(R.id.rbProfessorRating);

        // Botão de Ação
        btnContratar = view.findViewById(R.id.btnEditar); // ID no XML é btnEditar, mas a função é Contratar

        // Infos Principais
        tvIdioma = view.findViewById(R.id.inputIdioma);
        tvEspecialidade = view.findViewById(R.id.inputEspecialidade);
        tvModalidade = view.findViewById(R.id.inputModalidade);
        groupValorPresencial = view.findViewById(R.id.groupValorPresencial);
        tvValorPresencial = view.findViewById(R.id.inputValorPresencial);
        groupValorOnline = view.findViewById(R.id.groupValorOnline);
        tvValorOnline = view.findViewById(R.id.inputValorOnline);

        // Tabs e Conteúdo
        tabLayout = view.findViewById(R.id.tabLayout);
        groupBio = view.findViewById(R.id.groupBio);
        tvBio = view.findViewById(R.id.tvBio);
        groupAvaliacoes = view.findViewById(R.id.groupAvaliacoes);
        rvAvaliacoes = view.findViewById(R.id.rvAvaliacoes);
        tvEmptyReviews = view.findViewById(R.id.tvEmptyReviews);
    }

    private void configurarListeners() {
        // 1. Botão de Contratar (Ação principal do perfil público)
        btnContratar.setOnClickListener(v -> {
            mostrarDialogoContratar();
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

    // -------------------------------------------------------------------
    // CARREGAMENTO DE DADOS
    // -------------------------------------------------------------------

    private void carregarDadosProfessor() {
        db.collection("users").document(professorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null || !documentSnapshot.exists()) return;

                    // --- 1. CARREGAMENTO DE INFO BÁSICA (String) ---
                    tvNomeProfessor.setText(documentSnapshot.getString("nome"));
                    tvIdioma.setText("Idioma: " + documentSnapshot.getString("idioma"));
                    tvEspecialidade.setText("Especialidade: " + documentSnapshot.getString("especialidade"));
                    tvModalidade.setText("Modalidade: " + documentSnapshot.getString("modalidade"));
                    tvBio.setText(documentSnapshot.getString("bio")); // Assume que o resto do código trata null/empty

                    // Avatar
                    String urlFotoPerfil = documentSnapshot.getString("urlFotoPerfil");
                    Glide.with(requireContext()).load(urlFotoPerfil).placeholder(R.drawable.img_avatar_circle).error(R.drawable.img_avatar_circle).into(ivAvatar);

                    // --- 2. CARREGAMENTO ULTRA-SEGURO DE VALORES (Price) ---
                    String valorPresencialDisplay = formatPriceSafely(documentSnapshot, "valorPresencial");
                    String valorOnlineDisplay = formatPriceSafely(documentSnapshot, "valorOnline");

                    // Bio
                    String bio = documentSnapshot.getString("bio");
                    tvBio.setText((bio != null && !bio.isEmpty()) ? bio : "Nenhuma bio disponível.");
                    controlarValores(valorPresencialDisplay, valorOnlineDisplay);

                    // --- 3. CARREGAMENTO DE RATING ---
                    Double media = documentSnapshot.getDouble("ratingMedia");
                    Long contagem = documentSnapshot.getLong("ratingCount");
                    controlarRating(media, contagem);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e("ProfessorPerfilPublico", "Erro ao carregar dados: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                });
    }

    private String formatPriceSafely(DocumentSnapshot snapshot, String fieldName) {
        if (!snapshot.contains(fieldName)) {
            return null; // Campo não existe
        }

        // Usa o método genérico get() para evitar o erro de cast
        Object rawValue = snapshot.get(fieldName);

        if (rawValue instanceof String) {
            return (String) rawValue; // Se for String, retorna diretamente
        } else if (rawValue instanceof Number) {
            // Se for Number (um Double ou Long), formata como moeda
            return String.format(Locale.getDefault(), "R$ %.2f", ((Number) rawValue).doubleValue());
        }
        // Se for nulo ou outro tipo que não String/Number, retorna nulo.
        return null;
    }

    private void controlarValores(String valorPresencial, String valorOnline) {
        if (valorPresencial != null && !valorPresencial.isEmpty()) {
            tvValorPresencial.setText(valorPresencial);
            groupValorPresencial.setVisibility(View.VISIBLE);
        } else {
            groupValorPresencial.setVisibility(View.GONE);
        }

        if (valorOnline != null && !valorOnline.isEmpty()) {
            tvValorOnline.setText(valorOnline);
            groupValorOnline.setVisibility(View.VISIBLE);
        } else {
            groupValorOnline.setVisibility(View.GONE);
        }
    }

    private void controlarRating(Double media, Long contagem) {
        if (contagem != null && contagem > 0 && media != null) {
            tvProfessorRatingMedia.setText(String.format(Locale.US, "%.1f", media)); // Exibe 5.0
            rbProfessorRating.setRating(media.floatValue()); // Define as estrelas como 5.0
            groupRating.setVisibility(View.VISIBLE);
        } else {
            groupRating.setVisibility(View.GONE);
        }
    }

    private void setupReviewRecyclerView(View view) {
        if (getContext() == null || rvAvaliacoes == null) return;

        reviewAdapter = new ReviewAdapter(getContext(), reviewList, ReviewAdapter.MODO_EXIBIR_ALUNO);
        rvAvaliacoes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvaliacoes.setAdapter(reviewAdapter);
    }

    private void fetchReviewsForProfessor(String idDoProfessor) {
        db.collection("avaliacoes")
                .whereEqualTo("professorId", idDoProfessor)
                .whereEqualTo("escritoPor", "aluno")
                .orderBy("dataAvaliacao", Query.Direction.DESCENDING)
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) return;
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
                    Log.w("ProfessorPerfilPublico", "Erro ao buscar avaliações.", e);
                });
    }

    private void mostrarDialogoContratar() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Contratar Professor")
                .setMessage("Solicitação de contrato enviada!") // TODO: Implementar lógica
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}