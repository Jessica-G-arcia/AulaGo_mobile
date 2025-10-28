package com.example.aulago;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilActivity extends AppCompatActivity {

    private DadosUsuario dadosUsuario;
    private FirebaseFirestore db;
    private String uid;

    // Campos de Edição (Dados Privados)
    private EditText inputNome, inputTelefone, inputCpf, inputDtNasc;
    private EditText inputEndereco, inputNumero, inputComplemento, inputBairro, inputCidade, inputCep;


    // --- Seção "Ser Professor" (A parte chave da demo) ---
    private Button btnSolicitarProfessor;
    private TextView tvStatusSolicitacao;

    private Button btnSalvar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil); // (XML da conversa anterior)

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Receber os dados da ProfessorPerfilActivity (ou da Home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            dadosUsuario = getIntent().getSerializableExtra("DADOS_USUARIO", DadosUsuario.class);
        } else {
            dadosUsuario = (DadosUsuario) getIntent().getSerializableExtra("DADOS_USUARIO");
        }

        if (dadosUsuario == null) {
            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        preencherDadosParaEdicao();
        configurarLogicaProfessor(); // <-- Chama a lógica de status
        configurarListeners();

    }

    private void initViews() {
        inputNome = findViewById(R.id.inputNome);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputCpf = findViewById(R.id.inputCpf);
        inputDtNasc = findViewById(R.id.inputDtNasc);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputNumero = findViewById(R.id.inputNumero);
        inputComplemento = findViewById(R.id.inputComplemento);
        inputBairro = findViewById(R.id.inputBairro);
        inputCidade = findViewById(R.id.inputCidade);
        inputCep = findViewById(R.id.inputCep);


        // Seção Professor
        btnSolicitarProfessor = findViewById(R.id.btnSolicitarProfessor);
        tvStatusSolicitacao = findViewById(R.id.tvStatusSolicitacao);

        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void preencherDadosParaEdicao() {
        inputNome.setText(dadosUsuario.getNome());
        inputTelefone.setText(dadosUsuario.getTelefone());
        inputCpf.setText(dadosUsuario.getCpf());
        inputDtNasc.setText(dadosUsuario.getDtNasc());
        inputEndereco.setText(dadosUsuario.getEndereco());
        inputBairro.setText(dadosUsuario.getBairro());
        inputCidade.setText(dadosUsuario.getCidade());
        inputCep.setText(dadosUsuario.getCep());
        inputNumero.setText(dadosUsuario.getNumero());
        inputComplemento.setText(dadosUsuario.getComplemento());

    }


//     ESTA É A LÓGICA CENTRAL DA SUA APRESENTAÇÃO | Ela lê o status do 'dadosUsuario' e muda a UI.

    private void configurarLogicaProfessor() {

        if (dadosUsuario.isProfessorVerificado()) {
            // Roteiro Passo 6: Aprovado pelo Admin
            btnSolicitarProfessor.setVisibility(View.GONE);
            tvStatusSolicitacao.setVisibility(View.VISIBLE);
            tvStatusSolicitacao.setText("✓ Você é um professor verificado.");
            tvStatusSolicitacao.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        } else if (dadosUsuario.temSolicitacaoPendente()) {
            // Roteiro Passo 3: Solicitação em análise
            btnSolicitarProfessor.setVisibility(View.GONE);
            tvStatusSolicitacao.setVisibility(View.VISIBLE);
            tvStatusSolicitacao.setText("Sua solicitação está em análise.");
            tvStatusSolicitacao.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        } else if ("rejeitado".equals(dadosUsuario.getStatusSolicitacao())) {
            // Roteiro (Extra): Admin rejeitou
            btnSolicitarProfessor.setVisibility(View.VISIBLE);
            btnSolicitarProfessor.setText("Reenviar Solicitação");
            tvStatusSolicitacao.setVisibility(View.VISIBLE);
            tvStatusSolicitacao.setText("Rejeitado: " + dadosUsuario.getMotivoRejeicao());
            tvStatusSolicitacao.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        } else {
            // Roteiro Passo 1: Nunca solicitou
            btnSolicitarProfessor.setVisibility(View.VISIBLE);
            btnSolicitarProfessor.setText("Quero ser Professor");
            tvStatusSolicitacao.setVisibility(View.GONE);
        }

        btnSolicitarProfessor.setOnClickListener(v -> {
            // Roteiro Passo 2: Inicia a tela de solicitação
            Intent intent = new Intent(this, SolicitarSerProfessorActivity.class);
            startActivity(intent);
        });
    }

    private void configurarListeners() {
        btnSalvar.setOnClickListener(v -> salvarAlteracoes());
    }


//     Salva apenas os dados pessoais (nome, endereço, etc.).
//     NÃO mexe nos campos de admin (status, verificado, etc.).


    private void salvarAlteracoes() {
        mostrarProgressDialog("Salvando...");

        // Cria um mapa APENAS com os dados que o usuário pode editar
        Map<String, Object> dadosEditados = new HashMap<>();
        dadosEditados.put("nome", inputNome.getText().toString());
        dadosEditados.put("telefone", inputTelefone.getText().toString());
        dadosEditados.put("cpf", inputCpf.getText().toString());
        dadosEditados.put("dtNasc", inputDtNasc.getText().toString());
        dadosEditados.put("endereco", inputEndereco.getText().toString());
        dadosEditados.put("numero", inputNumero.getText().toString());
        dadosEditados.put("complemento", inputComplemento.getText().toString());
        dadosEditados.put("bairro", inputBairro.getText().toString());
        dadosEditados.put("cidade", inputCidade.getText().toString());
        dadosEditados.put("cep", inputCep.getText().toString());

        // Usa .update() para salvar apenas estes campos
        db.collection("users").document(uid).update(dadosEditados)
                .addOnSuccessListener(aVoid -> {
                    esconderProgressDialog();
                    Toast.makeText(this, "Dados salvos!", Toast.LENGTH_SHORT).show();

                    // Avisa a ProfessorPerfilActivity que os dados mudaram
                    setResult(RESULT_OK);
                    finish(); // Fecha a tela de edição
                })
                .addOnFailureListener(e -> {
                    esconderProgressDialog();
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void esconderProgressDialog() {
    }

    private void mostrarProgressDialog(String s) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega o status caso o usuário tenha acabado de enviar
        // (Isso é uma simplificação, o ideal seria usar um launcher,
        // mas para a demo, recarregar na volta da solicitação é ok)

        // Se o usuário voltar da 'SolicitarProfessorActivity', os dados locais
        // 'dadosUsuario' estarão desatualizados. Precisamos buscar de novo.
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        dadosUsuario = documentSnapshot.toObject(DadosUsuario.class);
                        if (dadosUsuario != null) {
                            // Reconfigura a UI com o status atualizado
                            configurarLogicaProfessor();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        // Verifica se o item clicado é o botão "Home" (a seta de voltar)
        if (item.getItemId() == android.R.id.home) {
            finish(); // Fecha a activity atual e volta para a anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
