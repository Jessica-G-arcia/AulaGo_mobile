package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar; // <-- Import
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

// Imports do Firebase (NOVOS NESTA TELA)
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List; // <-- Import
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CadastroActivity extends AppCompatActivity {

    public static final String KEY_DADOS_USUARIO = "dadosUsuario";
    public static final String KEY_SENHA = "senha";
    public static final String KEY_FLUXO_GOOGLE = "fluxoGoogle";
    public static final String KEY_DADOS_GOOGLE = "dadosGoogle";

    private View mainLayout;
    private TextInputLayout textInputLayoutSenha;
    private EditText inputNome, inputCpf, inputDtNasc, inputTelefone, inputEmail, inputSenha;
    private Button btnContinuar, btnCancelar;
    private boolean isGoogleFlow = false;

    // --- VARIÁVEIS NOVAS ---
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    // --- FIM DAS VARIÁVEIS NOVAS ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        // --- INICIALIZAÇÃO DO FIREBASE ---
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // --- FIM DA INICIALIZAÇÃO ---

        inicializarViews();
        configurarListeners();
        ajustarLayout();
        receberDadosDoGoogle();
    }

    private void receberDadosDoGoogle() {
        Intent intent = getIntent();
        isGoogleFlow = intent.getBooleanExtra(KEY_FLUXO_GOOGLE, false);

        if (isGoogleFlow) {
            DadosUsuario dadosGoogle = (DadosUsuario) intent.getSerializableExtra(KEY_DADOS_GOOGLE);

            if (dadosGoogle != null) {
                inputNome.setText(dadosGoogle.getNome());
                inputEmail.setText(dadosGoogle.getEmail());
                inputNome.setEnabled(true);
                inputEmail.setEnabled(false);
                inputSenha.setEnabled(false);
                textInputLayoutSenha.setHelperText(null);
                Snackbar.make(mainLayout, "Complete seu cadastro para continuar.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void inicializarViews() {
        mainLayout = findViewById(R.id.main);
        textInputLayoutSenha = findViewById(R.id.textInputLayoutSenha);
        inputNome = findViewById(R.id.inputNome);
        inputCpf = findViewById(R.id.inputCpf);
        inputDtNasc = findViewById(R.id.inputDtNasc);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnCancelar = findViewById(R.id.btnCancelar);
        progressBar = findViewById(R.id.progressBarVerificacao); // <-- NOVO

        // Máscaras (Seu código)
        inputCpf.addTextChangedListener(MaskUtil.insert(inputCpf, MaskUtil.MaskType.CPF));
        inputTelefone.addTextChangedListener(MaskUtil.insert(inputTelefone, MaskUtil.MaskType.FONE));
        inputDtNasc.addTextChangedListener(MaskUtil.insert(inputDtNasc, MaskUtil.MaskType.DATA));
    }

    private void configurarListeners() {
        btnContinuar.setOnClickListener(v -> validarEVerificarDuplicidade()); // <-- MUDOU
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void ajustarLayout() {
        // (Seu código de ajuste de layout Edge-to-Edge)
        View mainView = findViewById(R.id.main);
        int originalPaddingLeft = mainView.getPaddingLeft();
        int originalPaddingTop = mainView.getPaddingTop();
        int originalPaddingRight = mainView.getPaddingRight();
        int originalPaddingBottom = mainView.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int paddingLeft = originalPaddingLeft + systemBars.left;
            int paddingTop = originalPaddingTop + systemBars.top;
            int paddingRight = originalPaddingRight + systemBars.right;
            int paddingBottom = originalPaddingBottom + Math.max(imeInsets.bottom, navBars.bottom);

            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            return insets;
        });
    }

    private String validarSenha(String senha) {
        if (senha.length() < 6) return "A senha deve ter no mínimo 6 caracteres.";
        final String MAIUSCULA_PATTERN = ".*[A-Z].*";
        final String MINUSCULA_PATTERN = ".*[a-z].*";
        final String NUMERO_PATTERN = ".*[0-9].*";
        final String ESPECIAL_PATTERN = ".*[^a-zA-Z0-9].*";
        if (!senha.matches(MAIUSCULA_PATTERN)) return "A senha deve conter pelo menos uma letra maiúscula.";
        if (!senha.matches(MINUSCULA_PATTERN)) return "A senha deve conter pelo menos uma letra minúscula.";
        if (!senha.matches(NUMERO_PATTERN)) return "A senha deve conter pelo menos um número.";
        if (!senha.matches(ESPECIAL_PATTERN)) return "A senha deve conter pelo menos um caractere especial (ex: @, #, $).";
        return null;
    }

    // --- MÉTODO ANTIGO (redirecionarParaProximaTela) FOI DIVIDIDO E RENOMEADO ---

    private void validarEVerificarDuplicidade() {
        String nome = inputNome.getText().toString().trim();
        String cpf = inputCpf.getText().toString().trim();
        String dtNasc = inputDtNasc.getText().toString().trim();
        String telefone = inputTelefone.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String senha = inputSenha.getText().toString().trim();

        textInputLayoutSenha.setError(null);

        // 1. Validação dos campos
        boolean camposVazios = false;
        if (nome.isEmpty() && inputNome.isEnabled()) camposVazios = true;
        if (cpf.isEmpty()) camposVazios = true;
        if (dtNasc.isEmpty()) camposVazios = true;
        if (telefone.isEmpty()) camposVazios = true;
        if (email.isEmpty() && inputEmail.isEnabled()) camposVazios = true;
        if (senha.isEmpty() && inputSenha.isEnabled() && !isGoogleFlow)
            camposVazios = true;

        if (camposVazios) {
            Snackbar.make(mainLayout, "Preencha todos os campos obrigatórios.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // 2. Validação da Senha
        if (!isGoogleFlow) {
            String erroSenha = validarSenha(senha);
            if (erroSenha != null) {
                textInputLayoutSenha.setError(erroSenha);
                return;
            }
        }
        textInputLayoutSenha.setError(null);

        // 3. Inicia a verificação de duplicidade
        setLoading(true); // Ativa o ProgressBar

        // O fluxo do Google não precisa checar o e-mail (já foi validado no Login),
        // mas PRECISA checar o CPF.
        if (isGoogleFlow) {
            verificarCpf(email, cpf, senha);
        } else {
            // O fluxo de E-mail/Senha checa o E-mail PRIMEIRO.
            verificarEmail(email, cpf, senha);
        }
    }

    // --- NOVOS MÉTODOS DE VERIFICAÇÃO ---

    /**
     * Passo 1: Verifica se o e-mail já existe no Firebase Auth.
     */
    private void verificarEmail(String email, String cpf, String senha) {
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> methods = task.getResult().getSignInMethods();
                        if (methods != null && !methods.isEmpty()) {
                            // Email JÁ EXISTE no Auth
                            setLoading(false);
                            Snackbar.make(mainLayout, "Este e-mail já está em uso.", Snackbar.LENGTH_LONG).show();
                        } else {
                            // Email OK! Agora checa o CPF (Passo 2)
                            verificarCpf(email, cpf, senha);
                        }
                    } else {
                        // Erro ao checar e-mail
                        setLoading(false);
                        Snackbar.make(mainLayout, "Erro ao verificar e-mail.", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Passo 2: Verifica se o CPF já existe no Firestore.
     */
    private void verificarCpf(String email, String cpf, String senha) {
        db.collection("users").whereEqualTo("cpf", cpf).limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            // CPF JÁ EXISTE no Firestore
                            setLoading(false);
                            Snackbar.make(mainLayout, "Este CPF já está em uso.", Snackbar.LENGTH_LONG).show();
                        } else {
                            // TUDO OK! Pode prosseguir para a próxima tela (Passo 3)
                            setLoading(false);
                            iniciarCadastroActivity2(email, cpf, senha);
                        }
                    } else {
                        // Erro ao checar CPF
                        setLoading(false);
                        Snackbar.make(mainLayout, "Erro ao verificar CPF.", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Passo 3: Reúne os dados e inicia a Activity 2.
     */
    private void iniciarCadastroActivity2(String email, String cpf, String senha) {
        DadosUsuario dadosUsuario = new DadosUsuario();
        dadosUsuario.setNome(inputNome.getText().toString().trim());
        dadosUsuario.setCpf(cpf);
        dadosUsuario.setDtNasc(inputDtNasc.getText().toString().trim());
        dadosUsuario.setTelefone(inputTelefone.getText().toString().trim());
        dadosUsuario.setEmail(email);

        Intent intent = new Intent(this, CadastroActivity2.class);
        intent.putExtra(KEY_DADOS_USUARIO, dadosUsuario);
        intent.putExtra(KEY_SENHA, senha); // Passa a senha separadamente
        intent.putExtra(KEY_FLUXO_GOOGLE, isGoogleFlow);
        startActivity(intent);
    }

    /**
     * Controla a visibilidade do ProgressBar e ativa/desativa o botão.
     */
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnContinuar.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnContinuar.setEnabled(true);
        }
    }
}