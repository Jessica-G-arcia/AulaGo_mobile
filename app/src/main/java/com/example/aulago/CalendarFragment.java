package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // MUDOU
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aulago.databinding.FragmentCalendarBinding; // IMPORTANTE: View Binding
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment { // MUDOU

    // 1. View Binding (substitui todos os findViewById)
    private FragmentCalendarBinding binding;

    // 2. Variáveis de Lógica (as mesmas de antes)
    private ClassAdapter adapter;
    private List<ClassModel> allClasses;
    private Calendar selectedCalendar;
    private SimpleDateFormat uiDateFormat;
    private SimpleDateFormat firebaseDataFormat;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // 3. onCreate (Para inicializar variáveis, não views)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializa variáveis que não são views
        allClasses = new ArrayList<>();
        uiDateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        firebaseDataFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        selectedCalendar = Calendar.getInstance();
    }

    // 4. onCreateView (Para inflar o XML)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // 5. onViewCreated (Para configurar as views e carregar dados)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // MUDOU: Usa requireActivity() para o contexto
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }
        currentUserId = currentUser.getUid();


        // Configura os componentes
        setupRecyclerView();
        setupCalendar();

        // Carrega as aulas do PROFESSOR logado
        loadClassesFromFirebase();
    }

    // 6. onDestroyView (Limpa o binding)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Previne memory leaks
    }



    private void setupRecyclerView() {
        // MUDOU: Usa binding e requireContext()
        binding.recyclerViewClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ClassAdapter(requireContext(), new ArrayList<>());
        binding.recyclerViewClasses.setAdapter(adapter);
    }

    // Esta função é a mesma, pois já estava correta (filtrando por professorId)
    private void loadClassesFromFirebase() {
        if (currentUserId == null) return;
        Log.d("Firestore", "Buscando aulas para o PROFESSOR ID: " + currentUserId);

        db.collection("aulas")
                .whereEqualTo("professorId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allClasses.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ClassModel classModel = new ClassModel();
                                String dataString = document.getString("data");
                                Date data = parseDate(dataString);
                                if (data == null) {
                                    Log.w("Firestore", "Ignorando aula com data inválida: " + document.getId());
                                    continue;
                                }
                                // Preenche o resto
                                classModel.setData(data);
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
                        updateClassList(); // Atualiza a lista com os dados do dia atual
                    } else {
                        Log.w("Firestore", "Erro ao carregar aulas (Professor)", task.getException());
                    }
                });
    }


    private void setupCalendar() {
        try {
            // MUDOU: Usa binding e requireContext()
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

    // --- MÉTODOS QUE NÃO MUDAM (PURA LÓGICA) ---

    /**
     * MELHORIA: Retorna a saudação correta baseada na hora do dia.
     */
    private String getGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            return "Bom dia";
        } else if (timeOfDay >= 12 && timeOfDay < 18) {
            return "Boa tarde";
        } else {
            return "Boa noite";
        }
    }

    private List<ClassModel> getClassesForDate(Date date) {
        List<ClassModel> filteredClasses = new ArrayList<>();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(date);

        for (ClassModel classModel : allClasses) {
            if (classModel.getData() == null) continue;
            Calendar classCal = Calendar.getInstance();
            classCal.setTime(classModel.getData());

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

    private Date parseDate(String dateString) {
        if (dateString == null) return null;
        try {
            return firebaseDataFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("ParseDate", "Erro ao formatar data: " + dateString, e);
            return null;
        }
    }
}