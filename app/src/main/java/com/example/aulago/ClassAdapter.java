package com.example.aulago;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private final Context context;
    private List<ClassModel> classList;
    private OnAvaliarClickListener onAvaliarClickListener;

    /**
     * Interface para comunicar o clique no botão "Avaliar" para o Fragment.
     */
    public interface OnAvaliarClickListener {
        void onAvaliarClick(ClassModel classModel);
    }

    /**
     * Método usado pelo Fragment para "ouvir" os eventos de clique.
     */
    public void setOnAvaliarClickListener(OnAvaliarClickListener listener) {
        this.onAvaliarClickListener = listener;
    }

    public ClassAdapter(Context context, List<ClassModel> classList) {
        this.context = context;
        this.classList = classList;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.class_card, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classList.get(position);

        // --- 1. Preenche os dados da aula no card ---
        holder.tvStartTime.setText(classModel.getHorarioInicio());
        holder.tvEndTime.setText(classModel.getHorarioFim());
        holder.tvTeacher.setText(classModel.getAlunoNome());
        holder.tvClassName.setText(classModel.getIdioma());

        // Lógica da Modalidade
        if (classModel.getLocal() != null) {
            String local = classModel.getLocal().toLowerCase(Locale.getDefault());
            holder.tvEmojiModalidade.setVisibility(View.VISIBLE);
            holder.tvModalidade.setVisibility(View.VISIBLE);
            if (local.contains("online") || local.contains("meet")) {
                holder.tvEmojiModalidade.setText("💻");
                holder.tvModalidade.setText("Aula Online");
            } else if (local.contains("presencial")) {
                holder.tvEmojiModalidade.setText("📍");
                holder.tvModalidade.setText("Aula Presencial");
            } else {
                holder.tvEmojiModalidade.setText("•");
                holder.tvModalidade.setText(classModel.getLocal());
            }
        } else {
            holder.tvEmojiModalidade.setVisibility(View.GONE);
            holder.tvModalidade.setVisibility(View.GONE);
        }

        // --- 2. Lógica principal: Mostrar "Avaliar" ou o Status ---
        Date classEndDateTime = getClassEndDateTime(classModel.getData(), classModel.getHorarioFim());
        boolean hasPassed = classEndDateTime != null && new Date().after(classEndDateTime);
        boolean isCancelled = classModel.getStatus() != null && classModel.getStatus().equalsIgnoreCase("cancelada");

        // CONDIÇÃO: A aula já terminou E não foi cancelada
        if (hasPassed && !isCancelled) {
            holder.chipStatus.setText("Avaliar");
            holder.chipStatus.setClickable(true);
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.verde_claro)));
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.verde_escuro));

            // Ação de clique para notificar o Fragment
            holder.chipStatus.setOnClickListener(v -> {
                if (onAvaliarClickListener != null) {
                    onAvaliarClickListener.onAvaliarClick(classModel);
                }
            });

        } else {
            // Se a aula ainda não terminou ou foi cancelada, mostra o status normal.
            holder.chipStatus.setText(classModel.getStatus());
            holder.chipStatus.setClickable(false);

            int chipColorRes = R.color.azul50;
            int textColorRes = R.color.marinho;

            if (classModel.getStatus() != null) {
                switch (classModel.getStatus().toLowerCase(Locale.getDefault())) {
                    case "confirmada":
                        chipColorRes = R.color.verde_claro;
                        textColorRes = R.color.verde_escuro;
                        break;
                    case "cancelada":
                        chipColorRes = R.color.vermelho_claro;
                        textColorRes = R.color.vermelho_escuro;
                        break;
                    // O status "agendada" usa a cor padrão (azul)
                }
            }
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, chipColorRes)));
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, textColorRes));
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public void updateList(List<ClassModel> newList) {
        this.classList = newList;
        notifyDataSetChanged();
    }

    /**
     * Combina a data da aula (dia/mês/ano) com o horário de término (hora:minuto)
     * para criar um objeto Date completo para comparação.
     */
    private Date getClassEndDateTime(Date classDate, String endTimeString) {
        if (classDate == null || endTimeString == null || endTimeString.isEmpty()) {
            return null;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(classDate);
            String[] timeParts = endTimeString.split(":");
            if (timeParts.length == 2) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                calendar.set(Calendar.SECOND, 0);
                return calendar.getTime();
            }
            return null;
        } catch (Exception e) {
            Log.e("ClassAdapter", "Erro ao parsear data/hora de término.", e);
            return null;
        }
    }


    /**
     * ViewHolder que armazena as referências para as views de cada card.
     */
    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvStartTime, tvEndTime, tvTeacher, tvClassName, tvEmojiModalidade, tvModalidade;
        Chip chipStatus;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvEmojiModalidade = itemView.findViewById(R.id.tvEmojiModalidade);
            tvModalidade = itemView.findViewById(R.id.tvModalidade);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}