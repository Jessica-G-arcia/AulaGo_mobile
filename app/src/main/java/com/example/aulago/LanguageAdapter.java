package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<Language> languageList;
    private Context context;
    private OnFlagClickListener flagClickListener;

    // Interface para o clique na bandeira
    public interface OnFlagClickListener {
        void onFlagClick(Language language);
    }

    public void setOnFlagClickListener(OnFlagClickListener listener) {
        this.flagClickListener = listener;
    }

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
                    .load(url)
                    .placeholder(R.drawable.ic_flag)
                    .error(R.drawable.ic_flag)
                    .into(holder.flagImage);
        } else {
            holder.flagImage.setImageResource(R.drawable.ic_flag);
        }

        // Opacidade: inglês destacado, outros apagados
        holder.flagImage.setAlpha(language.getName().equalsIgnoreCase("Inglês") ? 1.0f : 0.3f);

        // Clique na bandeira
        holder.itemView.setOnClickListener(v -> {
            if (flagClickListener != null) {
                flagClickListener.onFlagClick(language);
            }
        });
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
