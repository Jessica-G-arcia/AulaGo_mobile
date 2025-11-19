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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserType;
    private String currentUserId;

    // <--- ALTERAÇÃO: Nova variável para guardar o status do professor
    private String currentStatusSolicitacao;

    // Adapters
    private LanguageAdapter languageAdapter;
    private TopUserAdapter topUserAdapter;
    private HomeAulaAdapter homeAulaAdapter;
    private ViewPager2 viewPagerAlunos;


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
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        currentUserType = "professor"; // Valor padrão, será atualizado pelo fetchUserTypeAndSetupViews

        if (currentUserId == null) {
            return;
        }

        loadWelcomeMessage();

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        viewPagerAlunos = binding.viewpagerAlunos;
        fetchUserTypeAndSetupViews();
    }


    private void loadWelcomeMessage() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (binding == null) return;

                        if (document.exists()) {
                            String nomeCompleto = document.getString("nome");
                            if (nomeCompleto != null && !nomeCompleto.isEmpty()) {
                                String primeiroNome = nomeCompleto.split(" ")[0];
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


    private String loadUserTypeFromPreferences() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("AulaGoPrefs", Context.MODE_PRIVATE);
        return sharedPref.getString("USER_TYPE", "aluno");
    }

    // --- VISÃO DO ALUNO ---
    private void setupAlunoView() {
        binding.tvTopUsersTitle.setText("Top 10 Professores");
        setupLanguagesCarousel();
        setupTopUsersCarousel("professor");
        setupAulasCarousel("alunoId");
    }

    // --- VISÃO DO PROFESSOR ---
    private void setupProfessorView() {
        binding.tvTopUsersTitle.setText("Top 10 Alunos");
        setupLanguagesCarousel();
        setupTopUsersCarousel("aluno");
        setupAulasCarousel("professorId");
    }


    // --- Carrossel de Idiomas ---
    private void setupLanguagesCarousel() {
        languageAdapter = new LanguageAdapter(new ArrayList<>());
        RecyclerView recyclerView = binding.recyclerLanguages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(languageAdapter);

        // <--- ALTERAÇÃO: Nova Lógica de Clique ---
        languageAdapter.setOnFlagClickListener(language -> {
            if (language.getName().equalsIgnoreCase("Inglês")) {

                Fragment nextFragment;

                // Verifica se é professor
                if ("professor".equalsIgnoreCase(currentUserType)) {

                    // Se for professor, verifica o status
                    // O equalsIgnoreCase é seguro mesmo se currentStatusSolicitacao for null? Não, melhor tratar null.
                    boolean isAprovado = currentStatusSolicitacao != null && currentStatusSolicitacao.equalsIgnoreCase("aprovado");

                    if (isAprovado) {
                        // Professor Aprovado -> Vai para busca de alunos
                        nextFragment = new SearchAlunosFragment();
                    } else {
                        // Professor NÃO Aprovado (Pendente, Reprovado, etc) -> Tela de Bloqueio
                        // Assumindo que o nome da sua classe java seja LockedFeatureFragment
                        nextFragment = new LockedFeatureFragment();
                    }

                } else {
                    // Se não for professor (é aluno) -> Vai para busca de professores
                    nextFragment = new SearchProfessoresFragment();
                }

                // Executa a navegação
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, nextFragment)
                        .addToBackStack(null)
                        .commit();

            } else {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Em breve")
                        .setMessage("Este idioma estará disponível em breve!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        // <--- FIM DA ALTERAÇÃO ---


        db.collection("languages")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (binding == null) return;
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

    private void setupTopUsersCarousel(String userTypeToFetch) {
        topUserAdapter = new TopUserAdapter(new ArrayList<>());
        viewPagerAlunos.setAdapter(topUserAdapter);

        viewPagerAlunos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateViewPagerHeight(position);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userType", userTypeToFetch)
                .orderBy("ratingMedia", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (getContext() == null) {
                        return;
                    }
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<UserModel> users = task.getResult().toObjects(UserModel.class);
                        topUserAdapter.updateList(users);
                        viewPagerAlunos.post(() -> updateViewPagerHeight(0));
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar users: ", task.getException());
                    }
                });
    }


    private void updateViewPagerHeight(int position) {
        if (topUserAdapter == null || topUserAdapter.getItemCount() == 0) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) viewPagerAlunos.getChildAt(0);
        if (recyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            View itemView = viewHolder.itemView;
            itemView.post(() -> {
                int wMeasureSpec = View.MeasureSpec.makeMeasureSpec(itemView.getWidth(), View.MeasureSpec.EXACTLY);
                int hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                itemView.measure(wMeasureSpec, hMeasureSpec);

                int measuredHeight = itemView.getMeasuredHeight();

                if (viewPagerAlunos.getLayoutParams().height != measuredHeight) {
                    ViewGroup.LayoutParams layoutParams = viewPagerAlunos.getLayoutParams();
                    layoutParams.height = measuredHeight;
                    viewPagerAlunos.setLayoutParams(layoutParams);
                }
            });
        }
    }

    // --- Carrossel Aulas de Hoje (Agenda) ---
    private void setupAulasCarousel(String idField) {
        homeAulaAdapter = new HomeAulaAdapter(new ArrayList<>(), this.currentUserType);

        RecyclerView recyclerViewAulas = binding.recyclerAulas;
        LinearLayoutManager layoutManagerAulas = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAulas.setLayoutManager(layoutManagerAulas);
        recyclerViewAulas.setAdapter(homeAulaAdapter);

        Timestamp startTimestamp = new Timestamp(new Date());

        db.collection("aulas")
                .whereEqualTo("status", "confirmada")
                .whereEqualTo(idField, currentUserId)
                .whereGreaterThanOrEqualTo("dataTimestamp", startTimestamp)
                .orderBy("dataTimestamp")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (binding == null) return;

                    if (task.isSuccessful()) {
                        List<ClassModel> aulas = task.getResult().toObjects(ClassModel.class);
                        if (aulas.isEmpty()) {
                            binding.recyclerAulas.setVisibility(View.GONE);
                            binding.tvSemAulas.setText("Nenhuma aula agendada.");
                            binding.tvSemAulas.setVisibility(View.VISIBLE);
                            binding.tvTodayClassesTitle.setText("Aulas Agendadas");
                        } else {
                            binding.recyclerAulas.setVisibility(View.VISIBLE);
                            binding.tvSemAulas.setVisibility(View.GONE);
                            binding.tvTodayClassesTitle.setText("Aulas Agendadas");
                            homeAulaAdapter.updateList(aulas);
                        }
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar aulas: ", task.getException());
                        binding.recyclerAulas.setVisibility(View.GONE);
                        binding.tvSemAulas.setText("Erro ao carregar aulas.");
                        binding.tvSemAulas.setVisibility(View.VISIBLE);
                    }
                });
    }


    private void fetchUserTypeAndSetupViews() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding == null) {
                        Log.w("HomeFragment", "Binding nulo. O fragmento foi destruído.");
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");

                        // <--- ALTERAÇÃO: Capturando o status da solicitação
                        this.currentStatusSolicitacao = documentSnapshot.getString("statusSolicitacao");
                        // <--- FIM DA ALTERAÇÃO

                        this.currentUserType = userType;

                        saveUserToPrefs(documentSnapshot);

                        if ("aluno".equals(userType)) {
                            setupAlunoView();
                        } else {
                            setupProfessorView();
                        }
                    } else {
                        Log.e("HomeFragment", "Usuário logado não encontrado no Firestore!");
                        this.currentUserType = "aluno";
                        setupAlunoView();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    Log.e("HomeFragment", "Erro ao buscar tipo de usuário", e);
                    this.currentUserType = "aluno";
                    setupAlunoView();
                });
    }

    private void saveUserToPrefs(DocumentSnapshot userDocument) {
        if (getContext() == null) return;

        String userType = userDocument.getString("userType");
        String userName = userDocument.getString("nome");
        String userAvatar = userDocument.getString("urlFotoPerfil");

        SharedPreferences sharedPref = requireContext().getSharedPreferences("AulaGoPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USER_TYPE", userType);
        editor.putString("USER_NAME", userName);
        editor.putString("USER_AVATAR_URL", userAvatar != null ? userAvatar : "");
        editor.apply();

        Log.d("HomeFragment", "Dados do usuário salvos no SharedPreferences.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}