package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AlunoPerfilActivity extends AppCompatActivity {

    // Views do XML
    private TextView tvNomeAluno, tvStatusSolicitacao;
    private TextView tvNivelAluno, tvModalidadeAluno, tvObjetivosAluno; // Campos de info
    private Button btnEditarPerfilAluno; // Botão de editar
    private ImageView ivAvatarAluno;
    private TabLayout tabLayoutAluno;
    private LinearLayout groupBioAluno, groupAvaliacoesAluno;
    private TextView tvBioAluno;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o seu XML
        setContentView(R.layout.activity_aluno_perfil);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Checagem de segurança
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Encontrar as Views
        initViews();

        // Configurar os cliques
        configurarListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega os dados toda vez que a tela volta ao foco
        carregarDadosAluno();
    }

    private void initViews() {
        tvNomeAluno = findViewById(R.id.tvNomeAluno);
        tvStatusSolicitacao = findViewById(R.id.tvStatusSolicitacao);
        btnEditarPerfilAluno = findViewById(R.id.btnEditarPerfilAluno);
        ivAvatarAluno = findViewById(R.id.ivAvatarAluno);

        tabLayoutAluno = findViewById(R.id.tabLayoutAluno);
        groupBioAluno = findViewById(R.id.groupBioAluno);
        groupAvaliacoesAluno = findViewById(R.id.groupAvaliacoesAluno);
        tvBioAluno = findViewById(R.id.tvBioAluno);

        tvNivelAluno = findViewById(R.id.tvNivelAluno);
        tvModalidadeAluno = findViewById(R.id.tvModalidadeAluno);
        tvObjetivosAluno = findViewById(R.id.tvObjetivosAluno);

        // O ID "btnSolicitarProfessor" não existe neste XML,
        // então NÃO tentamos encontrá-lo (isso conserta o crash).
    }

    private void configurarListeners() {
        // Botão para editar dados
        btnEditarPerfilAluno.setOnClickListener(v -> {
            // Abre a tela de edição de perfil PÚBLICO
            Intent intent = new Intent(AlunoPerfilActivity.this, EditarPerfilAlunoActivity.class);
            startActivity(intent);
        });

        // O listener para "btnSolicitarProfessor" foi removido
        // porque o botão não está nesta tela.

        // Listener para o TabLayout (Bio / Avaliações)
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
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Busca os dados do aluno no Firestore e atualiza a UI.
     */
    private void carregarDadosAluno() {
        String uid = currentUser.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Preenche o nome
                        tvNomeAluno.setText(documentSnapshot.getString("nome"));

                        // Carrega a Bio
                        String bio = documentSnapshot.getString("bio");
                        if (bio != null && !bio.isEmpty()) {
                            tvBioAluno.setText(bio);
                        } else {
                            tvBioAluno.setText("O aluno ainda não escreveu uma bio.");
                        }

                        // Carrega Nível
                        String nivel = documentSnapshot.getString("nivel");
                        if (nivel != null && !nivel.isEmpty()) {
                            tvNivelAluno.setText("Nível: " + nivel);
                            tvNivelAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvNivelAluno.setVisibility(View.GONE);
                        }

                        // Carrega Modalidade
                        String modalidade = documentSnapshot.getString("preferenciaModalidade");
                        if (modalidade != null && !modalidade.isEmpty()) {
                            tvModalidadeAluno.setText("Modalidade: " + modalidade);
                            tvModalidadeAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvModalidadeAluno.setVisibility(View.GONE);
                        }

                        // Carrega Objetivos
                        String objetivos = documentSnapshot.getString("objetivos");
                        if (objetivos != null && !objetivos.isEmpty()) {
                            tvObjetivosAluno.setText("Objetivos: " + objetivos);
                            tvObjetivosAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvObjetivosAluno.setVisibility(View.GONE);
                        }

                        // Lógica do status (para o texto "Solicitação Pendente")
                        String status = documentSnapshot.getString("statusSolicitacao");
                        controlarStatusSolicitacao(status);

                    } else {
                        Toast.makeText(this, "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Controla o texto do status da solicitação
     */
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
                // (Não deveria estar aqui, mas é uma segurança)
                tvStatusSolicitacao.setText("Status: Professor Aprovado");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "nenhum":
            default:
                // Se não tem solicitação, esconde o texto
                tvStatusSolicitacao.setVisibility(View.GONE);
                break;
        }
    }
}