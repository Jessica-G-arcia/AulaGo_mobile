package com.example.aulago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlunoAdapter extends RecyclerView.Adapter<AlunoAdapter.AlunoViewHolder> {

    private List<Aluno> alunoList;
    public AlunoAdapter(List<Aluno> alunoList) {
        this.alunoList = alunoList;
    }

    public void updateList(List<Aluno> newList) {
        this.alunoList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlunoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aluno, parent, false);
        return new AlunoViewHolder(view);
    }

    // Este método conecta os dados de um aluno específico à parte visual
    @Override
    public void onBindViewHolder(@NonNull AlunoViewHolder holder, int position) {
        Aluno aluno = alunoList.get(position);

        // Lógica de conversão da imagem
        String fotoRef = aluno.getFotoRef();
        int fotoId = getResourceId(holder.itemView.getContext(), fotoRef);
        if (fotoId != 0) { // Se encontrou o drawable
            holder.foto.setImageResource(fotoId);
        }

        holder.ratingBar.setRating(aluno.getRating());
        holder.nome.setText(aluno.getNome());
        holder.idioma.setText("Idioma(s): " + aluno.getIdioma());
        holder.citacao.setText("\"" + aluno.getCitacao() + "\"");
        holder.autorCitacao.setText(aluno.getAutorCitacao());
    }

    // função helper
    private int getResourceId(Context context, String name) {
        // converte a string "aluna1" no ID R.drawable.aluna1
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    @Override
    public int getItemCount() {
        return alunoList.size();
    }

    public static class AlunoViewHolder extends RecyclerView.ViewHolder {
        ImageView foto;
        RatingBar ratingBar;
        TextView nome, idioma, citacao, autorCitacao;
        ImageView arrowLeft, arrowRight;

        public AlunoViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.iv_aluno_foto);
            ratingBar = itemView.findViewById(R.id.rating_bar_aluno);
            nome = itemView.findViewById(R.id.tv_aluno_nome);
            idioma = itemView.findViewById(R.id.tv_aluno_idioma);
            citacao = itemView.findViewById(R.id.tv_aluno_citacao);
            autorCitacao = itemView.findViewById(R.id.tv_autor_citacao);
            arrowLeft = itemView.findViewById(R.id.iv_arrow_left_aluno);
            arrowRight = itemView.findViewById(R.id.iv_arrow_right_aluno);
        }
    }
}