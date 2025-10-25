package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Adicionado para referenciar o layout principal
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Mantido, mas não mais usado para exibir mensagens

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar; // Adicionado para Snackbar

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CadastroActivity extends AppCompatActivity {

    public static final String KEY_DADOS_USUARIO = "dadosUsuario"; // Chave para passar o objeto
    public static final String KEY_FLUXO_GOOGLE = "fluxoGoogle";
    public static final String KEY_DADOS_GOOGLE = "dadosGoogle";

    // Adicionado referência para o layout principal (R.id.main)
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
                // 1. Preenche Nome e Email
                inputNome.setText(dadosGoogle.nome);
                inputEmail.setText(dadosGoogle.email);

                // 2. DESABILITA A EDIÇÃO (E-mail e Nome já estão validados pelo Google)
                inputNome.setEnabled(true); // Manter habilitado para o usuário ver/ajustar o nome, se quiser
                inputEmail.setEnabled(false);

                // 3. OCULTA OU DESABILITA O CAMPO SENHA
                // O usuário do Google não precisa criar senha, pois ele usa o token do Google.
                inputSenha.setText(dadosGoogle.senha != null && !dadosGoogle.senha.isEmpty() ? dadosGoogle.senha : "SENHA_FAKE");
                inputSenha.setEnabled(false); // Impede que o usuário mude
                // Oculta o helper text quando está desabilitado
                textInputLayoutSenha.setHelperText(null);

                // ALTERADO: Toast substituído por Snackbar
                Snackbar.make(mainLayout, "Complete seu cadastro para continuar.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void inicializarViews() {
        // Inicializando o layout principal
        mainLayout = findViewById(R.id.main);

        // Inicializando o TextInputLayout
        textInputLayoutSenha = findViewById(R.id.textInputLayoutSenha);

        inputNome = findViewById(R.id.inputNome);
        inputCpf = findViewById(R.id.inputCpf);
        inputDtNasc = findViewById(R.id.inputDtNasc);
        inputTelefone = findViewById(R.id.inputTelefone);
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Adicionando as máscaras nos campos de texto
        inputCpf.addTextChangedListener(MaskUtil.insert(inputCpf, MaskUtil.MaskType.CPF));
        inputTelefone.addTextChangedListener(MaskUtil.insert(inputTelefone, MaskUtil.MaskType.FONE));
        inputDtNasc.addTextChangedListener(MaskUtil.insert(inputDtNasc, MaskUtil.MaskType.DATA));
    }

    private void configurarListeners() {
        btnContinuar.setOnClickListener(v -> redirecionarParaProximaTela());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void ajustarLayout() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Valida a senha contra as políticas de segurança do Firebase.
     * @param senha A string da senha a ser validada.
     * @return Uma string de erro, ou null se a senha for válida.
     */
    private String validarSenha(String senha) {
        if (senha.length() < 6) {
            return "A senha deve ter no mínimo 6 caracteres.";
        }

        // Expressões Regulares para as políticas:
        // Padrão para verificar maiúscula, minúscula, número e caractere especial (não alfanumérico)
        final String MAIUSCULA_PATTERN = ".*[A-Z].*";
        final String MINUSCULA_PATTERN = ".*[a-z].*";
        final String NUMERO_PATTERN = ".*[0-9].*";
        // Padrão para qualquer caractere que NÃO seja uma letra ou um número.
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

        // Limpa erros anteriores antes de validar
        textInputLayoutSenha.setError(null);

        // 1. Validação dos campos
        boolean camposVazios = false;

        // Verifica campos obrigatórios. No fluxo Google, a senha e o e-mail não são obrigatórios
        // porque já foram preenchidos ou serão ignorados.
        if (nome.isEmpty() && inputNome.isEnabled()) camposVazios = true;
        if (cpf.isEmpty()) camposVazios = true;
        if (dtNasc.isEmpty()) camposVazios = true;
        if (telefone.isEmpty()) camposVazios = true;
        if (email.isEmpty() && inputEmail.isEnabled()) camposVazios = true;
        if (senha.isEmpty() && inputSenha.isEnabled()) camposVazios = true;

        if (camposVazios) {
            // ALTERADO: Toast substituído por Snackbar
            Snackbar.make(mainLayout, "Preencha todos os campos obrigatórios da primeira etapa.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Validação Simplificada: Apenas verifica se CPF, Data Nasc e Telefone estão vazios
        if (cpf.isEmpty() || dtNasc.isEmpty() || telefone.isEmpty()) {
            Snackbar.make(mainLayout, "Preencha todos os campos obrigatórios da primeira etapa.", Snackbar.LENGTH_LONG).show();
            return;
        }


        // 2. Validação da Senha
        if (!isGoogleFlow) { // SOMENTE verifica a senha se NÃO for fluxo Google
            String erroSenha = validarSenha(senha);
            if (erroSenha != null) {
                // Exibe o erro específico no TextInputLayout (Método recomendado)
                textInputLayoutSenha.setError(erroSenha);
                return;
            }
        }

        // Se a validação for bem-sucedida, remove qualquer erro remanescente
        textInputLayoutSenha.setError(null);


        // 3. Cria o objeto de dados
        DadosUsuario dadosUsuario = new DadosUsuario(nome, cpf, dtNasc, telefone, email, senha);

        // 4. Envia os dados para CadastroActivity2
        Intent intent = new Intent(this, CadastroActivity2.class);
        intent.putExtra(KEY_DADOS_USUARIO, dadosUsuario);
        intent.putExtra(KEY_FLUXO_GOOGLE, isGoogleFlow); // Passa o flag adiante
        startActivity(intent);
    }
}
