package com.example.aulago;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aulago.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // <-- Importe o FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // <-- ADICIONEI ESTA LINHA
    private String currentUserType;
    private String currentUserId;

    // Adapters
    private LanguageAdapter languageAdapter;
    private TopUserAdapter topUserAdapter;
    private HomeAulaAdapter homeAulaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // <-- ADICIONEI ESTA LINHA
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        currentUserType = loadUserTypeFromPreferences();

        if (currentUserId == null) {
            // Lógica de erro, usuário não logado
            return;
        }

        // --- ADICIONEI A CHAMADA PARA O MÉTODO NOVO ---
        loadWelcomeMessage();

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Lógica de visualização
        if ("aluno".equals(currentUserType)) {
            setupAlunoView();
        } else {
            setupProfessorView();
        }
    }

    // --- NOVO MÉTODO ADICIONADO ---

    /**
     * Busca o nome do usuário no Firestore e atualiza o TextView
     */
    private void loadWelcomeMessage() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Busca o documento do usuário na coleção 'users'
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        // Verifica se o binding ainda é válido
                        if (binding == null) return;

                        if (document.exists()) {
                            String nomeCompleto = document.getString("nome");
                            if (nomeCompleto != null && !nomeCompleto.isEmpty()) {

                                // Pega apenas o primeiro nome
                                String primeiroNome = nomeCompleto.split(" ")[0];

                                // Atualiza o TextView (assumindo que o ID no XML é tvBoasVindas)
                                binding.tvBoasVindas.setText("Olá, " + primeiroNome + "!");
                            } else {
                                binding.tvBoasVindas.setText("Olá!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeFragment", "Erro ao buscar nome", e);
                        if (binding != null) {
                            binding.tvBoasVindas.setText("Olá!");
                        }
                    });
        } else {
            binding.tvBoasVindas.setText("Olá!");
        }
    }


    // Carrega o tipo de usuário salvo no login
    private String loadUserTypeFromPreferences() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("AulaGoPrefs", Context.MODE_PRIVATE);
        return sharedPref.getString("USER_TYPE", "aluno");
    }

    // --- VISÃO DO ALUNO ---
    private void setupAlunoView() {
        // Aluno vê "Top 10 Professores"
        binding.tvTopUsersTitle.setText("Top 10 Professores"); // <-- Usei o ID do seu XML

        setupLanguagesCarousel();
//        setupTopUsersCarousel("professor"); // <-- Puxa PROFESSORES
        setupAulasCarousel("alunoId"); // <-- Puxa aulas do ALUNO
    }

    // --- VISÃO DO PROFESSOR ---
    private void setupProfessorView() {
        // Professor vê "Top 10 Alunos"
        binding.tvTopUsersTitle.setText("Top 10 Alunos"); // <-- Usei o ID do seu XML

        setupLanguagesCarousel();
//        setupTopUsersCarousel("aluno"); // <-- Puxa ALUNOS
        setupAulasCarousel("professorId"); // <-- Puxa aulas do PROFESSOR
    }


    // --- Carrossel de Idiomas (Existente) ---
    private void setupLanguagesCarousel() {
        languageAdapter = new LanguageAdapter(new ArrayList<>());
        RecyclerView recyclerView = binding.recyclerLanguages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(languageAdapter);

        // TODO: Lógica dos botões de scroll (binding.btnScrollLeft, etc)

        db.collection("languages") // Você precisa ter essa coleção
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (binding == null) return; // Verifica se o fragmento ainda existe
                        List<Language> languages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            languages.add(document.toObject(Language.class));
                        }
                        languageAdapter.updateList(languages);
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar languages: ", task.getException());
                    }
                });
    }

//    // --- Carrossel "Top Users" (Corrigido) ---
//    private void setupTopUsersCarousel(String userTypeToFetch) {
//
//        // 1. Inicializa o adapter
//        // Garanta que seu TopUserAdapter pode ser inicializado assim.
//        topUserAdapter = new TopUserAdapter(new ArrayList<>());
//
//        // 2. Vincula o adapter ao ViewPager2
//        // (O ID 'viewpager_alunos' parece correto, conforme seu XML)
//        binding.viewpagerAlunos.setAdapter(topUserAdapter);
//
//        // 3. Busca os dados no Firebase
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users")
//                .whereEqualTo("userType", userTypeToFetch)
//                .orderBy("ratingMedia", Query.Direction.DESCENDING)
//                .limit(10)
//                .get()
//                .addOnCompleteListener(task -> {
//
//                    // --- VERIFICAÇÃO DE SEGURANÇA CRÍTICA ---
//                    // O fragmento ainda existe quando o Firebase respondeu?
//                    // Se o usuário saiu da tela, o 'binding' será nulo
//                    // e tentar usá-lo causaria o crash que você viu.
//                    if (binding == null) {
//                        Log.w("HomeFragment", "Binding nulo. O fragmento foi destruído antes da consulta do Firebase terminar.");
//                        return; // Sai da função para evitar o crash
//                    }
//                    // --- Fim da Verificação ---
//
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        // Tenta converter os documentos.
//                        // Se ISTO falhar, veja os "Pontos Críticos" abaixo.
//                        List<UserModel> users = task.getResult().toObjects(UserModel.class);
//
//                        if (users.isEmpty()) {
//                            Log.i("HomeFragment", "Nenhum usuário encontrado para o tipo: " + userTypeToFetch);
//                            // Você pode até mostrar uma mensagem de "Nenhum professor encontrado"
//                        }
//
//                        // Se tudo deu certo, atualiza o adapter.
//                        topUserAdapter.updateList(users);
//
//                    } else {
//                        Log.e("FirebaseError", "Erro ao buscar users: ", task.getException());
//                    }
//                });
//    }

    // --- Carrossel Aulas de Hoje (Agenda) ---
    private void setupAulasCarousel(String idField) {
        homeAulaAdapter = new HomeAulaAdapter(new ArrayList<>());
        RecyclerView recyclerViewAulas = binding.recyclerAulas;
        LinearLayoutManager layoutManagerAulas = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAulas.setLayoutManager(layoutManagerAulas);
        recyclerViewAulas.setAdapter(homeAulaAdapter);

        // TODO: Lógica dos botões de scroll (binding.btnAulasLeft, etc) ...

        db.collection("aulas")
                .whereEqualTo("status", "Agendada")
                .whereEqualTo(idField, currentUserId) // Filtra pelo ID do Aluno ou Professor
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (binding == null) return;
                        List<ClassModel> aulas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            aulas.add(document.toObject(ClassModel.class));
                        }
                        homeAulaAdapter.updateList(aulas);
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar aulas: ", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}