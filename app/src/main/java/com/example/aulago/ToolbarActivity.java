package com.example.aulago;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuInflater;
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

import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper;

// IMPORTS ADICIONADOS PARA A CORREÇÃO DE COR
import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper; // <-- 1. NOVO IMPORT

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.aulago.databinding.AppBarMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ToolbarActivity extends AppCompatActivity {

    private AppBarMainBinding binding;
    public static final String ROLE_PROFESSOR = "Professor";
    public static final String ROLE_ALUNO = "Aluno";

    // --- CONSTANTES ---
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String SECURE_PREFS_NAME = "SecureAuthPrefs";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PASS = "userPass";
    private static final String KEY_BIOMETRIC_EMAIL_ALIAS = "biometricEmailAlias";
    // --- FIM DAS CONSTANTES ---

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AppBarMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- CHAMADA DO MÉTODO ADICIONADA AQUI ---
        ajustarLayout();

        setSupportActionBar(binding.toolbarLayout.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle("");
        }

        ImageView notificationIcon = binding.toolbarLayout.ivNotifications;
        notificationIcon.setOnClickListener(view -> {
            replaceFragment(new NotificationsFragment());
        });

        loadUserDataAndSetupUI();
    }

    /**
     * Função principal que busca os dados do usuário no Firestore
     * e configura toda a UI baseada nesses dados.
     */
    private void loadUserDataAndSetupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String userRole = ROLE_ALUNO; // Assume "Aluno" por padrão
                    String userPhotoUrl = null;

                    if (document.exists()) {
                        String status = document.getString("statusSolicitacao");
                        if ("aprovado".equals(status)) {
                            userRole = ROLE_PROFESSOR;
                        }
                        userPhotoUrl = document.getString("urlFotoPerfil");
                    }

                    setupUIWithRole(userRole, userPhotoUrl);
                })
                .addOnFailureListener(e -> {
                    setupUIWithRole(ROLE_ALUNO, null);
                });
    }

    /**
     * Configura toda a UI da Activity depois que os dados do usuário foram buscados.
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
                .addToBackStack(null)
                .commit();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("JUST_LOGGED_OUT", true); // Envia a flag
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Este método não é chamado no logout normal,
     * mas é mantido aqui caso seja necessário em outro fluxo (ex: login google)
     */
    private void apagarCredenciaisSeguras() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences securePreferences = EncryptedSharedPreferences.create(
                    SECURE_PREFS_NAME,
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Apaga as chaves criptografadas
            securePreferences.edit()
                    .remove(KEY_USER_EMAIL)
                    .remove(KEY_USER_PASS)
                    .apply();
            Log.d("SecurePrefs", "Credenciais de biometria apagadas.");

        } catch (GeneralSecurityException | IOException e) {
            Log.e("SecurePrefs", "Erro ao apagar credenciais seguras", e);
        }
    }

    // --- MÉTODO ADICIONADO DA OPÇÃO 1 ---
    private void ajustarLayout() {
        // 'getRoot()' é a sua view principal (provavelmente um ConstraintLayout)
        View mainView = binding.getRoot();

        // Salva o padding original do seu XML (se houver)
        int originalPaddingLeft = mainView.getPaddingLeft();
        int originalPaddingTop = mainView.getPaddingTop();
        int originalPaddingRight = mainView.getPaddingRight();
        int originalPaddingBottom = mainView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            // Pega os insets da barra de status (topo)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Pega os insets do TECLADO (IME)
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Pega os insets da barra de navegação (gestos/botões)
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            // Calcula o padding
            int paddingLeft = originalPaddingLeft + systemBars.left;
            int paddingTop = originalPaddingTop + systemBars.top; // <-- Adiciona padding no topo
            int paddingRight = originalPaddingRight + systemBars.right;

            // O padding de baixo é o original + o MAIOR valor entre o teclado e a barra de navegação
            int paddingBottom = originalPaddingBottom + Math.max(imeInsets.bottom, navBars.bottom); // <-- Adiciona padding embaixo

            // Aplica o padding
            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            return insets;
        });
    }
}