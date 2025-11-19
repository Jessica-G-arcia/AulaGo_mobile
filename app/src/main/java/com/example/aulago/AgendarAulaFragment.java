package com.example.aulago;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.aulago.databinding.FragmentAgendarAulaBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AgendarAulaFragment extends Fragment {

    private FragmentAgendarAulaBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String professorId;
    private String professorNome;
    private String professorAvatarUrl;

    private Calendar dataSelecionada;
    private String horarioSelecionado;
    private String modalidadeSelecionada = "Online";

    // Lista base de horários possíveis
    private final List<String> TODOS_HORARIOS = Arrays.asList(
            "08:00", "09:00", "10:00", "11:00",
            "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00"
    );

    public static AgendarAulaFragment newInstance(String professorId, String professorNome, String professorAvatarUrl) {
        AgendarAulaFragment fragment = new AgendarAulaFragment();
        Bundle args = new Bundle();
        args.putString("PROFESSOR_ID", professorId);
        args.putString("PROFESSOR_NOME", professorNome);
        args.putString("PROFESSOR_FOTO", professorAvatarUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            professorId = getArguments().getString("PROFESSOR_ID");
            professorNome = getArguments().getString("PROFESSOR_NOME");
            professorAvatarUrl = getArguments().getString("PROFESSOR_FOTO");
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        dataSelecionada = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAgendarAulaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCalendar();
        setupModalidade();

        // Carrega horários iniciais (para hoje)
        carregarHorariosDisponiveis();

        binding.btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnConfirmarAgendamento.setOnClickListener(v -> confirmarAgendamento());
    }

    private void setupCalendar() {
        binding.calendarView.setMinDate(System.currentTimeMillis() - 1000);

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            dataSelecionada.set(year, month, dayOfMonth);
            horarioSelecionado = null; // Reseta seleção

            // 🔴 Agora chamamos o método que consulta o banco
            carregarHorariosDisponiveis();
        });
    }

    private void setupModalidade() {
        binding.toggleModalidade.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnOnline) modalidadeSelecionada = "Online";
                else modalidadeSelecionada = "Presencial";
            }
        });
    }

    // 🔴 O GRANDE SEGREDO: Método que filtra horários ocupados
    private void carregarHorariosDisponiveis() {
        // Limpa a lista visualmente enquanto carrega para evitar bugs
        configurarRecyclerView(new ArrayList<>());
        binding.rvHorarios.setAlpha(0.5f); // Efeito visual de "carregando"

        // 1. Define o intervalo de tempo do dia selecionado (00:00 até 23:59)
        Calendar inicioDoDia = (Calendar) dataSelecionada.clone();
        inicioDoDia.set(Calendar.HOUR_OF_DAY, 0);
        inicioDoDia.set(Calendar.MINUTE, 0);
        inicioDoDia.set(Calendar.SECOND, 0);

        Calendar fimDoDia = (Calendar) dataSelecionada.clone();
        fimDoDia.set(Calendar.HOUR_OF_DAY, 23);
        fimDoDia.set(Calendar.MINUTE, 59);
        fimDoDia.set(Calendar.SECOND, 59);

        // 2. Consulta no Firebase: "Quais aulas esse professor tem NESTE intervalo?"
        db.collection("aulas")
                .whereEqualTo("professorId", professorId)
                .whereEqualTo("status", "confirmada") // Ignora canceladas
                .whereGreaterThanOrEqualTo("dataTimestamp", new Timestamp(inicioDoDia.getTime()))
                .whereLessThanOrEqualTo("dataTimestamp", new Timestamp(fimDoDia.getTime()))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    binding.rvHorarios.setAlpha(1f); // Remove efeito de loading

                    // Lista de horários que JÁ ESTÃO OCUPADOS
                    List<String> horariosOcupados = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String horarioInicio = doc.getString("horarioInicio");
                        if (horarioInicio != null) {
                            horariosOcupados.add(horarioInicio);
                        }
                    }

                    // 3. Filtra a lista final
                    List<String> listaFinal = filtrarHorarios(horariosOcupados);

                    // Atualiza a tela
                    configurarRecyclerView(listaFinal);

                    if (listaFinal.isEmpty()) {
                        Toast.makeText(getContext(), "Agenda cheia para este dia!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.rvHorarios.setAlpha(1f);
                    Log.e("Agendar", "Erro ao buscar disponibilidade", e);
                    Toast.makeText(getContext(), "Erro ao verificar agenda.", Toast.LENGTH_SHORT).show();
                });
    }

    // Lógica local para remover horários passados e ocupados
    private List<String> filtrarHorarios(List<String> ocupados) {
        List<String> disponiveis = new ArrayList<>();
        Calendar agora = Calendar.getInstance();
        boolean isHoje = isSameDay(dataSelecionada, agora);
        int horaAtual = agora.get(Calendar.HOUR_OF_DAY);

        for (String horario : TODOS_HORARIOS) {
            // Regra 1: Se for hoje, o horário já passou?
            if (isHoje) {
                int horaDoItem = Integer.parseInt(horario.split(":")[0]);
                if (horaDoItem <= horaAtual) {
                    continue; // Pula esse horário (já passou)
                }
            }

            // Regra 2: O horário já está na lista de ocupados do banco?
            if (ocupados.contains(horario)) {
                continue; // Pula esse horário (já tem aula)
            }

            // Se passou nos testes, adiciona na lista
            disponiveis.add(horario);
        }
        return disponiveis;
    }

    private void configurarRecyclerView(List<String> lista) {
        HorarioAdapter adapter = new HorarioAdapter(getContext(), lista, horario -> {
            this.horarioSelecionado = horario;
        });
        binding.rvHorarios.setLayoutManager(new GridLayoutManager(getContext(), 4));
        binding.rvHorarios.setAdapter(adapter);
    }

    private void confirmarAgendamento() {
        if (horarioSelecionado == null) {
            Toast.makeText(getContext(), "Selecione um horário.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auth.getCurrentUser() == null) return;

        binding.btnConfirmarAgendamento.setEnabled(false);
        binding.btnConfirmarAgendamento.setText("Agendando...");

        // Configura hora final no objeto Calendar
        String[] parts = horarioSelecionado.split(":");
        dataSelecionada.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        dataSelecionada.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        dataSelecionada.set(Calendar.SECOND, 0);

        Date dataFinal = dataSelecionada.getTime();

        Calendar calFim = (Calendar) dataSelecionada.clone();
        calFim.add(Calendar.HOUR_OF_DAY, 1);
        String horarioFim = String.format("%02d:%02d", calFim.get(Calendar.HOUR_OF_DAY), calFim.get(Calendar.MINUTE));

        String alunoId = auth.getCurrentUser().getUid();

        db.collection("users").document(alunoId).get().addOnSuccessListener(documentSnapshot -> {
            String alunoNome = documentSnapshot.getString("nome");
            String alunoUrl = documentSnapshot.getString("urlFotoPerfil");

            com.google.firebase.firestore.DocumentReference refAula = db.collection("aulas").document();
            String idGerado = refAula.getId();

            ClassModel novaAula = new ClassModel();
            novaAula.setAulaId(idGerado);
            novaAula.setAlunoId(alunoId);
            novaAula.setAlunoNome(alunoNome);
            novaAula.setAlunoAvatarUrl(alunoUrl);

            novaAula.setProfessorId(professorId);
            novaAula.setProfessorNome(professorNome);
            novaAula.setProfessorAvatarUrl(professorAvatarUrl);

            novaAula.setDataTimestamp(new Timestamp(dataFinal));
            novaAula.setHorarioInicio(horarioSelecionado);
            novaAula.setHorarioFim(horarioFim);

            novaAula.setLocal(modalidadeSelecionada);
            novaAula.setModalidade(modalidadeSelecionada);
            novaAula.setIdioma("Inglês");
            novaAula.setStatus("confirmada");

            refAula.set(novaAula)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Agendado!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnConfirmarAgendamento.setEnabled(true);
                        binding.btnConfirmarAgendamento.setText("Confirmar Agendamento");
                        Toast.makeText(getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}