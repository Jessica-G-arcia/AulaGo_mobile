package com.example.aulago;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilAlunoActivity extends AppCompatActivity {

    // Campos do Perfil Público
    private TextInputEditText etBioAluno, etNivelAluno, etModalidadePreferida, etObjetivosAluno;

    // --- MOVIDO PARA CÁ ---
    private TextView tvStatusSolicitacao;
    private Button btnSolicitarProfessor;
    // --- FIM ---

    private Button btnSalvarPerfilAluno;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_aluno); // O XML com todos os campos

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }
        uid = user.getUid();

        initViews();
        configurarListeners();
        carregarDadosDoUsuario();
    }

    private void initViews() {
        etBioAluno = findViewById(R.id.etBioAluno);
        etNivelAluno = findViewById(R.id.etNivelAluno);
        etModalidadePreferida = findViewById(R.id.etModalidadePreferida);
        etObjetivosAluno = findViewById(R.id.etObjetivosAluno);
        btnSalvarPerfilAluno = findViewById(R.id.btnSalvarPerfilAluno);

        // --- MOVIDO PARA CÁ ---
        tvStatusSolicitacao = findViewById(R.id.tvStatusSolicitacao);
        btnSolicitarProfessor = findViewById(R.id.btnSolicitarProfessor);
        // --- FIM ---

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void configurarListeners() {
        btnSalvarPerfilAluno.setOnClickListener(v -> salvarPerfilPublico());

        // --- MOVIDO PARA CÁ ---
        btnSolicitarProfessor.setOnClickListener(v -> {
            Intent intent = new Intent(this, SolicitarSerProfessorActivity.class);
            startActivity(intent);
        });
        // --- FIM ---
    }

    private void carregarDadosDoUsuario() {
        progressDialog.setMessage("Carregando dados...");
        progressDialog.show();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();

                    if (document.exists()) {
                        // Preenche os campos do perfil
                        etBioAluno.setText(document.getString("bio"));
                        etNivelAluno.setText(document.getString("nivel"));
                        etModalidadePreferida.setText(document.getString("preferenciaModalidade"));
                        etObjetivosAluno.setText(document.getString("objetivos"));

                        // --- MOVIDO PARA CÁ ---
                        // Controla o status de professor
                        String status = document.getString("statusSolicitacao");
                        controlarStatusProfessor(status);
                        // --- FIM ---

                    } else {
                        Toast.makeText(this, "Erro: Documento não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Controla qual item (botão ou texto) deve ser mostrado na seção "Status"
     */
    private void controlarStatusProfessor(String status) {
        if (status == null) status = "nenhum";

        switch (status) {
            case "pendente_analise":
                tvStatusSolicitacao.setText("Status: Em Análise");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                btnSolicitarProfessor.setVisibility(View.GONE);
                break;
            case "aprovado":
                tvStatusSolicitacao.setText("Status: Professor Aprovado");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                btnSolicitarProfessor.setVisibility(View.GONE);
                break;
            case "rejeitado":
                tvStatusSolicitacao.setText("Status: Solicitação Rejeitada");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                btnSolicitarProfessor.setText("Reenviar Solicitação");
                btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;
            case "nenhum":
            default:
                tvStatusSolicitacao.setVisibility(View.GONE);
                btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Salva os dados do PERFIL PÚBLICO no Firestore
     */
    private void salvarPerfilPublico() {
        progressDialog.setMessage("Salvando perfil...");
        progressDialog.show();

        Map<String, Object> perfilPublico = new HashMap<>();
        perfilPublico.put("bio", etBioAluno.getText().toString().trim());
        perfilPublico.put("nivel", etNivelAluno.getText().toString().trim());
        perfilPublico.put("preferenciaModalidade", etModalidadePreferida.getText().toString().trim());
        perfilPublico.put("objetivos", etObjetivosAluno.getText().toString().trim());

        db.collection("users").document(uid)
                .update(perfilPublico)
                .addOnSuccessListener(aVoid -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, "Perfil público salvo!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}