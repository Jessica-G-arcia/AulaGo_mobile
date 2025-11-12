package com.example.aulago;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// IMPORT ADICIONADO
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AlunoPerfilFragment extends Fragment {

    // Views do XML
    private TextView tvNomeAluno, tvStatusSolicitacao;
    private TextView tvNivelAluno, tvModalidadeAluno, tvObjetivosAluno;
    private Button btnEditarPerfilAluno;
    private ImageView ivAvatarAluno; // <-- Estava no seu XML
    private TabLayout tabLayoutAluno;
    private LinearLayout groupBioAluno, groupAvaliacoesAluno;
    private TextView tvBioAluno;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aluno_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUser == null) {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
            return;
        }

        initViews(view);
        configurarListeners(); // <-- Este método foi corrigido
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarDadosAluno(); // <-- Este método foi atualizado
    }

    private void initViews(View view) {
        tvNomeAluno = view.findViewById(R.id.tvNomeAluno);
        tvStatusSolicitacao = view.findViewById(R.id.tvStatusSolicitacao);
        btnEditarPerfilAluno = view.findViewById(R.id.btnEditar);
        ivAvatarAluno = view.findViewById(R.id.ivAvatarAluno); // <-- Encontrando o avatar

        tabLayoutAluno = view.findViewById(R.id.tabLayoutAluno);
        groupBioAluno = view.findViewById(R.id.groupBioAluno);
        groupAvaliacoesAluno = view.findViewById(R.id.groupAvaliacoesAluno);
        tvBioAluno = view.findViewById(R.id.tvBioAluno);

        tvNivelAluno = view.findViewById(R.id.tvNivelAluno);
        tvModalidadeAluno = view.findViewById(R.id.tvModalidadeAluno);
        tvObjetivosAluno = view.findViewById(R.id.tvObjetivosAluno);
    }

    /**
     * CORRIGIDO: Este método agora pede à ToolbarActivity para trocar o fragmento,
     * em vez de tentar iniciar uma Activity (o que causava o crash).
     */
    private void configurarListeners() {
        // Proteção: se a view do botão não foi encontrada, avisa e não tenta usar.
        if (btnEditarPerfilAluno == null) {
            Toast.makeText(requireContext(), "Botão de editar não encontrado no layout (id inválido).", Toast.LENGTH_LONG).show();
            android.util.Log.e("AlunoPerfilFragment", "btnEditarPerfilAluno == null - verifique R.id.btnEditar no XML fragment_aluno_perfil.xml");
            return;
        }

        btnEditarPerfilAluno.setOnClickListener(v -> {
            try {
                // Preferência: usar método da ToolbarActivity se disponível
                if (getActivity() instanceof ToolbarActivity) {
                    ((ToolbarActivity) getActivity()).replaceFragment(new EditarPerfilAlunoFragment());
                    return;
                }

                // Fallback: faz a transação diretamente no FragmentManager.
                // Usa android.R.id.content como container padrão (root view da Activity).
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, new EditarPerfilAlunoFragment())
                        .addToBackStack(null)
                        .commit();
            } catch (Exception e) {
                // Se acontecer qualquer erro, loga e mostra um Toast para o usuário
                android.util.Log.e("AlunoPerfilFragment", "Erro ao abrir EditarPerfilAlunoFragment", e);
                Toast.makeText(requireContext(), "Não foi possível abrir a tela de edição: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * ATUALIZADO: Agora também carrega a foto do avatar.
     */
    private void carregarDadosAluno() {
        String uid = currentUser.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Checagem de segurança
                    if (!isAdded()) return;

                    if (documentSnapshot.exists()) {
                        // Preenche o nome
                        tvNomeAluno.setText(documentSnapshot.getString("nome"));

                        // ATUALIZAÇÃO: Carrega a foto do perfil
                        String fotoUrl = documentSnapshot.getString("urlFotoPerfil");
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .error(R.drawable.img_avatar_circle)
                                    .into(ivAvatarAluno);
                        } else {
                            ivAvatarAluno.setImageResource(R.drawable.img_avatar_circle);
                        }

                        // Carrega a Bio
                        String bio = documentSnapshot.getString("bio");
                        if (bio != null && !bio.isEmpty()) {
                            tvBioAluno.setText(bio);
                        } else {
                            tvBioAluno.setText("O aluno ainda não escreveu uma bio.");
                        }

                        // Carrega Nível
                        String nivel = documentSnapshot.getString("nivel");
                        if (nivel != null && !nivel.isEmpty()) {
                            tvNivelAluno.setText("Nível: " + nivel);
                            tvNivelAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvNivelAluno.setVisibility(View.GONE);
                        }

                        // Carrega Modalidade
                        String modalidade = documentSnapshot.getString("preferenciaModalidade");
                        if (modalidade != null && !modalidade.isEmpty()) {
                            tvModalidadeAluno.setText("Modalidade: " + modalidade);
                            tvModalidadeAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvModalidadeAluno.setVisibility(View.GONE);
                        }

                        // Carrega Objetivos
                        String objetivos = documentSnapshot.getString("objetivos");
                        if (objetivos != null && !objetivos.isEmpty()) {
                            tvObjetivosAluno.setText("Objetivos: " + objetivos);
                            tvObjetivosAluno.setVisibility(View.VISIBLE);
                        } else {
                            tvObjetivosAluno.setVisibility(View.GONE);
                        }

                        // Lógica do status
                        String status = documentSnapshot.getString("statusSolicitacao");
                        controlarStatusSolicitacao(status);

                    } else {
                        Toast.makeText(requireContext(), "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void controlarStatusSolicitacao(String status) {
        if (status == null) status = "nenhum";

        switch (status) {
            case "pendente_analise":
                tvStatusSolicitacao.setText("Solicitação: Em Análise");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "rejeitado":
                tvStatusSolicitacao.setText("Solicitação: Rejeitada");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "aprovado":
                tvStatusSolicitacao.setText("Status: Professor Aprovado");
                tvStatusSolicitacao.setVisibility(View.VISIBLE);
                break;
            case "nenhum":
            default:
                tvStatusSolicitacao.setVisibility(View.GONE);
                break;
        }
    }
}