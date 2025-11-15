package com.example.aulago;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment; // MUDOU

import com.example.aulago.databinding.FragmentSolicitarSerProfessorBinding; // IMPORTANTE: View Binding
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolicitarSerProfessorFragment extends Fragment { // MUDOU

    // 1. View Binding
    private FragmentSolicitarSerProfessorBinding binding;

    // 2. IMPORTANTE: O novo 'Lançador' de Activity (substitui onActivityResult)
    private ActivityResultLauncher<String[]> documentPickerLauncher;

    private Uri documentoUri;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    // 3. onCreate (Para inicializar dados e o Lançador)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // 4. Inicializa o Lançador
        // Isso registra o que fazer quando o seletor de arquivos retornar
        documentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), // Contrato para abrir um documento
                uri -> {
                    // Este é o 'callback', o que seu 'onActivityResult' fazia
                    if (uri != null) {
                        documentoUri = uri;
                        try {
                            // "Pega" a permissão para o URI
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    documentoUri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            binding.tvDocumentoSelecionado.setText("Documento selecionado!");
                        } catch (SecurityException e) {
                            Log.e("SolicitarProfessor", "Falha ao pegar permissão persistente.", e);
                        }
                    }
                }
        );
    }

    // 5. onCreateView (Para inflar o XML)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSolicitarSerProfessorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // 6. onViewCreated (Para configurar views e cliques)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MUDOU: Usa requireContext()
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        setupSpinner();
        configurarListeners();
    }

    // 7. onDestroyView (Limpa o binding)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void configurarListeners() {
        // MUDOU: Usa 'binding'
        binding.btnSelecionarDocumento.setOnClickListener(v -> selecionarDocumento());
        binding.btnEnviar.setOnClickListener(v -> validarEEnviar());
    }

    private void setupSpinner() {
        String[] certificacoes = {"Selecione", "TOEFL", "IELTS", "Cambridge CAE", "CELTA"};
        // MUDOU: Usa requireContext()
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, certificacoes);
        binding.spinnerCertificacao.setAdapter(adapter);

        // **FIX:** O AutoCompleteTextView usa 'setOnItemClickListener', não 'OnItemSelectedListener'
        binding.spinnerCertificacao.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (selected.equals("TOEFL") || selected.equals("IELTS")) {
                binding.layoutPontuacao.setVisibility(View.VISIBLE);
            } else {
                binding.layoutPontuacao.setVisibility(View.GONE);
            }
        });
    }

    // 8. MÉTODO DE SELEÇÃO ATUALIZADO
    private void selecionarDocumento() {
        String[] mimeTypes = {"application/pdf", "image/jpeg", "image/png"};
        // Usa o 'Lançador' que criamos no onCreate
        documentPickerLauncher.launch(mimeTypes);
    }

    // 9. onActivityResult (REMOVIDO!)
    // Não precisamos mais dele, pois o 'documentPickerLauncher' faz todo o trabalho.

    private void validarEEnviar() {
        // 1. Pega todos os dados
        String nome = binding.etNomeCompleto.getText().toString().trim();
        String tipoCert = binding.spinnerCertificacao.getText().toString().trim();
        String numCert = binding.etNumeroCertificado.getText().toString().trim();
        String instituicao = binding.etInstituicao.getText().toString().trim();
        String pontuacao = binding.etPontuacao.getText().toString().trim();

        // 2. Valida os campos obrigatórios
        if (nome.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha seu nome completo.", Toast.LENGTH_SHORT).show();
            binding.etNomeCompleto.requestFocus(); // Pede foco no campo
            return;
        }

        if (tipoCert.isEmpty() || tipoCert.equals("Selecione")) {
            Toast.makeText(requireContext(), "Selecione o tipo de certificação.", Toast.LENGTH_SHORT).show();
            binding.spinnerCertificacao.requestFocus();
            return;
        }

        if (numCert.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha o número do certificado.", Toast.LENGTH_SHORT).show();
            binding.etNumeroCertificado.requestFocus();
            return;
        }

        if (instituicao.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha a instituição emissora.", Toast.LENGTH_SHORT).show();
            binding.etInstituicao.requestFocus();
            return;
        }

        // 3. Valida a pontuação (se visível)
        if (binding.layoutPontuacao.getVisibility() == View.VISIBLE && pontuacao.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha a pontuação (TOEFL/IELTS).", Toast.LENGTH_SHORT).show();
            binding.etPontuacao.requestFocus();
            return;
        }

        // 4. Valida o documento
        if (documentoUri == null) {
            Toast.makeText(requireContext(), "Selecione o documento do certificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Se tudo estiver OK, envia
        enviarDocumentoEAtualizarFirestore();
    }

    private void enviarDocumentoEAtualizarFirestore() {
        progressDialog.setMessage("Enviando documento...");
        progressDialog.show();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Erro: Usuário não está logado.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = currentUser.getUid();
        String nomeArquivo = "certificado_" + uid + "_" + UUID.randomUUID().toString();
        StorageReference docRef = storage.getReference().child("certificados_professores/" + uid + "/" + nomeArquivo);

        // 1. Upload do Documento
        docRef.putFile(documentoUri)
                .addOnSuccessListener(taskSnapshot -> docRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // 2. URL obtida, salvar no Firestore
                    salvarSolicitacaoFirestore(uid, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("UploadErro", "Erro completo: ", e);
                    Toast.makeText(requireContext(), "Erro ao enviar documento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void salvarSolicitacaoFirestore(String uid, String docUrl) {
        progressDialog.setMessage("Registrando solicitação...");

        Map<String, Object> solicitacao = new HashMap<>();
        // MUDOU: Usa 'binding'
        solicitacao.put("tipoCertificacao", binding.spinnerCertificacao.getText().toString());
        solicitacao.put("numeroCertificado", binding.etNumeroCertificado.getText().toString().trim());
        solicitacao.put("instituicaoCertificacao", binding.etInstituicao.getText().toString().trim());
        solicitacao.put("nomeCompletoCertificado", binding.etNomeCompleto.getText().toString().trim());
        solicitacao.put("certificadoUrl", docUrl);
        solicitacao.put("dataSolicitacao", FieldValue.serverTimestamp());
        solicitacao.put("pontuacaoCertificado", binding.etPontuacao.getText().toString().trim());
        solicitacao.put("statusSolicitacao", "pendente_analise");
        solicitacao.put("motivoRejeicao", FieldValue.delete());
        solicitacao.put("professorVerificado", false);

        db.collection("users").document(uid)
                .set(solicitacao, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    mostrarDialogoSucesso();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao salvar solicitação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogoSucesso() {
        // MUDOU: Usa requireContext() e requireActivity().onBackPressed()
        new AlertDialog.Builder(requireContext())
                .setTitle("✓ Solicitação Enviada!")
                .setMessage("Sua solicitação será analisada pela equipe.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Em vez de finish(), voltamos da pilha de fragmentos
                    requireActivity().onBackPressed();
                })
                .setCancelable(false)
                .show();
    }
}