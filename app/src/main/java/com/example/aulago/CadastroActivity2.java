package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.RadioButton; // <-- IMPORT NOVO
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; // <-- IMPORTAÇÃO ADICIONADA
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
import com.google.firebase.firestore.FieldValue;

// Importe o HashMap e Map para a lógica de salvar
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.FieldValue;


public class CadastroActivity2 extends AppCompatActivity {

    private EditText inputCep, inputEndereco, inputBairro, inputCidade, inputEstado;
    private EditText inputNumero, inputComplemento;
    private AutoCompleteTextView spinnerGenero;
    private Button btnCadastrar, btnCancelar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DadosUsuario dadosUsuario; // Objeto para armazenar os dados

    // CORREÇÃO: A senha deve ser uma variável separada, não parte do objeto
    private String senha;

    private View mainLayout;
    private boolean isGoogleFlow = false;
    private RadioGroup rgTipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro2);

        isGoogleFlow = getIntent().getBooleanExtra(CadastroActivity.KEY_FLUXO_GOOGLE, false);
        dadosUsuario = (DadosUsuario) getIntent().getSerializableExtra(CadastroActivity.KEY_DADOS_USUARIO);

        // CORREÇÃO: Pega a senha separadamente
        senha = getIntent().getStringExtra(CadastroActivity.KEY_SENHA);

        // Verifica se os dados essenciais vieram
        // (Se for fluxo de email, a senha é essencial)
        if (dadosUsuario == null || (!isGoogleFlow && (senha == null || senha.isEmpty()))) {
            Snackbar.make(findViewById(R.id.main), "Erro de dados. Retorne à tela anterior.", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarViews();
        configurarListeners();
        ajustarLayout();
        configurarListenerCep();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configurarDropdown();
    }

    private void inicializarViews() {
        mainLayout = findViewById(R.id.main);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputNumero = findViewById(R.id.inputNumero);
        inputComplemento = findViewById(R.id.inputComplemento);
        inputCep = findViewById(R.id.inputCep);
        inputBairro = findViewById(R.id.inputBairro);
        inputCidade = findViewById(R.id.inputCidade);
        inputEstado = findViewById(R.id.inputEstado);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCancelar = findViewById(R.id.btnCancelar);
        rgTipoUsuario = findViewById(R.id.rgTipoUsuario);
    }

    private void configurarListeners() {
        btnCadastrar.setOnClickListener(v -> realizarCadastroCompleto());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarListenerCep() {
        // (Seu código ViaCEP está ótimo e não precisa de mudanças)
        inputCep.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                buscarEnderecoPorCep();
                return true;
            }
            return false;
        });

        inputCep.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                buscarEnderecoPorCep();
            }
        });
    }

    private void buscarEnderecoPorCep() {
        String cep = inputCep.getText().toString().trim().replace("-", "");
        if (cep.length() != 8) {
            inputCep.setError("CEP inválido");
            return;
        }
        inputCep.setError(null);
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("erro") && response.getBoolean("erro")) {
                            Snackbar.make(mainLayout, "CEP não encontrado.", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        inputEndereco.setText(response.getString("logradouro"));
                        inputBairro.setText(response.getString("bairro"));
                        inputCidade.setText(response.getString("localidade"));
                        inputEstado.setText(response.getString("uf")); // Seu código já estava correto
                        inputNumero.requestFocus();
                    } catch (JSONException e) {
                        Log.e("ViaCEP", "Erro no JSON: " + e.getMessage());
                        Snackbar.make(mainLayout, "Erro ao processar dados do CEP.", Snackbar.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("ViaCEP", "Erro de requisição: " + error.toString());
                    Snackbar.make(mainLayout, "Erro de rede ao buscar CEP. Verifique sua conexão.", Snackbar.LENGTH_LONG).show();
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void ajustarLayout() {
        // 'main' é o seu ConstraintLayout
        View mainView = findViewById(R.id.main);

        // 1. Salva o padding original que você definiu no XML
        // (Isso captura seus 24dp de start/end e 8dp de top/bottom)
        int originalPaddingLeft = mainView.getPaddingLeft();
        int originalPaddingTop = mainView.getPaddingTop();
        int originalPaddingRight = mainView.getPaddingRight();
        int originalPaddingBottom = mainView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            // 2. Pega os insets da barra de status (topo)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 3. Pega os insets do TECLADO (IME)
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            // 4. Pega os insets da barra de navegação (gestos/botões)
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            // 5. Calcula o padding
            int paddingLeft = originalPaddingLeft + systemBars.left;
            int paddingTop = originalPaddingTop + systemBars.top;
            int paddingRight = originalPaddingRight + systemBars.right;

            // O padding de baixo é o original + o MAIOR valor entre o teclado e a barra de navegação
            int paddingBottom = originalPaddingBottom + Math.max(imeInsets.bottom, navBars.bottom);

            // 6. Aplica o padding
            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            return insets;
        });
    }

    private void configurarDropdown() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.generos_array,
                android.R.layout.simple_dropdown_item_1line
        );
        spinnerGenero.setAdapter(adapter);
    }


    private void realizarCadastroCompleto() {
        // CORREÇÃO: Usando "setters" para atualizar o objeto
        dadosUsuario.setEndereco(inputEndereco.getText().toString().trim());
        dadosUsuario.setNumero(inputNumero.getText().toString().trim());
        dadosUsuario.setComplemento(inputComplemento.getText().toString().trim());
        dadosUsuario.setCep(inputCep.getText().toString().trim());
        dadosUsuario.setBairro(inputBairro.getText().toString().trim());
        dadosUsuario.setCidade(inputCidade.getText().toString().trim());
        dadosUsuario.setEstado(inputEstado.getText().toString().trim());
        dadosUsuario.setGenero(spinnerGenero.getText().toString().trim());

        // CORREÇÃO: Usando "getters" para validar
        if (dadosUsuario.getEndereco().isEmpty() || dadosUsuario.getNumero().isEmpty() || dadosUsuario.getCep().isEmpty() ||
                dadosUsuario.getBairro().isEmpty() || dadosUsuario.getCidade().isEmpty() || dadosUsuario.getEstado().isEmpty() || dadosUsuario.getGenero().isEmpty()) {
            Snackbar.make(mainLayout, "Preencha todos os campos obrigatórios e selecione seu gênero.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (isGoogleFlow) {
            salvarDadosGoogleNoFirestore();
        } else {
            criarContaEmailESenhaECompletar();
        }
    }

    private String traduzirErroFirebase(Exception exception) {
        // (Seu código de tradução está ótimo)
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "Senha fraca. Sua senha deve seguir as políticas de segurança.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "E-mail inválido ou credenciais incorretas.";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            return "Este e-mail já está em uso por outro usuário.";
        } else {
            return "Erro no cadastro. Por favor, tente novamente.";
        }
    }

    // --- CORREÇÃO: Este método agora salva um Map, não o objeto
    private void salvarDadosGoogleNoFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Snackbar.make(mainLayout, "Erro: Usuário Google não autenticado.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Salva os dados no Firestore usando um Map
        // (Isso usa a mesma lógica do 'criarContaEmailESenhaECompletar')
        salvarDadosNoFirestore(user.getUid());
    }


    private void criarContaEmailESenhaECompletar() {
        // CORREÇÃO: Usa 'getEmail()' do objeto e a variável 'senha'
        auth.createUserWithEmailAndPassword(dadosUsuario.getEmail(), senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Salva os dados no Firestore
                            salvarDadosNoFirestore(user.getUid());
                        }
                    } else {
                        String mensagemErro = traduzirErroFirebase(task.getException());
                        Snackbar.make(mainLayout, "Erro no cadastro: " + mensagemErro, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // --- MÉTODO AUXILIAR ATUALIZADO ---

    /**
     * Pega o objeto 'dadosUsuario' e o salva no Firestore
     * usando um Map, para garantir o schema correto.
     */
    private void salvarDadosNoFirestore(String uid) {
        // Cria um Map para salvar os dados
        Map<String, Object> userData = new HashMap<>();

        String tipoUsuarioSelecionado = "aluno"; // Padrão
        String statusSolicitacao = "nenhum";

        int selectedId = rgTipoUsuario.getCheckedRadioButtonId();

        if (selectedId == R.id.rbProfessor) {
            tipoUsuarioSelecionado = "professor";
            // O professor nasce com status "nenhum" (ainda não enviou docs).
            // Isso fará ele cair na tela de bloqueio (LockedFeatureFragment) quando tentar ver alunos.
            statusSolicitacao = "nenhum";
        }

        // Dados Pessoais (Telas 1 e 2)
        userData.put("uid", uid);
        userData.put("nome", dadosUsuario.getNome());
        userData.put("email", dadosUsuario.getEmail());
        userData.put("cpf", dadosUsuario.getCpf());
        userData.put("telefone", dadosUsuario.getTelefone());
        userData.put("dataNascimento", dadosUsuario.getDtNasc());
        userData.put("genero", dadosUsuario.getGenero());

        // Dados de Endereço (Tela 2)
        userData.put("endereco", dadosUsuario.getEndereco());
        userData.put("numero", dadosUsuario.getNumero());
        userData.put("complemento", dadosUsuario.getComplemento());
        userData.put("cep", dadosUsuario.getCep());
        userData.put("bairro", dadosUsuario.getBairro());
        userData.put("cidade", dadosUsuario.getCidade());
        userData.put("estado", dadosUsuario.getEstado());

        // Campos de Controle Padrão
        userData.put("userType", tipoUsuarioSelecionado);
        userData.put("statusSolicitacao", statusSolicitacao);
        userData.put("statusVerificacao", "nenhum");
        userData.put("comprovanteUrl", "");
        userData.put("urlFotoPerfil", "");
        userData.put("dataCadastro", FieldValue.serverTimestamp());


        // Inicializa campos para evitar NullPointerException depois
        if (tipoUsuarioSelecionado.equals("professor")) {
            userData.put("professorVerificado", false);
            userData.put("certificadoUrl", "");
        }

        // Salva o Map no Firestore
        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {

                    // --- INÍCIO DA CORREÇÃO ---

                    // 1. O LOG QUE VOCÊ PEDIU:
                    // Verifique a aba "Logcat" no Android Studio filtrando por "CADASTRO_DEBUG"
                    Log.d("CADASTRO_DEBUG", "SUCESSO: .addOnSuccessListener foi chamado.");

                    // 2. VERIFICAÇÃO DE SEGURANÇA:
                    // Checa se a Activity ainda está ativa antes de mostrar o diálogo.
                    // Se 'isFinishing()' for true, a Activity está morrendo e não pode mostrar um diálogo.
                    if (isFinishing() || isDestroyed()) {
                        Log.w("CADASTRO_DEBUG", "Activity está finalizando. Diálogo de sucesso pulado.");
                        return; // Não faz mais nada
                    }

                    // 3. SEU CÓDIGO ORIGINAL (agora seguro):
                    // Exibe um pop-up de sucesso antes de navegar
                    new AlertDialog.Builder(this)
                            .setTitle("")
                            .setMessage("Cadastro realizado com sucesso!")
                            .setPositiveButton("OK", (dialog, which) -> {
                                Log.d("CADASTRO_DEBUG", "Usuário clicou em OK. Navegando...");
                                // A navegação agora acontece DENTRO do clique do botão
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .setCancelable(false) // Impede o usuário de fechar
                            .show();
                    // --- FIM DA CORREÇÃO ---

                })
                .addOnFailureListener(e -> {
                    Log.e("CADASTRO_DEBUG", "FALHA ao salvar no Firestore: ", e);
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null && !isGoogleFlow) {
                        // Se for fluxo de email, deleta o usuário do Auth
                        user.delete();
                    }
                    Snackbar.make(mainLayout, "Erro ao salvar dados. Tente novamente.", Snackbar.LENGTH_LONG).show();
                });
    }
}