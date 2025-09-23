package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputSenha;
    private Button btnEntrar, btnCriarConta, btnGoogleLogin;
    private FirebaseAuth auth;
//    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        inicializarViews();
        configurarListeners();
//        configurarGoogleSignIn();
    }

    private void inicializarViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        //btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void configurarListeners() {
        btnEntrar.setOnClickListener(v -> loginComEmailESenha());
        btnCriarConta.setOnClickListener(v -> redirecionarParaCadastro());
        //btnGoogleLogin.setOnClickListener(v -> loginComGoogle());
    }

//    private void configurarGoogleSignIn() {
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//    }

    private void loginComEmailESenha() {
        String email = inputEmail.getText().toString().trim();
        String senha = inputSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        redirecionarParaTelaPrincipal();
                    } else {
                        String mensagemErro = "Erro ao fazer login. Tente novamente.";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            mensagemErro = "Este email não está cadastrado.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            mensagemErro = "A senha está incorreta.";
                        } catch (Exception e) {
                            mensagemErro = "Erro: " + e.getMessage();
                        }
                        Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirecionarParaCadastro() {
        Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

    private void redirecionarParaTelaPrincipal() {
        // Redirecione para a tela principal do seu app, por exemplo, HomeActivity
        // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        // startActivity(intent);
        // finish();
    }

//    private void loginComGoogle() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }

    //@Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                Toast.makeText(this, "Erro no login com Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        redirecionarParaTelaPrincipal();
                    } else {
                        Toast.makeText(this, "Erro ao autenticar com Google.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}