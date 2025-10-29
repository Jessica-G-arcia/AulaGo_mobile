package com.example.aulago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aulago.Aula;
import com.example.aulago.R;

import java.util.List;

public class AulasAdapter extends RecyclerView.Adapter<AulasAdapter.AulaViewHolder> {

    private List<Aula> aulas;

    public AulasAdapter(List<Aula> aulas) {
        this.aulas = aulas;
    }

    @NonNull
    @Override
    public AulaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aula, parent, false);
        return new AulaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AulaViewHolder holder, int position) {
        Aula aula = aulas.get(position);
        holder.bind(aula);
    }

    @Override
    public int getItemCount() {
        return aulas.size();
    }

    // Método para atualizar a lista de aulas
    public void updateAulas(List<Aula> novasAulas) {
        this.aulas = novasAulas;
        notifyDataSetChanged();
    }

    // ViewHolder
    static class AulaViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewNomeAluno;
        private TextView textViewHorario;
        private TextView textViewModalidade;
        private Button buttonAvaliar;
        private View viewIndicadorModalidade;

        public AulaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNomeAluno = itemView.findViewById(R.id.textViewNomeAluno);
            textViewHorario = itemView.findViewById(R.id.textViewHorario);
            textViewModalidade = itemView.findViewById(R.id.textViewModalidade);
            buttonAvaliar = itemView.findViewById(R.id.buttonAvaliar);
            viewIndicadorModalidade = itemView.findViewById(R.id.viewIndicadorModalidade);
        }

        public void bind(final Aula aula) {
            textViewNomeAluno.setText(aula.getNomeAluno());
            textViewHorario.setText(aula.getHorarioCompleto());
            textViewModalidade.setText(aula.getModalidade().toString());

            // Cor do indicador lateral conforme a modalidade
            int color = itemView.getResources().getColor(aula.getModalidade().getColorResId());
            viewIndicadorModalidade.setBackgroundColor(color);

            // Lógica para mostrar o botão de avaliação
            if (aula.jaOcorreu()) {
                buttonAvaliar.setVisibility(View.VISIBLE);
            } else {
                buttonAvaliar.setVisibility(View.GONE);
            }

            buttonAvaliar.setOnClickListener(v -> {
                // Aqui você pode abrir um Dialog para o usuário dar a nota e o comentário
                Toast.makeText(itemView.getContext(), "Direcionando para página de avaliação da aula de " + aula.getNomeAluno(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
