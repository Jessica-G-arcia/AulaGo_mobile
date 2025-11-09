package com.example.aulago;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarDadosPessoaisActivity extends AppCompatActivity {

    // Views de Dados Pessoais
    private EditText inputNome, inputTelefone, inputCpf, inputDtNasc;

    // Views de Endereço
    private EditText inputCep, inputEndereco, inputNumero, inputComplemento, inputBairro, inputCidade;

    // Views de Status
    private TextView tvStatusSolicitacao;
    private Button btnSolicitarProfessor;

    private Button btnSalvar;
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dados_pessoais); // Seu XML

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            // Segurança: Se o usuário não está logado, não há nada para editar.
            finish();
            return;
        }
        uid = user.getUid();

        // Encontrar todas as Views
        initViews();

        // Configurar os cliques
        configurarListeners();

        // Buscar dados do Firestore e preencher o formulário
        carregarDadosDoUsuario();
    }

    private void initViews() {
        // Status
        tvStatusSolicitacao = findViewById(R.id.tvStatusSolicitacao);
        btnSolicitarProfessor = findViewById(R.id.btnSolicitarProfessor);

        // Dados Pessoais
        inputNome = findViewById(R.id.inputNome);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputCpf = findViewById(R.id.inputCpf);
        inputDtNasc = findViewById(R.id.inputDtNasc);

        // Endereço
        inputCep = findViewById(R.id.inputCep);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputNumero = findViewById(R.id.inputNumero);
        inputComplemento = findViewById(R.id.inputComplemento);
        inputBairro = findViewById(R.id.inputBairro);
        inputCidade = findViewById(R.id.inputCidade);

        // Botão Salvar
        btnSalvar = findViewById(R.id.btnSalvar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void configurarListeners() {
        btnSalvar.setOnClickListener(v -> salvarAlteracoes());

        btnSolicitarProfessor.setOnClickListener(v -> {
            // Abre a tela de solicitação que você já criou
            Intent intent = new Intent(this, SolicitarSerProfessorActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Busca os dados no Firestore e preenche os campos EditText.
     */
    private void carregarDadosDoUsuario() {
        progressDialog.setMessage("Carregando dados...");
        progressDialog.show();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (isFinishing()) return; // Previne crash
                    progressDialog.dismiss();

                    if (document.exists()) {
                        // Preenche os campos de dados pessoais
                        inputNome.setText(document.getString("nome"));
                        inputTelefone.setText(document.getString("telefone"));
                        inputCpf.setText(document.getString("cpf"));
                        inputDtNasc.setText(document.getString("dataNascimento")); // Assumindo que o nome do campo é "dataNascimento"

                        // Preenche os campos de endereço
                        inputCep.setText(document.getString("cep"));
                        inputEndereco.setText(document.getString("endereco"));
                        inputNumero.setText(document.getString("numero"));
                        inputComplemento.setText(document.getString("complemento"));
                        inputBairro.setText(document.getString("bairro"));
                        inputCidade.setText(document.getString("cidade"));

                        // Lógica para o status de professor
                        String status = document.getString("statusSolicitacao");
                        controlarStatusProfessor(status);

                    } else {
                        Toast.makeText(this, "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Controla qual item (botão ou texto) deve ser mostrado na seção "Status de Professor"
     */
    private void controlarStatusProfessor(String status) {
        if (status == null) status = "nenhum"; // Padrão

        switch (status) {
            case "pendente_analise":
                tvStatusSolicitacao.setText("Status: Em Análise");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                btnSolicitarProfessor.setVisibility(View.GONE);
                break;
            case "aprovado":
                // Se ele já é professor, não mostramos o botão
                tvStatusSolicitacao.setText("Status: Professor Aprovado");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                btnSolicitarProfessor.setVisibility(View.GONE);
                break;
            case "rejeitado":
                tvStatusSolicitacao.setText("Status: Solicitação Rejeitada");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                btnSolicitarProfessor.setText("Reenviar Solicitação"); // Muda o texto
                btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;
            case "nenhum":
            default:
                // Se não tem solicitação, mostra o botão
                tvStatusSolicitacao.setVisibility(View.GONE);
                btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Pega todos os dados dos campos e salva no Firestore usando .update()
     */
    private void salvarAlteracoes() {
        progressDialog.setMessage("Salvando alterações...");
        progressDialog.show();

        // Cria o "Mapa" de dados para atualizar
        Map<String, Object> dadosPessoais = new HashMap<>();

        // Dados Pessoais
        dadosPessoais.put("nome", inputNome.getText().toString().trim());
        dadosPessoais.put("telefone", inputTelefone.getText().toString().trim());
        dadosPessoais.put("cpf", inputCpf.getText().toString().trim());
        dadosPessoais.put("dataNascimento", inputDtNasc.getText().toString().trim()); // Assumindo "dataNascimento"

        // Endereço
        dadosPessoais.put("cep", inputCep.getText().toString().trim());
        dadosPessoais.put("endereco", inputEndereco.getText().toString().trim());
        dadosPessoais.put("numero", inputNumero.getText().toString().trim());
        dadosPessoais.put("complemento", inputComplemento.getText().toString().trim());
        dadosPessoais.put("bairro", inputBairro.getText().toString().trim());
        dadosPessoais.put("cidade", inputCidade.getText().toString().trim());

        // Usa .update() para não sobrescrever outros campos (como userType)
        db.collection("users").document(uid)
                .update(dadosPessoais)
                .addOnSuccessListener(aVoid -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a tela de edição
                })
                .addOnFailureListener(e -> {
                    if (isFinishing()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}