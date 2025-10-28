package com.example.aulago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeAulaAdapter extends RecyclerView.Adapter<HomeAulaAdapter.AulaViewHolder> {

    private final List<Aula> aulaList;

    public HomeAulaAdapter(List<Aula> aulaList) {
        this.aulaList = aulaList;
    }

    @NonNull
    @Override
    public AulaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aula, parent, false);
        return new AulaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AulaViewHolder holder, int position) {
        Aula aula = aulaList.get(position);
        holder.aluno.setText("Aluno: " + aula.getAluno());
        holder.local.setText(aula.getLocal());
        holder.horario.setText("Horário: " + aula.getHorario());
        holder.data.setText(aula.getData());
    }

    @Override
    public int getItemCount() {
        return aulaList.size();
    }

    public static class AulaViewHolder extends RecyclerView.ViewHolder {
        TextView aluno, local, horario, data;

        public AulaViewHolder(@NonNull View itemView) {
            super(itemView);
            aluno = itemView.findViewById(R.id.tv_aluno_aula);
            local = itemView.findViewById(R.id.tv_local_aula);
            horario = itemView.findViewById(R.id.tv_horario_aula);
            data = itemView.findViewById(R.id.tv_data_aula);
        }
    }
}