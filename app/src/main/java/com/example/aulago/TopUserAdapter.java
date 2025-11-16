package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Locale;

public class TopUserAdapter extends RecyclerView.Adapter<TopUserAdapter.TopUserViewHolder> {

    private List<UserModel> userList; // Use seu modelo (UserModel, Aluno ou Professor)
    private Context context;

    public TopUserAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public TopUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        // Infla o SEU layout de card
        View view = LayoutInflater.from(context).inflate(R.layout.item_aluno, parent, false);
        return new TopUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopUserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        if (user == null) return;

        // 1. Carrega a Foto
        Glide.with(context)
                .load(user.getUrlFotoPerfil())
                .placeholder(R.drawable.img_avatar_circle)
                .error(R.drawable.img_avatar_circle)
                .into(holder.ivUserPhoto);

        // 2. Carrega o Nome
        holder.tvUserName.setText(user.getNome());

        // 3. LÓGICA DA AVALIAÇÃO
        if (user.getRatingCount() > 0) {
            holder.tvProfessorRating.setVisibility(View.VISIBLE);
            holder.tvProfessorReviews.setVisibility(View.VISIBLE);
            holder.tvProfessorRating.setText(String.format(Locale.getDefault(), "⭐ %.1f", user.getRatingMedia()));
            holder.tvProfessorReviews.setText(String.format(Locale.getDefault(), "(%d avaliações)", user.getRatingCount()));
        } else {
            // Esconde os campos de avaliação se não houver nenhuma
            holder.tvProfessorRating.setVisibility(View.GONE);
            holder.tvProfessorReviews.setVisibility(View.GONE);
        }

        // 4. Lógica da "Especialidade" (Dinâmico)
        if ("aluno".equals(user.getUserType())) {
            // Se for aluno, mostra o Nível (ex: "Nível: Básico")
            holder.tvUserSpecialty.setText("Nível: " + user.getNivel());
        } else {
            // Se for professor, mostra a Especialidade (ex: "Especialidade: Inglês")
            holder.tvUserSpecialty.setText("Especialidade: " + user.getEspecialidade());
        }

        // 5. LÓGICA DO "COMENTÁRIO"
        // Verifica se há um comentário para exibir
        String melhorComentario = user.getComentarioMelhorAvaliado();
        String autorComentario = user.getAutorComentarioMelhorAvaliado();

        if (melhorComentario != null && !melhorComentario.trim().isEmpty() && autorComentario != null) {
            holder.tvUserQuote.setVisibility(View.VISIBLE);
            holder.tvUserQuoteAuthor.setVisibility(View.VISIBLE);
            holder.tvUserQuote.setText("\"" + melhorComentario + "\"");
            holder.tvUserQuoteAuthor.setText(autorComentario);
        } else {
            // Esconde os campos de citação se não houver comentário
            holder.tvUserQuote.setVisibility(View.GONE);
            holder.tvUserQuoteAuthor.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<UserModel> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    // Mapeia os IDs do seu "item_aluno.xml"
    public static class TopUserViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView ivUserPhoto;
        TextView tvUserName, tvUserSpecialty, tvUserQuote, tvUserQuoteAuthor;
        TextView tvProfessorRating, tvProfessorReviews;

        public TopUserViewHolder(@NonNull View itemView) {
            super(itemView);

            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = itemView.findViewById(R.id.tvUserName);

            tvProfessorRating = itemView.findViewById(R.id.tvProfessorRating);   // ← ADICIONADO
            tvProfessorReviews = itemView.findViewById(R.id.tvProfessorReviews); // ← ADICIONADO

            tvUserSpecialty = itemView.findViewById(R.id.tvUserSpecialty);
            tvUserQuote = itemView.findViewById(R.id.tvUserQuote);
            tvUserQuoteAuthor = itemView.findViewById(R.id.tvUserQuoteAuthor);
        }
    }

}