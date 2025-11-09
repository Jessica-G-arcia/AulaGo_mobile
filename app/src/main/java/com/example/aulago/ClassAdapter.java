package com.example.aulago;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log; // 👈 NOVO IMPORT
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.Calendar; // 👈 NOVO IMPORT
import java.util.Date;     // 👈 NOVO IMPORT
import java.util.List;
import java.util.Locale;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private Context context;
    private List<ClassModel> classList;

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

        // --- Seções 1, 2 e 3 (Sem alterações) ---

        // 1. Define os Horários
        if (classModel.getHorarioInicio() != null) {
            holder.tvStartTime.setText(classModel.getHorarioInicio());
        }
        if (classModel.getHorarioFim() != null) {
            holder.tvEndTime.setText(classModel.getHorarioFim());
        }

        // 2. Define os Detalhes da Aula (Nome do Aluno)
        if (classModel.getAlunoNome() != null) {
            holder.tvTeacher.setText(classModel.getAlunoNome());
        }
        if (classModel.getIdioma() != null) {
            holder.tvClassName.setText(classModel.getIdioma());
        }

        // 3. Define a Modalidade
        if (classModel.getLocal() != null) {
            String local = classModel.getLocal().toLowerCase(Locale.getDefault());
            if (local.contains("online") || local.contains("meet") || local.contains("zoom")) {
                holder.tvEmojiModalidade.setText("💻");
                holder.tvModalidade.setText("Aula Online");
            } else if (local.contains("presencial") || local.contains("endereço")) {
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

        // ---
        //
        // ✅ 4. LÓGICA DE STATUS ATUALIZADA (Data Passou?)
        //
        // ---
        if (classModel.getStatus() != null && classModel.getData() != null) {

            // Pega a data da aula e a data de hoje (início do dia)
            Date classDate = classModel.getData();
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();

            String status = classModel.getStatus().toLowerCase(Locale.getDefault());

            // Verifica as condições
            boolean hasPassed = classDate.before(today); // A data é anterior a hoje
            boolean isCompleted = status.equals("concluída");
            boolean isCancelled = status.equals("cancelada");

            // CONDIÇÃO: (A data passou E a aula não foi cancelada) OU (A aula foi concluída)
            if ((hasPassed && !isCancelled) || isCompleted) {

                holder.chipStatus.setText("Avaliar");
                holder.chipStatus.setClickable(true); // Torna o chip clicável


                int chipColorRes = R.color.verde_claro; // Ex: #EDE7F6
                int textColorRes = R.color.verde_escuro; // Ex: #5E35B1

                holder.chipStatus.setChipBackgroundColor(
                        ColorStateList.valueOf(ContextCompat.getColor(context, chipColorRes))
                );
                holder.chipStatus.setTextColor(
                        ContextCompat.getColor(context, textColorRes)
                );

                // Adiciona a ação de clique
                holder.chipStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Ação ao clicar em "Avaliar"
                        Log.d("ClassAdapter", "Clicou para avaliar a aula do aluno: " + classModel.getAlunoNome());

                        // TODO: Iniciar a Activity de Avaliação
                        // Intent intent = new Intent(context, ReviewActivity.class);
                        // intent.putExtra("classId", classModel.getId()); // (Você precisaria de um ID da aula)
                        // context.startActivity(intent);
                    }
                });

            } else {
                // LÓGICA ANTIGA: Se a data não passou, exibe o status normal

                holder.chipStatus.setText(classModel.getStatus()); // Status original
                holder.chipStatus.setClickable(false); // Não é clicável

                // (Lógica de cor original)
                int chipColorRes = R.color.azul50;
                int textColorRes = R.color.marinho;

                switch (status) {
                    case "confirmada":
                        chipColorRes = R.color.verde_claro;
                        textColorRes = R.color.verde_escuro;
                        break;
                    case "concluída": // Pego pela lógica acima, mas deixamos aqui por segurança
                        chipColorRes = R.color.cinza_claro;
                        textColorRes = R.color.cinza_escuro;
                        break;
                    case "cancelada":
                        chipColorRes = R.color.vermelho_claro;
                        textColorRes = R.color.vermelho_escuro;
                        break;
                    // "Agendada" usa o default (azul)
                }

                holder.chipStatus.setChipBackgroundColor(
                        ColorStateList.valueOf(ContextCompat.getColor(context, chipColorRes))
                );
                holder.chipStatus.setTextColor(
                        ContextCompat.getColor(context, textColorRes)
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    // Função de atualização chamada pela Activity
    public void updateList(List<ClassModel> newList) {
        this.classList = newList;
        notifyDataSetChanged();
    }


    // --- VIEWHOLDER (Sem alterações) ---
    public static class ClassViewHolder extends RecyclerView.ViewHolder {

        TextView tvStartTime;
        TextView tvEndTime;
        TextView tvTeacher;
        TextView tvClassName;
        TextView tvEmojiModalidade;
        TextView tvModalidade;
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