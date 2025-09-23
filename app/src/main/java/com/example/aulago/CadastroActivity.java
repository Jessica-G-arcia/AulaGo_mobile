package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CadastroActivity extends AppCompatActivity {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final String SENHA_REGEX = "^(?=.*[A-Z])(?=.*\\d).{6,}$";

    // Constante para a chave da Intent
    public static final String KEY_DADOS_USUARIO = "dados_usuario";

    private EditText inputNome, inputCpf, inputDtNasc, inputTelefone, inputEmail, inputSenha;
    private Button btnContinuar, btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        inicializarViews();
        configurarListeners();
        ajustarLayout();
    }

    private void inicializarViews() {
        inputNome = findViewById(R.id.inputNome);
        inputCpf = findViewById(R.id.inputCpf);
        inputDtNasc = findViewById(R.id.inputDtNasc);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void configurarListeners() {
        btnContinuar.setOnClickListener(v -> validarECadastrar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void ajustarLayout() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validarECadastrar() {
        String nome = inputNome.getText().toString().trim();
        String cpf = inputCpf.getText().toString().trim();
        String dtNasc = inputDtNasc.getText().toString().trim();
        String telefone = inputTelefone.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String senha = inputSenha.getText().toString().trim();

        if (nome.isEmpty() || cpf.isEmpty() || dtNasc.isEmpty() || telefone.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValido(email)) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isSenhaValida(senha)) {
            Toast.makeText(this, "Senha fraca. Use ao menos 6 caracteres, uma letra maiúscula e um número", Toast.LENGTH_LONG).show();
            return;
        }

        // Cria o objeto de dados e passa para a próxima Activity
        DadosUsuario dadosUsuario = new DadosUsuario(nome, cpf, dtNasc, telefone, email, senha);
        Intent intent = new Intent(this, CadastroActivity2.class);
        intent.putExtra(KEY_DADOS_USUARIO, dadosUsuario);
        startActivity(intent);
    }

    private boolean isEmailValido(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isSenhaValida(String senha) {
        Pattern pattern = Pattern.compile(SENHA_REGEX);
        Matcher matcher = pattern.matcher(senha);
        return matcher.matches();
    }
}