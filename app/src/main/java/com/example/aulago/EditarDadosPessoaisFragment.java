package com.example.aulago;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aulago.databinding.FragmentEditarDadosPessoaisBinding; // <-- Certifique-se que o nome do XML está correto aqui
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarDadosPessoaisFragment extends Fragment {

    private FragmentEditarDadosPessoaisBinding binding; // <-- View Binding
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usa o View Binding para inflar o layout
        binding = FragmentEditarDadosPessoaisBinding.inflate(inflater, container, false);
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

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        // Configurar o clique do botão Salvar
        binding.btnSalvar.setOnClickListener(v -> salvarAlteracoes());

        // Buscar dados do Firestore e preencher o formulário
        carregarDadosDoUsuario();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa o binding
    }

    /**
     * Busca os dados no Firestore e preenche os campos EditText.
     */
    private void carregarDadosDoUsuario() {
        progressDialog.setMessage("Carregando dados...");
        progressDialog.show();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (!isAdded() || binding == null) return;
                    progressDialog.dismiss();

                    if (document.exists()) {
                        // Dados Pessoais
                        binding.inputNome.setText(document.getString("nome"));
                        binding.inputTelefone.setText(document.getString("telefone"));
                        binding.inputCpf.setText(document.getString("cpf"));
                        binding.inputDtNasc.setText(document.getString("dataNascimento"));

                        // Endereço
                        binding.inputCep.setText(document.getString("cep"));
                        binding.inputEndereco.setText(document.getString("endereco"));
                        binding.inputNumero.setText(document.getString("numero"));
                        binding.inputComplemento.setText(document.getString("complemento"));
                        binding.inputBairro.setText(document.getString("bairro"));
                        binding.inputCidade.setText(document.getString("cidade"));
                        binding.inputEstado.setText(document.getString("estado")); // <-- CAMPO NOVO ADICIONADO

                    } else {
                        Toast.makeText(requireContext(), "Erro: Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Pega todos os dados dos campos e salva no Firestore usando .update()
     */
    private void salvarAlteracoes() {
        progressDialog.setMessage("Salvando alterações...");
        progressDialog.show();

        Map<String, Object> dadosPessoais = new HashMap<>();

        // Dados Pessoais
        dadosPessoais.put("nome", binding.inputNome.getText().toString().trim());
        dadosPessoais.put("telefone", binding.inputTelefone.getText().toString().trim());
        dadosPessoais.put("cpf", binding.inputCpf.getText().toString().trim());
        dadosPessoais.put("dataNascimento", binding.inputDtNasc.getText().toString().trim());

        // Endereço
        dadosPessoais.put("cep", binding.inputCep.getText().toString().trim());
        dadosPessoais.put("endereco", binding.inputEndereco.getText().toString().trim());
        dadosPessoais.put("numero", binding.inputNumero.getText().toString().trim());
        dadosPessoais.put("complemento", binding.inputComplemento.getText().toString().trim());
        dadosPessoais.put("bairro", binding.inputBairro.getText().toString().trim());
        dadosPessoais.put("cidade", binding.inputCidade.getText().toString().trim());
        dadosPessoais.put("estado", binding.inputEstado.getText().toString().trim()); // <-- CAMPO NOVO ADICIONADO

        db.collection("users").document(uid)
                .update(dadosPessoais)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();

                    // Simula o "voltar"
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}