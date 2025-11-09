package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- ADICIONADO: Import do Glide para carregar imagens ---
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

public class ProfessorPerfilActivity extends AppCompatActivity {

    // Views da UI
    private TextView inputNome, inputEspecialidade, inputModalidade, inputValorPresencial, inputValorOnline, tvBio;
    private ImageView ivAvatar;
    private Button btnEditar;
    private Chip statusProfessor; // Selo "Verificado"
    private TabLayout tabLayout;
    private LinearLayout groupBio, groupAvaliacoes;
    private LinearLayout groupValorPresencial, groupValorOnline;

    // --- NOVO: Chips para os Planos ---
    private Chip chipPlanoPro;
    private Chip chipPlanoPremium;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // --- Variáveis para a Lista de Avaliações ---
    private RecyclerView rvAvaliacoes;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> listaDeAvaliacoes;
    private TextView tvEmptyReviews; // TextView para "Nenhuma avaliação"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_perfil);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Encontrar todas as Views do seu XML
        initViews();

        // Configurar os cliques
        configurarListeners();

        // Configurar o RecyclerView de avaliações
        setupReviewRecyclerView();

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Simula o botão "voltar" do sistema
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega (ou recarrega) os dados do perfil e as avaliações
        carregarDadosProfessor();
        carregarAvaliacoes();
    }

    private void initViews() {
        // Banner
        btnEditar = findViewById(R.id.btnEditar);
        ivAvatar = findViewById(R.id.ivAvatar);

        // Info Principal
        inputNome = findViewById(R.id.inputNome);
        statusProfessor = findViewById(R.id.statusProfessor); // Selo "Verificado"

        // --- NOVO: Inicializa os chips dos Planos ---
        chipPlanoPro = findViewById(R.id.chipPlanoPro);
        chipPlanoPremium = findViewById(R.id.chipPlanoPremium);

        // Bloco de Detalhes
        inputEspecialidade = findViewById(R.id.inputEspecialidade);
        inputModalidade = findViewById(R.id.inputModalidade);
        inputValorPresencial = findViewById(R.id.inputValorPresencial);
        inputValorOnline = findViewById(R.id.inputValorOnline);
        groupValorPresencial = findViewById(R.id.groupValorPresencial);
        groupValorOnline = findViewById(R.id.groupValorOnline);

        // TabLayout
        tabLayout = findViewById(R.id.tabLayout);
        groupBio = findViewById(R.id.groupBio);
        groupAvaliacoes = findViewById(R.id.groupAvaliacoes);
        tvBio = findViewById(R.id.tvBio);

        // Views da aba Avaliações
        rvAvaliacoes = findViewById(R.id.rvAvaliacoes);
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews);
    }

    private void configurarListeners() {
        // Botão para abrir a tela de Edição
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(ProfessorPerfilActivity.this, EditarPerfilProfessorActivity.class);
            startActivity(intent);
        });

        // Listener para o TabLayout (Bio / Avaliações)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // Posição 0 = "Bio"
                    groupBio.setVisibility(View.VISIBLE);
                    groupAvaliacoes.setVisibility(View.GONE);
                } else { // Posição 1 = "Avaliações"
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

    /**
     * Busca os dados do professor no Firestore e atualiza a UI
     */
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
                        String modalidade = document.getString("preferenciaAula");
                        String bio = document.getString("bio");

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

                        // --- CÓDIGO DA FOTO DESCOMENTADO ---
                        //Carregar foto do avatar (usei o campo "urlFotoPerfil" da sua imagem)
                        String fotoUrl = document.getString("urlFotoPerfil");
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.img_avatar_circle) // Imagem padrão
                                    .error(R.drawable.img_avatar_circle)       // Imagem de erro
                                    .into(ivAvatar);
                        }

                    } else {
                        Toast.makeText(this, "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    // --- Método para configurar a lista de reviews ---
    private void setupReviewRecyclerView() {
        listaDeAvaliacoes = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, listaDeAvaliacoes, ReviewAdapter.MODO_EXIBIR_ALUNO);
        rvAvaliacoes.setLayoutManager(new LinearLayoutManager(this));
        rvAvaliacoes.setAdapter(reviewAdapter);
    }

    // --- Método para carregar as avaliações do Firebase ---
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