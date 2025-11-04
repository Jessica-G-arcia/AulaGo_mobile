package com.example.aulago;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.aulago.databinding.FragmentHomeBinding;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
// Imports do Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeFragment extends Fragment {

    // ViewBinding para o layout do fragmento (fragment_home.xml)
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private LanguageAdapter languageAdapter;
    private AlunoAdapter alunoAdapter;
    private HomeAulaAdapter homeAulaAdapter;

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

        // define o título na Toolbar da MainActivity
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar();
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        // Chama os métodos para configurar cada carrossel
        setupLanguagesCarousel();
        setupAlunosCarousel();
        setupAulasCarousel();
    }


    private void setupLanguagesCarousel() {
//        List<Language> languages = new ArrayList<>();
//        languages.add(new Language("Inglês", R.drawable.us_flag));
//        languages.add(new Language("Espanhol", R.drawable.spain_flag));
//        languages.add(new Language("Francês", R.drawable.france_flag));
//        languages.add(new Language("Alemão", R.drawable.germany_flag));
//        languages.add(new Language("Mandarim", R.drawable.china_flag));

        languageAdapter = new LanguageAdapter(new ArrayList<>());

        
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
//        List<Aluno> alunos = new ArrayList<>();
//        alunos.add(new Aluno(
//                R.drawable.aluna1,
//                5.0f,
//                "Rafaela Gonçalves",
//                "Inglês",
//                "Rafaela tem um aprendizado rápido, super educada. Aluna sensacional!",
//                "Rogério Lima"
//        ));

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

    private void setupAulasCarousel() {
//        List<Aula> aulas = new ArrayList<>();
//        aulas.add(new Aula("Lucas Marques", "Sorocaba - SP", "8:00 às 9:00 - Presencial", "01/04/2025", "Inglês", 0, false));
//        aulas.add(new Aula("Ana Clara", "Sorocaba - SP", "9:30 às 10:30 - Online", "01/04/2025", "Espanhol", 0, false));
//        aulas.add(new Aula("Pedro Henrique", "São Paulo - SP", "11:00 às 12:00 - Online", "01/04/2025", "Francês", 0, false));

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

        db.collection("home_aulas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Aula> aulas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            aulas.add(document.toObject(Aula.class));
                        }
                        homeAulaAdapter.updateList(aulas);
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar home_aulas: ", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
