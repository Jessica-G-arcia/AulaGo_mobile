package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide; // Import do Glide
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    // Constantes para definir o "modo" do adapter
    public static final int MODO_EXIBIR_ALUNO = 1;    // Mostra a foto/nome do Aluno (usado no perfil do prof)
    public static final int MODO_EXIBIR_PROFESSOR = 2; // Mostra a foto/nome do Prof (usado no perfil do aluno)

    private Context context;
    private List<ReviewModel> reviewList;
    private int modoExibicao;

    // Construtor que recebe o "modo"
    public ReviewAdapter(Context context, List<ReviewModel> reviewList, int modoExibicao) {
        this.context = context;
        this.reviewList = reviewList;
        this.modoExibicao = modoExibicao;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);
        if (review == null) return;

        // 1. Preenche os campos comuns
        holder.rbReviewRating.setRating((float) review.getRating());
        holder.tvReviewComment.setText(review.getComentario());

        // 2. Formata a data
        if (review.getDataAvaliacao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvReviewDate.setText(sdf.format(review.getDataAvaliacao()));
        } else {
            holder.tvReviewDate.setText(""); // Oculta se a data for nula
        }

        // 3. A LÓGICA MÁGICA: Decide qual nome/foto mostrar
        String nomeParaExibir = "";
        String avatarParaExibir = "";

        if (modoExibicao == MODO_EXIBIR_ALUNO) {
            // Estamos no perfil do Professor, mostramos quem escreveu (o Aluno)
            nomeParaExibir = review.getAlunoNome();
            avatarParaExibir = review.getAlunoAvatarUrl();

        } else if (modoExibicao == MODO_EXIBIR_PROFESSOR) {
            // Estamos no perfil do Aluno, mostramos quem escreveu (o Professor)
            nomeParaExibir = review.getProfessorNome();
            avatarParaExibir = review.getProfessorAvatarUrl();
        }

        // 4. Define o nome
        holder.tvReviewerName.setText(nomeParaExibir);
//
//        // 5. Carrega a imagem (com Glide)
//        Glide.with(context)
//                .load(avatarParaExibir)
//                .placeholder(R.drawable.img_avatar_circle) // Imagem padrão
//                .error(R.drawable.img_avatar_circle)       // Imagem de erro
//                .into(holder.ivReviewerAvatar);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // Para atualizar a lista dinamicamente
    public void updateList(List<ReviewModel> newList) {
        this.reviewList = newList;
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    // Mapeia os IDs do seu XML (review_card.xml)
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView ivReviewerAvatar;
        TextView tvReviewerName;
        RatingBar rbReviewRating;
        TextView tvReviewDate;
        TextView tvReviewComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewerAvatar = itemView.findViewById(R.id.ivReviewerAvatar);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
        }
    }
}