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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aulago.databinding.FragmentCalendarBinding; // IMPORTANTE: View Binding
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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

public class CalendarFragment extends Fragment {

    // 1. View Binding (substitui todos os findViewById)
    private FragmentCalendarBinding binding;

    // 2. Variáveis de Lógica (as mesmas de antes)
    private ClassAdapter adapter;
    private List<ClassModel> listaDeAulas;
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
        listaDeAulas = new ArrayList<>();
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

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();


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
        // Verificação de segurança
        if (binding == null) return;

        binding.recyclerViewClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Tipo "professor" sempre nesse fragment!
        adapter = new ClassAdapter(requireContext(), listaDeAulas, "professor");
        binding.recyclerViewClasses.setAdapter(adapter);

        adapter.setOnAvaliarClickListener(classModel -> abrirFragmentAvaliacao(classModel));
    }

    private void setupCalendar() {
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

    private void loadClassesFromFirebase() {
        if (currentUserId == null) return;
        Log.d("Firestore", "Buscando aulas para o PROFESSOR ID: " + currentUserId);

        db.collection("aulas")
                .whereEqualTo("professorId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    // 1. CORREÇÃO CRUCIAL: Se o usuário saiu da tela, paramos aqui.
                    if (binding == null) return;

                    if (task.isSuccessful()) {
                        listaDeAulas.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ClassModel classModel = new ClassModel();


                                // 1. Busque o campo "dataTimestamp" como um Timestamp
                                Timestamp dataTimestamp = document.getTimestamp("dataTimestamp");
                                if (dataTimestamp == null) continue;
                                classModel.setDataTimestamp(dataTimestamp);


                                // Preenche o resto
                                classModel.setAlunoId(document.getString("alunoId"));
                                classModel.setProfessorId(document.getString("professorId"));
                                classModel.setAlunoNome(document.getString("alunoNome"));
                                classModel.setProfessorNome(document.getString("professorNome"));
                                classModel.setIdioma(document.getString("idioma"));
                                classModel.setLocal(document.getString("local"));
                                classModel.setHorarioInicio(document.getString("horarioInicio"));
                                classModel.setHorarioFim(document.getString("horarioFim"));
                                classModel.setStatus(document.getString("status"));
                                listaDeAulas.add(classModel);
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

        for (ClassModel classModel : listaDeAulas) {
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


    private void abrirFragmentAvaliacao(ClassModel aula) {

        // A. Formata Data e Hora
        String textoDataHora = "--/--";
        if (aula.getDataTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", new Locale("pt", "BR"));
            String dataStr = sdf.format(aula.getDataTimestamp().toDate());
            String inicio = aula.getHorarioInicio() != null ? aula.getHorarioInicio() : "--:--";
            String fim = aula.getHorarioFim() != null ? aula.getHorarioFim() : "--:--";
            textoDataHora = dataStr + " • " + inicio + " - " + fim;
        }

        // B. Pega a Modalidade (Tenta campo modalidade, senão usa local, senão Presencial)
        String textoModalidade = "Presencial";
        if (aula.getModalidade() != null && !aula.getModalidade().isEmpty()) {
            textoModalidade = aula.getModalidade();
            // Capitaliza a primeira letra (opcional)
            textoModalidade = textoModalidade.substring(0, 1).toUpperCase() + textoModalidade.substring(1);
        } else if (aula.getLocal() != null) {
            textoModalidade = aula.getLocal();
        }

        // C. Define QUEM será avaliado (Professor avalia Aluno)
        String idParaAvaliar = aula.getAlunoId();

        // D. Pega o ID da Aula
        String idDaAula = aula.getAulaId();

        // E. Abre o Fragmento
        AvaliacaoFragment fragment = AvaliacaoFragment.newInstance(
                idParaAvaliar,
                textoDataHora,
                textoModalidade,
                idDaAula
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Verifique se o ID do container é esse mesmo
                .addToBackStack(null)
                .commit();
    }

}