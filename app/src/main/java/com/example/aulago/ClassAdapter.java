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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private final Context context;
    private List<ClassModel> classList;
    private String userType; // ADICIONADO

    // Use este construtor no calendário e em todas telas
    public ClassAdapter(Context context, List<ClassModel> classList, String userType) {
        this.context = context;
        this.classList = classList;
        this.userType = userType;
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

        // Data da aula
        if (classModel.getDataTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dataFormatada = sdf.format(classModel.getDataTimestamp().toDate());
            holder.tvDate.setText(dataFormatada);
        } else {
            holder.tvDate.setText("--/--/----");
        }

        holder.tvStartTime.setText(classModel.getHorarioInicio());
        holder.tvEndTime.setText(classModel.getHorarioFim());
        holder.tvClassName.setText(classModel.getIdioma());

        // Se for aluno, mostra professor. Se for professor, mostra aluno.
        if ("aluno".equalsIgnoreCase(userType)) {
            holder.tvTeacher.setText(classModel.getProfessorNome());
        } else {
            holder.tvTeacher.setText(classModel.getAlunoNome());
        }

        // Modalidade e emoji
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
            }
        } else {
            holder.tvEmojiModalidade.setVisibility(View.GONE);
            holder.tvModalidade.setVisibility(View.GONE);
        }

        // Status/Avaliar (igual antes)
        Date classEndDateTime = getClassEndDateTime(classModel.getDataTimestamp().toDate(), classModel.getHorarioFim());
        boolean hasPassed = classEndDateTime != null && new Date().after(classEndDateTime);
        boolean isCancelled = classModel.getStatus() != null && classModel.getStatus().equalsIgnoreCase("cancelada");

        if (hasPassed && !isCancelled) {
            holder.chipStatus.setText("Avaliar");
            holder.chipStatus.setClickable(true);
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.verde_claro)));
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.verde_escuro));
            // Adicione listener aqui se desejar
        } else {
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

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStartTime, tvEndTime, tvTeacher, tvClassName, tvEmojiModalidade, tvModalidade;
        Chip chipStatus;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
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
