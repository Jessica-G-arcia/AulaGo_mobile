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

// (Opcional) Importe o Glide ou Picasso se for carregar fotos da internet
import com.bumptech.glide.Glide;

import java.util.List;

// Este adapter aceita um List<UserModel>
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
        // Usa o layout 'item_aluno.xml' que você criou
        View view = LayoutInflater.from(context).inflate(R.layout.item_aluno, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.tvName.setText(user.getNome());
        holder.rbRating.setRating((float) user.getRatingMedia());
        holder.tvSpecialty.setText(user.getEspecialidade());
        holder.tvQuote.setText("\"" + user.getQuote() + "\"");
        holder.tvQuoteAuthor.setText(user.getQuoteAuthor());

        // Para carregar a foto do Firebase Storage
        if (user.getFotoUrl() != null && !user.getFotoUrl().isEmpty()) {
            Glide.with(context).load(user.getFotoUrl()).into(holder.ivPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Método para o Fragment atualizar a lista
    public void updateList(List<UserModel> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder com os IDs do 'item_aluno.xml'
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvSpecialty, tvQuote, tvQuoteAuthor;
        RatingBar rbRating;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvName = itemView.findViewById(R.id.tvUserName);
            rbRating = itemView.findViewById(R.id.rbUserRating);
            tvSpecialty = itemView.findViewById(R.id.tvUserSpecialty);
            tvQuote = itemView.findViewById(R.id.tvUserQuote);
            tvQuoteAuthor = itemView.findViewById(R.id.tvUserQuoteAuthor);
        }
    }
}