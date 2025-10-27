package com.example.aulago;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolicitarSerProfessorActivity extends AppCompatActivity {

    private static final int PICK_DOCUMENT = 100;

    private Spinner spinnerCertificacao;
    private EditText etNumeroCertificado, etInstituicao, etNomeCompleto, etPontuacao;
    private LinearLayout layoutPontuacao;
    private TextView tvDocumentoSelecionado;
    private Button btnSelecionarDocumento, btnEnviar;

    private Uri documentoUri;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_ser_professor); // (XML da conversa anterior)

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinner();
        configurarListeners();
    }

    private void initViews() {
        spinnerCertificacao = findViewById(R.id.spinnerCertificacao);
        etNumeroCertificado = findViewById(R.id.etNumeroCertificado);
        etInstituicao = findViewById(R.id.etInstituicao);
        etNomeCompleto = findViewById(R.id.etNomeCompleto);
        etPontuacao = findViewById(R.id.etPontuacao);
        layoutPontuacao = findViewById(R.id.layoutPontuacao);
        btnSelecionarDocumento = findViewById(R.id.btnSelecionarDocumento);
        btnEnviar = findViewById(R.id.btnEnviar);
        tvDocumentoSelecionado = findViewById(R.id.tvDocumentoSelecionado);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void configurarListeners() {
        btnSelecionarDocumento.setOnClickListener(v -> selecionarDocumento());
        btnEnviar.setOnClickListener(v -> validarEEnviar());
    }

    private void setupSpinner() {
        // (Use o seu array de certificações)
        String[] certificacoes = {"Selecione", "TOEFL", "IELTS", "Cambridge CAE", "CELTA"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, certificacoes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCertificacao.setAdapter(adapter);

        spinnerCertificacao.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = certificacoes[position];
                if (selected.equals("TOEFL") || selected.equals("IELTS")) {
                    layoutPontuacao.setVisibility(View.VISIBLE);
                } else {
                    layoutPontuacao.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void selecionarDocumento() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Selecione o certificado"), PICK_DOCUMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DOCUMENT && resultCode == RESULT_OK && data != null) {
            documentoUri = data.getData();
            tvDocumentoSelecionado.setText("Documento selecionado!");
        }
    }

    private void validarEEnviar() {
        // (Faça suas validações de campos vazios aqui) ...

        if (documentoUri == null) {
            Toast.makeText(this, "Selecione o documento do certificado", Toast.LENGTH_SHORT).show();
            return;
        }
        enviarDocumentoEAtualizarFirestore();
    }

    private void enviarDocumentoEAtualizarFirestore() {
        progressDialog.setMessage("Enviando documento...");
        progressDialog.show();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) { /* ... tratar erro ... */
            return;
        }

        String uid = currentUser.getUid();
        String nomeArquivo = "certificado_" + uid + "_" + UUID.randomUUID().toString();
        StorageReference docRef = storage.getReference().child("certificados_professores/" + uid + "/" + nomeArquivo);

        // 1. Upload do Documento
        docRef.putFile(documentoUri)
                .addOnSuccessListener(taskSnapshot -> docRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // 2. URL obtida, salvar no Firestore
                    salvarSolicitacaoFirestore(uid, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro ao enviar documento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void salvarSolicitacaoFirestore(String uid, String docUrl) {
        progressDialog.setMessage("Registrando solicitação...");

        Map<String, Object> solicitacao = new HashMap<>();
        // Dados da solicitação
        solicitacao.put("tipoCertificacao", spinnerCertificacao.getSelectedItem().toString());
        solicitacao.put("numeroCertificado", etNumeroCertificado.getText().toString().trim());
        solicitacao.put("instituicaoCertificacao", etInstituicao.getText().toString().trim());
        solicitacao.put("nomeCompletoCertificado", etNomeCompleto.getText().toString().trim());
        solicitacao.put("certificadoUrl", docUrl);
        solicitacao.put("dataSolicitacao", FieldValue.serverTimestamp());
        solicitacao.put("pontuacaoCertificado", etPontuacao.getText().toString().trim());

        // --- ESTE É O CAMPO MAIS IMPORTANTE PARA A LÓGICA ---
        solicitacao.put("statusSolicitacao", "pendente_analise");

        // Limpa campos de admin antigos, caso seja um reenvio
        solicitacao.put("motivoRejeicao", FieldValue.delete());
        solicitacao.put("professorVerificado", false);

        // Usamos SetOptions.merge() para não apagar os dados existentes (nome, cpf, etc.)
        db.collection("users").document(uid)
                .set(solicitacao, SetOptions.merge()) // Usa merge para não apagar dados
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    mostrarDialogoSucesso();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro ao salvar solicitação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogoSucesso() {
        new AlertDialog.Builder(this)
                .setTitle("✓ Solicitação Enviada!")
                .setMessage("Sua solicitação será analisada pela equipe.")
                .setPositiveButton("OK", (dialog, which) -> finish()) // Fecha a tela de solicitação
                .setCancelable(false)
                .show();
    }
}
