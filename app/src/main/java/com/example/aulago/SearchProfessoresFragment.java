package com.example.aulago;

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
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchProfessoresFragment extends Fragment implements ProfessorAdapter.OnProfessorClickListener {

    private static final String TAG = "SearchProfessoresFrag";

    // Views
    private EditText etSearch;
    private RecyclerView rvProfessores;
    private TextView tvTitle;

    // Adapter e Lista
    private ProfessorAdapter adapter;
    private List<Professor> listaDeProfessores = new ArrayList<>();

    // Firebase e Localização
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // 1. Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 2. Registra o callback da permissão
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permissão concedida: busca com GPS
                        obterLocalizacaoEBuscarProfessores();
                    } else {
                        // Permissão negada: busca sem GPS
                        Log.w(TAG, "Permissão de localização negada.");
                        fetchProfessoresFromFirestore(null);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Reutiliza o layout (certifique-se de que os IDs batem)
        return inflater.inflate(R.layout.fragment_search_professores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Ajuste de Teclado
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        etSearch = view.findViewById(R.id.etSearch);
        rvProfessores = view.findViewById(R.id.rvProfessores);

        // Tente encontrar o tvTitle se ele existir no seu layout fragment_search_professores
        // Se não tiver o ID no XML, pode comentar essas duas linhas
        tvTitle = view.findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText("Encontre Professores...");
        }

        etSearch.setHint("Buscar professores...");

        setupRecyclerView();
        setupSearchFilter();

        // 4. Inicia o processo de busca (pede GPS primeiro)
        obterLocalizacaoEBuscarProfessores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 5. Reseta o ajuste do teclado
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        }
    }

    // ------------------------------------------------------------------------
    // MÉTODOS DE LOCALIZAÇÃO E BUSCA
    // ------------------------------------------------------------------------

    private void obterLocalizacaoEBuscarProfessores() {
        // Verifica permissão
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Pega a última localização conhecida
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // Busca passando a localização do ALUNO (usuário logado)
                        fetchProfessoresFromFirestore(location);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erro ao pegar localização", e);
                        fetchProfessoresFromFirestore(null);
                    });

        } else {
            // Pede permissão
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchProfessoresFromFirestore(@Nullable Location alunoLocation) {
        Log.d(TAG, "Buscando professores no Firestore...");

        db.collection("users")
                .whereEqualTo("userType", "professor")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaDeProfessores.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Professor professor = document.toObject(Professor.class);
                            professor.setId(document.getString("uid"));

                            // --- CÁLCULO DE DISTÂNCIA ---
                            // Só calcula se tivermos a localização do Aluno (quem busca)
                            // E a localização do Professor (no cadastro dele)
                            if (alunoLocation != null && professor.getLocalizacao() != null) {
                                GeoPoint profGeoPoint = professor.getLocalizacao();

                                float[] results = new float[1];
                                Location.distanceBetween(
                                        alunoLocation.getLatitude(), alunoLocation.getLongitude(),
                                        profGeoPoint.getLatitude(), profGeoPoint.getLongitude(),
                                        results
                                );

                                // Converte metros para KM
                                float distanciaEmKm = results[0] / 1000;

                                // Certifique-se que sua classe Professor tem este método
                                professor.setDistanciaCalculada(distanciaEmKm);
                            }
                            // ----------------------------

                            listaDeProfessores.add(professor);
                        }
                        adapter.updateList(listaDeProfessores);
                    } else {
                        Log.w(TAG, "Erro ao buscar professores.", task.getException());
                    }
                });
    }

    // ------------------------------------------------------------------------
    // CONFIGURAÇÃO DE UI
    // ------------------------------------------------------------------------

    private void setupRecyclerView() {
        adapter = new ProfessorAdapter(getContext(), listaDeProfessores, this);
        rvProfessores.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProfessores.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ------------------------------------------------------------------------
    // NAVEGAÇÃO
    // ------------------------------------------------------------------------

    @Override
    public void onProfessorClick(Professor professor) {
        Log.d(TAG, "Clicou no professor: " + professor.getNome());

        String idDoProfessor = professor.getId();
        Fragment profileFragment = ProfessorPerfilPublicoFragment.newInstance(idDoProfessor);

        if (getActivity() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}