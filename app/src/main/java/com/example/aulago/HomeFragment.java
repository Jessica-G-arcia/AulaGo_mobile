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
import com.google.firebase.auth.FirebaseUser; // <-- Importe o FirebaseUser
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
    private FirebaseAuth auth; // <-- ADICIONEI ESTA LINHA
    private String currentUserType;
    private String currentUserId;

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
        auth = FirebaseAuth.getInstance(); // <-- ADICIONEI ESTA LINHA
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        currentUserType = "professor";

        if (currentUserId == null) {
            // Lógica de erro, usuário não logado
            return;
        }

        loadWelcomeMessage();

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        viewPagerAlunos = binding.viewpagerAlunos;
        fetchUserTypeAndSetupViews();
    }


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
        setupTopUsersCarousel("professor"); // <-- Puxa PROFESSORES
        setupAulasCarousel("alunoId"); // <-- Puxa aulas do ALUNO
    }

    // --- VISÃO DO PROFESSOR ---
    private void setupProfessorView() {
        // Professor vê "Top 10 Alunos"
        binding.tvTopUsersTitle.setText("Top 10 Alunos"); // <-- Usei o ID do seu XML

        setupLanguagesCarousel();
        setupTopUsersCarousel("aluno"); // <-- Puxa ALUNOS
        setupAulasCarousel("professorId"); // <-- Puxa aulas do PROFESSOR
    }


    // --- Carrossel de Idiomas (Existente) ---
    private void setupLanguagesCarousel() {
        languageAdapter = new LanguageAdapter(new ArrayList<>());
        RecyclerView recyclerView = binding.recyclerLanguages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(languageAdapter);

        // Coloque isso logo após criar o adapter e o RecyclerView dos idiomas
        languageAdapter.setOnFlagClickListener(language -> {
            if (language.getName().equalsIgnoreCase("Inglês")) {
                Fragment nextFragment;
                if ("aluno".equalsIgnoreCase(currentUserType)) {
                    // Usuário é aluno → busca professores
                    nextFragment = new SearchProfessoresFragment(); // ou SearchProfessoresFragment.newInstance(), conforme seu projeto
                } else {
                    // Qualquer outro tipo, por padrão professor → busca alunos
                    nextFragment = new SearchAlunosFragment(); // ou SearchAlunosFragment.newInstance(), conforme seu projeto
                }

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

    // No seu Fragmento (ex: HomeFragment.java)

    private void setupTopUsersCarousel(String userTypeToFetch) {
        // 1. Inicializa o adapter
        topUserAdapter = new TopUserAdapter(new ArrayList<>());

        // 2. Vincula o adapter ao ViewPager2
        // Garanta que 'viewPagerAlunos' foi inicializado (ex: viewPagerAlunos = binding.viewpagerAlunos;)
        viewPagerAlunos.setAdapter(topUserAdapter);

        // 3. Registra o callback para mudança de página
        // Fazemos isso ANTES de buscar os dados
        viewPagerAlunos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Chama nosso novo método para ajustar a altura
                updateViewPagerHeight(position);
            }
        });

        // 4. Busca os dados no Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userType", userTypeToFetch)
                .orderBy("ratingMedia", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    // Prevenção de crash se o fragmento for destruído
                    if (getContext() == null) {
                        return;
                    }

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<UserModel> users = task.getResult().toObjects(UserModel.class);
                        topUserAdapter.updateList(users);

                        // --- PONTO CRÍTICO DA CORREÇÃO ---
                        // Após a lista ser atualizada, ajusta a altura para o primeiro item (posição 0)
                        // Usamos post para garantir que o layout já foi recalculado com os novos dados
                        viewPagerAlunos.post(() -> updateViewPagerHeight(0));

                    } else {
                        Log.e("FirebaseError", "Erro ao buscar users: ", task.getException());
                    }
                });
    }


    /**
     * Método reutilizável para medir a altura de um item do ViewPager2 e ajustar o layout.
     *
     * @param position A posição do item a ser medido.
     */
    private void updateViewPagerHeight(int position) {
        // Garante que o adapter e o viewpager estão prontos
        if (topUserAdapter == null || topUserAdapter.getItemCount() == 0) {
            return;
        }

        // Acessa o RecyclerView interno do ViewPager2 de forma segura
        RecyclerView recyclerView = (RecyclerView) viewPagerAlunos.getChildAt(0);
        if (recyclerView == null) {
            return;
        }

        // Encontra o ViewHolder para a posição alvo
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            View itemView = viewHolder.itemView;

            // Mede a altura e atualiza o layout do ViewPager2
            itemView.post(() -> {
                int wMeasureSpec = View.MeasureSpec.makeMeasureSpec(itemView.getWidth(), View.MeasureSpec.EXACTLY);
                int hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                itemView.measure(wMeasureSpec, hMeasureSpec);

                int measuredHeight = itemView.getMeasuredHeight();

                // Ajusta a altura do ViewPager2 se for diferente
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


        // --- LÓGICA DA DATA ---
        // 1. Pega a partir do horario atual
        Timestamp startTimestamp = new Timestamp(new Date());

        // (O endTimestamp não é mais usado na consulta,
        // mas pode ser útil para outra lógica no futuro)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfToday = cal.getTime();
        Timestamp endTimestamp = new Timestamp(endOfToday);


        // Busca todas as aulas (limite 10) a partir do INÍCIO de hoje
        db.collection("aulas")
                .whereEqualTo("status", "confirmada")
                .whereEqualTo(idField, currentUserId)
                .whereGreaterThanOrEqualTo("dataTimestamp", startTimestamp) // A partir do início de hoje
                .orderBy("dataTimestamp") // Ordena pela data (mais próxima primeiro)
                .limit(10) // Pega as próximas 10
                .get()
                .addOnCompleteListener(task -> {
                    if (binding == null) return; // Proteção contra crash

                    if (task.isSuccessful()) {
                        List<ClassModel> aulas = task.getResult().toObjects(ClassModel.class);

                        if (aulas.isEmpty()) {
                            // Nenhuma aula HOJE nem no FUTURO.
                            binding.recyclerAulas.setVisibility(View.GONE);
                            binding.tvSemAulas.setText("Nenhuma aula agendada.");
                            binding.tvSemAulas.setVisibility(View.VISIBLE);
                            binding.tvTodayClassesTitle.setText("Aulas Agendadas");

                        } else {
                            // Temos aulas!
                            binding.recyclerAulas.setVisibility(View.VISIBLE);
                            binding.tvSemAulas.setVisibility(View.GONE);

                            // Define o título fixo (como você pediu)
                            binding.tvTodayClassesTitle.setText("Aulas Agendadas");

                            // Envia a lista para o adapter
                            homeAulaAdapter.updateList(aulas);
                        }

                    } else {
                        // --- CASO 3: A TAREFA FALHOU (Ex: Erro de índice) ---
                        Log.e("FirebaseError", "Erro ao buscar aulas: ", task.getException());
                        binding.recyclerAulas.setVisibility(View.GONE);
                        binding.tvSemAulas.setText("Erro ao carregar aulas.");
                        binding.tvSemAulas.setVisibility(View.VISIBLE);
                    }
                });
    }


    /**
     * Busca o documento do usuário logado no Firestore para descobrir seu tipo
     * e, em seguida, configura a view (aluno ou professor).
     */
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

                        // --- CORREÇÃO AQUI ---
                        // Salva o tipo de usuário na variável de nível de classe
                        this.currentUserType = userType;

                        saveUserToPrefs(documentSnapshot);

                        if ("aluno".equals(userType)) {
                            setupAlunoView();
                        } else {
                            setupProfessorView();
                        }
                    } else {
                        Log.e("HomeFragment", "Usuário logado não encontrado no Firestore!");
                        this.currentUserType = "aluno"; // Define um padrão
                        setupAlunoView();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    Log.e("HomeFragment", "Erro ao buscar tipo de usuário", e);
                    this.currentUserType = "aluno"; // Define um padrão
                    setupAlunoView();
                });
    }

    /**
     * Função auxiliar para salvar os dados do usuário no SharedPreferences
     */
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