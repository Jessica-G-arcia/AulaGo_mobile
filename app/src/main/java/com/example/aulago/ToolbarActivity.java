package com.example.aulago;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.aulago.databinding.AppBarMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ToolbarActivity extends AppCompatActivity {

    private AppBarMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public static final String ROLE_PROFESSOR = "Professor";
    public static final String ROLE_ALUNO = "Aluno";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Infla o layout usando ViewBinding
        binding = AppBarMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicia o processo de carregamento de dados e configuração da UI
        loadUserDataAndSetupUI();
    }

    /**
     * Ponto de entrada principal: busca os dados do usuário no Firestore
     * e, em caso de sucesso, configura toda a UI.
     */
    private void loadUserDataAndSetupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(document -> {
                    String userRole = ROLE_ALUNO; // Assume "Aluno" por padrão
                    String userPhotoUrl = null;

                    if (document.exists()) {
                        // Lógica para determinar a role (ex: Professor se a solicitação foi aprovada)
                        if ("aprovado".equals(document.getString("statusSolicitacao"))) {
                            userRole = ROLE_PROFESSOR;
                        }
                        userPhotoUrl = document.getString("urlFotoPerfil");


                        android.util.Log.d("ToolbarActivity", "URL da foto recebida do Firestore: " + userPhotoUrl);
                    }
                    // Com os dados em mãos, configura toda a interface
                    setupUIWithRole(userRole, userPhotoUrl);
                })
                .addOnFailureListener(e -> {
                    // Em caso de falha, carrega a UI com o papel padrão de Aluno
                    setupUIWithRole(ROLE_ALUNO, null);
                });
    }

    /**
     * Configura toda a UI da Activity (barras de navegação e listeners)
     * depois que os dados do usuário foram buscados.
     */
    private void setupUIWithRole(String userRole, String userPhotoUrl) {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.getMenu().clear();

        if (ROLE_PROFESSOR.equals(userRole)) {
            bottomNav.inflateMenu(R.menu.bottom_menu_professor);
        } else {
            bottomNav.inflateMenu(R.menu.bottom_menu_aluno);
        }

        // Carrega a foto do perfil, que vai SOBRESCREVER as regras acima para o item de perfil
        loadProfileImageIntoNav(userPhotoUrl);

        setupClickListeners(userRole);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupClickListeners(String userRole) {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        Drawable transparentBackground = new ColorDrawable(Color.TRANSPARENT);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // **LÓGICA PRINCIPAL DA CORREÇÃO**
            // Muda a regra de fundo ANTES de fazer a navegação.
            if (id == R.id.nav_profile) {
                bottomNav.setItemBackground(transparentBackground);
            } else {
                bottomNav.setItemBackground(null);
            }

            // Lógica de navegação que você já tinha
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_classes) {
                replaceFragment(new AulasFragment());
            } else if (id == R.id.nav_calendar) {
                replaceFragment(new CalendarFragment());
            } else if (id == R.id.nav_search) {
                replaceFragment(ROLE_PROFESSOR.equals(userRole) ? new SearchAlunosFragment() : new SearchProfessoresFragment());
            } else if (id == R.id.nav_profile) {
                replaceFragment(ROLE_PROFESSOR.equals(userRole) ? new ProfessorPerfilFragment() : new AlunoPerfilFragment());
            }
            return true;
        });

        // Listeners da barra de ferramentas superior (continuam iguais)
        ImageView notificationsIcon = binding.toolbarLayout.ivNotifications;
        notificationsIcon.setOnClickListener(v -> replaceFragment(new NotificationsFragment()));

        ImageView settingsIcon = binding.toolbarLayout.ivSettings;
        settingsIcon.setOnClickListener(v -> replaceFragment(new EditarDadosPessoaisFragment()));

        ImageView logoutIcon = binding.toolbarLayout.ivLogout;
        logoutIcon.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    /**
     * Carrega a imagem do perfil e DESATIVA a pintura (tint) sobre ela.
     * A lógica de fundo foi movida para o setupClickListeners.
     */
    private void loadProfileImageIntoNav(@Nullable String userPhotoUrl) {
        MenuItem profileMenuItem = binding.bottomNavigation.getMenu().findItem(R.id.nav_profile);
        if (profileMenuItem == null) return;

        // Desativa a pintura laranja/roxa sobre a foto de perfil.
        profileMenuItem.setIconTintList(null);

        if (userPhotoUrl != null && !userPhotoUrl.isEmpty()) {
            Glide.with(this)
                    .asBitmap()
                    .load(userPhotoUrl)
                    .circleCrop()
                    .into(new CustomTarget<android.graphics.Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull android.graphics.Bitmap resource, @Nullable Transition<? super android.graphics.Bitmap> transition) {
                            Drawable profileIcon = new BitmapDrawable(getResources(), resource);
                            profileMenuItem.setIcon(profileIcon);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        // **IMPORTANTE**: Garante que se o app iniciar na tela de perfil, o fundo já esteja transparente.
        if (binding.bottomNavigation.getSelectedItemId() == R.id.nav_profile) {
            binding.bottomNavigation.setItemBackground(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Permite voltar ao fragmento anterior com o botão "Voltar"
                .commit();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sair", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        auth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}