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

        currentUserType = getArguments() != null ? getArguments().getString(ARG_USER_TYPE, "aluno") : "aluno";
        boolean isConcluida = getArguments() != null && getArguments().getBoolean(ARG_IS_CONCLUIDA, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAulasFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaDeAulas = new ArrayList<>();
        adapter = new ClassAdapter(getContext(), listaDeAulas, currentUserType);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        carregarDadosDoFirebase(isConcluida);
    }

    private void carregarDadosDoFirebase(boolean isConcluida) {
        listaDeAulas.clear();

        String field = currentUserType.equals("aluno") ? "alunoId" : "professorId";

        db.collection("aulas")
                .whereEqualTo(field, currentUserId)
                .whereEqualTo("status", "confirmada")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long agora = System.currentTimeMillis();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ClassModel aula = document.toObject(ClassModel.class);

                            Date diaAula = aula.getDataTimestamp() != null ? aula.getDataTimestamp().toDate() : null;
                            String horarioFim = aula.getHorarioFim(); // Ex: "19:14"
                            if (diaAula == null || horarioFim == null || !horarioFim.contains(":")) continue;

                            // Junta o dia + horário de fim, para verificar se a aula já terminou:
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(diaAula);
                            String[] parts = horarioFim.split(":");
                            if (parts.length != 2) continue;
                            int hour = Integer.parseInt(parts[0]);
                            int minute = Integer.parseInt(parts[1]);
                            cal.set(Calendar.HOUR_OF_DAY, hour);
                            cal.set(Calendar.MINUTE, minute);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);

                            long terminoAulaMillis = cal.getTimeInMillis();
                            boolean aulaConcluida = agora > terminoAulaMillis;

                            // Opcional: debug
                            // Log.d("AulasDebug", "Aula: "+aula.getAlunoNome()+", Termino: "+cal.getTime()+", Agora: "+(new Date(agora))+", Concluida? "+aulaConcluida);

                            if (aulaConcluida == isConcluida) {
                                listaDeAulas.add(aula);
                            }
                        }
                        adapter.updateList(listaDeAulas);
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar aulas: ", task.getException());
                    }
                });
    }


}
