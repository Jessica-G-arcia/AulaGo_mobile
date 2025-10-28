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

    // Instância do Firebase
    private FirebaseFirestore db;
    private AulasAdapter adapter;

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

        // Inicialize o adapter com uma lista vazia
        // O AulasAdapter já tem o método filterList, que vamos usar
        adapter = new AulasAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Inicialize o Firestore
        db = FirebaseFirestore.getInstance();

        // 3. Chame a nova função para carregar os dados
        carregarDadosDoFirebase(isConcluida);

//        List<Aula> todasAsAulas = carregarDadosDeExemplo();
//        List<Aula> listaFiltrada = new ArrayList<>();
//        for (Aula aula : todasAsAulas) {
//            if (aula.isConcluida() == isConcluida) {
//                listaFiltrada.add(aula);
//            }
//        }

//        AulasAdapter adapter = new AulasAdapter(listaFiltrada);
//        recyclerView.setAdapter(adapter);
    }

    // função para buscar dados
    private void carregarDadosDoFirebase(boolean isConcluida) {
        List<Aula> listaFiltrada = new ArrayList<>();

        db.collection("aulas")
                .whereEqualTo("concluida", isConcluida) // A MÁGICA! Filtra no Firebase
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte o documento para um objeto Aula
                            Aula aula = document.toObject(Aula.class);
                            listaFiltrada.add(aula);
                        }
                        // 5. Usa o método filterList do seu adapter para atualizar a UI
                        adapter.filterList(listaFiltrada);
                    } else {
                        Log.e("FirebaseError", "Erro ao buscar aulas: ", task.getException());
                    }
                });
    }

//    private List<Aula> carregarDadosDeExemplo() {
//        List<Aula> aulas = new ArrayList<>();
//        // Agendadas
//        aulas.add(new Aula("Fernanda Dias", "Sorocaba - SP", "8:00 às 9:00", "17/07/2025", "Inglês", 0, false));
//        aulas.add(new Aula("Marcos Paulo", "Sorocaba - SP", "9:00 às 10:00", "25/07/2025", "Inglês", 0, false));
//        // Concluídas
//        aulas.add(new Aula("Maria Silva", "Sorocaba - SP", "10:00 às 11:00", "10/06/2025", "Inglês", 5, true));
//        aulas.add(new Aula("Carlos Souza", "Sorocaba - SP", "11:00 às 12:00", "11/06/2025", "Inglês", 4, true));
//        return aulas;
//    }
}