package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Adicionado para Snackbar
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Mantido, mas não mais usado
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar; // Adicionado para Snackbar
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity2 extends AppCompatActivity {

    // NOVO: Adicione inputEstado aqui
    private EditText inputCep,inputEndereco, inputBairro, inputCidade, inputEstado;
    private EditText inputNumero, inputComplemento;
    private AutoCompleteTextView spinnerGenero;
    private Button btnCadastrar, btnCancelar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DadosUsuario dadosUsuario; // Objeto para armazenar e salvar todos os dados

    private View mainLayout; // Adicionado para referência do Snackbar

    private boolean isGoogleFlow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro2);

        isGoogleFlow = getIntent().getBooleanExtra(CadastroActivity.KEY_FLUXO_GOOGLE, false);

        // **********************************************
        // 1. RECUPERA O OBJETO DE DADOS DA PRIMEIRA TELA
        // **********************************************
        dadosUsuario = (DadosUsuario) getIntent().getSerializableExtra(CadastroActivity.KEY_DADOS_USUARIO);

        // Verifica se os dados foram passados corretamente
        if (dadosUsuario == null) {
            // Toast substituído por Snackbar
            Snackbar.make(findViewById(R.id.main), "Erro de dados. Retorne à tela anterior.", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarViews();
        configurarListeners();
        ajustarLayout();
        configurarListenerCep();

        // Inicializa o Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configurarDropdown();


    }

    private void inicializarViews() {
        // Inicializa o layout principal para o Snackbar
        mainLayout = findViewById(R.id.main);

        inputEndereco = findViewById(R.id.inputEndereco);
        inputNumero = findViewById(R.id.inputNumero);
        inputComplemento = findViewById(R.id.inputComplemento);
        inputCep = findViewById(R.id.inputCep);
        inputBairro = findViewById(R.id.inputBairro);
        inputCidade = findViewById(R.id.inputCidade);
        // NOVO: Inicializa o inputEstado
        inputEstado = findViewById(R.id.inputEstado);

        spinnerGenero = findViewById(R.id.spinnerGenero);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void configurarListeners() {
        btnCadastrar.setOnClickListener(v -> realizarCadastroCompleto());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarListenerCep() {
        // Listener que é acionado ao pressionar Enter ou perder o foco
        inputCep.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                buscarEnderecoPorCep();
                return true;
            }
            return false;
        });

        // Outro Listener: ao perder o foco (ex: clica em outro campo)
        inputCep.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                buscarEnderecoPorCep();
            }
        });
    }

    private void buscarEnderecoPorCep() {
        String cep = inputCep.getText().toString().trim().replace("-", ""); // Limpa o CEP

        if (cep.length() != 8) {
            inputCep.setError("CEP inválido");
            return;
        }

        // Limpa possíveis erros anteriores
        inputCep.setError(null);

        // API ViaCEP: URL padrão é https://viacep.com.br/ws/{cep}/json/
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("erro") && response.getBoolean("erro")) {
                            // CEP não encontrado (Viacep retorna "erro": true)
                            // Toast substituído por Snackbar
                            Snackbar.make(mainLayout, "CEP não encontrado.", Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        // Preenche os campos
                        inputEndereco.setText(response.getString("logradouro"));
                        inputBairro.setText(response.getString("bairro"));
                        inputCidade.setText(response.getString("localidade")); // localidade é a cidade/município
                        // CORREÇÃO: Preenche o campo de Estado/UF
                        inputEstado.setText(response.getString("uf"));

                        // Move o foco para o próximo campo necessário (Número)
                        inputNumero.requestFocus();

                    } catch (JSONException e) {
                        Log.e("ViaCEP", "Erro no JSON: " + e.getMessage());
                        // Toast substituído por Snackbar
                        Snackbar.make(mainLayout, "Erro ao processar dados do CEP.", Snackbar.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("ViaCEP", "Erro de requisição: " + error.toString());
                    // Toast substituído por Snackbar
                    Snackbar.make(mainLayout, "Erro de rede ao buscar CEP. Verifique sua conexão.", Snackbar.LENGTH_LONG).show();
                });

        // Adiciona a requisição à fila do Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void ajustarLayout() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configurarDropdown() {
        // Usa simple_dropdown_item_1line para o AutoCompleteTextView
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.generos_array,
                android.R.layout.simple_dropdown_item_1line
        );
        spinnerGenero.setAdapter(adapter);
    }


    private void realizarCadastroCompleto() {
        // 2. Coleta os dados da segunda tela e atualiza o objeto DadosUsuario
        dadosUsuario.endereco = inputEndereco.getText().toString().trim();
        dadosUsuario.numero = inputNumero.getText().toString().trim();
        dadosUsuario.complemento = inputComplemento.getText().toString().trim();
        dadosUsuario.cep = inputCep.getText().toString().trim();
        dadosUsuario.bairro = inputBairro.getText().toString().trim();
        dadosUsuario.cidade = inputCidade.getText().toString().trim();
        dadosUsuario.estado = inputEstado.getText().toString().trim();
        dadosUsuario.genero = spinnerGenero.getText().toString().trim();

        // 3. Validação final dos campos
        if (dadosUsuario.endereco.isEmpty() || dadosUsuario.numero.isEmpty() || dadosUsuario.cep.isEmpty() ||
                dadosUsuario.bairro.isEmpty() || dadosUsuario.cidade.isEmpty() || dadosUsuario.estado.isEmpty() || dadosUsuario.genero.isEmpty()) {
            // Toast substituído por Snackbar
            Snackbar.make(mainLayout, "Preencha todos os campos obrigatórios e selecione seu gênero.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (isGoogleFlow) {
            // FLUXO GOOGLE: A conta já foi criada (no login). Apenas salva os dados no Firestore.
            salvarDadosGoogleNoFirestore();
        } else {
            // FLUXO E-MAIL/SENHA: Cria a conta no Auth E salva os dados no Firestore.
            criarContaEmailESenhaECompletar();
        }
    }

    /**
     * Traduz códigos de erro comuns do Firebase Authentication para o português.
     * @param exception A exceção de erro retornada pelo Firebase.
     * @return Uma string de erro traduzida.
     */
    private String traduzirErroFirebase(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "Senha fraca. Sua senha deve seguir as políticas de segurança (mínimo de 6 caracteres, maiúscula, minúscula, número e especial).";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "E-mail inválido ou credenciais incorretas.";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            return "Este e-mail já está em uso por outro usuário.";
        } else {
            // Erro geral (ex: rede, servidor, etc.)
            return "Erro no cadastro. Por favor, tente novamente ou verifique sua conexão.";
        }
    }


    private void salvarDadosGoogleNoFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Toast substituído por Snackbar
            Snackbar.make(mainLayout, "Erro: Usuário do Google não autenticado. Redirecionando para login.", Snackbar.LENGTH_LONG).show();
            // Redireciona para o login para tentar novamente
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // O UID do usuário já existe. Basta usar o set() para salvar os dados completos
        db.collection("users").document(user.getUid())
                .set(dadosUsuario)
                .addOnSuccessListener(aVoid -> {
                    // Toast substituído por Snackbar
                    Snackbar.make(mainLayout, "Cadastro completo com Google realizado!", Snackbar.LENGTH_LONG).show();
                    // Redireciona para a tela principal
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Toast substituído por Snackbar
                    Snackbar.make(mainLayout, "Erro ao salvar dados complementares. Tente novamente.", Snackbar.LENGTH_LONG).show();
                });
    }


    private void criarContaEmailESenhaECompletar() {
        auth.createUserWithEmailAndPassword(dadosUsuario.email, dadosUsuario.senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Salva o objeto DadosUsuario completo no Firestore
                            db.collection("users").document(user.getUid())
                                    .set(dadosUsuario)
                                    // ... (código de sucesso/falha igual antes)
                                    .addOnSuccessListener(aVoid -> {
                                        // Toast substituído por Snackbar
                                        Snackbar.make(mainLayout, "Cadastro realizado com sucesso!", Snackbar.LENGTH_LONG).show();
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        user.delete(); // Exclui a conta criada
                                        // Toast substituído por Snackbar
                                        Snackbar.make(mainLayout, "Erro ao salvar dados. Tente novamente.", Snackbar.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        // TRADUÇÃO AQUI: Usa a nova função para traduzir o erro antes de exibir no Snackbar
                        String mensagemErro = traduzirErroFirebase(task.getException());
                        Snackbar.make(mainLayout, "Erro no cadastro: " + mensagemErro, Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
