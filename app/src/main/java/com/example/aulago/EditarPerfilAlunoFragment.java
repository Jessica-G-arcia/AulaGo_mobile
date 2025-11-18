package com.example.aulago;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// View Binding (Essencial)
import com.example.aulago.databinding.FragmentEditarPerfilAlunoBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilAlunoFragment extends Fragment {

    private FragmentEditarPerfilAlunoBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String uid;

    // Lançador para buscar foto
    private ActivityResultLauncher<String> fotoPickerLauncher;
    private Uri fotoUriSelecionada;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializações básicas do Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Registro do seletor de fotos
        fotoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoUriSelecionada = uri;
                        binding.ivFotoPerfil.setImageURI(uri);
                        // Opcional: Fazer upload imediato ou esperar clicar em salvar.
                        // Aqui optei por fazer upload imediato para dar feedback rápido
                        uploadFotoParaFirebaseStorage(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usando View Binding (Melhor prática do Código A)
        binding = FragmentEditarPerfilAlunoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            requireActivity().finish();
            return;
        }
        uid = user.getUid();

        configurarListeners();
        carregarDadosDoUsuario();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evita Memory Leaks
    }

    private void configurarListeners() {
        binding.btnSalvarPerfil.setOnClickListener(v -> salvarPerfilPublico());
        binding.btnEscolherFoto.setOnClickListener(v -> fotoPickerLauncher.launch("image/*"));

        // A ação deste botão será dinâmica baseada no status do usuário (definido em carregarDados)
        binding.btnSolicitarProfessor.setOnClickListener(v -> navegarSolicitacaoProfessor());
    }

    private void navegarSolicitacaoProfessor() {
        if (getActivity() instanceof ToolbarActivity) {
            // Se o texto for "Mudar para Perfil Professor", levamos para o perfil
            if (binding.btnSolicitarProfessor.getText().toString().contains("Mudar")) {
                ((ToolbarActivity) getActivity()).replaceFragment(new ProfessorPerfilFragment());
            } else {
                // Senão, levamos para a solicitação
                ((ToolbarActivity) getActivity()).replaceFragment(new SolicitarSerProfessorFragment());
            }
        }
    }

    private void carregarDadosDoUsuario() {
        setLoading(true); // Usa lógica do Código B (ProgressBar)

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || binding == null) return;
                    setLoading(false);

                    if (document.exists()) {
                        // Carregando campos (União do Código A e B)
                        binding.etBioAluno.setText(document.getString("bio"));
                        binding.etNivelAluno.setText(document.getString("nivel"));
                        binding.etModalidadePreferida.setText(document.getString("preferenciaModalidade")); // Verifique se no banco é "preferenciaModalidade" ou "modalidadePreferida"
                        binding.etObjetivosAluno.setText(document.getString("objetivos"));
                        binding.etIdiomaAluno.setText(document.getString("idioma"));

                        // Foto
                        String urlFotoPerfil = document.getString("urlFotoPerfil");
                        if (urlFotoPerfil != null && !urlFotoPerfil.isEmpty()) {
                            Glide.with(this)
                                    .load(urlFotoPerfil)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .into(binding.ivFotoPerfil);
                        }

                        // Lógica Híbrida Inteligente de Status
                        String status = document.getString("statusSolicitacao");
                        String role = document.getString("role");

                        controlarStatusEBotao(status, role);

                    } else {
                        Toast.makeText(requireContext(), "Erro: Documento não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Fusão da lógica de Status (A) com Role (B)
    private void controlarStatusEBotao(String status, String role) {
        if (status == null) status = "nenhum";

        // Prioridade 1: Se o usuário já é professor (Lógica B)
        if ("professor".equals(role) || "aprovado".equals(status)) {
            binding.tvStatusSolicitacao.setText("Status: Professor Aprovado");
            binding.tvStatusSolicitacao.setVisibility(View.VISIBLE);

            binding.btnSolicitarProfessor.setText("Mudar para Perfil Professor");
            binding.btnSolicitarProfessor.setVisibility(View.VISIBLE);
            return;
        }

        // Prioridade 2: Status da solicitação (Lógica A)
        switch (status) {
            case "pendente_analise":
                binding.tvStatusSolicitacao.setText("Status: Em Análise");
                binding.tvStatusSolicitacao.setVisibility(View.VISIBLE);
                binding.btnSolicitarProfessor.setVisibility(View.GONE); // Esconde botão para não reenviar
                break;

            case "rejeitado":
                binding.tvStatusSolicitacao.setText("Status: Solicitação Rejeitada");
                binding.tvStatusSolicitacao.setVisibility(View.VISIBLE);
                binding.btnSolicitarProfessor.setText("Reenviar Solicitação");
                binding.btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;

            case "nenhum":
            default:
                binding.tvStatusSolicitacao.setVisibility(View.GONE);
                binding.btnSolicitarProfessor.setText("Quero ser Professor");
                binding.btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void salvarPerfilPublico() {
        setLoading(true);

        Map<String, Object> perfilPublico = new HashMap<>();
        perfilPublico.put("bio", binding.etBioAluno.getText().toString().trim());
        perfilPublico.put("nivel", binding.etNivelAluno.getText().toString().trim());
        // Atenção aqui: padronize a chave do banco. Usei 'preferenciaModalidade' baseado no Codigo A
        perfilPublico.put("preferenciaModalidade", binding.etModalidadePreferida.getText().toString().trim());
        perfilPublico.put("objetivos", binding.etObjetivosAluno.getText().toString().trim());
        perfilPublico.put("idioma", binding.etIdiomaAluno.getText().toString().trim());

        db.collection("users").document(uid)
                .update(perfilPublico)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded() || binding == null) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    // Opcional: Voltar tela
                    // requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadFotoParaFirebaseStorage(Uri uri) {
        setLoading(true);

        StorageReference fotoRef = storage.getReference().child("fotos_perfil").child(uid + "_perfil.jpg");
        fotoRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                    // Atualiza URL no Firestore
                    db.collection("users").document(uid)
                            .update("urlFotoPerfil", downloadUri.toString())
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded() || binding == null) return;
                                setLoading(false);
                                Toast.makeText(requireContext(), "Foto atualizada!", Toast.LENGTH_SHORT).show();

                                Glide.with(this)
                                        .load(downloadUri)
                                        .placeholder(R.drawable.img_avatar_circle)
                                        .into(binding.ivFotoPerfil);
                            });
                }))
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), "Erro ao enviar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper para controlar Loading (Lógica B adaptada para ViewBinding)
    private void setLoading(boolean isLoading) {
        if (binding == null) return;

        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE); // Certifique-se de ter um ProgressBar no XML com id 'progressBar'
            binding.btnSalvarPerfil.setEnabled(false);
            binding.btnEscolherFoto.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSalvarPerfil.setEnabled(true);
            binding.btnEscolherFoto.setEnabled(true);
        }
    }
}