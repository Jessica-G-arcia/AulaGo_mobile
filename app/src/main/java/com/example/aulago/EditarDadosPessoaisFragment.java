package com.example.aulago;



import static android.os.Build.VERSION_CODES.N;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aulago.databinding.FragmentEditarDadosPessoaisBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List; // <-- IMPORT NECESSÁRIO
import java.util.Map;

public class EditarDadosPessoaisFragment extends Fragment {

    private FragmentEditarDadosPessoaisBinding binding;
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

        binding.btnSalvar.setOnClickListener(v -> salvarAlteracoes());
        carregarDadosDoUsuario();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Busca os dados no Firestore e preenche os campos EditText.
     * ATUALIZADO: Agora checa se é professor e carrega os dados bancários.
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
                        binding.inputEstado.setText(document.getString("estado"));

                        // --- LÓGICA DE DADOS BANCÁRIOS (NOVA) ---
                        // Verifica se o usuário é professor
                        List<String> roles = (List<String>) document.get("roles");
                        if (roles != null && roles.contains("professor")) {
                            // É professor, mostra a seção
                            binding.layoutDadosBancarios.setVisibility(View.VISIBLE);

                            // Carrega os dados bancários (se existirem)
                            binding.inputBanco.setText(document.getString("banco"));
                            binding.inputAgencia.setText(document.getString("agencia"));
                            binding.inputConta.setText(document.getString("conta"));
                            binding.inputPix.setText(document.getString("pix"));
                        } else {
                            // Não é professor, esconde a seção
                            binding.layoutDadosBancarios.setVisibility(View.GONE);
                        }
                        // --- FIM DA LÓGICA ---

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
     * ATUALIZADO: Agora salva os dados bancários se a seção estiver visível.
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
        dadosPessoais.put("estado", binding.inputEstado.getText().toString().trim());

        // --- LÓGICA DE DADOS BANCÁRIOS (NOVA) ---
        // Só salva os dados bancários se a seção estiver visível
        if (binding.layoutDadosBancarios.getVisibility() == View.VISIBLE) {
            dadosPessoais.put("banco", binding.inputBanco.getText().toString().trim());
            dadosPessoais.put("agencia", binding.inputAgencia.getText().toString().trim());
            dadosPessoais.put("conta", binding.inputConta.getText().toString().trim());
            dadosPessoais.put("pix", binding.inputPix.getText().toString().trim());
        }
        // --- FIM DA LÓGICA ---

        db.collection("users").document(uid)
                .update(dadosPessoais)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();

                    // Simula o "voltar"
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();

                    // --- CORREÇÃO AQUI ---
                    // O 'N' estava fora das aspas
                    Toast.makeText(requireContext(), "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}