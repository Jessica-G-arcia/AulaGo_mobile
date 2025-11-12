package com.example.aulago;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<Language> languageList;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        Language language = languageList.get(position);
        holder.languageName.setText(language.getName());
        String flagRef = language.getFlagRef();
        int flagId = getResourceId(holder.itemView.getContext(), flagRef);
        if (flagId != 0) { // Se encontrou o drawable
            holder.flagImage.setImageResource(flagId);
        }
    }

    private int getResourceId(Context context, String name) {
        if (name == null) return 0;
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    @Override
    public int getItemCount() {
        return languageList.size();
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