package com.example.aulago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// MUDOU: Agora usa ClassModel em vez de Aula
public class HomeAulaAdapter extends RecyclerView.Adapter<HomeAulaAdapter.AulaViewHolder> {

    private List<ClassModel> aulaList;

    public HomeAulaAdapter(List<ClassModel> aulaList) {
        this.aulaList = aulaList;
    }

    public void updateList(List<ClassModel> newList) {
        this.aulaList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AulaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aula, parent, false);
        return new AulaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AulaViewHolder holder, int position) {
        ClassModel aula = aulaList.get(position);

        // MUDOU: Usa os novos getters do ClassModel
        holder.aluno.setText("Aluno: " + aula.getAlunoNome());
        holder.local.setText(aula.getLocal());
        holder.horario.setText("Horário: " + aula.getHorarioInicio()); // Usei Inicio

        // (O seu ClassModel tem um 'Date data', precisamos de o formatar)
        // (Por agora, vamos deixar em branco ou usar um placeholder)
        // holder.data.setText(aula.getData().toString()); // Isto ficaria feio, precisa de formatação
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