package com.example.aulago;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilAlunoFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String uid;

    // Views (MUDANÇA: etNome REMOVIDO e NOVOS CAMPOS ADICIONADOS)
    private EditText etBioAluno, etNivelAluno, etModalidadePreferida, etObjetivosAluno;
    private ImageView ivFotoPerfil;
    private Button btnEscolherFoto;
    private Button btnSalvarPerfil;
    private ProgressBar progressBar;
    private Button btnSolicitarProfessor;
    private Button btnMudarParaProfessor;
    private TextView tvStatusSolicitacao;

    // Lógica de Foto
    private ActivityResultLauncher<String> mGetContent;
    private Uri imageUri;
    private String currentFotoUrl;

    // ===========================
    // Ciclo de vida do Fragment
    // ===========================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = auth.getCurrentUser().getUid();

        // Inicializa seletor de imagem
        mGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        ivFotoPerfil.setImageURI(imageUri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_editar_perfil_aluno, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        btnEscolherFoto.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnSalvarPerfil.setOnClickListener(v -> salvarPerfil());


        // Exemplo de listener para o novo botão (você pode precisar implementar a lógica)
        btnSolicitarProfessor.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Lógica de Solicitação de Professor!", Toast.LENGTH_SHORT).show()
        );

        carregarDadosAtuais();
    }


    private void initViews(View view) {
        // MUDANÇA: etNome REMOVIDO. Mapeando novos IDs do XML
        etBioAluno = view.findViewById(R.id.etBioAluno);
        etNivelAluno = view.findViewById(R.id.etNivelAluno);
        etModalidadePreferida = view.findViewById(R.id.etModalidadePreferida);
        etObjetivosAluno = view.findViewById(R.id.etObjetivosAluno);
        tvStatusSolicitacao = view.findViewById(R.id.tvStatusSolicitacao);

        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);
        btnEscolherFoto = view.findViewById(R.id.btnEscolherFoto);
        btnSalvarPerfil = view.findViewById(R.id.btnSalvarPerfil);
        progressBar = view.findViewById(R.id.progressBar);
        btnSolicitarProfessor = view.findViewById(R.id.btnSolicitarProfessor); // Novo botão

    }

    private void carregarDadosAtuais() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    // Adicionada verificação de segurança
                    if (!isAdded()) return;

                    if (document.exists()) {
                        // MUDANÇA: Carregando os NOVOS campos
                        etBioAluno.setText(document.getString("bio"));
                        etNivelAluno.setText(document.getString("nivel"));
                        etModalidadePreferida.setText(document.getString("modalidadePreferida"));
                        etObjetivosAluno.setText(document.getString("objetivos"));

                        // Lógica da foto (mantida)
                        currentFotoUrl = document.getString("urlFotoPerfil");
                        if (currentFotoUrl != null && !currentFotoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(currentFotoUrl)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .into(ivFotoPerfil);
                        }

                        String userRole = document.getString("role");

                        if (userRole != null && userRole.equals("professor")) {
                            // CASO 1: USUÁRIO JÁ É PROFESSOR

                            // 1. Torna o botão visível
                            btnSolicitarProfessor.setVisibility(View.VISIBLE);

                            // 2. MUDAR O TEXTO DO BOTÃO
                            btnSolicitarProfessor.setText("Mudar para Perfil Professor");

                            // 3. MUDAR A AÇÃO (Listener de navegação)
                            btnSolicitarProfessor.setOnClickListener(v -> {
                                mudarPerfilParaProfessor(); // Chama a função de navegação
                            });

                            tvStatusSolicitacao.setVisibility(View.GONE); // Oculta o status text

                        } else {
                            // CASO 2: USUÁRIO É APENAS ALUNO

                            // 1. Torna o botão visível (se for a regra de negócio)
                            btnSolicitarProfessor.setVisibility(View.VISIBLE);

                            // 2. MANTÉM O TEXTO ORIGINAL
                            btnSolicitarProfessor.setText("Quero ser Professor");

                            // 3. MANTÉM A AÇÃO ORIGINAL (Listener de solicitação)
                            btnSolicitarProfessor.setOnClickListener(v -> {
                                // Implemente a lógica de envio de solicitação aqui, ex:
                                // enviarSolicitacaoProfessor();
                                Toast.makeText(requireContext(), "Enviando solicitação...", Toast.LENGTH_SHORT).show();
                            });

                            tvStatusSolicitacao.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) { // Verificação de segurança
                        Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void enviarSolicitacaoProfessor() {
        // Implemente aqui a lógica para marcar o usuário como "solicitante"
        // ou criar um novo registro de solicitação no Firestore.
        Toast.makeText(requireContext(), "Sua solicitação para ser professor foi enviada para análise.", Toast.LENGTH_LONG).show();

        // Exemplo: desabilitar o botão após o envio para evitar reenvio
        btnSolicitarProfessor.setEnabled(false);
    }

    private void mudarPerfilParaProfessor() {
        // Delega a tarefa de troca de Fragment para a Activity principal.
        if (getActivity() instanceof ToolbarActivity) {
            ToolbarActivity activity = (ToolbarActivity) getActivity();
            activity.replaceFragment(new ProfessorPerfilFragment());

        } else {
            Toast.makeText(requireContext(), "Erro: Não foi possível navegar para o perfil de professor.", Toast.LENGTH_SHORT).show();
        }
    }


    //
    // Salvamento de perfil
    //

    private void salvarPerfil() {
        setLoading(true);

        if (imageUri != null) {
            fazerUploadDaImagem();
        } else {
            salvarDadosNoFirestore(currentFotoUrl);
        }
    }

    private void fazerUploadDaImagem() {
        String fileName = uid + ".jpg";
        StorageReference storageRef = storage.getReference().child("fotosDePerfil/" + fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                salvarDadosNoFirestore(uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    if (isAdded()) { // Verificação de segurança
                        Toast.makeText(requireContext(),
                                "Erro no upload da foto: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
    }

    private void salvarDadosNoFirestore(String fotoUrl) {
        // MUDANÇA: Coletando os NOVOS campos, "nome" REMOVIDO
        String bio = etBioAluno.getText().toString().trim();
        String nivel = etNivelAluno.getText().toString().trim();
        String modalidade = etModalidadePreferida.getText().toString().trim();
        String objetivos = etObjetivosAluno.getText().toString().trim();

        Map<String, Object> alunoData = new HashMap<>();
        // Mapeando para os campos do Firestore (ajuste os nomes das chaves conforme seu DB)
        alunoData.put("bio", bio);
        alunoData.put("nivel", nivel);
        alunoData.put("modalidadePreferida", modalidade);
        alunoData.put("objetivos", objetivos);
        // O campo "nome" não será mais atualizado aqui.

        if (fotoUrl != null) {
            alunoData.put("urlFotoPerfil", fotoUrl);
        }

        db.collection("users").document(uid)
                .update(alunoData)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Perfil atualizado com sucesso!",
                                Toast.LENGTH_SHORT).show();

                        // Voltar para o fragment anterior
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Erro ao atualizar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSalvarPerfil.setEnabled(false);
            btnEscolherFoto.setEnabled(false); // Desabilitar escolha de foto durante o load
        } else {
            progressBar.setVisibility(View.GONE);
            btnSalvarPerfil.setEnabled(true);
            btnEscolherFoto.setEnabled(true);
        }
    }
}