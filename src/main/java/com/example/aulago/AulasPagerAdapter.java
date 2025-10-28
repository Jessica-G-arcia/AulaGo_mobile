package com.example.aulago;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AulasPagerAdapter extends FragmentStateAdapter {

    public AulasPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            // Aba "Concluídas" (isConcluida = true)
            return AulasListFragment.newInstance(true);
        }
        // Aba "Agendadas" (isConcluida = false)
        return AulasListFragment.newInstance(false);
    }

    @Override
    public int getItemCount() {
        return 2; // Temos 2 abas
    }
}