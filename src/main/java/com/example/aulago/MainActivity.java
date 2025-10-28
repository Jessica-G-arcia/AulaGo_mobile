package com.example.aulago;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.aulago.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbarLayout.toolbar);

        if (getSupportActionBar() != null) {
            // 1. Desabilita a exibição do título
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // 2. Define o texto do título como vazio (garantia extra)
            getSupportActionBar().setTitle("");
        }

        drawerLayout = binding.drawerLayout;
        toggle = new ActionBarDrawerToggle(this, drawerLayout, binding.appBarMain.toolbarLayout.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Encontra o ImageView da notificação pelo seu ID no layout da toolbar
        ImageView notificationIcon = findViewById(R.id.iv_notifications);

        // Configura o listener de clique
        notificationIcon.setOnClickListener(view -> {
            replaceFragment(new NotificationsFragment());

        });

        // Configura o listener do menu de navegação
        setupNavigationListener();

        // Carrega o fragmento inicial (Home)
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            binding.navView.setCheckedItem(R.id.nav_home);
        }
    }

    private void setupNavigationListener() {
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_classes) {
                replaceFragment(new AulasFragment());
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}