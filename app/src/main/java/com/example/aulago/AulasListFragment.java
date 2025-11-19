package com.example.aulago;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat; // Importe para formatar data
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale; // Importe para o idioma da data

public class AulasListFragment extends Fragment {

    private static final String ARG_IS_CONCLUIDA = "is_concluida";
    private static final String ARG_USER_TYPE = "user_type";

    private FirebaseFirestore db;
    private FirebaseAuth auth; // Adicionei o Auth
    private ClassAdapter adapter;
    private List<ClassModel> listaDeAulas;
    private String currentUserType;
    private String currentUserId;
    private boolean isConcluidaTab; // Variável global que vamos usar

    private LinearLayout layoutEmptyState;
    private TextView tvEmptyTitle;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public static AulasListFragment newInstance(boolean isConcluida, String userType) {
        AulasListFragment fragment = new AulasListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_CONCLUIDA, isConcluida);
        args.putString(ARG_USER_TYPE, userType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aulas_list, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Recupera argumentos
        currentUserType = getArguments() != null ? getArguments().getString(ARG_USER_TYPE, "aluno") : "aluno";
        isConcluidaTab = getArguments() != null && getArguments().getBoolean(ARG_IS_CONCLUIDA, false);

        // 2. Pega o usuário logado com segurança
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return;
        }

        recyclerView = view.findViewById(R.id.recyclerViewAulasFragment);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        progressBar = view.findViewById(R.id.progressBarAulas);

        if (isConcluidaTab) {
            tvEmptyTitle.setText("Nenhuma aula concluída");
        } else {
            tvEmptyTitle.setText("Nenhuma aula agendada");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAulasFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaDeAulas = new ArrayList<>();
        adapter = new ClassAdapter(getContext(), listaDeAulas, currentUserType);
        recyclerView.setAdapter(adapter);

        adapter.setOnAvaliarClickListener(classModel -> abrirFragmentAvaliacao(classModel));

        db = FirebaseFirestore.getInstance();

        // CORREÇÃO: Chamada sem parâmetros (agora funciona)
        carregarDadosDoFirebase();
    }

    private void carregarDadosDoFirebase() {
        listaDeAulas.clear();

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        String field = currentUserType.equals("aluno") ? "alunoId" : "professorId";

        Log.d("DEBUG_AULAS", "Buscando aulas onde " + field + " == " + currentUserId);
        Log.d("DEBUG_AULAS", "Aba Concluida? " + isConcluidaTab);

        Query.Direction direcao = Query.Direction.ASCENDING;

        db.collection("aulas")
                .whereEqualTo(field, currentUserId)
                .whereEqualTo("status", "confirmada")
                .orderBy("dataTimestamp", direcao)
                .get()
                .addOnCompleteListener(task -> {

                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<ClassModel> tempBatch = new ArrayList<>();
                        Log.d("DEBUG_AULAS", "Firebase retornou " + task.getResult().size() + " documentos.");

                        long agora = System.currentTimeMillis();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ClassModel aula = document.toObject(ClassModel.class);

                                aula.setAulaId(document.getId());

                                if (aula.getDataTimestamp() == null || aula.getHorarioFim() == null) {
                                    continue;
                                }

                                Date diaAula = aula.getDataTimestamp().toDate();

                                // Usando o método helper para limpar o código
                                long terminoAulaMillis = calcularTerminoEmMillis(diaAula, aula.getHorarioFim());

                                if (terminoAulaMillis == 0) continue; // Erro no parse

                                boolean aulaJaPassou = agora > terminoAulaMillis;

                                // CORREÇÃO: Usando a variável global isConcluidaTab
                                if (aulaJaPassou == isConcluidaTab) {
                                    tempBatch.add(aula);
                                }

                            } catch (Exception e) {
                                Log.e("DEBUG_AULAS", "Erro ao processar item: " + e.getMessage());
                            }
                        }

                        // Inverte a lista visualmente se for aba de Concluídas (para ver as mais recentes no topo)
                        if (isConcluidaTab) {
                            java.util.Collections.reverse(tempBatch);
                        }

                        listaDeAulas.addAll(tempBatch);
                        adapter.updateList(listaDeAulas);

                        if (listaDeAulas.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            layoutEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            layoutEmptyState.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("FirebaseError", "Erro fatal no Firebase: ", task.getException());
                    }
                });
    }

    private void abrirFragmentAvaliacao(ClassModel aula) {

        // A. Formata Data e Hora
        String textoDataHora = "--/--";
        if (aula.getDataTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", new Locale("pt", "BR"));
            String dataStr = sdf.format(aula.getDataTimestamp().toDate());
            String inicio = aula.getHorarioInicio() != null ? aula.getHorarioInicio() : "--:--";
            String fim = aula.getHorarioFim() != null ? aula.getHorarioFim() : "--:--";
            textoDataHora = dataStr + " • " + inicio + " - " + fim;
        }

        // B. Pega a Modalidade (Tenta campo modalidade, senão usa local, senão Presencial)
        String textoModalidade = "Presencial";
        if (aula.getModalidade() != null && !aula.getModalidade().isEmpty()) {
            textoModalidade = aula.getModalidade();
            // Capitaliza a primeira letra (opcional)
            textoModalidade = textoModalidade.substring(0, 1).toUpperCase() + textoModalidade.substring(1);
        } else if (aula.getLocal() != null) {
            textoModalidade = aula.getLocal();
        }

        // C. Define QUEM será avaliado (Professor avalia Aluno)
        String idParaAvaliar = aula.getAlunoId();

        // D. Pega o ID da Aula
        String idDaAula = aula.getAulaId();

        // E. Abre o Fragmento
        AvaliacaoFragment fragment = AvaliacaoFragment.newInstance(
                idParaAvaliar,
                textoDataHora,
                textoModalidade,
                idDaAula
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Verifique se o ID do container é esse mesmo
                .addToBackStack(null)
                .commit();
    }


    // Helper para calcular o timestamp final da aula
    private long calcularTerminoEmMillis(Date dataAula, String horarioFim) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataAula);

            String[] parts = horarioFim.split(":");
            if (parts.length != 2) return 0;

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}