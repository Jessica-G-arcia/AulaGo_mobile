package com.example.aulago;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aulago.databinding.FragmentLockedFeatureBinding;

public class LockedFeatureFragment extends Fragment {

    private FragmentLockedFeatureBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLockedFeatureBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Define o clique do botão
        binding.btnVerificarStatus.setOnClickListener(v -> {
            // Abre a tela de 'Solicitar Ser Professor'
            if (getActivity() != null) {
                ((ToolbarActivity) getActivity()).replaceFragment(new SolicitarSerProfessorFragment());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}