package com.example.aulago;

// ❗️ 1. IMPORTAÇÕES NECESSÁRIAS

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView; // ❗️ Importe o TextView

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient; // ❗️ Importe
import com.google.android.gms.location.LocationServices; // ❗️ Importe
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint; // ❗️ Importe
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchAlunosFragment extends Fragment implements AlunoAdapter.OnAlunoClickListener {

    private static final String TAG = "SearchAlunosFragment";

    // Views
    private EditText etSearch;
    private RecyclerView rvAlunos;
    private TextView tvTitle; //  Para o layout

    // Firebase
    private AlunoAdapter adapter;
    private List<Aluno> listaDeAlunos = new ArrayList<>();
    private FirebaseFirestore db;

    //  2. GPS / LOCALIZAÇÃO
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        //  3. Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        //  4. Regista o "callback" da permissão
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permissão dada, tente buscar de novo
                        obterLocalizacaoEBuscarAlunos();
                    } else {
                        // Permissão negada, busque sem calcular distância
                        Log.w(TAG, "Permissão de localização negada.");
                        fetchAlunosFromFirestore(null);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_alunos, container, false);
        // db = FirebaseFirestore.getInstance(); // Movido para onCreate
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 5. LIGA O AJUSTE DO TECLADO
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        //  6. Encontra os IDs
        etSearch = view.findViewById(R.id.etSearch);
        tvTitle = view.findViewById(R.id.tvTitle); // ID do XML
        rvAlunos = view.findViewById(R.id.rvAlunos); // ID do XML

        // 7. Define os textos específicos
        tvTitle.setText("Encontre Alunos...");
        etSearch.setHint("Buscar alunos...");

        setupRecyclerView();
        setupSearchFilter();

        //️ 8. INICIA O PROCESSO DE BUSCA (com GPS)
        obterLocalizacaoEBuscarAlunos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //️ 9. DESLIGA O AJUSTE DO TECLADO
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        }
    }

    /**
     * 10. NOVO MÉTODO: Pede permissão e, se tiver, pega o GPS.
     */
    private void obterLocalizacaoEBuscarAlunos() {
        // Verifica se já tem permissão
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Tenta pegar a última localização (é mais rápido)
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // CONSEGUIMOS A LOCALIZAÇÃO!
                        fetchAlunosFromFirestore(location);
                    })
                    .addOnFailureListener(e -> {
                        // Falhou (ex: GPS desligado). Busca sem localização.
                        Log.e(TAG, "Erro ao pegar localização", e);
                        fetchAlunosFromFirestore(null);
                    });

        } else {
            // Não tem permissão, PEDE.
            Log.d(TAG, "Pedindo permissão de localização...");
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * 11. MÉTODO ANTIGO (MODIFICADO): Agora recebe a localização do professor.
     */
    private void fetchAlunosFromFirestore(@Nullable Location professorLocation) {
        Log.d(TAG, "Buscando alunos no Firestore...");

        db.collection("users")
                .whereEqualTo("userType", "aluno")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaDeAlunos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte o documento para um objeto Aluno
                            // (Isto já pega nome, nivel, idioma, modalidade, objetivos, localizacao, etc.)
                            Aluno aluno = document.toObject(Aluno.class);

                            aluno.setId(document.getString("uid"));

                            // Pega rating (que você já tinha)
                            if (document.contains("ratingMedia")) {
                                aluno.setRatingMedia(document.getDouble("ratingMedia"));
                            }
                            if (document.contains("ratingCount")) {
                                aluno.setRatingCount(document.getLong("ratingCount"));
                            }


                            // Só calcula se tivermos o GPS do professor E o do aluno
                            if (professorLocation != null && aluno.getLocalizacao() != null) {

                                GeoPoint alunoGeoPoint = aluno.getLocalizacao();

                                float[] results = new float[1];
                                Location.distanceBetween(
                                        professorLocation.getLatitude(), professorLocation.getLongitude(),
                                        alunoGeoPoint.getLatitude(), alunoGeoPoint.getLongitude(),
                                        results
                                );

                                // Salva a distância (em KM) no objeto
                                float distanciaEmKm = results[0] / 1000;
                                aluno.setDistanciaCalculada(distanciaEmKm);
                            }

                            listaDeAlunos.add(aluno);
                        }

                        Log.d(TAG, "Busca completa. " + listaDeAlunos.size() + " alunos encontrados.");
                        // ❗️ 13. ENTREGA A LISTA FINAL PARA O ADAPTER
                        adapter.updateList(listaDeAlunos);

                    } else {
                        Log.w(TAG, "Erro ao buscar documentos.", task.getException());
                    }
                });
    }


    private void setupRecyclerView() {
        adapter = new AlunoAdapter(getContext(), listaDeAlunos, this);
        rvAlunos.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlunos.setAdapter(adapter);
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

    @Override
    public void onAlunoClick(Aluno aluno) {
        Log.d(TAG, "Clicou para ver perfil de: " + aluno.getNome());
        String idDoAlunoClicado = aluno.getId();
        Fragment profileFragment = AlunoPerfilPublicoFragment.newInstance(idDoAlunoClicado);

        if (getActivity() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}