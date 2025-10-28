package com.example.aulago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlunoAdapter extends RecyclerView.Adapter<AlunoAdapter.AlunoViewHolder> {

    private final List<Aluno> alunoList;

    public AlunoAdapter(List<Aluno> alunoList) {
        this.alunoList = alunoList;
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

        holder.foto.setImageResource(aluno.getFotoResourceId());
        holder.ratingBar.setRating(aluno.getRating());
        holder.nome.setText(aluno.getNome());
        holder.idioma.setText("Idioma(s): " + aluno.getIdioma());
        holder.citacao.setText("\"" + aluno.getCitacao() + "\"");
        holder.autorCitacao.setText(aluno.getAutorCitacao());
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