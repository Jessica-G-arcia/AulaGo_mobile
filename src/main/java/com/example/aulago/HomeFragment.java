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

public class HomeFragment extends Fragment {

    // ViewBinding para o layout do fragmento (fragment_home.xml)
    private FragmentHomeBinding binding;

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

        // Define o título na Toolbar da MainActivity
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar();
            // Habilita o título se você quiser ver "Início"
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Chama os métodos para configurar cada carrossel
        setupLanguagesCarousel();
        setupAlunosCarousel();
        setupAulasCarousel();
    }


    private void setupLanguagesCarousel() {
        List<Language> languages = new ArrayList<>();
        languages.add(new Language("Inglês", R.drawable.us_flag));
        languages.add(new Language("Espanhol", R.drawable.spain_flag));
        languages.add(new Language("Francês", R.drawable.france_flag));
        languages.add(new Language("Alemão", R.drawable.germany_flag));
        languages.add(new Language("Mandarim", R.drawable.china_flag));

        LanguageAdapter adapter = new LanguageAdapter(languages);

        // Acessa as views diretamente pelo binding do fragmento
        RecyclerView recyclerView = binding.recyclerLanguages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        ImageButton scrollLeftButton = binding.btnScrollLeft;
        ImageButton scrollRightButton = binding.btnScrollRight;

        scrollRightButton.setOnClickListener(v -> {
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            if (lastVisible < adapter.getItemCount() - 1) {
                recyclerView.smoothScrollToPosition(lastVisible + 1);
            }
        });

        scrollLeftButton.setOnClickListener(v -> {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            if (firstVisible > 0) {
                recyclerView.smoothScrollToPosition(firstVisible - 1);
            }
        });
    }

    private void setupAlunosCarousel() {
        List<Aluno> alunos = new ArrayList<>();
        alunos.add(new Aluno(
                R.drawable.aluna1,
                5.0f,
                "Rafaela Gonçalves",
                "Inglês",
                "Rafaela tem um aprendizado rápido, super educada. Aluna sensacional!",
                "Rogério Lima"
        ));
        // Adicione mais alunos se precisar

        AlunoAdapter adapter = new AlunoAdapter(alunos);
        ViewPager2 viewPager = binding.viewpagerAlunos; // Acesso pelo binding do fragmento
        viewPager.setAdapter(adapter);
    }

    private void setupAulasCarousel() {
        List<Aula> aulas = new ArrayList<>();
        aulas.add(new Aula("Lucas Marques", "Sorocaba - SP", "8:00 às 9:00 - Presencial", "01/04/2025", "Inglês", 0, false));
        aulas.add(new Aula("Ana Clara", "Sorocaba - SP", "9:30 às 10:30 - Online", "01/04/2025", "Espanhol", 0, false));
        aulas.add(new Aula("Pedro Henrique", "São Paulo - SP", "11:00 às 12:00 - Online", "01/04/2025", "Francês", 0, false));

        HomeAulaAdapter adapter = new HomeAulaAdapter(aulas);

        RecyclerView recyclerViewAulas = binding.recyclerAulas; // Acesso pelo binding do fragmento
        LinearLayoutManager layoutManagerAulas = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAulas.setLayoutManager(layoutManagerAulas);
        recyclerViewAulas.setAdapter(adapter);

        ImageButton aulasScrollLeftButton = binding.btnAulasLeft;
        ImageButton aulasScrollRightButton = binding.btnAulasRight;

        aulasScrollRightButton.setOnClickListener(v -> {
            int lastVisible = layoutManagerAulas.findLastVisibleItemPosition();
            if (lastVisible < adapter.getItemCount() - 1) {
                recyclerViewAulas.smoothScrollToPosition(lastVisible + 1);
            }
        });

        aulasScrollLeftButton.setOnClickListener(v -> {
            int firstVisible = layoutManagerAulas.findFirstVisibleItemPosition();
            if (firstVisible > 0) {
                recyclerViewAulas.smoothScrollToPosition(firstVisible - 1);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpa o binding para evitar vazamento de memória
        binding = null;
    }
}