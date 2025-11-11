package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.aulago.ClassModel;

public class HomeAulaAdapter extends RecyclerView.Adapter<HomeAulaAdapter.AulaViewHolder> {

    private List<ClassModel> aulasList;
    private Context context;
    private SimpleDateFormat dateFormatter;

    public HomeAulaAdapter(List<ClassModel> aulasList) {
        this.aulasList = aulasList;
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

        holder.tvAluno.setText("Aluno: " + aula.getAlunoNome());
        holder.tvLocal.setText(aula.getLocal());

        if (aula.getData() != null) {
            holder.tvData.setText(dateFormatter.format(aula.getData()));
        }

        String modalidade = (aula.getLocal() != null && !aula.getLocal().isEmpty()) ? "Presencial" : "Online";

        String horarioCompleto = "Horário: " + aula.getHorarioInicio() +
                " às " + aula.getHorarioFim() +
                " - " + modalidade;
        holder.tvHorario.setText(horarioCompleto);
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