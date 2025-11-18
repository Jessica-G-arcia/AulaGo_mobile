package com.example.aulago;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchProfessoresFragment extends Fragment implements ProfessorAdapter.OnProfessorClickListener {

    private static final String TAG = "SearchProfessoresFragment";

    private EditText etSearch;
    private RecyclerView rvProfessores;

    // Usa o ProfessorAdapter
    private ProfessorAdapter adapter;
    private List<Professor> listaDeProfessores = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Reutiliza o mesmo layout de busca, se os IDs forem iguais
        View view = inflater.inflate(R.layout.fragment_search_professores, container, false);
        db = FirebaseFirestore.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        etSearch = view.findViewById(R.id.etSearch);
        rvProfessores = view.findViewById(R.id.rvProfessores);

        setupRecyclerView();
        setupSearchFilter();

        // Busca os professores
        fetchProfessoresFromFirestore();

    }

    private void setupRecyclerView() {
        // Inicializa com o ProfessorAdapter
        adapter = new ProfessorAdapter(getContext(), listaDeProfessores, this);
        rvProfessores.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProfessores.setAdapter(adapter);
    }

    private void fetchProfessoresFromFirestore() {
        // Busca na coleção "users" apenas os que tem userType == "professor"
        db.collection("users")
                .whereEqualTo("userType", "professor")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaDeProfessores.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte para o objeto Professor
                            Professor professor = document.toObject(Professor.class);

                            // ⬇️ Define o ID usando o campo 'uid' (ou 'document.getId()')
                            // Escolha um dos dois, dependendo de como você salvou o ID
                            // professor.setId(document.getId());
                            professor.setId(document.getString("uid"));

                            listaDeProfessores.add(professor);
                        }
                        adapter.updateList(listaDeProfessores);
                    } else {
                        Log.w(TAG, "Erro ao buscar documentos.", task.getException());
                    }
                });
    }

    private void setupSearchFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 🟢 Lógica de navegação corrigida: usa newInstance(ID)
     */
    @Override
    public void onProfessorClick(Professor professor) {
        Log.d(TAG, "Clicou para ver perfil de: " + professor.getNome());

        // 1. Pega o ID (que foi corretamente definido na busca)
        String idDoProfessorClicado = professor.getId();

        // 2. Cria o Fragment de PERFIL PÚBLICO, usando newInstance(ID)
        Fragment profileFragment = ProfessorPerfilPublicoFragment.newInstance(idDoProfessorClicado);

        // 3. Inicia a transação
        if (getActivity() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

}