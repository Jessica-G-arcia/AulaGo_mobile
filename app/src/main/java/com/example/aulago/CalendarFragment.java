package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aulago.databinding.FragmentCalendarBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private ClassAdapter adapter;
    private List<ClassModel> allClasses;
    private Calendar selectedCalendar;
    private SimpleDateFormat uiDateFormat;
    private SimpleDateFormat firebaseDataFormat;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        allClasses = new ArrayList<>();
        uiDateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        firebaseDataFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        selectedCalendar = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }
        currentUserId = currentUser.getUid();

        setupRecyclerView();
        setupCalendar();
        loadClassesFromFirebase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // O binding vira null aqui
    }

    private void setupRecyclerView() {
        // Verificação de segurança
        if (binding == null) return;

        binding.recyclerViewClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ClassAdapter(requireContext(), new ArrayList<>());
        binding.recyclerViewClasses.setAdapter(adapter);

        adapter.setOnAvaliarClickListener(classModel -> {
            String alunoId = classModel.getAlunoId();
            if (alunoId == null || alunoId.isEmpty()) {
                Toast.makeText(getContext(), "Erro: ID do aluno não encontrado.", Toast.LENGTH_SHORT).show();
                return;
            }
            AvaliacaoFragment avaliacaoFragment = AvaliacaoFragment.newInstance(alunoId);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, avaliacaoFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadClassesFromFirebase() {
        if (currentUserId == null) return;

        db.collection("aulas")
                .whereEqualTo("professorId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    // 1. CORREÇÃO CRUCIAL: Se o usuário saiu da tela, paramos aqui.
                    if (binding == null) return;

                    if (task.isSuccessful()) {
                        allClasses.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ClassModel classModel = new ClassModel();
                                Timestamp dataTimestamp = document.getTimestamp("dataTimestamp");

                                if (dataTimestamp == null) continue;

                                classModel.setDataTimestamp(dataTimestamp);
                                classModel.setAlunoId(document.getString("alunoId"));
                                classModel.setProfessorId(document.getString("professorId"));
                                classModel.setAlunoNome(document.getString("alunoNome"));
                                classModel.setProfessorNome(document.getString("professorNome"));
                                classModel.setIdioma(document.getString("idioma"));
                                classModel.setLocal(document.getString("local"));
                                classModel.setHorarioInicio(document.getString("horarioInicio"));
                                classModel.setHorarioFim(document.getString("horarioFim"));
                                classModel.setStatus(document.getString("status"));

                                allClasses.add(classModel);
                            } catch (Exception e) {
                                Log.e("Firestore", "Erro ao processar aula: " + document.getId(), e);
                            }
                        }
                        updateClassList();
                    } else {
                        Log.w("Firestore", "Erro ao carregar aulas", task.getException());
                    }
                });
    }

    private void setupCalendar() {
        if (binding == null) return;

        try {
            binding.calendarView.setBackgroundColor(requireContext().getResources().getColor(android.R.color.transparent));
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
            updateClassList();
        });
    }

    private void updateClassList() {
        // 2. CORREÇÃO CRUCIAL: Verifica se binding existe antes de tocar na tela
        if (binding == null) return;

        List<ClassModel> filteredClasses = getClassesForDate(selectedCalendar.getTime());
        String formattedDate = uiDateFormat.format(selectedCalendar.getTime());

        if (isSameDay(selectedCalendar, Calendar.getInstance())) {
            binding.tvSelectedDate.setText("Aulas de hoje");
        } else {
            binding.tvSelectedDate.setText("Aulas de " + formattedDate);
        }

        binding.chipClassCount.setText(filteredClasses.size() + " aula" +
                (filteredClasses.size() != 1 ? "s" : ""));

        if (filteredClasses.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewClasses.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerViewClasses.setVisibility(View.VISIBLE);
            adapter.updateList(filteredClasses);
        }
    }

    private List<ClassModel> getClassesForDate(Date date) {
        List<ClassModel> filteredClasses = new ArrayList<>();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(date);

        for (ClassModel classModel : allClasses) {
            if (classModel.getDataTimestamp() == null) continue;
            Calendar classCal = Calendar.getInstance();
            classCal.setTime(classModel.getDataTimestamp().toDate());

            if (isSameDay(targetCal, classCal)) {
                filteredClasses.add(classModel);
            }
        }
        return filteredClasses;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}