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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // <-- MUDOU

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditarPerfilProfessorFragment extends Fragment { // <-- MUDOU

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private String uid;

    // Views
    private EditText etEspecialidade, etIdioma, etModalidade, etValorPresencial, etValorOnline, etBio;
    private ImageView ivFotoPerfil;
    private Button btnEscolherFoto;
    private Button btnSalvarPerfil;
    private Button btnSolicitarProfessor;
    private ProgressBar progressBar;

    // Lógica de Foto
    private ActivityResultLauncher<String> mGetContent;
    private Uri imageUri;
    private String currentUrlFotoPerfil;

    // 'onCreate' do Fragmento: para inicializar dados não-visuais
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicialize o Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = auth.getCurrentUser().getUid();

        // MUDOU: O 'registerForActivityResult' deve ser chamado no 'onCreate' do Fragmento
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

    // 'onCreateView': para carregar o XML
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Carrega o seu ficheiro XML renomeado
        return inflater.inflate(R.layout.fragment_editar_perfil_professor, container, false);
    }

    // 'onViewCreated': Onde toda a lógica do 'onCreate' da Activity vai
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Encontre os Views (agora precisa de 'view.')
        initViews(view);

        // Configure o clique do botão
        btnSalvarPerfil.setOnClickListener(v -> salvarPerfil());

        btnEscolherFoto.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        btnSolicitarProfessor.setOnClickListener(v -> {
            if (getActivity() instanceof ToolbarActivity) {
                ((ToolbarActivity) getActivity()).replaceFragment(new EditarPerfilAlunoFragment());
            }
        });

        // Carregue os dados atuais do professor para preencher os campos
        carregarDadosAtuais();
    }

    // MUDOU: Este método agora precisa de 'view'
    private void initViews(View view) {
        etIdioma = view.findViewById(R.id.etIdioma);
        etEspecialidade = view.findViewById(R.id.etEspecialidade);
        etModalidade = view.findViewById(R.id.etModalidade);
        etValorPresencial = view.findViewById(R.id.etValorPresencial);
        etValorOnline = view.findViewById(R.id.etValorOnline);
        etBio = view.findViewById(R.id.etBio);
        btnSalvarPerfil = view.findViewById(R.id.btnSalvarPerfil);
        progressBar = view.findViewById(R.id.progressBar);
        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);
        btnEscolherFoto = view.findViewById(R.id.btnEscolherFoto);
        btnSolicitarProfessor = view.findViewById(R.id.btnSolicitarProfessor);
    }

    private void carregarDadosAtuais() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Preenche os campos de texto
                        etIdioma.setText(document.getString("idioma"));
                        etBio.setText(document.getString("bio"));
                        etEspecialidade.setText(document.getString("especialidade"));

                        etModalidade.setText(document.getString("preferenciaModalidade"));

                        if (document.getDouble("valorPresencial") != null) {
                            etValorPresencial.setText(String.valueOf(document.getDouble("valorPresencial")));
                        }
                        if (document.getDouble("valorOnline") != null) {
                            etValorOnline.setText(String.valueOf(document.getDouble("valorOnline")));
                        }

                        currentUrlFotoPerfil = document.getString("urlFotoPerfil");
                        if (currentUrlFotoPerfil != null && !currentUrlFotoPerfil.isEmpty()) {
                            // MUDOU: 'this' para 'requireContext()'
                            Glide.with(requireContext())
                                    .load(currentUrlFotoPerfil)
                                    .placeholder(R.drawable.img_avatar_circle)
                                    .into(ivFotoPerfil);
                        }
                        if (btnSolicitarProfessor != null) {
                            btnSolicitarProfessor.setVisibility(View.VISIBLE);
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    // MUDOU: 'this' para 'requireContext()'
                    Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Ponto de entrada: Decide se precisa fazer upload ou não.
     */
    private void salvarPerfil() {
        setLoading(true);
        if (imageUri != null) {
            fazerUploadDaImagem();
        } else {
            salvarDadosNoFirestore(currentUrlFotoPerfil);
        }
    }

    /**
     * Passo 1 (se necessário): Faz o upload da imagem para o Storage.
     */
    private void fazerUploadDaImagem() {
        String fileName = uid + ".jpg";
        StorageReference storageRef = storage.getReference().child("fotosDePerfil/" + fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        salvarDadosNoFirestore(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    // MUDOU: 'this' para 'requireContext()'
                    Toast.makeText(requireContext(), "Erro no upload da foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    /**
     * Passo Final: Salva TODOS os dados (texto + URL da foto) no Firestore.
     */
    private void salvarDadosNoFirestore(String urlFotoPerfil) {
        String especialidade = etEspecialidade.getText().toString().trim();
        String preferenciaModalidade = etModalidade.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String idioma = etIdioma.getText().toString().trim();


        double valorP = 0;
        double valorO = 0;
        try {
            String strValorP = etValorPresencial.getText().toString().trim();
            String strValorO = etValorOnline.getText().toString().trim();
            if (!strValorP.isEmpty()) valorP = Double.parseDouble(strValorP);
            if (!strValorO.isEmpty()) valorO = Double.parseDouble(strValorO);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Por favor, insira valores válidos (ex: 100.50)", Toast.LENGTH_SHORT).show();

            setLoading(false);
            return;
        }

        Map<String, Object> professorData = new HashMap<>();
        professorData.put("especialidade", especialidade);
        professorData.put("idioma", idioma);

        // CORREÇÃO: Salvando no campo "preferenciaModalidade"
        professorData.put("preferenciaModalidade", preferenciaModalidade);

        professorData.put("bio", bio);
        professorData.put("valorPresencial", valorP);
        professorData.put("valorOnline", valorO);

        if (urlFotoPerfil != null) {
            professorData.put("urlFotoPerfil", urlFotoPerfil);
        }

        db.collection("users").document(uid)
                .update(professorData)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    // MUDOU: 'this' para 'requireContext()'
                    Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // MUDOU: 'finish()' para 'popBackStack()'
                    // Isto "aperta o botão voltar" e regressa ao ProfessorPerfilFragment
                    if (isAdded()) { // Garante que o fragmento ainda está "vivo"
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    // MUDOU: 'this' para 'requireContext()'
                    Toast.makeText(requireContext(), "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSalvarPerfil.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSalvarPerfil.setEnabled(true);
        }
    }
}