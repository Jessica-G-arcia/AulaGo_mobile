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

public class AlunoAdapter extends RecyclerView.Adapter<AlunoAdapter.AlunoViewHolder> {

    private Context context;
    // A lista 'fullList' guarda TODOS os alunos (para o filtro)
    private List<Aluno> fullList;
    // A lista 'filteredList' guarda os alunos que estão sendo exibidos
    private List<Aluno> filteredList;
    private OnAlunoClickListener listener;

    /**
     * Interface para lidar com o clique no botão "Conferir Perfil"
     */
    public interface OnAlunoClickListener {
        void onAlunoClick(Aluno aluno);
    }

    // Construtor
    public AlunoAdapter(Context context, List<Aluno> alunoList, OnAlunoClickListener listener) {
        this.context = context;
        this.fullList = new ArrayList<>(alunoList); // Copia a lista
        this.filteredList = new ArrayList<>(alunoList); // Copia a lista
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlunoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o seu XML de card de aluno aqui
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_card, parent, false);
        return new AlunoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlunoViewHolder holder, int position) {
        // Pega o aluno da lista FILTRADA
        Aluno aluno = filteredList.get(position);

        // --- É AQUI QUE OS DADOS SÃO DEFINIDOS ---

        // Define os dados básicos (Corrigido de getHome() para getNome())
        holder.tvStudentName.setText(aluno.getNome());
        holder.tvStudentAge.setText(String.format(Locale.US, "%d anos", aluno.getIdade()));
        holder.tvStudentDescription.setText(aluno.getBio());

        // Carrega a foto de perfil
        Glide.with(context)
                .load(aluno.getUrlFotoPerfil())
                .placeholder(R.drawable.img_avatar_circle) // Imagem padrão
                .error(R.drawable.img_avatar_circle) // Imagem de erro
                .circleCrop() // Arredonda a imagem
                .into(holder.ivStudentAvatar);

        // --- LÓGICA DA AVALIAÇÃO
        long contagem = aluno.getRatingCount();

        if (contagem > 0) {
            // 1. Pega a média.
            // CORREÇÃO: Usa getRatingMedia() em vez de getRating()
            double media = aluno.getRatingMedia();

            // 2. Formata a nota média (ex: "⭐ 4.8")
            String mediaFormatada = String.format(Locale.US, "⭐ %.1f", media);
            holder.tvStudentRating.setText(mediaFormatada);

            // 3. Formata a contagem (ex: "(12 avaliações)")
            String contagemFormatada = String.format(Locale.getDefault(), "(%d avaliaç%s)",
                    contagem,
                    contagem > 1 ? "ões" : "ão");
            holder.tvStudentReviews.setText(contagemFormatada);

            // 4. Garante que eles estão visíveis
            holder.tvStudentRating.setVisibility(View.VISIBLE);
            holder.tvStudentReviews.setVisibility(View.VISIBLE);
        } else {
            // Se não há avaliações, mostra "Novo" e esconde a contagem
            holder.tvStudentRating.setText("⭐ Novo");
            holder.tvStudentReviews.setVisibility(View.GONE);
        }

        // Define o clique no botão
        holder.btnContact.setOnClickListener(v -> {
            listener.onAlunoClick(aluno);
        });

        // TODO: Lógica para tvStudentDistance (Distância)
        holder.tvStudentDistance.setText("A 2.5 km de distância");
    }

    @Override
    public int getItemCount() {
        return filteredList.size(); // Retorna o tamanho da lista FILTRADA
    }

    /**
     * ViewHolder que "segura" as Views do seu XML
     */
    static class AlunoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStudentAvatar;
        TextView tvStudentName, tvStudentAge, tvStudentRating, tvStudentReviews;
        TextView tvStudentDescription, tvStudentDistance;
        Button btnContact;
        // LinearLayout layoutTags; // Você pode adicionar as tags aqui

        public AlunoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Linka as variáveis com os IDs do seu layout_card_aluno.xml
            ivStudentAvatar = itemView.findViewById(R.id.ivStudentAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentAge = itemView.findViewById(R.id.tvStudentAge);
            tvStudentRating = itemView.findViewById(R.id.tvStudentRating);
            tvStudentReviews = itemView.findViewById(R.id.tvStudentReviews);
            tvStudentDescription = itemView.findViewById(R.id.tvStudentDescription);
            tvStudentDistance = itemView.findViewById(R.id.tvStudentDistance);
            btnContact = itemView.findViewById(R.id.btnContact);
            // layoutTags = itemView.findViewById(R.id.layoutTags);
        }
    }

    // --- MÉTODOS DE FILTRO E ATUALIZAÇÃO ---

    /**
     * Atualiza a lista quando o Firebase termina de carregar
     */
    public void updateList(List<Aluno> newList) {
        fullList.clear();
        fullList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged(); // Avisa o RecyclerView para redesenhar
    }

    /**
     * Filtra a lista baseado no texto digitado
     */
    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            text = text.toLowerCase().trim();
            for (Aluno aluno : fullList) {
                if (aluno.getNome().toLowerCase().contains(text)) {
                    filteredList.add(aluno);
                }
            }
        }
        notifyDataSetChanged(); // Avisa o RecyclerView para redesenhar
    }
}