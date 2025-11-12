package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.StringJoiner;

public class TopUserAdapter extends RecyclerView.Adapter<TopUserAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private Context context;

    public TopUserAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        // PASSO MAIS IMPORTANTE: Inflando o NOVO layout do card azul
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        // Preenchendo os dados no card
        holder.tvUserName.setText(user.getNome());
        holder.rbUserRating.setVisibility(View.VISIBLE);
        holder.rbUserRating.setRating((float) user.getRatingMedia());

        // Carregando a foto com Glide
        if (user.getFotoUrl() != null && !user.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getFotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.img_avatar_circle) // Imagem padrão
                    .into(holder.ivUserPhoto);
        } else {
            holder.ivUserPhoto.setImageResource(R.drawable.img_avatar_circle); // Imagem padrão
        }

        // Juntando a lista de idiomas em um texto
        if (user.getIdiomas() != null && !user.getIdiomas().isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ");
            for (String idioma : user.getIdiomas()) {
                joiner.add(idioma);
            }
            holder.tvUserSpecialty.setText("Idiomas: " + joiner.toString());
        } else {
            holder.tvUserSpecialty.setText("Idiomas: Não informado");
        }

        // Mostrando a citação e o autor
        holder.tvUserQuote.setText(user.getQuote() != null ? "\"" + user.getQuote() + "\"" : "");
        holder.tvUserQuoteAuthor.setText(user.getQuoteAuthor() != null ? "— " + user.getQuoteAuthor() : "");
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void updateList(List<UserModel> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder com os IDs do novo layout 'item_top_user_card.xml'
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserPhoto;
        TextView tvUserName, tvUserSpecialty, tvUserQuote, tvUserQuoteAuthor;
        RatingBar rbUserRating;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            rbUserRating = itemView.findViewById(R.id.rbUserRating);
            tvUserSpecialty = itemView.findViewById(R.id.tvUserSpecialty);
            tvUserQuote = itemView.findViewById(R.id.tvUserQuote);
            tvUserQuoteAuthor = itemView.findViewById(R.id.tvUserQuoteAuthor);
        }
    }
}