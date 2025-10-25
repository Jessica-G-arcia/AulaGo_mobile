package com.example.aulago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputSenha;
    private Button btnEntrar, btnCriarConta, btnGoogleLogin;
    // Mantenha como Switch se estiver usando android.widget.Switch ou MaterialSwitch
    private Switch switchLembrarSenha;

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    FirebaseFirestore db;

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Supondo que você tem R.layout.activity_main
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inicializarViews();
        verificarSessaoSalva(); // Verifica se há sessão ativa ao iniciar
        configurarGoogleSignIn();
        configurarListeners();
    }

    private void inicializarViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        switchLembrarSenha = findViewById(R.id.switchLembrarSenha);

        // Recupera o estado anterior do Switch
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);
        switchLembrarSenha.setChecked(rememberMe);
    }

    private void verificarSessaoSalva() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);

        if (rememberMe) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                // Sessão ativa e marcada para ser lembrada: redireciona imediatamente
                Toast.makeText(this, "Bem-vindo de volta, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                redirecionarParaTelaPrincipal();
            }
        }
    }

    private void configurarListeners() {
        btnEntrar.setOnClickListener(v -> loginComEmailESenha());
        btnCriarConta.setOnClickListener(v -> redirecionarParaCadastro());
        btnGoogleLogin.setOnClickListener(v -> loginComGoogle());
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // O default_web_client_id DEVE estar no seu strings.xml
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void verificarExistenciaUsuarioELogar(String email, String senha) {

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        salvarEstadoDoSwitch();
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        redirecionarParaTelaPrincipal();
                    } else {
                        String mensagemErro = "Erro ao fazer login. Verifique as credenciais.";
                        try {
                            // Lança a exceção para ser capturada
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            // **ESTA É A EXCEÇÃO CORRETA:** Usuário não encontrado no Firebase Auth.
                            mensagemErro = "O e-mail digitado não está cadastrado no sistema.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            // Usuário existe, mas credenciais inválidas (geralmente, senha incorreta).
                            // Ocasionalmente, o Firebase emite essa exceção para *usuário não existente* também.
                            mensagemErro = "A senha está incorreta ou o usuário não está cadastrado.";
                        } catch (Exception e) {
                            mensagemErro = "Erro desconhecido: " + e.getLocalizedMessage();
                            Log.e("LoginError", "Erro de login: " + e.getMessage());
                        }
                        Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void loginComEmailESenha() {
        String email = inputEmail.getText().toString().trim();
        String senha = inputSenha.getText().toString().trim();

        boolean camposPreenchidos = true;

        if (email.isEmpty()) {
            inputEmail.setError("O e-mail é obrigatório.");
            camposPreenchidos = false;
        } else {
            inputEmail.setError(null);
        }

        if (senha.isEmpty()) {
            inputSenha.setError("A senha é obrigatória.");
            camposPreenchidos = false;
        } else {
            inputSenha.setError(null);
        }

        if (!camposPreenchidos) {
            Toast.makeText(this, "Preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        verificarExistenciaUsuarioELogar(email, senha);
    }

    private void redirecionarParaCadastro() {
        Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

    private void redirecionarParaTelaPrincipal() {
        // Redirecione para a tela principal real do seu app (ex: HomeActivity.class)
        // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        // startActivity(intent);
        // finish();
        Toast.makeText(this, "Redirecionando para a tela principal...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Erro no login com Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("FirebaseAuth", "Autenticando com credenciais Google: " + acct.getIdToken());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        salvarEstadoDoSwitch();
                        // Sucesso no Firebase Auth
                        FirebaseUser user = auth.getCurrentUser();

                        // CHAMA O NOVO FLUXO DE REDIRECIONAMENTO
                        tratarLoginGoogle(user, acct);

                    } else {
                        // Falha no Firebase Auth
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
                            // USUÁRIO NOVO: Redireciona para o cadastro (primeira tela) com dados preenchidos

                            Toast.makeText(this, "Bem-vindo! Complete seu cadastro.", Toast.LENGTH_LONG).show();

                            // Cria o objeto de dados parciais
                            DadosUsuario dadosParciais = new DadosUsuario();
                            dadosParciais.nome = acct.getDisplayName();
                            dadosParciais.email = acct.getEmail();

                            // Redireciona para a primeira tela de cadastro
                            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);

                            // Adiciona um FLAG especial para o CadastroActivity saber que é um fluxo Google
                            intent.putExtra(CadastroActivity.KEY_FLUXO_GOOGLE, true);

                            // Envia os dados parciais
                            intent.putExtra(CadastroActivity.KEY_DADOS_GOOGLE, dadosParciais);

                            startActivity(intent);
                            finish(); // Fecha a MainActivity

                        } else {
                            // USUÁRIO EXISTENTE: Salva dados básicos (se necessário) e redireciona para a principal
                            salvarUsuarioFirestoreSeNovo(user, acct);

                            Toast.makeText(this, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            redirecionarParaTelaPrincipal();
                        }
                    } else {
                        Log.e("Firestore", "Erro ao verificar existência do usuário: " + task.getException());
                        // Em caso de erro, por segurança, trata como login normal (pode dar erro na próxima tela)
                        Toast.makeText(this, "Erro ao verificar dados. Tentando login normal...", Toast.LENGTH_SHORT).show();
                        redirecionarParaTelaPrincipal();
                    }
                });
    }

    private void salvarEstadoDoSwitch() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER_ME, switchLembrarSenha.isChecked());
        editor.apply();
    }

    /**
     * Verifica se o usuário é novo (primeiro login com Google) e salva dados no Firestore.
     */
    private void salvarUsuarioFirestoreSeNovo(FirebaseUser user, GoogleSignInAccount acct) {
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            // Usuário novo! Salvar dados básicos no Firestore.
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nome", acct.getDisplayName());
                            userData.put("email", acct.getEmail());
                            userData.put("genero", "Não informado (Google)");

                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Dados do Google salvos com sucesso."))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Erro ao salvar dados do Google: " + e.getMessage()));
                        }
                    } else {
                        Log.e("Firestore", "Erro ao verificar existência do usuário: " + task.getException());
                    }
                });
    }
}