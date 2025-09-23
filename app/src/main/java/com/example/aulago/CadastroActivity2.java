package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CadastroActivity2 extends AppCompatActivity {

    private EditText inputEndereco, inputNumero, inputComplemento, inputCep, inputBairro, inputCidade;
    private Spinner spinnerGenero;
    private Button btnCadastrar, btnCancelar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DadosUsuario dadosUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro2);

        // Recupera o objeto de dados da Activity anterior
        dadosUsuario = (DadosUsuario) getIntent().getSerializableExtra(CadastroActivity.KEY_DADOS_USUARIO);

        // Inicializa as views da segunda tela
        inicializarViews();
        configurarListeners();
        ajustarLayout();

        // Inicializa o Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configurarSpinner();
    }

    private void inicializarViews() {
        inputEndereco = findViewById(R.id.inputEndereco);
        inputNumero = findViewById(R.id.inputNumero);
        inputComplemento = findViewById(R.id.inputComplemento);
        inputCep = findViewById(R.id.inputCep);
        inputBairro = findViewById(R.id.inputBairro);
        inputCidade = findViewById(R.id.inputCidade);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void configurarListeners() {
        btnCadastrar.setOnClickListener(v -> realizarCadastroCompleto());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void ajustarLayout() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configurarSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.generos_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapter);
    }

    private void realizarCadastroCompleto() {
        // Pega os dados da segunda tela
        dadosUsuario.endereco = inputEndereco.getText().toString().trim();
        dadosUsuario.numero = inputNumero.getText().toString().trim();
        dadosUsuario.complemento = inputComplemento.getText().toString().trim();
        dadosUsuario.cep = inputCep.getText().toString().trim();
        dadosUsuario.bairro = inputBairro.getText().toString().trim();
        dadosUsuario.cidade = inputCidade.getText().toString().trim();
        dadosUsuario.genero = spinnerGenero.getSelectedItem().toString();

        // Validação dos campos da segunda tela
        if (dadosUsuario.endereco.isEmpty() || dadosUsuario.numero.isEmpty() || dadosUsuario.cep.isEmpty() ||
                dadosUsuario.bairro.isEmpty() || dadosUsuario.cidade.isEmpty() || dadosUsuario.genero.equals("Selecione seu gênero")) {
            Toast.makeText(this, "Preencha todos os campos e selecione seu gênero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realiza o cadastro no Firebase Auth
        auth.createUserWithEmailAndPassword(dadosUsuario.email, dadosUsuario.senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Salva os dados completos no Firestore usando o UID do usuário
                            db.collection("users").document(user.getUid())
                                    .set(dadosUsuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                        // Redireciona para a tela de login (MainActivity)
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Se falhar o salvamento, exclui a conta criada para evitar inconsistência
                                        user.delete();
                                        Toast.makeText(this, "Erro ao salvar dados do usuário: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        // Se a autenticação falhar
                        Toast.makeText(this, "Erro no cadastro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}