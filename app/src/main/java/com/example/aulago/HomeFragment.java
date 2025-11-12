package com.example.aulago;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView; // Importe o TextView
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Importe o Query
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private String currentUserType;
    private String currentUserId;

    // Adapters
    private LanguageAdapter languageAdapter;
    private TopUserAdapter topUserAdapter; // <-- Nosso NOVO adapter
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
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        currentUserType = loadUserTypeFromPreferences();

        if (currentUserId == null) {
            // Lógica de erro, usuário não logado
            return;
        }

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

    // Carrega o tipo de usuário salvo no login
    private String loadUserTypeFromPreferences() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("AulaGoPrefs", Context.MODE_PRIVATE);
        return sharedPref.getString("USER_TYPE", "aluno");
    }

    // --- VISÃO DO ALUNO ---
    private void setupAlunoView() {
        // Aluno vê "Top 10 Professores"
        // (Assumindo que o ID do TextView no XML é 'tv_titulo_top_users')
        // binding.tvTituloTopUsers.setText("Top 10 Professores");

        setupLanguagesCarousel();
        setupTopUsersCarousel("professor"); // <-- Puxa PROFESSORES
        setupAulasCarousel("alunoId"); // <-- Puxa aulas do ALUNO
    }

    // --- VISÃO DO PROFESSOR ---
    private void setupProfessorView() {
        // Professor vê "Top 10 Alunos"
        // binding.tvTituloTopUsers.setText("Top 10 Alunos");

        setupLanguagesCarousel();
        setupTopUsersCarousel("aluno"); // <-- Puxa ALUNOS
        setupAulasCarousel("professorId"); // <-- Puxa aulas do PROFESSOR
    }


    // --- Carrossel de Idiomas (Existente) ---
    private void setupLanguagesCarousel() {
        languageAdapter = new LanguageAdapter(new ArrayList<>());
        // Assumindo que o ID no XML é 'recycler_languages'
        RecyclerView recyclerView = binding.recyclerLanguages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(languageAdapter);

        // ... Lógica dos botões de scroll (binding.btnScrollLeft, etc) ...

        db.collection("home_languages") // Você precisa ter essa coleção
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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

    // --- Carrossel "Top Users" (CORRIGIDO) ---
    private void setupTopUsersCarousel(String userTypeToFetch) {

        // 1. Corrige o erro de compilação: Usa o NOVO adapter
        topUserAdapter = new TopUserAdapter(new ArrayList<>());

        // 2. Assumindo que o ID no XML é 'viewpager_alunos'
        ViewPager2 viewPager = binding.viewpagerAlunos;
        viewPager.setAdapter(topUserAdapter);

        // 3. Busca na coleção "users"
        db.collection("users")
                .whereEqualTo("userType", userTypeToFetch) // "aluno" ou "professor"
                .orderBy("ratingMedia", Query.Direction.DESCENDING) // Ordena pela nota
                .limit(10) // Top 10
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserModel> users = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            users.add(document.toObject(UserModel.class));
                        }
                        topUserAdapter.updateList(users); // Atualiza o novo adapter
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar top users: ", task.getException());
                    }
                });
    }

    // --- Carrossel Aulas de Hoje (Agenda) ---
    private void setupAulasCarousel(String idField) {
        homeAulaAdapter = new HomeAulaAdapter(new ArrayList<>());
        // Assumindo que o ID no XML é 'recycler_aulas'
        RecyclerView recyclerViewAulas = binding.recyclerAulas;
        LinearLayoutManager layoutManagerAulas = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAulas.setLayoutManager(layoutManagerAulas);
        recyclerViewAulas.setAdapter(homeAulaAdapter);

        // ... Lógica dos botões de scroll (binding.btnAulasLeft, etc) ...

        db.collection("aulas")
                .whereEqualTo("status", "Agendada")
                .whereEqualTo(idField, currentUserId) // Filtra pelo ID do Aluno ou Professor
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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