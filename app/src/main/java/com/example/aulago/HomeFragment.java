package com.example.aulago;

// Imports Essenciais do Android

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.Timestamp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // <-- Import que estava a falhar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;

// Import do ViewBinding (gerado do 'fragment_home.xml')
import com.example.aulago.databinding.FragmentHomeBinding;

// Imports do Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Imports das suas classes (Adapters e Models)
import com.example.aulago.LanguageAdapter;
import com.example.aulago.AlunoAdapter;
import com.example.aulago.HomeAulaAdapter;
import com.example.aulago.Language; // Model
import com.example.aulago.Aluno;    // Model
import com.example.aulago.ClassModel; // O seu NOVO Model de Aula

// Imports do Java
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ViewBinding para o layout do fragmento (fragment_home.xml)
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private LanguageAdapter languageAdapter;
    private AlunoAdapter alunoAdapter;
    private HomeAulaAdapter homeAulaAdapter; // Este foi atualizado para ClassModel

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout e inicializa o binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Define o título na Toolbar da ToolbarActivity
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar();
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        // Chama os métodos para configurar cada carrossel
        setupLanguagesCarousel();
        setupAlunosCarousel();
        setupAulasCarousel(); // Este método está atualizado
    }


    private void setupLanguagesCarousel() {
        // Inicialize o adapter com lista vazia
        languageAdapter = new LanguageAdapter(new ArrayList<>());
        RecyclerView recyclerView = binding.recyclerLanguages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(languageAdapter);

        ImageButton scrollLeftButton = binding.btnScrollLeft;
        ImageButton scrollRightButton = binding.btnScrollRight;

        scrollRightButton.setOnClickListener(v -> {
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            if (lastVisible < languageAdapter.getItemCount() - 1) {
                recyclerView.smoothScrollToPosition(lastVisible + 1);
            }
        });

        scrollLeftButton.setOnClickListener(v -> {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            if (firstVisible > 0) {
                recyclerView.smoothScrollToPosition(firstVisible - 1);
            }
        });

        // Busca os dados do Firebase
        db.collection("home_languages")
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

    private void setupAlunosCarousel() {
        // inicializa o adapter com lista vazia
        alunoAdapter = new AlunoAdapter(new ArrayList<>());
        ViewPager2 viewPager = binding.viewpagerAlunos;
        viewPager.setAdapter(alunoAdapter);

        db.collection("home_alunos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Aluno> alunos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            alunos.add(document.toObject(Aluno.class));
                        }
                        alunoAdapter.updateList(alunos); // Atualiza o adapter
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar alunos: ", task.getException());
                    }
                });
    }

    // ESTE MÉTODO ESTÁ ATUALIZADO PARA USAR 'ClassModel'
    private void setupAulasCarousel() {
        homeAulaAdapter = new HomeAulaAdapter(new ArrayList<>());
        RecyclerView recyclerViewAulas = binding.recyclerAulas;
        LinearLayoutManager layoutManagerAulas = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAulas.setLayoutManager(layoutManagerAulas);
        recyclerViewAulas.setAdapter(homeAulaAdapter);

        ImageButton aulasScrollLeftButton = binding.btnAulasLeft;
        ImageButton aulasScrollRightButton = binding.btnAulasRight;

        aulasScrollRightButton.setOnClickListener(v -> {
            int lastVisible = layoutManagerAulas.findLastVisibleItemPosition();
            if (lastVisible < homeAulaAdapter.getItemCount() - 1) {
                recyclerViewAulas.smoothScrollToPosition(lastVisible + 1);
            }
        });

        aulasScrollLeftButton.setOnClickListener(v -> {
            int firstVisible = layoutManagerAulas.findFirstVisibleItemPosition();
            if (firstVisible > 0) {
                recyclerViewAulas.smoothScrollToPosition(firstVisible - 1);
            }
        });

        db.collection("aulas") // <-- MUDOU: de "home_aulas" para "aulas"
                .whereEqualTo("status", "Agendada") // <-- Exemplo de consulta
                .limit(10) // <-- Boa prática: limite os resultados para um carrossel
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