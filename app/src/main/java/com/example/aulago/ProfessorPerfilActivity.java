package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfessorPerfilActivity extends AppCompatActivity {

    private static final String TAG = "ProfessorPerfilActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TabLayout tabLayout;
    private View groupBio, groupAvaliacoes;
    private TextView tvNome, tvEmail, tvTelefone, tvGenero;
    private TextView tvDataNascimento, tvCpf;

    // TextViews para endereço
    private TextView tvEndereco, tvNumero, tvComplemento;
    private TextView tvBairro, tvCidade, tvCep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_perfil);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initViews();
        carregarDadosProfessor();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        groupBio = findViewById(R.id.groupBio);
        groupAvaliacoes = findViewById(R.id.groupAvaliacoes);
        //groupHighlights = findViewById(R.id.groupHighlights);

        // Dados pessoais
        tvNome = findViewById(R.id.inputNome);
        tvEmail = findViewById(R.id.inputEmail);
        tvTelefone = findViewById(R.id.inputTelefone);
        tvGenero = findViewById(R.id.spinnerGenero);
        tvDataNascimento = findViewById(R.id.inputDtNasc);
        tvCpf = findViewById(R.id.inputCpf);

        // Endereço
        tvEndereco = findViewById(R.id.inputEndereco);
        tvNumero = findViewById(R.id.inputNumero);
        tvComplemento = findViewById(R.id.inputComplemento);
        tvBairro = findViewById(R.id.inputBairro);
        tvCidade = findViewById(R.id.inputCidade);
        tvCep = findViewById(R.id.inputCep);
    }

    private void carregarDadosProfessor() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Nenhum usuário logado.", Toast.LENGTH_SHORT).show();

             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);
            return;
        }

        String uid = currentUser.getUid();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Converte o documento do Firestore para o nosso objeto DadosUsuario
                DadosUsuario professor = documentSnapshot.toObject(DadosUsuario.class);
                if (professor != null) {
                    // Preenche a UI com os dados do objeto
                    preencherUI(professor);
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
    private void preencherUI(DadosUsuario professor) {

        // Dados Pessoais
        tvNome.setText(professor.getNome() != null ? professor.getNome() : "Não informado");
        tvEmail.setText(professor.getEmail() != null ? professor.getEmail() : "Não informado");
        tvTelefone.setText(professor.getTelefone());
        tvGenero.setText(professor.getGenero() != null ? professor.getGenero() : "Não informado");
        tvDataNascimento.setText(professor.getDtNasc());
        tvCpf.setText(professor.getCpf());

        // Endereço
        tvEndereco.setText(professor.getEndereco() != null ? professor.getEndereco() : "Não informado");
        tvNumero.setText(professor.getNumero() != null ? professor.getNumero() : "S/N");
        tvComplemento.setText(professor.getComplemento() != null && !professor.getComplemento().isEmpty()
                ? professor.getComplemento() : "-");
        tvBairro.setText(professor.getBairro() != null ? professor.getBairro() : "Não informado");
        tvCidade.setText(professor.getCidade() != null ? professor.getCidade() : "Não informado");
        tvCep.setText(professor.getCep());
    }
    private void setupTabs() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText("Bio"));
            tabLayout.addTab(tabLayout.newTab().setText("Avaliações"));
            //tabLayout.addTab(tabLayout.newTab().setText("Highlights"));
        }
        showSection(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { showSection(tab.getPosition()); }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void showSection(int index) {
        groupBio.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        groupAvaliacoes.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        //groupHighlights.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }
}


