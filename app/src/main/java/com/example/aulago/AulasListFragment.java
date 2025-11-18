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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AulasListFragment extends Fragment {

    private static final String ARG_IS_CONCLUIDA = "is_concluida";
    private FirebaseFirestore db;
    private ClassAdapter adapter;
    private List<ClassModel> listaDeAulas;

    public static AulasListFragment newInstance(boolean isConcluida) {
        AulasListFragment fragment = new AulasListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_CONCLUIDA, isConcluida);
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

        boolean isConcluida = getArguments() != null && getArguments().getBoolean(ARG_IS_CONCLUIDA, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAulasFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaDeAulas = new ArrayList<>();
        adapter = new ClassAdapter(getContext(), listaDeAulas);
        recyclerView.setAdapter(adapter);
        adapter.setOnAvaliarClickListener(classModel -> abrirFragmentAvaliacao(classModel));


        db = FirebaseFirestore.getInstance();
        carregarDadosDoFirebase(isConcluida);
    }

    private void carregarDadosDoFirebase(boolean isConcluida) {
        listaDeAulas.clear();

        db.collection("aulas")
                .whereEqualTo("status", "confirmada")
                // .whereEqualTo("professorId", ...) // Descomente para filtrar somente do professor logado
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long agora = System.currentTimeMillis();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ClassModel aula = document.toObject(ClassModel.class);
                            long dataAula = aula.getDataTimestamp().toDate().getTime();
                            boolean aulaConcluida = dataAula < agora;

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

    private void abrirFragmentAvaliacao(ClassModel aula) {
        AvaliacaoFragment fragment = AvaliacaoFragment.newInstance(aula.getAlunoNome());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

}
