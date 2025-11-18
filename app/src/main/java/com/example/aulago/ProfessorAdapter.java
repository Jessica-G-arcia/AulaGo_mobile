package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_professor_card, parent, false);
        return new ProfessorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessorViewHolder holder, int position) {
        Professor professor = filteredList.get(position);

        // --- 1. NOME ---
        holder.tvProfessorName.setText(professor.getNome());

        // --- 2. IDADE (tvProfessorAge) ---
        if (professor.getIdade() != null) {
            holder.tvProfessorAge.setText(professor.getIdade() + " anos");
            holder.tvProfessorAge.setVisibility(View.VISIBLE);
        } else {
            holder.tvProfessorAge.setVisibility(View.GONE);
        }

        // --- 3. ESPECIALIDADE (tvProfessorDescription) ---
        // Usamos o campo de descrição para mostrar a especialidade
        if (professor.getEspecialidade() != null && !professor.getEspecialidade().isEmpty()) {
            holder.tvProfessorDescription.setText(professor.getEspecialidade());
            holder.tvProfessorDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvProfessorDescription.setText("Especialidade não informada");
        }

        // Garante que o botão "Ver mais" esteja oculto (conforme solicitado anteriormente)
        holder.tvShowMore.setVisibility(View.GONE);


        // --- 4. TAG: IDIOMA ---
        if (professor.getIdioma() != null && !professor.getIdioma().isEmpty()) {
            holder.tvProfessorLanguage.setText(professor.getIdioma());
            holder.layoutTagLanguage.setVisibility(View.VISIBLE);
        } else {
            holder.layoutTagLanguage.setVisibility(View.GONE);
        }

        // --- 5. TAG: MODALIDADE ---
        String modalidade = (professor.getPreferenciaModalidade() != null) ? professor.getPreferenciaModalidade() : "Online";

        if (professor.getPreferenciaModalidade() != null && !professor.getPreferenciaModalidade().isEmpty()) {
            holder.tvProfessorModality.setText(modalidade);
            holder.layoutTagModality.setVisibility(View.VISIBLE);

            // Troca o ícone dentro da tag (Câmera vs Perfil/Local)
            if (modalidade.equalsIgnoreCase("Presencial")) {
                holder.ivProfessorModalityIcon.setImageResource(R.drawable.ic_perfil); // ou ic_location se tiver
            } else {
                holder.ivProfessorModalityIcon.setImageResource(R.drawable.ic_camera); // certifique-se de ter esse drawble, ou use ic_mundo
            }
        } else {
            holder.layoutTagModality.setVisibility(View.GONE);
        }

        // --- 6. DISTÂNCIA ---
        holder.tvProfessorDistance.setText("9 km de você");
        // Se quiser esconder caso não tenha distância:
        // holder.layoutDistance.setVisibility(View.VISIBLE);

        // --- 7. FOTO ---
        Glide.with(context)
                .load(professor.getUrlFotoPerfil())
                .placeholder(R.drawable.img_avatar_circle)
                .error(R.drawable.img_avatar_circle)
                .circleCrop()
                .into(holder.ivProfessorAvatar);

        // --- 8. AVALIAÇÃO ---
        long contagem = professor.getRatingCount();
        if (contagem > 0) {
            double media = professor.getRatingMedia();
            holder.tvProfessorRating.setText(String.format(Locale.US, "⭐ %.1f", media));

            String contagemFormatada = String.format(Locale.getDefault(), "(%d avaliaç%s)",
                    contagem, contagem > 1 ? "ões" : "ão");
            holder.tvProfessorReviews.setText(contagemFormatada);

            holder.tvProfessorRating.setVisibility(View.VISIBLE);
            holder.tvProfessorReviews.setVisibility(View.VISIBLE);
        } else {
            holder.tvProfessorRating.setText("⭐ Novo");
            holder.tvProfessorReviews.setVisibility(View.GONE);
        }

        // --- 9. CLIQUE ---
        holder.btnContact.setOnClickListener(v -> listener.onProfessorClick(professor));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // ================= VIEW HOLDER =================
    // Mapeamento exato do seu XML enviado
    static class ProfessorViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfessorAvatar;
        TextView tvProfessorName;
        TextView tvProfessorAge;
        TextView tvProfessorRating, tvProfessorReviews;

        // Tags (Layouts e Conteúdo)
        LinearLayout layoutTagLanguage;
        TextView tvProfessorLanguage;

        LinearLayout layoutTagModality;
        TextView tvProfessorModality;
        ImageView ivProfessorModalityIcon;

        // Área da Descrição/Especialidade
        TextView tvProfessorDescription;
        TextView tvShowMore;

        // Área da Distância
        LinearLayout layoutDistance;
        TextView tvProfessorDistance;

        MaterialButton btnContact;

        public ProfessorViewHolder(@NonNull View itemView) {
            super(itemView);

            // Cabeçalho
            ivProfessorAvatar = itemView.findViewById(R.id.ivProfessorAvatar);
            tvProfessorName = itemView.findViewById(R.id.tvProfessorName);
            tvProfessorAge = itemView.findViewById(R.id.tvProfessorAge);
            tvProfessorRating = itemView.findViewById(R.id.tvProfessorRating);
            tvProfessorReviews = itemView.findViewById(R.id.tvProfessorReviews);

            // Tag Idioma
            layoutTagLanguage = itemView.findViewById(R.id.layoutTagLanguage);
            tvProfessorLanguage = itemView.findViewById(R.id.tvProfessorLanguage);

            // Tag Modalidade
            layoutTagModality = itemView.findViewById(R.id.layoutTagModality);
            tvProfessorModality = itemView.findViewById(R.id.tvProfessorModality);
            ivProfessorModalityIcon = itemView.findViewById(R.id.ivProfessorModalityIcon);

            // Descrição
            tvProfessorDescription = itemView.findViewById(R.id.tvProfessorDescription);
            tvShowMore = itemView.findViewById(R.id.tvShowMore);

            // Distância
            layoutDistance = itemView.findViewById(R.id.layoutDistance);
            tvProfessorDistance = itemView.findViewById(R.id.tvProfessorDistance);

            // Botão
            btnContact = itemView.findViewById(R.id.btnContact);
        }
    }

    // --- MÉTODOS DE FILTRO ---
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
                if (prof.getNome().toLowerCase().contains(text) ||
                        (prof.getEspecialidade() != null && prof.getEspecialidade().toLowerCase().contains(text)) ||
                        (prof.getIdioma() != null && prof.getIdioma().toLowerCase().contains(text))) {
                    filteredList.add(prof);
                }
            }
        }
        notifyDataSetChanged();
    }
}