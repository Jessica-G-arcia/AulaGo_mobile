package com.example.aulago;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.aulago.ClassModel;

public class HomeAulaAdapter extends RecyclerView.Adapter<HomeAulaAdapter.AulaViewHolder> {

    private List<ClassModel> aulasList;
    private Context context;
    private SimpleDateFormat dateFormatter;
    private String currentUserType;
    private SimpleDateFormat dateComparator; // Para comparar "yyyyMMdd"
    private String todayDateString; // String de "hoje"

    public HomeAulaAdapter(List<ClassModel> aulasList, String currentUserType) { // <-- MUDANÇA AQUI
        this.aulasList = aulasList;
        this.currentUserType = currentUserType; // <-- ADICIONE ESTA LINHA
        // Formato de exibição para datas futuras
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    @NonNull
    @Override
    public AulaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_aula, parent, false);
        return new AulaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AulaViewHolder holder, int position) {
        ClassModel aula = aulasList.get(position);

        if ("aluno".equals(currentUserType)) {
            // Se o usuário logado é ALUNO, mostre o nome do PROFESSOR
            holder.tvAluno.setText("Prof: " + aula.getProfessorNome());
        } else {
            // Se o usuário logado é PROFESSOR, mostre o nome do ALUNO
            holder.tvAluno.setText("Aluno: " + aula.getAlunoNome());
        }

        // 2. LÓGICA DE DATA (ÚNICA)
        if (aula.getDataTimestamp() != null) {
            long aulaMillis = aula.getDataTimestamp().toDate().getTime();
            long nowMillis = System.currentTimeMillis();

            // Gera a string relativa (Ex: "Hoje", "Amanhã")
            CharSequence dataRelativa = DateUtils.getRelativeTimeSpanString(
                    aulaMillis,
                    nowMillis,
                    DateUtils.DAY_IN_MILLIS
            );
            holder.tvData.setText(dataRelativa);
        }

        // 3. LÓGICA DE MODALIDADE E HORÁRIO
        String modalidade = aula.getModalidade();
        if (modalidade == null || modalidade.isEmpty()) {
            modalidade = "Presencial"; // Valor Padrão
        }

        // Define o horário (incluindo a modalidade)
        String horarioCompleto = "Horário: " + aula.getHorarioInicio() +
                " às " + aula.getHorarioFim();
        holder.tvHorario.setText(horarioCompleto); // (Definido apenas uma vez)

        // 4. LÓGICA DE ÍCONE E LOCAL
        if (modalidade.equalsIgnoreCase("Online") || modalidade.equalsIgnoreCase("Híbrida")) {
            holder.tvLocal.setText("Online");
            holder.tvLocal.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_camera, 0, 0, 0);
        } else {
            holder.tvLocal.setText(aula.getLocal());
            holder.tvLocal.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_loc, 0, 0, 0);
        }
    }
    @Override
    public int getItemCount() {
        return aulasList != null ? aulasList.size() : 0;
    }

    public static class AulaViewHolder extends RecyclerView.ViewHolder {
        TextView tvAluno, tvLocal, tvHorario, tvData;

        public AulaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAluno = itemView.findViewById(R.id.tv_aluno_aula);
            tvLocal = itemView.findViewById(R.id.tv_local_aula);
            tvHorario = itemView.findViewById(R.id.tv_horario_aula);
            tvData = itemView.findViewById(R.id.tv_data_aula);
        }
    }

    public void updateList(List<ClassModel> newList) {
        this.aulasList = newList;
        notifyDataSetChanged();
    }
}