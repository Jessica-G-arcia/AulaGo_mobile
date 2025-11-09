package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
// IMPORTAÇÕES ADICIONADAS
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
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

public class CalendarActivity extends AppCompatActivity {

    // Variáveis do perfil (professor logado)
    private ShapeableImageView imageViewProfile;
    private TextView textViewNomeUsuario;

    private CalendarView calendarView;
    private RecyclerView recyclerViewClasses;
    private TextView tvSelectedDate;
    private Chip chipClassCount;
    private LinearLayout emptyState;
    private ClassAdapter adapter;
    private List<ClassModel> allClasses;
    private Calendar selectedCalendar;

    private SimpleDateFormat uiDateFormat;
    private SimpleDateFormat firebaseDataFormat;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId; // Este agora é o ID do PROFESSOR logado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mAuth = FirebaseAuth.getInstance();
        // Correção da ordem de inicialização
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        // Carrega o perfil do PROFESSOR logado
        loadUserProfile();

        initializeViews();
        setupRecyclerView();
        setupCalendar();

        selectedCalendar = Calendar.getInstance();

        // Carrega as aulas do PROFESSOR logado
        loadClassesFromFirebase();
    }

    private void initializeViews() {
        // Referências para os views de perfil
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewNomeUsuario = findViewById(R.id.textViewNomeUsuario);

        // Views do calendário e lista
        calendarView = findViewById(R.id.calendarView);
        recyclerViewClasses = findViewById(R.id.recyclerViewClasses);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        chipClassCount = findViewById(R.id.chipClassCount);
        emptyState = findViewById(R.id.emptyState);

        uiDateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        firebaseDataFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        // 'db' já foi inicializado no onCreate
        allClasses = new ArrayList<>();
    }

    // Esta função carrega o perfil do usuário logado (o Professor)
    private void loadUserProfile() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 1. Carregar o Nome do Professor
                                String nome = document.getString("nome");
                                textViewNomeUsuario.setText(nome);

                                // 2. Carregar a Foto do Professor
                                String fotoUrl = document.getString("urlFotoPerfil");

                                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                                    Glide.with(CalendarActivity.this)
                                            .load(fotoUrl)
                                            .placeholder(R.drawable.img_avatar_circle)
                                            .error(R.drawable.img_avatar_circle)
                                            .circleCrop()
                                            .into(imageViewProfile);
                                } else {
                                    imageViewProfile.setImageResource(R.drawable.img_avatar_circle);
                                }
                            } else {
                                Log.d("Firestore", "Nenhum documento encontrado para o usuário");
                                textViewNomeUsuario.setText("Professor");
                            }
                        } else {
                            Log.d("Firestore", "Erro ao buscar usuário: ", task.getException());
                            textViewNomeUsuario.setText("Erro");
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        recyclerViewClasses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(this, new ArrayList<>());
        recyclerViewClasses.setAdapter(adapter);
    }

    // ---
    //
    // ✅ MUDANÇA PRINCIPAL AQUI ✅
    //
    // ---
    private void loadClassesFromFirebase() {
        if (currentUserId == null) return; // Segurança

        // ADICIONEI ESTE LOG PARA AJUDAR A DEPURAR
        Log.d("Firestore", "Buscando aulas para o PROFESSOR ID: " + currentUserId);

        // ATUALIZADO: A query agora filtra por "professorId"
        db.collection("aulas")
                .whereEqualTo("professorId", currentUserId) // <-- MUDANÇA DE "alunoId" PARA "professorId"
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
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

                                    // Preenchendo o objeto (incluindo o alunoNome)
                                    classModel.setData(data);
                                    classModel.setAlunoId(document.getString("alunoId"));
                                    classModel.setProfessorId(document.getString("professorId"));
                                    classModel.setAlunoNome(document.getString("alunoNome")); // <-- IMPORTANTE
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
                            // IMPORTANTE: Verifique o Logcat para este erro!
                            Log.w("Firestore", "Erro ao carregar aulas (Professor)", task.getException());
                        }
                    }
                });
    }


    // --- NENHUMA MUDANÇA ABAIXO ---

    private void setupCalendar() {
        try {
            calendarView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } catch (Exception e) {
            e.printStackTrace();
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedCalendar.set(year, month, dayOfMonth);
                updateClassList();
            }
        });
    }

    private void updateClassList() {
        List<ClassModel> filteredClasses = getClassesForDate(selectedCalendar.getTime());

        String formattedDate = uiDateFormat.format(selectedCalendar.getTime());
        if (isSameDay(selectedCalendar, Calendar.getInstance())) {
            tvSelectedDate.setText("Aulas de hoje");
        } else {
            tvSelectedDate.setText("Aulas de " + formattedDate);
        }

        chipClassCount.setText(filteredClasses.size() + " aula" +
                (filteredClasses.size() != 1 ? "s" : ""));

        if (filteredClasses.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerViewClasses.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerViewClasses.setVisibility(View.VISIBLE);
            adapter.updateList(filteredClasses);
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