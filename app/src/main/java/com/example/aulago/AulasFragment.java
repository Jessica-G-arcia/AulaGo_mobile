package com.example.aulago;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AulasFragment extends Fragment {

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

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

        // PEGUE O TIPO DO USUÁRIO (aluno ou professor)
        String tipoUsuario = getTipoUsuario(); // Implemente este método ou pegue de prefs/firestore

        // INICIALIZE O ADAPTER COM O TIPO
        AulasPagerAdapter pagerAdapter = new AulasPagerAdapter(requireActivity(), tipoUsuario);
        viewPager.setAdapter(pagerAdapter);

        // CONFIGURE AS ABAS
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("AGENDADAS");
            } else {
                tab.setText("CONCLUÍDAS");
            }
        }).attach();
    }

    // Exemplo de como pegar o tipo de usuário (pode ser de prefs, Firebase, etc.)
    private String getTipoUsuario() {
        // Aqui um exemplo usando SharedPreferences (troque conforme sua lógica)
        // SharedPreferences prefs = requireActivity().getSharedPreferences("AulaGoPrefs", Context.MODE_PRIVATE);
        // return prefs.getString("USER_TYPE", "aluno");
        return "professor"; // Troque pela lógica real para obter o tipo
    }
}
