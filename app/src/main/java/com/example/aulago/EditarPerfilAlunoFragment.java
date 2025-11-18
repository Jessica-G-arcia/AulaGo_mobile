package com.example.aulago;

import android.app.ProgressDialog;
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
import androidx.fragment.app.Fragment; // MUDOU

// IMPORTANTE: View Binding
import com.example.aulago.databinding.FragmentEditarPerfilAlunoBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilAlunoFragment extends Fragment { // MUDOU

    private FragmentEditarPerfilAlunoBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String uid;

    // Lançador para buscar foto da galeria
    private ActivityResultLauncher<String> fotoPickerLauncher;
    private Uri fotoUriSelecionada;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        fotoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoUriSelecionada = uri;
                        binding.ivFotoPerfil.setImageURI(uri);
                        uploadFotoParaFirebaseStorage(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 3. Infla o layout com View Binding
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

        // Inicializa o ProgressDialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        // Configura os cliques
        configurarListeners();

        // Carrega os dados
        carregarDadosDoUsuario();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa o binding
    }

    private void configurarListeners() {
        binding.btnSalvarPerfil.setOnClickListener(v -> salvarPerfilPublico());
        binding.btnEscolherFoto.setOnClickListener(v -> fotoPickerLauncher.launch("image/*"));
        binding.btnSolicitarProfessor.setOnClickListener(v -> {
            // Pede para a Activity "pai" (ToolbarActivity) fazer a troca
            if (getActivity() instanceof ToolbarActivity) {
                ((ToolbarActivity) getActivity()).replaceFragment(new SolicitarSerProfessorFragment());
            }
        });
    }

    private void carregarDadosDoUsuario() {
        progressDialog.setMessage("Carregando dados...");
        progressDialog.show();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || binding == null) return;
                    progressDialog.dismiss();

                    if (document.exists()) {
                        binding.etBioAluno.setText(document.getString("bio"));
                        binding.etNivelAluno.setText(document.getString("nivel"));
                        binding.etModalidadePreferida.setText(document.getString("preferenciaModalidade"));
                        binding.etObjetivosAluno.setText(document.getString("objetivos"));
                        binding.etIdiomaAluno.setText(document.getString("idioma"));

                        // Exibir foto de perfil, se tiver
                        String urlFotoPerfil = document.getString("urlFotoPerfil");
                        if (urlFotoPerfil != null && !urlFotoPerfil.isEmpty()) {
                            Glide.with(this)
                                    .load(urlFotoPerfil)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .into(binding.ivFotoPerfil);
                        } else {
                            binding.ivFotoPerfil.setImageResource(R.drawable.img_avatar_circle);
                        }

                        String status = document.getString("statusSolicitacao");
                        controlarStatusProfessor(status);

                    } else {
                        Toast.makeText(requireContext(), "Erro: Documento não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void controlarStatusProfessor(String status) {
        if (status == null) status = "nenhum";
        switch (status) {
            case "pendente_analise":
                binding.tvStatusSolicitacao.setText("Status: Em Análise");
                binding.tvStatusSolicitacao.setVisibility(View.VISIBLE);
                binding.btnSolicitarProfessor.setVisibility(View.GONE);
                break;
            case "aprovado":
                binding.tvStatusSolicitacao.setText("Status: Professor Aprovado");
                binding.tvStatusSolicitacao.setVisibility(View.VISIBLE);
                binding.btnSolicitarProfessor.setVisibility(View.GONE);
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
                binding.btnSolicitarProfessor.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void salvarPerfilPublico() {
        progressDialog.setMessage("Salvando perfil...");
        progressDialog.show();

        Map<String, Object> perfilPublico = new HashMap<>();
        perfilPublico.put("bio", binding.etBioAluno.getText().toString().trim());
        perfilPublico.put("nivel", binding.etNivelAluno.getText().toString().trim());
        perfilPublico.put("preferenciaModalidade", binding.etModalidadePreferida.getText().toString().trim());
        perfilPublico.put("objetivos", binding.etObjetivosAluno.getText().toString().trim());
        perfilPublico.put("idioma", binding.etIdiomaAluno.getText().toString().trim());

        db.collection("users").document(uid)
                .update(perfilPublico)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded() || binding == null) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Perfil público salvo!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Upload da foto para Storage e salvando URL no Firestore
    private void uploadFotoParaFirebaseStorage(Uri uri) {
        progressDialog.setMessage("Enviando foto...");
        progressDialog.show();

        StorageReference fotoRef = storage.getReference().child("fotos_perfil").child(uid + "_perfil.jpg");
        fotoRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    db.collection("users").document(uid)
                            .update("urlFotoPerfil", downloadUri.toString())
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded() || binding == null) return;
                                progressDialog.dismiss();
                                Toast.makeText(requireContext(), "Foto alterada!", Toast.LENGTH_SHORT).show();
                                // Atualiza a imagem após upload
                                Glide.with(this)
                                        .load(downloadUri)
                                        .placeholder(R.drawable.img_avatar_circle)
                                        .into(binding.ivFotoPerfil);
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded() || binding == null) return;
                                progressDialog.dismiss();
                                Toast.makeText(requireContext(), "Erro ao salvar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao enviar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}