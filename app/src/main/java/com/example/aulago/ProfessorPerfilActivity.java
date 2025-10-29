package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
// import com.bumptech.glide.Glide; // Importe se você usa Glide para fotos

public class ProfessorPerfilActivity extends AppCompatActivity {

    private static final String TAG = "ProfessorPerfilActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DadosUsuario dadosUsuarioAtual; // Guardar o objeto para passar para a edição

    // --- Views Públicas ---
    private TabLayout tabLayout;
    private View groupBio, groupAvaliacoes;
    private TextView inputNome, inputEspecialidade, inputValorPresencial, inputValorOnline;
    private ImageView ivAvatar;
    private Button btnEditar;

    // Lançador para a tela de edição
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_perfil);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        initViews();
        setupTabs();
        configurarListeners();
        registrarLauncher(); // Configura o que fazer ao voltar da edição

        if (currentUser == null) {
            tratarUsuarioNaoLogado();
            return;
        }

        carregarDadosProfessor();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        groupBio = findViewById(R.id.groupBio);
        groupAvaliacoes = findViewById(R.id.groupAvaliacoes);

        // Header (Dados Públicos)
        inputNome = findViewById(R.id.inputNome);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnEditar = findViewById(R.id.btnEditar);

        // Info Pública
        inputEspecialidade = findViewById(R.id.inputEspecialidade);
        inputValorPresencial = findViewById(R.id.inputValorPresencial);
        inputValorOnline = findViewById(R.id.inputValorOnline);
    }

    private void registrarLauncher() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Checa se o resultado da EditarPerfilActivity foi "OK"
                    if (result.getResultCode() == RESULT_OK) {
                        // Se sim, recarrega os dados do professor do Firestore
                        Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                        carregarDadosProfessor();
                    }
                }
        );
    }

    private void configurarListeners() {
        btnEditar.setOnClickListener(v -> {
            if (dadosUsuarioAtual == null) {
                Toast.makeText(this, "Aguarde, carregando dados...", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inicia a EditarPerfilActivity
            Intent intent = new Intent(this, EditarPerfilActivity.class);
            // Passa o objeto de dados inteiro para a tela de edição
            intent.putExtra("DADOS_USUARIO", dadosUsuarioAtual);

            // Inicia a activity usando o launcher
            editProfileLauncher.launch(intent);
        });
    }

    private void carregarDadosProfessor() {
        String uid = currentUser.getUid();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Guarda o objeto de dados completo
                dadosUsuarioAtual = documentSnapshot.toObject(DadosUsuario.class);
                if (dadosUsuarioAtual != null) {
                    // Preenche a UI apenas com os dados PÚBLICOS
                    preencherUI(dadosUsuarioAtual);
                }
            } else {
                Log.d(TAG, "Nenhum documento encontrado para este usuário.");
                Toast.makeText(this, "Perfil não encontrado.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erro ao buscar dados do perfil", e);
            Toast.makeText(this, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Preenche a UI APENAS com dados públicos.
     */
    private void preencherUI(DadosUsuario professor) {
        inputNome.setText(professor.getNome() != null ? professor.getNome() : "Não informado");


        // Assumindo que você tenha getters para estes campos no seu DadosUsuario.java
        // tvEspecialidade.setText(professor.getEspecialidade());
        // tvValorPresencial.setText(professor.getValorPresencial());
        // tvValorOnline.setText(professor.getValorOnline());

        // Ex: Carregar foto com Glide
        // if (professor.getFotoUrl() != null) {
        //     Glide.with(this).load(professor.getFotoUrl()).into(ivAvatar);
        // }
    }

    private void tratarUsuarioNaoLogado() {
        Toast.makeText(this, "Nenhum usuário logado.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class); // (ou LoginActivity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Bio"));
        tabLayout.addTab(tabLayout.newTab().setText("Avaliações"));

        showSection(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showSection(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showSection(int index) {
        groupBio.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        groupAvaliacoes.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    }
}
