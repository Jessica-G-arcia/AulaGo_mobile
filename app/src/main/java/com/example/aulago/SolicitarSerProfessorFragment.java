package com.example.aulago;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aulago.databinding.FragmentSolicitarSerProfessorBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SolicitarSerProfessorFragment extends Fragment {

    private FragmentSolicitarSerProfessorBinding binding;
    private ActivityResultLauncher<String[]> documentPickerLauncher;

    private Uri documentoUri;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    // RecyclerView e Adapter para lista de certificados
    private CertificadosAdapter adapter;
    private List<CertificadoModel> listaCertificados = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        documentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        documentoUri = uri;
                        requireContext().getContentResolver().takePersistableUriPermission(
                                documentoUri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        binding.tvDocumentoSelecionado.setText("Documento selecionado!");
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSolicitarSerProfessorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        setupSpinner();
        configurarListeners();

        // Configura o RecyclerView
        adapter = new CertificadosAdapter(listaCertificados);
        binding.recyclerViewCertificados.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCertificados.setAdapter(adapter);

        // Carrega a lista de certificados enviados
        carregarTodosCertificados();

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
    }

    private void configurarListeners() {
        binding.btnSelecionarDocumento.setOnClickListener(v -> selecionarDocumento());
        binding.btnEnviar.setOnClickListener(v -> validarEEnviar());
    }

    private void setupSpinner() {
        String[] certificacoes = {"Selecione", "TOEFL", "IELTS", "Cambridge CAE", "CELTA"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, certificacoes);
        binding.spinnerCertificacao.setAdapter(adapter);

        binding.spinnerCertificacao.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (selected.equals("TOEFL") || selected.equals("IELTS")) {
                binding.layoutPontuacao.setVisibility(View.VISIBLE);
            } else {
                binding.layoutPontuacao.setVisibility(View.GONE);
            }
        });
    }

    private void selecionarDocumento() {
        String[] mimeTypes = {"application/pdf", "image/jpeg", "image/png"};
        documentPickerLauncher.launch(mimeTypes);
    }

    private void validarEEnviar() {
        String nome = binding.etNomeCompleto.getText().toString().trim();
        String tipoCert = binding.spinnerCertificacao.getText().toString().trim();
        String numCert = binding.etNumeroCertificado.getText().toString().trim();
        String instituicao = binding.etInstituicao.getText().toString().trim();
        String pontuacao = binding.etPontuacao.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha seu nome completo.", Toast.LENGTH_SHORT).show();
            binding.etNomeCompleto.requestFocus();
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
        if (binding.layoutPontuacao.getVisibility() == View.VISIBLE && pontuacao.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha a pontuação (TOEFL/IELTS).", Toast.LENGTH_SHORT).show();
            binding.etPontuacao.requestFocus();
            return;
        }
        if (documentoUri == null) {
            Toast.makeText(requireContext(), "Selecione o documento do certificado", Toast.LENGTH_SHORT).show();
            return;
        }
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

        docRef.putFile(documentoUri)
                .addOnSuccessListener(taskSnapshot -> docRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    salvarSolicitacaoFirestore(uid, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao enviar documento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Salva cada certificado como documento individual
    private void salvarSolicitacaoFirestore(String uid, String docUrl) {
        progressDialog.setMessage("Registrando solicitação...");

        Map<String, Object> solicitacao = new HashMap<>();
        solicitacao.put("tipoCertificacao", binding.spinnerCertificacao.getText().toString());
        solicitacao.put("numeroCertificado", binding.etNumeroCertificado.getText().toString().trim());
        solicitacao.put("instituicaoCertificacao", binding.etInstituicao.getText().toString().trim());
        solicitacao.put("nomeCompletoCertificado", binding.etNomeCompleto.getText().toString().trim());
        solicitacao.put("certificadoUrl", docUrl);
        solicitacao.put("dataSolicitacao", FieldValue.serverTimestamp());
        solicitacao.put("pontuacaoCertificado", binding.etPontuacao.getText().toString().trim());
        solicitacao.put("statusSolicitacao", "pendente_analise");
        solicitacao.put("professorVerificado", false);

        db.collection("users").document(uid)
                .collection("certificados")
                .add(solicitacao)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    mostrarDialogoSucesso();
                    carregarTodosCertificados(); // Recarrega lista
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao salvar solicitação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogoSucesso() {
        new AlertDialog.Builder(requireContext())
                .setTitle("✓ Solicitação Enviada!")
                .setMessage("Sua solicitação será analisada pela equipe.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Pode limpar campos aqui se desejar
                })
                .setCancelable(false)
                .show();
    }

    // Carrega todos os certificados enviados pelo professor
    private void carregarTodosCertificados() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = currentUser.getUid();
        db.collection("users").document(uid).collection("certificados")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaCertificados.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tipo = doc.getString("tipoCertificacao");
                        String emissora = doc.getString("instituicaoCertificacao");
                        listaCertificados.add(new CertificadoModel(tipo, emissora));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Erro ao buscar certificados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Modelo simples do certificado
    static class CertificadoModel {
        String tipo;
        String emissora;
        CertificadoModel(String tipo, String emissora) {
            this.tipo = tipo;
            this.emissora = emissora;
        }
    }

    // Adapter para RecyclerView
    static class CertificadosAdapter extends RecyclerView.Adapter<CertificadosAdapter.CertificadoViewHolder> {
        private final List<CertificadoModel> lista;

        CertificadosAdapter(List<CertificadoModel> lista) {
            this.lista = lista;
        }

        @NonNull
        @Override
        public CertificadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new CertificadoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CertificadoViewHolder holder, int position) {
            CertificadoModel cert = lista.get(position);
            ((android.widget.TextView) holder.itemView.findViewById(android.R.id.text1)).setText(cert.tipo);
            ((android.widget.TextView) holder.itemView.findViewById(android.R.id.text2)).setText("Emissora: " + cert.emissora);
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }

        static class CertificadoViewHolder extends RecyclerView.ViewHolder {
            CertificadoViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
