package com.example.aulago;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AulasFragment extends Fragment {

    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aulas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        // Tente achar uma ProgressBar no seu layout, ou crie uma.
        // Se não tiver no XML, o app só vai demorar 1seg pra carregar sem aviso.
        progressBar = view.findViewById(R.id.progressBar); // Adicione um ID se tiver

        identificarUsuarioEConfigurarAbas();
    }

    private void identificarUsuarioEConfigurarAbas() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Mostra loading se possível
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // ATENÇÃO: Verifique se a coleção de usuários no seu banco chama "usuarios" ou "users"
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        // Tenta pegar o campo "tipo" (ex: "professor" ou "aluno")
                        String tipo = documentSnapshot.getString("userType");

                        // Se não tiver campo 'tipo', tenta adivinhar ou define padrão
                        if (tipo == null) {
                            // Lógica alternativa: Se tiver CREF é professor, etc.
                            // Por segurança, vamos assumir aluno se falhar
                            tipo = "aluno";
                        }

                        Log.d("AulasFragment", "Usuário identificado como: " + tipo);
                        configurarViewPager(tipo);

                    } else {
                        // Documento não achado? Assume aluno por padrão
                        configurarViewPager("aluno");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AulasFragment", "Erro ao buscar usuário", e);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    configurarViewPager("aluno"); // Fallback
                });
    }

    private void configurarViewPager(String tipoUsuario) {
        // Agora passamos o tipo correto (vindo do banco) para o Adapter
        AulasPagerAdapter pagerAdapter = new AulasPagerAdapter(requireActivity(), tipoUsuario);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("AGENDADAS");
            } else {
                tab.setText("CONCLUÍDAS");
            }
        }).attach();
    }
}