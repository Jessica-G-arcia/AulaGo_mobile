package com.example.aulago;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilProfessorActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String uid;

    // Views de Texto
    private EditText etEspecialidade, etModalidade, etValorPresencial, etValorOnline, etBio;

    // Views de Foto
    private ImageView ivFotoPerfil;
    private Button btnEscolherFoto;

    private Button btnSalvarPerfil;
    private ProgressBar progressBar;

    // Lógica de Foto
    private ActivityResultLauncher<String> mGetContent;
    private Uri imageUri;
    private String currentFotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_professor);

        // Inicialize o Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = auth.getCurrentUser().getUid();

        // Encontre os Views do seu XML
        etEspecialidade = findViewById(R.id.etEspecialidade);
        etModalidade = findViewById(R.id.etModalidade);
        etValorPresencial = findViewById(R.id.etValorPresencial);
        etValorOnline = findViewById(R.id.etValorOnline);
        etBio = findViewById(R.id.etBio);
        btnSalvarPerfil = findViewById(R.id.btnSalvarPerfil);
        progressBar = findViewById(R.id.progressBar);

        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        btnEscolherFoto = findViewById(R.id.btnEscolherFoto);

        mGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        ivFotoPerfil.setImageURI(imageUri);
                    }
                }
        );

        // Configure o clique do botão
        btnSalvarPerfil.setOnClickListener(v -> salvarPerfil());

        btnEscolherFoto.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        // Carregue os dados atuais do professor para preencher os campos
        carregarDadosAtuais();
    }

    private void carregarDadosAtuais() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Preenche os campos de texto
                        etBio.setText(document.getString("bio"));
                        etEspecialidade.setText(document.getString("especialidade"));

                        // CORREÇÃO: Lendo do campo "preferenciaAula"
                        etModalidade.setText(document.getString("preferenciaAula"));

                        if (document.getDouble("valorPresencial") != null) {
                            etValorPresencial.setText(String.valueOf(document.getDouble("valorPresencial")));
                        }
                        if (document.getDouble("valorOnline") != null) {
                            etValorOnline.setText(String.valueOf(document.getDouble("valorOnline")));
                        }

                        currentFotoUrl = document.getString("urlFotoPerfil");
                        if (currentFotoUrl != null && !currentFotoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(currentFotoUrl)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .into(ivFotoPerfil);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Ponto de entrada: Decide se precisa fazer upload ou não.
     */
    private void salvarPerfil() {
        setLoading(true);

        if (imageUri != null) {
            fazerUploadDaImagem();
        } else {
            salvarDadosNoFirestore(currentFotoUrl);
        }
    }

    /**
     * Passo 1 (se necessário): Faz o upload da imagem para o Storage.
     */
    private void fazerUploadDaImagem() {
        String fileName = uid + ".jpg";
        StorageReference storageRef = storage.getReference().child("fotosDePerfil/" + fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        salvarDadosNoFirestore(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro no upload da foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    /**
     * Passo Final: Salva TODOS os dados (texto + URL da foto) no Firestore.
     */
    private void salvarDadosNoFirestore(String fotoUrl) {
        String especialidade = etEspecialidade.getText().toString().trim();
        String modalidade = etModalidade.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        double valorP = 0;
        double valorO = 0;
        try {
            String strValorP = etValorPresencial.getText().toString().trim();
            String strValorO = etValorOnline.getText().toString().trim();
            if (!strValorP.isEmpty()) valorP = Double.parseDouble(strValorP);
            if (!strValorO.isEmpty()) valorO = Double.parseDouble(strValorO);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, insira valores válidos (ex: 100.50)", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        Map<String, Object> professorData = new HashMap<>();
        professorData.put("especialidade", especialidade);

        // CORREÇÃO: Salvando no campo "preferenciaAula"
        professorData.put("preferenciaAula", modalidade);

        professorData.put("bio", bio);
        professorData.put("valorPresencial", valorP);
        professorData.put("valorOnline", valorO);

        if (fotoUrl != null) {
            professorData.put("urlFotoPerfil", fotoUrl);
        }

        db.collection("users").document(uid)
                .update(professorData)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(EditarPerfilProfessorActivity.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(EditarPerfilProfessorActivity.this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSalvarPerfil.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSalvarPerfil.setEnabled(true);
        }
    }
}