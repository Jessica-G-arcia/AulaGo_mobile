package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfessorAdapter extends RecyclerView.Adapter<ProfessorAdapter.ProfessorViewHolder> {

    private Context context;
    private List<Professor> fullList;
    private List<Professor> filteredList;
    private OnProfessorClickListener listener;

    public interface OnProfessorClickListener {
        void onProfessorClick(Professor professor);
    }

    public ProfessorAdapter(Context context, List<Professor> professorList, OnProfessorClickListener listener) {
        this.context = context;
        this.fullList = new ArrayList<>(professorList);
        this.filteredList = new ArrayList<>(professorList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfessorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ Corrigido: Infla o layout específico do Professor
        View view = LayoutInflater.from(context).inflate(R.layout.item_professor_card, parent, false);
        return new ProfessorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessorViewHolder holder, int position) {
        Professor professor = filteredList.get(position);

        // Define os dados
        holder.tvProfessorName.setText(professor.getNome());
        holder.tvProfessorSpecialty.setText("Especialidade: " + professor.getEspecialidade());
        holder.tvProfessorBio.setText(professor.getBio());

        // Carrega a imagem
        Glide.with(context)
                .load(professor.getUrlFotoPerfil())
                .placeholder(R.drawable.img_avatar_circle)
                .error(R.drawable.img_avatar_circle)
                .circleCrop()
                .into(holder.ivProfessorAvatar);

        // Lógica da Avaliação
        long contagem = professor.getRatingCount();

        if (contagem > 0) {
            // 1. Pega a média.
            // Garanta que sua classe Professor tem o método getRatingMedia()
            double media = professor.getRatingMedia();

            // 2. Formata a nota média (ex: "⭐ 4.8")
            String mediaFormatada = String.format(Locale.US, "⭐ %.1f", media);
            holder.tvProfessorRating.setText(mediaFormatada);

            // 3. Formata a contagem (ex: "(12 avaliações)")
            String contagemFormatada = String.format(Locale.getDefault(), "(%d avaliaç%s)",
                    contagem,
                    contagem > 1 ? "ões" : "ão");
            holder.tvProfessorReviews.setText(contagemFormatada);

            // 4. Garante que eles estão visíveis
            holder.tvProfessorRating.setVisibility(View.VISIBLE);
            holder.tvProfessorReviews.setVisibility(View.VISIBLE);
        } else {
            // Se não há avaliações, mostra "Novo" e esconde a contagem
            holder.tvProfessorRating.setText("⭐ Novo");
            holder.tvProfessorReviews.setVisibility(View.GONE);
        }
        // Define o clique para navegação
        holder.btnContact.setOnClickListener(v -> listener.onProfessorClick(professor));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // --- ViewHolder ---
    static class ProfessorViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfessorAvatar;
        // IDs mapeados para o novo layout de Professor
        TextView tvProfessorName, tvProfessorSpecialty, tvProfessorBio, tvProfessorRating, tvProfessorReviews;
        Button btnContact;

        public ProfessorViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfessorAvatar = itemView.findViewById(R.id.ivProfessorAvatar);
            tvProfessorName = itemView.findViewById(R.id.tvProfessorName);
            tvProfessorSpecialty = itemView.findViewById(R.id.tvProfessorSpecialty);
            tvProfessorBio = itemView.findViewById(R.id.tvProfessorBio);
            tvProfessorRating = itemView.findViewById(R.id.tvProfessorRating);
            tvProfessorReviews = itemView.findViewById(R.id.tvProfessorReviews);
            btnContact = itemView.findViewById(R.id.btnContact);
        }
    }

    // --- Métodos de Filtro e Atualização (Permanecem os mesmos) ---
    public void updateList(List<Professor> newList) {
        fullList.clear();
        fullList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            text = text.toLowerCase().trim();
            for (Professor prof : fullList) {
                if (prof.getNome().toLowerCase().contains(text)) {
                    filteredList.add(prof);
                }
            }
        }
        notifyDataSetChanged();
    }
}