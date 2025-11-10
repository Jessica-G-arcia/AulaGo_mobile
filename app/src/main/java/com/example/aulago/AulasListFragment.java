package com.example.aulago;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

// Imports do Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AulasListFragment extends Fragment {

    private static final String ARG_IS_CONCLUIDA = "is_concluida";

    private FirebaseFirestore db;
    private ClassAdapter adapter; // <-- MUDOU: de AulasAdapter para ClassAdapter
    private List<ClassModel> listaDeAulas; // <-- MUDOU: de Aula para ClassModel

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

        boolean isConcluida = getArguments() != null && getArguments().getBoolean(ARG_IS_CONCLUIDA);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAulasFragment);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        listaDeAulas = new ArrayList<>();
        // MUDOU: Inicializa o novo ClassAdapter
        adapter = new ClassAdapter(getContext(), listaDeAulas);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        carregarDadosDoFirebase(isConcluida);
    }

    private void carregarDadosDoFirebase(boolean isConcluida) {
        listaDeAulas.clear();
        String statusQuery;

        // MUDOU: Traduz o 'boolean' para o 'String' do seu novo ClassModel
        if (isConcluida) {
            statusQuery = "Concluída";
        } else {
            statusQuery = "Agendada"; // Ou "Confirmada", "Pendente", etc.
            // Ajuste conforme o seu Firebase
        }

        db.collection("aulas") // Certifique-se que o nome da coleção está correto
                .whereEqualTo("status", statusQuery) // <-- MUDOU: Query por status
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // MUDOU: Converte para ClassModel
                            ClassModel aula = document.toObject(ClassModel.class);
                            listaDeAulas.add(aula);
                        }
                        adapter.updateList(listaDeAulas); // Atualiza o adapter
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar aulas: ", task.getException());
                    }
                });
    }
}