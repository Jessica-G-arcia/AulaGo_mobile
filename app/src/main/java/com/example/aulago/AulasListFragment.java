package com.example.aulago;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AulasListFragment extends Fragment {

    private static final String ARG_IS_CONCLUIDA = "is_concluida";
    private static final String ARG_USER_TYPE = "user_type";

    private FirebaseFirestore db;
    private ClassAdapter adapter;
    private List<ClassModel> listaDeAulas;
    private String currentUserType;
    private String currentUserId;
    private boolean isConcluidaTab; // Variável global que vamos usar

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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAulasFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaDeAulas = new ArrayList<>();
        adapter = new ClassAdapter(getContext(), listaDeAulas, currentUserType);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // CORREÇÃO: Chamada sem parâmetros (agora funciona)
        carregarDadosDoFirebase();
    }

    private void carregarDadosDoFirebase() {
        listaDeAulas.clear();

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
                    if (task.isSuccessful()) {
                        List<ClassModel> tempBatch = new ArrayList<>();
                        Log.d("DEBUG_AULAS", "Firebase retornou " + task.getResult().size() + " documentos.");

                        long agora = System.currentTimeMillis();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ClassModel aula = document.toObject(ClassModel.class);

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

                    } else {
                        Log.e("FirebaseError", "Erro fatal no Firebase: ", task.getException());
                    }
                });
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