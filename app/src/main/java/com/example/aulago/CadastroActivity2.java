package com.example.aulago;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build; // IMPORT NECESSÁRIO
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity2 extends AppCompatActivity {

    private EditText inputEndereco, inputNumero, inputComplemento, inputCep, inputBairro, inputCidade;
    private Spinner spinnerGenero;
    private Button btnCadastrar, btnCancelar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Variáveis para guardar os dados recebidos
    private DadosUsuario dadosUsuario;
    private String senha; // <-- ADICIONADO: Variável local para a senha

    private ProgressDialog mProgressDialog;

    // Defina as chaves (devem ser as MESMAS da CadastroActivity)
    public static final String KEY_DADOS_USUARIO = "DADOS_USUARIO_KEY";
    public static final String KEY_SENHA = "SENHA_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // EdgeToEdge não é necessário com o ajuste de layout abaixo
        setContentView(R.layout.activity_cadastro2);

        // --- CORREÇÃO AO RECEBER DADOS ---
        // Recupera o objeto de dados da Activity anterior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Forma nova e segura (Android 13+)
            dadosUsuario = getIntent().getSerializableExtra(KEY_DADOS_USUARIO, DadosUsuario.class);
        } else {
            // Forma antiga (necessária para APIs < 33)
            dadosUsuario = (DadosUsuario) getIntent().getSerializableExtra(KEY_DADOS_USUARIO);
        }

        // Recupera a SENHA separadamente
        senha = getIntent().getStringExtra(KEY_SENHA);

        // Verifica se os dados essenciais vieram
        if (dadosUsuario == null || senha == null || senha.isEmpty()) {
            Toast.makeText(this, "Erro ao carregar dados. Tente novamente.", Toast.LENGTH_LONG).show();
            finish(); // Volta para a tela anterior se houver erro
            return;
        }
        // --- FIM DA CORREÇÃO ---

        // Inicializa as views da segunda tela
        inicializarViews();
        configurarListeners();
        ajustarLayout(); // Ajuste de layout para EdgeToEdge

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
        btnCancelar.setOnClickListener(v -> {
            // Apenas fecha esta activity, voltando para a Tela 1
            finish();
        });
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
        String endereco = inputEndereco.getText().toString().trim();
        String numero = inputNumero.getText().toString().trim();
        String complemento = inputComplemento.getText().toString().trim();
        String cep = inputCep.getText().toString().trim();
        String bairro = inputBairro.getText().toString().trim();
        String cidade = inputCidade.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();

        // Validação dos campos da segunda tela
        if (endereco.isEmpty() || numero.isEmpty() || cep.isEmpty() ||
                bairro.isEmpty() || cidade.isEmpty() || genero.equals("Selecione seu gênero")) {
            Toast.makeText(this, "Preencha todos os campos e selecione seu gênero", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- CORREÇÃO (USANDO SETTERS) ---
        // Atualiza o objeto 'dadosUsuario' com os novos dados
        dadosUsuario.setEndereco(endereco);
        dadosUsuario.setNumero(numero);
        dadosUsuario.setComplemento(complemento);
        dadosUsuario.setCep(cep);
        dadosUsuario.setBairro(bairro);
        dadosUsuario.setCidade(cidade);
        dadosUsuario.setGenero(genero);

        mostrarProgressDialog("Criando sua conta...");

        // --- CORREÇÃO (USANDO GETTER E VARIÁVEL SENHA) ---
        // Realiza o cadastro no Firebase Auth
        auth.createUserWithEmailAndPassword(dadosUsuario.getEmail(), senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {

                            // --- ADIÇÃO IMPORTANTE ---
                            // Adiciona o UID ao objeto antes de salvar no banco
                            dadosUsuario.setUid(user.getUid());

                            // Salva os dados completos no Firestore usando o UID do usuário
                            db.collection("users").document(user.getUid())
                                    .set(dadosUsuario) // Agora o objeto não contém mais a senha
                                    .addOnSuccessListener(aVoid -> {
                                        esconderProgressDialog();
                                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                                        // Redireciona para a tela de login (ou Home) e limpa o histórico
                                        Intent intent = new Intent(this, MainActivity.class); // Mude para MainActivity (Login) ou HomeActivity
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish(); // Finaliza esta e a CadastroActivity
                                    })
                                    .addOnFailureListener(e -> {
                                        esconderProgressDialog();
                                        // Se falhar o salvamento, exclui a conta criada para evitar inconsistência
                                        user.delete();
                                        Toast.makeText(this, "Erro ao salvar dados do usuário: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        esconderProgressDialog();
                        // Se a autenticação falhar (ex: email já existe)
                        Toast.makeText(this, "Erro no cadastro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- Métodos de ProgressDialog (opcional, mas recomendado) ---

    private void mostrarProgressDialog(String mensagem) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(mensagem);
        mProgressDialog.show();
    }

    private void esconderProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
