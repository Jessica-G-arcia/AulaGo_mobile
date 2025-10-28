package com.example.aulago;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AulasAdapter extends RecyclerView.Adapter<AulasAdapter.AulaViewHolder> {

    private List<Aula> aulasList;

    public AulasAdapter(List<Aula> aulasList) {
        this.aulasList = aulasList;
    }

    @NonNull
    @Override
    public AulaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o layout do item da grade, que tem as estrelas
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aula_agendada, parent, false);
        return new AulaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AulaViewHolder holder, int position) {
        Aula aula = aulasList.get(position);
        holder.tvNomeAluno.setText("Aluno: " + aula.getAluno());
        holder.tvLocal.setText(aula.getLocal());
        holder.tvHorario.setText(aula.getHorario());
        holder.tvIdioma.setText(aula.getIdioma());
        holder.tvData.setText(aula.getData());
        holder.ratingBar.setRating(aula.getAvaliacao());

        // Verifica se a aula tem uma avaliação maior que 0 para ser considerada "concluída"
        if (aula.getAvaliacao() > 0) {
            // Se for concluída, pinta as estrelas de amarelo
            int yellowColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.rating_star_yellow);
            holder.ratingBar.setProgressTintList(ColorStateList.valueOf(yellowColor));

            int grayColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.rating_star_gray);
            holder.ratingBar.setProgressBackgroundTintList(ColorStateList.valueOf(grayColor));

        } else {
            // Se for agendada (sem avaliação), deixa as estrelas cinzas
            // 'else' é  importante para a reciclagem de views do RecyclerView
            int grayColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.rating_star_gray);
            holder.ratingBar.setProgressTintList(ColorStateList.valueOf(grayColor));
            holder.ratingBar.setProgressBackgroundTintList(ColorStateList.valueOf(grayColor));
        }
    }

    @Override
    public int getItemCount() {
        return aulasList.size();
    }
    public void filterList(List<Aula> filteredList) {
        this.aulasList = filteredList;
        notifyDataSetChanged();
    }

    // ViewHolder que "segura" os componentes do layout item_aula_agendada.xml
    public static class AulaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeAluno, tvLocal, tvHorario, tvIdioma, tvData;
        RatingBar ratingBar;

        public AulaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeAluno = itemView.findViewById(R.id.tvNomeAluno);
            tvLocal = itemView.findViewById(R.id.tvLocal);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            tvIdioma = itemView.findViewById(R.id.tvIdioma);
            tvData = itemView.findViewById(R.id.tvData);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}