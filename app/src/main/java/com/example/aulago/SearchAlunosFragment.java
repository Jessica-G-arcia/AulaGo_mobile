package com.example.aulago;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// ❗️ MUDANÇA: Implementa a interface de clique do Adapter
public class SearchAlunosFragment extends Fragment implements AlunoAdapter.OnAlunoClickListener {

    private static final String TAG = "SearchAlunosFragment";

    private EditText etSearch;
    private RecyclerView rvAlunos;
    private AlunoAdapter adapter;
    private List<Aluno> listaDeAlunos = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_alunos, container, false);
        db = FirebaseFirestore.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // IDs do seu fragment_search.xml
        etSearch = view.findViewById(R.id.etSearch);
        rvAlunos = view.findViewById(R.id.rvAlunos);

        setupRecyclerView();

        // ❗️ MUDANÇA: Chama a função de filtro
        setupSearchFilter();

        // Busca os dados do Firebase (filtrando apenas por "aluno")
        fetchAlunosFromFirestore();
    }

    private void setupRecyclerView() {
        // ❗️ MUDANÇA: Passa 'this' (o Fragment) como o listener
        adapter = new AlunoAdapter(getContext(), listaDeAlunos, this);
        rvAlunos.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlunos.setAdapter(adapter);
    }

    private void fetchAlunosFromFirestore() {
        // Busca na coleção "users" apenas os que tem userType == "aluno"
        db.collection("users")
                .whereEqualTo("userType", "aluno")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaDeAlunos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // O @DocumentId no Aluno.java vai pegar o ID do doc
                            Aluno aluno = document.toObject(Aluno.class);
                            aluno.setId(document.getString("uid"));

                            listaDeAlunos.add(aluno);
                        }
                        // ❗️ AVISA O ADAPTER (que agora tem a lista completa)
                        adapter.updateList(listaDeAlunos);
                    } else {
                        Log.w(TAG, "Erro ao buscar documentos.", task.getException());
                    }
                });
    }

    // ---
    // LÓGICA DO FILTRO IMPLEMENTADA
    // ---
    private void setupSearchFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ❗️ CHAMA O FILTRO DO ADAPTER A CADA TECLA DIGITADA
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Esta função é chamada pelo AlunoAdapter quando o botão "Conferir Perfil" é clicado.
     */
    @Override
    public void onAlunoClick(Aluno aluno) {
        Log.d(TAG, "Clicou para ver perfil de: " + aluno.getNome());

        // 1. Obtenha o ID do aluno que foi clicado.
        String idDoAlunoClicado = aluno.getId();

        // 2. Crie o Fragment de PERFIL PÚBLICO, USANDO o newInstance
        //    (Isto garante que a chave "ALUNO_ID" é usada, o que o Fragment de destino espera)
        Fragment profileFragment = AlunoPerfilPublicoFragment.newInstance(idDoAlunoClicado);


        // 3. Inicia a transação de Fragment
        if (getActivity() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null) // Permite ao usuário "voltar"
                    .commit();
        }
    }
}