package com.example.aulago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup; // Import necessário para o layout do Dialog
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout; // Import necessário para o layout do Dialog
import android.widget.Switch;
import android.widget.TextView; // Import do TextView
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

// Imports de Biometria e Criptografia
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputSenha;
    private Button btnEntrar, btnCriarConta, btnGoogleLogin;
    private Switch switchLembrarSenha;
    private TextView txtEsqueciSenha; // Nova variável

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    FirebaseFirestore db;

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    // --- Variáveis de Biometria e Criptografia ---
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private SharedPreferences securePreferences;
    private static final String SECURE_PREFS_NAME = "SecureAuthPrefs";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PASS = "userPass";

    // Chave para salvar o e-mail (não criptografado) para lookup
    private static final String KEY_BIOMETRIC_EMAIL_ALIAS = "biometricEmailAlias";

    // Flags de controle
    private boolean isParaSalvarBiometria = false;
    private String tempEmail;
    private String tempSenha;
    private boolean isBiometricPromptShowing = false;
    // --- Fim Variáveis Biometria ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inicializarSecurePreferences();
        inicializarViews();
        configurarGoogleSignIn();
        configurarBiometria();
        configurarListeners();

        // Tenta logar o usuário automaticamente (Sessão Firebase ou Biometria)
        boolean justLoggedOut = getIntent().getBooleanExtra("JUST_LOGGED_OUT", false);
        tentarLoginAutomatico(justLoggedOut);

        FloatingActionButton fabChatbot = findViewById(R.id.fabChatbot);
        fabChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Inicializa as SharedPreferences Criptografadas.
     */
    private void inicializarSecurePreferences() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            securePreferences = EncryptedSharedPreferences.create(
                    SECURE_PREFS_NAME,
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e("SecurePrefs", "Erro ao inicializar EncryptedSharedPreferences", e);
        }
    }

    /**
     * Configura o Executor, o Callback e o Pop-up de Biometria.
     */
    private void configurarBiometria() {
        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                isBiometricPromptShowing = false; // Permite um novo prompt

                if (isParaSalvarBiometria) {
                    isParaSalvarBiometria = false;
                    Toast.makeText(getApplicationContext(), "Ativação da biometria cancelada.", Toast.LENGTH_SHORT).show();
                    abrirTela(ToolbarActivity.class);
                } else {
                    Toast.makeText(getApplicationContext(), "Autenticação cancelada.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                isBiometricPromptShowing = false; // Permite um novo prompt

                if (isParaSalvarBiometria) {
                    // --- FLUXO DE SALVAR ---
                    isParaSalvarBiometria = false;
                    salvarCredenciaisSeguras(tempEmail, tempSenha); // Agora salva (incluindo o alias)
                    tempEmail = null;
                    tempSenha = null;

                    Toast.makeText(getApplicationContext(), "Biometria ativada com sucesso!", Toast.LENGTH_SHORT).show();
                    abrirTela(ToolbarActivity.class);

                } else {
                    // --- FLUXO DE LOGAR ---
                    if (securePreferences == null) {
                        Toast.makeText(getApplicationContext(), "Erro de segurança. Faça login manualmente.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String email = securePreferences.getString(KEY_USER_EMAIL, null);
                    String senha = securePreferences.getString(KEY_USER_PASS, null);

                    if (email != null && senha != null) {
                        Toast.makeText(getApplicationContext(), "Autenticado! Entrando...", Toast.LENGTH_SHORT).show();
                        verificarExistenciaUsuarioELogar(email, senha);
                    } else {
                        Toast.makeText(getApplicationContext(), "Credenciais não encontradas. Faça login manualmente.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                isBiometricPromptShowing = false; // Permite um novo prompt
                Toast.makeText(getApplicationContext(), "Autenticação falhou.", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login Biométrico")
                .setSubtitle("Use sua digital ou rosto para fazer login")
                .setNegativeButtonText("Cancelar")
                .build();
    }

    /**
     * Tenta logar o usuário automaticamente.
     */
    private void tentarLoginAutomatico(boolean justLoggedOut) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);
        FirebaseUser user = auth.getCurrentUser();

        if (rememberMe && user != null) {
            Log.d("Login", "Login automático via sessão Firebase.");
            abrirTela(ToolbarActivity.class);

        } else if (podeAutenticarComBiometria() && temCredenciaisSalvas() && !justLoggedOut) {
            Log.d("Login", "Login automático via Biometria.");
            isBiometricPromptShowing = true;
            biometricPrompt.authenticate(promptInfo);

        } else {
            Log.d("Login", "Nenhum login automático. Aguardando entrada manual.");
        }
    }

    private void inicializarViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // --- INICIALIZAÇÃO NOVA ---
        txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

        switchLembrarSenha = findViewById(R.id.switchLembrarSenha);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);
        switchLembrarSenha.setChecked(rememberMe);
    }

    private void configurarListeners() {
        btnEntrar.setOnClickListener(v -> loginComEmailESenha());
        btnCriarConta.setOnClickListener(v -> redirecionarParaCadastro());
        btnGoogleLogin.setOnClickListener(v -> loginComGoogle());

        // --- LISTENER NOVO PARA ESQUECI SENHA ---
        txtEsqueciSenha.setOnClickListener(v -> mostrarDialogoRecuperacao());

        // Aciona a biometria assim que o usuário para de digitar o e-mail
        inputEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isBiometricPromptShowing) {
                // Usuário terminou de digitar o e-mail
                String emailDigitado = inputEmail.getText().toString().trim();
                if (emailCorrespondeAoSalvo(emailDigitado)) {
                    Log.d("Login", "E-mail com biometria detectado (via Foco). Acionando prompt.");
                    isBiometricPromptShowing = true;
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
    }

    // Método helper para checar o "alias" do e-mail
    private boolean emailCorrespondeAoSalvo(String emailDigitado) {
        if (emailDigitado.isEmpty() || !podeAutenticarComBiometria() || !temCredenciaisSalvas()) {
            return false;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String emailSalvo = prefs.getString(KEY_BIOMETRIC_EMAIL_ALIAS, null);

        return emailDigitado.equalsIgnoreCase(emailSalvo);
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void verificarExistenciaUsuarioELogar(String email, String senha) {
        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Login", "Login manual bem-sucedido.");
                        processarPosLoginManual(email, senha);
                    } else {
                        // FALHA NO LOGIN
                        String mensagemErro = "Erro ao fazer login. Verifique as credenciais.";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            mensagemErro = "O e-mail digitado não está cadastrado no sistema.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            mensagemErro = "A senha está incorreta ou o usuário não está cadastrado.";
                        } catch (Exception e) {
                            mensagemErro = "Erro desconhecido: " + e.getLocalizedMessage();
                        }
                        Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Decide o que fazer após um login manual bem-sucedido.
     */
    private void processarPosLoginManual(String email, String senha) {
        // 1. Salva o estado do switch "Lembrar Senha"
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        boolean querLembrar = switchLembrarSenha.isChecked();
        editor.putBoolean(KEY_REMEMBER_ME, querLembrar);
        editor.apply();

        // 2. Verifica se pode OFERECER a biometria
        boolean podeHabilitarBiometria = podeAutenticarComBiometria() && !temCredenciaisSalvas();

        if (podeHabilitarBiometria) {
            // Mostra o pop-up de oferta
            new AlertDialog.Builder(this)
                    .setTitle("Login Rápido")
                    .setMessage("Deseja habilitar o login com biometria para esta conta?")
                    .setPositiveButton("Sim, habilitar", (dialog, which) -> {
                        // Prepara as variáveis para o callback
                        isParaSalvarBiometria = true;
                        tempEmail = email;
                        tempSenha = senha;

                        // Chama o pop-up do SISTEMA para confirmar
                        isBiometricPromptShowing = true;
                        biometricPrompt.authenticate(promptInfo);
                    })
                    .setNegativeButton("Agora não", (dialog, which) -> {
                        abrirTela(ToolbarActivity.class);
                    })
                    .setCancelable(false)
                    .show();
        } else {
            abrirTela(ToolbarActivity.class);
        }
    }


    /**
     * Limpa a biometria após o login do Google.
     */
    private void processarPosLoginGoogle() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER_ME, switchLembrarSenha.isChecked());
        editor.apply();
        apagarCredenciaisSeguras(); // Login Google não usa senha, então limpa biometria local
    }

    private void loginComEmailESenha() {
        String email = inputEmail.getText().toString().trim();
        String senha = inputSenha.getText().toString().trim();

        if (email.isEmpty()) {
            inputEmail.setError("O e-mail é obrigatório.");
            inputEmail.requestFocus();
            return;
        }

        // Se o prompt já estiver aparecendo (pelo listener de foco), não faz nada.
        if (isBiometricPromptShowing) {
            return;
        }

        // LÓGICA DE FALLBACK (Botão "Entrar")
        if (senha.isEmpty() && emailCorrespondeAoSalvo(email)) {
            Log.d("Login", "E-mail com biometria detectado (via Botão). Acionando prompt.");
            isBiometricPromptShowing = true;
            biometricPrompt.authenticate(promptInfo);
            return;
        }

        if (senha.isEmpty()) {
            inputSenha.setError("A senha é obrigatória.");
            inputSenha.requestFocus();
            return;
        }

        verificarExistenciaUsuarioELogar(email, senha);
    }

    private void redirecionarParaCadastro() {
        Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

    private void loginComGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GoogleAuth", "Google sign in falhou", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        processarPosLoginGoogle();
                        FirebaseUser user = auth.getCurrentUser();
                        tratarLoginGoogle(user, acct);
                    } else {
                        Toast.makeText(this, "Erro ao autenticar com Google no Firebase.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void tratarLoginGoogle(FirebaseUser user, GoogleSignInAccount acct) {
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            // USUÁRIO NOVO
                            Toast.makeText(this, "Bem-vindo! Complete seu cadastro.", Toast.LENGTH_LONG).show();
                            DadosUsuario dadosParciais = new DadosUsuario();
                            dadosParciais.setNome(acct.getDisplayName());
                            dadosParciais.setEmail(acct.getEmail());
                            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
                            intent.putExtra(CadastroActivity.KEY_FLUXO_GOOGLE, true);
                            intent.putExtra(CadastroActivity.KEY_DADOS_GOOGLE, dadosParciais);
                            startActivity(intent);
                            finish();
                        } else {
                            // USUÁRIO EXISTENTE
                            salvarUsuarioFirestoreSeNovo(user, acct); // Apenas por garantia
                            Toast.makeText(this, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            abrirTela(ToolbarActivity.class);
                        }
                    } else {
                        Log.e("Firestore", "Erro ao verificar existência do usuário.", task.getException());
                        abrirTela(ToolbarActivity.class);
                    }
                });
    }

    private void salvarUsuarioFirestoreSeNovo(FirebaseUser user, GoogleSignInAccount acct) {
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nome", acct.getDisplayName());
                            userData.put("email", acct.getEmail());
                            userData.put("genero", "Não informado (Google)");
                            db.collection("users").document(user.getUid()).set(userData);
                        }
                    }
                });
    }

    private void abrirTela(Class<?> telaDestino) {
        Intent intent = new Intent(this, telaDestino);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- (Início) Métodos Auxiliares de Biometria e Criptografia ---

    private boolean podeAutenticarComBiometria() {
        if (securePreferences == null) return false;
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private boolean temCredenciaisSalvas() {
        if (securePreferences == null) return false;
        return securePreferences.contains(KEY_USER_EMAIL) && securePreferences.contains(KEY_USER_PASS);
    }

    private void salvarCredenciaisSeguras(String email, String senha) {
        if (securePreferences == null) return;
        try {
            securePreferences.edit()
                    .putString(KEY_USER_EMAIL, email)
                    .putString(KEY_USER_PASS, senha)
                    .apply();

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(KEY_BIOMETRIC_EMAIL_ALIAS, email);
            editor.apply();

            Log.d("SecurePrefs", "Credenciais de biometria e alias salvos.");
        } catch (Exception e) {
            Log.e("SecurePrefs", "Erro ao salvar credenciais seguras", e);
        }
    }

    private void apagarCredenciaisSeguras() {
        if (securePreferences == null) return;
        try {
            securePreferences.edit()
                    .remove(KEY_USER_EMAIL)
                    .remove(KEY_USER_PASS)
                    .apply();

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.remove(KEY_BIOMETRIC_EMAIL_ALIAS);
            editor.apply();

            Log.d("SecurePrefs", "Credenciais de biometria e alias apagados.");
        } catch (Exception e) {
            Log.e("SecurePrefs", "Erro ao apagar credenciais seguras", e);
        }
    }

    // --- NOVO MÉTODO: EXIBE O DIALOG DE RECUPERAÇÃO DE SENHA ---
    private void mostrarDialogoRecuperacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Redefinir Senha");
        builder.setMessage("Digite seu e-mail para receber o link de redefinição:");

        // Cria o campo de input programaticamente
        final EditText inputDialog = new EditText(this);
        inputDialog.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Tenta preencher automaticamente se o usuário já digitou no campo principal
        String emailAtual = inputEmail.getText().toString().trim();
        if (!emailAtual.isEmpty()) {
            inputDialog.setText(emailAtual);
        }

        // Adiciona margens ao EditText para ficar visualmente agradável
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 60;
        params.rightMargin = 60;
        inputDialog.setLayoutParams(params);
        container.addView(inputDialog);
        builder.setView(container);

        // Botão Enviar
        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = inputDialog.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, digite um e-mail.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "E-mail enviado! Verifique sua caixa de entrada.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String erro = "Erro ao enviar e-mail.";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                erro = "Este e-mail não está cadastrado.";
                            } catch (Exception e) {
                                erro = "Erro: " + e.getMessage();
                            }
                            Toast.makeText(MainActivity.this, erro, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Botão Cancelar
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}