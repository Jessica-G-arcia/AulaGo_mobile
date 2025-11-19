package com.example.aulago;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.ViewHolder> {

    private Context context;
    private List<String> horarios;
    private int selectedPosition = -1; // Nenhum selecionado no início
    private OnHorarioSelectedListener listener;

    public interface OnHorarioSelectedListener {
        void onHorarioSelected(String horario);
    }

    public HorarioAdapter(Context context, List<String> horarios, OnHorarioSelectedListener listener) {
        this.context = context;
        this.horarios = horarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Vamos usar um layout simples para o item, pode criar um xml separado ou inflar dinamicamente
        // Para simplificar, estou assumindo que você criará um arquivo chamado 'item_horario.xml'
        View view = LayoutInflater.from(context).inflate(R.layout.item_horario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String horario = horarios.get(position);
        holder.tvHorario.setText(horario);

        if (selectedPosition == position) {
            // Estilo Selecionado (Azul Preenchido)
            // Fundo: Laranja Clarinho
            holder.cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.laranja50));

            // Texto: Laranja Forte
            holder.tvHorario.setTextColor(ContextCompat.getColor(context, R.color.laranja));

            // Borda: Laranja Forte
            holder.cardContainer.setStrokeColor(ContextCompat.getColor(context, R.color.laranja));
            holder.cardContainer.setStrokeWidth(2);
        } else {
            // Estilo Normal (Branco com Borda)
            holder.cardContainer.setCardBackgroundColor(Color.WHITE);

            // Texto: Preto (ou Cinza Escuro)
            holder.tvHorario.setTextColor(Color.BLACK);

            // Borda: Cinza Claro
            holder.cardContainer.setStrokeColor(Color.parseColor("#E0E0E0")); // ou R.color.cinza_borda
            holder.cardContainer.setStrokeWidth(2);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousItem = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousItem);
            notifyItemChanged(selectedPosition);
            listener.onHorarioSelected(horario);
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHorario;
        MaterialCardView cardContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHorario = itemView.findViewById(R.id.tvHorarioItem); // ID do XML item_horario
            cardContainer = (MaterialCardView) itemView; // O root é o CardView
        }
    }
}