package com.example.aulago;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aulago.databinding.FragmentAvaliacaoBinding; // Importante: o nome pode variar
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class AvaliacaoFragment extends Fragment {

    // Argumento para receber o ID do usuário que será avaliado
    private static final String ARG_AVALIADO_ID = "avaliado_id";

    private FragmentAvaliacaoBinding binding; // Objeto de View Binding
    private RatingManager ratingManager;
    private String idDoUsuarioAvaliado;

    /**
     * Método estático para criar uma nova instância do fragmento, passando o ID do usuário a ser avaliado.
     * Esta é a forma correta de passar argumentos para um Fragment.
     */
    public static AvaliacaoFragment newInstance(String avaliadoId) {
        AvaliacaoFragment fragment = new AvaliacaoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AVALIADO_ID, avaliadoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pega o ID passado como argumento
        if (getArguments() != null) {
            idDoUsuarioAvaliado = getArguments().getString(ARG_AVALIADO_ID);
        }
        ratingManager = new RatingManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout usando View Binding
        binding = FragmentAvaliacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configura o listener do botão
        binding.btnEnviarAvaliacao.setOnClickListener(v -> {
            submeterAvaliacao();
        });
    }

    private void submeterAvaliacao() {
        // Validações
        if (idDoUsuarioAvaliado == null || idDoUsuarioAvaliado.isEmpty()) {
            Toast.makeText(getContext(), "Erro: ID do usuário a ser avaliado não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String idDoAvaliador = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        double nota = binding.ratingBarNota.getRating();
        String comentario = binding.etComentario.getText().toString();

        if (nota == 0) {
            Toast.makeText(getContext(), "Por favor, selecione uma nota.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostra um indicador de loading (opcional, mas recomendado)
        binding.btnEnviarAvaliacao.setEnabled(false);
        binding.btnEnviarAvaliacao.setText("Enviando...");

        // Chama o RatingManager
        ratingManager.submitRating(idDoUsuarioAvaliado, idDoAvaliador, nota, comentario, new RatingManager.RatingCallback() {
            @Override
            public void onSuccess() {
                if (getContext() == null) return; // Evita crash se o fragment não estiver mais visível
                
                Toast.makeText(getContext(), "Avaliação enviada com sucesso!", Toast.LENGTH_SHORT).show();
                // Fecha o fragment e volta para a tela anterior
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onError(Exception e) {
                if (getContext() == null) return;

                Toast.makeText(getContext(), "Erro ao enviar avaliação: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // Reabilita o botão em caso de erro
                binding.btnEnviarAvaliacao.setEnabled(true);
                binding.btnEnviarAvaliacao.setText("Enviar Avaliação");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpa a referência ao binding para evitar memory leaks
        binding = null;
    }
}