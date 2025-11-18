package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        Aluno aluno = filteredList.get(position);

        // --- DADOS BÁSICOS ---
        holder.tvStudentName.setText(aluno.getNome());

        // Carrega a foto de perfil
        Glide.with(context)
                .load(aluno.getUrlFotoPerfil())
                .placeholder(R.drawable.img_avatar_circle)
                .error(R.drawable.img_avatar_circle)
                .circleCrop()
                .into(holder.ivStudentAvatar);

        // --- IDADE ---
        Integer idade = aluno.getIdade();
        if (idade != null) {
            holder.tvStudentAge.setText(idade + " anos");
            holder.tvStudentAge.setVisibility(View.VISIBLE);
        } else {
            holder.tvStudentAge.setText("Idade não informada");
            // ou holder.tvStudentAge.setVisibility(View.GONE);
        }

        // --- AVALIAÇÃO (Rating) ---
        long contagem = aluno.getRatingCount();
        if (contagem > 0) {
            holder.tvStudentRating.setText(String.format(Locale.US, "%.1f", aluno.getRatingMedia()));
            holder.tvStudentReviews.setText(String.format(Locale.getDefault(), "(%d avaliaç%s)",
                    contagem, contagem > 1 ? "ões" : "ão"));
            holder.tvStudentRating.setVisibility(View.VISIBLE);
            holder.tvStudentReviews.setVisibility(View.VISIBLE);
            holder.ivRatingIcon.setVisibility(View.VISIBLE);
        } else {
            holder.tvStudentRating.setText("Novo");
            holder.tvStudentReviews.setVisibility(View.GONE);
            // Você pode esconder o ícone de estrela se for "Novo"
            // holder.ivRatingIcon.setVisibility(View.GONE);
        }

        // --- TAGS (Idioma, Nível, Modalidade) ---

        // Idioma
        if (aluno.getIdioma() != null && !aluno.getIdioma().isEmpty()) {
            holder.tvStudentLanguage.setText(aluno.getIdioma());
            holder.layoutTagLanguage.setVisibility(View.VISIBLE);
        } else {
            holder.layoutTagLanguage.setVisibility(View.GONE);
        }

        // Nível
        if (aluno.getNivel() != null && !aluno.getNivel().isEmpty()) {
            holder.tvStudentLevel.setText(aluno.getNivel());
            holder.layoutTagLevel.setVisibility(View.VISIBLE);
        } else {
            holder.layoutTagLevel.setVisibility(View.GONE);
        }

        // Modalidade
        if (aluno.getPreferenciaModalidade() != null && !aluno.getPreferenciaModalidade().isEmpty()) {
            holder.tvStudentModality.setText(aluno.getPreferenciaModalidade());
            holder.layoutTagModality.setVisibility(View.VISIBLE);
            // (Opcional) Mudar o ícone dinamicamente
            if (aluno.getPreferenciaModalidade().equalsIgnoreCase("Online")) {
                holder.ivStudentModalityIcon.setImageResource(R.drawable.ic_camera); // ic_videocam
            } else {
                holder.ivStudentModalityIcon.setImageResource(R.drawable.ic_perfil); // ic_people
            }
        } else {
            holder.layoutTagModality.setVisibility(View.GONE);
        }

        // --- DESCRIÇÃO (Objetivos) ---
        if (aluno.getObjetivos() != null && !aluno.getObjetivos().isEmpty()) {
            holder.tvStudentDescription.setText(aluno.getObjetivos());
            holder.layoutDescription.setVisibility(View.VISIBLE);

            // Define o estado (Expandido ou Encolhido)
            if (aluno.isExpanded()) {
                holder.tvStudentDescription.setMaxLines(Integer.MAX_VALUE);
                holder.tvShowMore.setText("Ver menos");
            } else {
                holder.tvStudentDescription.setMaxLines(1);
                holder.tvShowMore.setText("Ver mais");
            }

            // ❗️ LÓGICA PARA SÓ MOSTRAR "Ver mais" SE O TEXTO FOR GRANDE ❗️
            // (Isto é complexo, então usamos um post() para esperar o texto ser desenhado)
            holder.tvShowMore.setVisibility(View.GONE); // Esconde por padrão
            holder.tvStudentDescription.post(() -> {
                // Verifica se o texto da linha 0 foi cortado (tem "...")
                if (holder.tvStudentDescription.getLayout() != null) {
                    if (holder.tvStudentDescription.getLayout().getEllipsisCount(0) > 0 || aluno.isExpanded()) {
                        // O texto é grande (foi cortado) OU já está expandido
                        holder.tvShowMore.setVisibility(View.VISIBLE);
                    }
                }
            });


            // Adiciona o clique no layout INTEIRO da descrição
            holder.layoutDescription.setOnClickListener(v -> {
                aluno.setExpanded(!aluno.isExpanded());
                notifyItemChanged(holder.getAdapterPosition());
            });

        } else {
            // Se não há objetivos, esconde tudo e remove o clique
            holder.layoutDescription.setVisibility(View.GONE);
            holder.layoutDescription.setOnClickListener(null);
        }

        // --- DISTÂNCIA ---
        float distancia = aluno.getDistanciaCalculada();
        if (distancia > 0) {
            holder.tvStudentDistance.setText(String.format(Locale.getDefault(), "%.1f km de você", distancia));
            holder.layoutDistance.setVisibility(View.VISIBLE);
        } else {
            holder.layoutDistance.setVisibility(View.GONE);
        }

        // --- BOTÃO ---
        holder.btnContact.setOnClickListener(v -> {
            listener.onAlunoClick(aluno);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    /**
     * ✅ ESTE É O MÉTODO QUE "SEGURA" OS IDs DO XML
     */
    static class AlunoViewHolder extends RecyclerView.ViewHolder {
        // Cabeçalho
        ImageView ivStudentAvatar;
        TextView tvStudentName, tvStudentAge;

        // Rating
        ImageView ivRatingIcon;
        TextView tvStudentRating, tvStudentReviews;

        // Tags
        LinearLayout layoutTagLanguage, layoutTagModality, layoutTagLevel;
        ImageView ivLanguageIcon, ivStudentModalityIcon, ivLevelIcon;
        TextView tvStudentLanguage, tvStudentModality, tvStudentLevel;

        // Linhas de Info
        LinearLayout layoutDescription, layoutDistance;
        ImageView ivDescriptionIcon, ivDistanceIcon;
        TextView tvStudentDescription, tvStudentDistance;
        TextView tvShowMore;

        // Botão
        Button btnContact;

        public AlunoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Linka as variáveis com os IDs do seu XML
            ivStudentAvatar = itemView.findViewById(R.id.ivStudentAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentAge = itemView.findViewById(R.id.tvStudentAge);

            ivRatingIcon = itemView.findViewById(R.id.ivRatingIcon);
            tvStudentRating = itemView.findViewById(R.id.tvStudentRating);
            tvStudentReviews = itemView.findViewById(R.id.tvStudentReviews);

            layoutTagLanguage = itemView.findViewById(R.id.layoutTagLanguage);
            ivLanguageIcon = itemView.findViewById(R.id.ivLanguageIcon);
            tvStudentLanguage = itemView.findViewById(R.id.tvStudentLanguage);

            layoutTagModality = itemView.findViewById(R.id.layoutTagModality);
            ivStudentModalityIcon = itemView.findViewById(R.id.ivStudentModalityIcon);
            tvStudentModality = itemView.findViewById(R.id.tvStudentModality);

            layoutTagLevel = itemView.findViewById(R.id.layoutTagLevel);
            ivLevelIcon = itemView.findViewById(R.id.ivLevelIcon);
            tvStudentLevel = itemView.findViewById(R.id.tvStudentLevel);

            layoutDescription = itemView.findViewById(R.id.layoutDescription);
            ivDescriptionIcon = itemView.findViewById(R.id.ivDescriptionIcon);
            tvStudentDescription = itemView.findViewById(R.id.tvStudentDescription);
            tvShowMore = itemView.findViewById(R.id.tvShowMore);

            layoutDistance = itemView.findViewById(R.id.layoutDistance);
            ivDistanceIcon = itemView.findViewById(R.id.ivDistanceIcon);
            tvStudentDistance = itemView.findViewById(R.id.tvStudentDistance);

            btnContact = itemView.findViewById(R.id.btnContact);
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

                // Verifica se o texto de busca está em QUALQUER um dos campos
                if (aluno.getNome().toLowerCase().contains(text) ||
                        (aluno.getNivel() != null && aluno.getNivel().toLowerCase().contains(text)) ||
                        (aluno.getIdioma() != null && aluno.getIdioma().toLowerCase().contains(text)) ||
                        (aluno.getPreferenciaModalidade() != null && aluno.getPreferenciaModalidade().toLowerCase().contains(text)) || // Use getModalidadePreferida se você mudou
                        (aluno.getObjetivos() != null && aluno.getObjetivos().toLowerCase().contains(text))) {
                    filteredList.add(aluno);
                }
            }
        }
        notifyDataSetChanged(); // Avisa o RecyclerView para redesenhar
    }
}