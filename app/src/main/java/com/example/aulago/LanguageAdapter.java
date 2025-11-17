package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <-- IMPORTAR O GLIDE

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<Language> languageList;
    private Context context; // <-- Adicionar contexto para o Glide

    public LanguageAdapter(List<Language> languageList) {
        this.languageList = languageList;
    }

    public void updateList(List<Language> newList) {
        this.languageList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inicializar o contexto aqui
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        Language language = languageList.get(position);
        holder.languageName.setText(language.getName());

        String url = language.getFlagUrl();

        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url) // Carrega a imagem da URL
                    .placeholder(R.drawable.ic_flag) // Opcional: uma imagem padrão enquanto carrega
                    .error(R.drawable.ic_flag) // Opcional: uma imagem de erro se falhar
                    .into(holder.flagImage); // Onde a imagem será exibida
        } else {
            // Caso a URL seja nula ou vazia, mostra uma imagem padrão
            holder.flagImage.setImageResource(R.drawable.ic_flag);
        }
    }


    @Override
    public int getItemCount() {
        return languageList != null ? languageList.size() : 0;
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView flagImage;
        TextView languageName;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            flagImage = itemView.findViewById(R.id.iv_flag);
            languageName = itemView.findViewById(R.id.tv_language_name);
        }
    }
}