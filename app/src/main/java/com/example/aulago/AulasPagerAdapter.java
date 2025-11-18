package com.example.aulago;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AulasPagerAdapter extends FragmentStateAdapter {
    private final String userType; // Adicione o tipo

    public AulasPagerAdapter(@NonNull FragmentActivity fragmentActivity, String userType) {
        super(fragmentActivity);
        this.userType = userType;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            // Aba "Concluídas" (isConcluida = true)
            return AulasListFragment.newInstance(true, userType);
        }
        // Aba "Agendadas" (isConcluida = false)
        return AulasListFragment.newInstance(false, userType);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
