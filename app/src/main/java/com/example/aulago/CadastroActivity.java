package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
// Removido o Toast
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

// Removido o import do 'Matcher' e 'Pattern'
// (Movido para dentro do método 'validarSenha')

public class CadastroActivity extends AppCompatActivity {

    // --- CORREÇÃO DAS CHAVES ---
    // Deixei igual ao CadastroActivity2 (o código que eu te passei)
    public static final String KEY_DADOS_USUARIO = "dadosUsuario"; // Use este
    public static final String KEY_SENHA = "senha"; // Use este
    public static final String KEY_FLUXO_GOOGLE = "fluxoGoogle";
    public static final String KEY_DADOS_GOOGLE = "dadosGoogle";

    private View mainLayout;
    private TextInputLayout textInputLayoutSenha;
    private EditText inputNome, inputCpf, inputDtNasc, inputTelefone, inputEmail, inputSenha;
    private Button btnContinuar, btnCancelar;
    private boolean isGoogleFlow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

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
                // CORREÇÃO: Usar getters
                inputNome.setText(dadosGoogle.getNome());
                inputEmail.setText(dadosGoogle.getEmail());

                inputNome.setEnabled(true);
                inputEmail.setEnabled(false);

                // CORREÇÃO: Não precisamos de senha fake, o fluxo 'isGoogleFlow'
                // vai pular a validação de senha.
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

        // (Seu código de Máscara - está ótimo)
        inputCpf.addTextChangedListener(MaskUtil.insert(inputCpf, MaskUtil.MaskType.CPF));
        inputTelefone.addTextChangedListener(MaskUtil.insert(inputTelefone, MaskUtil.MaskType.FONE));
        inputDtNasc.addTextChangedListener(MaskUtil.insert(inputDtNasc, MaskUtil.MaskType.DATA));
    }

    private void configurarListeners() {
        btnContinuar.setOnClickListener(v -> redirecionarParaProximaTela());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void ajustarLayout() {
        // (Seu código de ajuste de layout - está ótimo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private String validarSenha(String senha) {
        // (Seu código de validação de senha - está ótimo)
        if (senha.length() < 6) {
            return "A senha deve ter no mínimo 6 caracteres.";
        }
        final String MAIUSCULA_PATTERN = ".*[A-Z].*";
        final String MINUSCULA_PATTERN = ".*[a-z].*";
        final String NUMERO_PATTERN = ".*[0-9].*";
        final String ESPECIAL_PATTERN = ".*[^a-zA-Z0-9].*";
        if (!senha.matches(MAIUSCULA_PATTERN)) {
            return "A senha deve conter pelo menos uma letra maiúscula.";
        }
        if (!senha.matches(MINUSCULA_PATTERN)) {
            return "A senha deve conter pelo menos uma letra minúscula.";
        }
        if (!senha.matches(NUMERO_PATTERN)) {
            return "A senha deve conter pelo menos um número.";
        }
        if (!senha.matches(ESPECIAL_PATTERN)) {
            return "A senha deve conter pelo menos um caractere especial (ex: @, #, $).";
        }
        return null; // Senha válida
    }

    private void redirecionarParaProximaTela() {
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
            camposVazios = true; // Senha só é obrigatória se NÃO for Google

        if (camposVazios) {
            Snackbar.make(mainLayout, "Preencha todos os campos obrigatórios da primeira etapa.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // (Removi a validação duplicada de cpf, dtNasc, telefone)

        // 2. Validação da Senha
        if (!isGoogleFlow) { // SÓ valida a senha se NÃO for fluxo Google
            String erroSenha = validarSenha(senha);
            if (erroSenha != null) {
                textInputLayoutSenha.setError(erroSenha);
                return;
            }
        }
        textInputLayoutSenha.setError(null);


        // 3. --- CORREÇÃO CRÍTICA NA CRIAÇÃO DOS DADOS ---
        // (Usando a lógica do Código 2, que é a correta)

        DadosUsuario dadosUsuario = new DadosUsuario();
        // Use os "setters" para preencher o objeto
        dadosUsuario.setNome(nome);
        dadosUsuario.setCpf(cpf);
        dadosUsuario.setDtNasc(dtNasc);
        dadosUsuario.setTelefone(telefone);
        dadosUsuario.setEmail(email);
        // NÃO COLOQUE A SENHA NO OBJETO!

        // 4. Envia os dados para CadastroActivity2
        Intent intent = new Intent(this, CadastroActivity2.class);
        intent.putExtra(KEY_DADOS_USUARIO, dadosUsuario); // Chave correta
        intent.putExtra(KEY_SENHA, senha); // Passa a senha separadamente
        intent.putExtra(KEY_FLUXO_GOOGLE, isGoogleFlow);
        startActivity(intent);
    }
}