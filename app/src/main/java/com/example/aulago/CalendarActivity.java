package com.example.aulago;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.example.aulago.Aula;
import com.example.aulago.AulasAdapter;
import com.example.aulago.Modalidade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerViewAulas;
    private TextView textViewDataSelecionada;
    private TextView textViewSemAulas;

    private AulasAdapter aulasAdapter;
    private List<Aula> todasAulas = new ArrayList<>();
    private Map<String, List<Aula>> aulasPorData = new HashMap<>();

    // Formato de data padrão para o projeto
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfBrasil = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initViews();
        initData();
        setupCalendar();
        setupRecyclerView();

        showAulasParaData(new Date());
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerViewAulas = findViewById(R.id.recyclerViewAulas);
        textViewDataSelecionada = findViewById(R.id.textViewDataSelecionada);
        textViewSemAulas = findViewById(R.id.textViewSemAulas);
    }

    private void initData() {
        // Limpa as listas para evitar duplicação
        todasAulas.clear();
        aulasPorData.clear();

        criarAulasExemplo();
        organizarAulasPorData();
    }

    private void criarAulasExemplo() {
        // Pega o calendário para criar datas dinâmicas
        Calendar cal = Calendar.getInstance();

        // Aula passada (ontem)
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String ontem = sdf.format(cal.getTime());
        todasAulas.add(new Aula("Ana Silva", "09:00", "10:00", ontem, Modalidade.PRESENCIAL));

        // Aulas para hoje
        String hoje = sdf.format(new Date());
        todasAulas.add(new Aula("João Carlos", "14:00", "15:00", hoje, Modalidade.ONLINE));
        todasAulas.add(new Aula("Maria Luiza", "17:00", "18:00", hoje, Modalidade.PRESENCIAL));


        // Aulas futuras (daqui a 2 dias)
        cal.setTime(new Date()); // Reseta para hoje
        cal.add(Calendar.DAY_OF_MONTH, 2);
        String futuro = sdf.format(cal.getTime());
        todasAulas.add(new Aula("Pedro Ramos", "16:00", "17:00", futuro, Modalidade.PRESENCIAL));
    }

    private void organizarAulasPorData() {
        for (Aula aula : todasAulas) {
            String data = aula.getData();
            List<Aula> aulasDoDia = aulasPorData.get(data);
            if (aulasDoDia == null) {
                aulasDoDia = new ArrayList<>();
                aulasPorData.put(data, aulasDoDia);
            }
            aulasDoDia.add(aula);
        }
    }

    private void setupCalendar() {
        // Aplica os pontinhos (eventos) nos dias com aulas
        aplicarEventosNoCalendario();

        // Define a data selecionada inicialmente (hoje)
        try {
            calendarView.setDate(Calendar.getInstance());
        } catch (OutOfDateRangeException ignored) { }
        // Reaplica eventos após possível refresh interno
        aplicarEventosNoCalendario();

        // Listener de clique no dia
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDay = eventDay.getCalendar();
                // Marca visualmente a seleção
                try {
                    calendarView.setDate(clickedDay);
                } catch (OutOfDateRangeException ignored) { }
                // Reaplica eventos após possível refresh interno
                aplicarEventosNoCalendario();
                showAulasParaData(clickedDay.getTime());
            }
        });
    }

    private void aplicarEventosNoCalendario() {
        List<EventDay> events = new ArrayList<>();
        for (Map.Entry<String, List<Aula>> entry : aulasPorData.entrySet()) {
            String data = entry.getKey();
            List<Aula> aulasDoDia = entry.getValue();

            boolean temPresencial = false;
            boolean temOnline = false;
            for (Aula aula : aulasDoDia) {
                if (aula.getModalidade() == Modalidade.PRESENCIAL) temPresencial = true;
                if (aula.getModalidade() == Modalidade.ONLINE) temOnline = true;
            }

            int drawableId;
            if (temPresencial && temOnline) {
                drawableId = R.drawable.event_dot_mixed;
            } else if (temPresencial) {
                drawableId = R.drawable.event_dot_presencial;
            } else if (temOnline) {
                drawableId = R.drawable.event_dot_online;
            } else {
                continue;
            }

            Calendar cal = Calendar.getInstance();
            try {
                Date date = sdf.parse(data);
                if (date != null) {
                    cal.setTime(date);
                    events.add(new EventDay(cal, drawableId));
                }
            } catch (ParseException ignored) { }
        }
        calendarView.setEvents(events);
    }

    private void setupRecyclerView() {
        recyclerViewAulas.setLayoutManager(new LinearLayoutManager(this));
        aulasAdapter = new AulasAdapter(new ArrayList<>());
        recyclerViewAulas.setAdapter(aulasAdapter);
    }

    private void showAulasParaData(Date data) {
        String dataFormatada = sdf.format(data);
        String dataFormatadaBrasil = sdfBrasil.format(data);

        textViewDataSelecionada.setText("Aulas para " + dataFormatadaBrasil);

        List<Aula> aulasDoDia = aulasPorData.get(dataFormatada);

        if (aulasDoDia == null || aulasDoDia.isEmpty()) {
            recyclerViewAulas.setVisibility(View.GONE);
            textViewSemAulas.setVisibility(View.VISIBLE);
        } else {
            recyclerViewAulas.setVisibility(View.VISIBLE);
            textViewSemAulas.setVisibility(View.GONE);
            aulasAdapter.updateAulas(aulasDoDia);
        }
    }
}
