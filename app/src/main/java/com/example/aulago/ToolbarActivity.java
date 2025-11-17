package com.example.aulago;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
// import android.widget.TextView; // Import não é mais necessário
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper;

// IMPORTS ADICIONADOS PARA A CORREÇÃO DE COR
import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper; // <-- 1. NOVO IMPORT
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.aulago.databinding.ActivityToolbarBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ToolbarActivity extends AppCompatActivity {

    private ActivityToolbarBinding binding;
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
    private String userStatus = "aluno";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityToolbarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configura o layout edge-to-edge (para o teclado funcionar)
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
        configurarLayoutImersivoHibrido();
    }

    private void loadUserDataAndSetupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Se o usuário for nulo, volta para o login
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String userRole = ROLE_ALUNO;
                    this.userStatus = "nenhum";

                    if (document.exists()) {
                        String tipoUsuario = document.getString("userType");
                        String status = document.getString("statusSolicitacao");
                        this.userStatus = status != null ? status : "nenhum";

                        if ("professor".equals(tipoUsuario)) {
                            userRole = ROLE_PROFESSOR;
                        }
                    }

                    setupUIWithRole(userRole);
                })
                .addOnFailureListener(e -> {
                    setupUIWithRole(ROLE_ALUNO);
                });
    }


    private void setupUIWithRole(String userRole) {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.getMenu().clear();

        if (ROLE_PROFESSOR.equals(userRole)) {
            bottomNav.inflateMenu(R.menu.bottom_menu_professor);
        } else {
            bottomNav.inflateMenu(R.menu.bottom_menu_aluno);
        }

        setupClickListeners(userRole);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupClickListeners(String userRole) {
        BottomNavigationView bottomNav = binding.bottomNavigation;

        // --- Listener do Bottom Navigation ---
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_classes) {
                replaceFragment(new AulasFragment());
            } else if (id == R.id.nav_calendar) {
                replaceFragment(new CalendarFragment());
            } else if (id == R.id.nav_search) {
                if (ROLE_PROFESSOR.equals(userRole)) {
                    if ("aprovado".equals(this.userStatus)) {
                        replaceFragment(new SearchAlunosFragment());
                    } else {
                        replaceFragment(new LockedFeatureFragment());
                    }
                } else {
                    replaceFragment(new SearchProfessoresFragment());
                }
            } else if (id == R.id.nav_profile) {
                replaceFragment(ROLE_PROFESSOR.equals(userRole) ? new ProfessorPerfilFragment() : new AlunoPerfilFragment());
            }
            return true;
        });

        // --- Listeners da Toolbar Superior ---

//        binding.toolbarLayout.ivChatbot.setOnClickListener(v ->
//                replaceFragment(new ChatFragment())
//        );

        binding.toolbarLayout.ivNotifications.setOnClickListener(v ->
                replaceFragment(new NotificationsFragment())
        );

        binding.toolbarLayout.ivSettings.setOnClickListener(v ->
                replaceFragment(new EditarDadosPessoaisFragment())
        );

        binding.toolbarLayout.ivLogout.setOnClickListener(v ->
                showLogoutConfirmationDialog()
        );
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

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();

        apagarCredenciaisSeguras();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("JUST_LOGGED_OUT", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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
            securePreferences.edit()
                    .remove(KEY_USER_EMAIL)
                    .remove(KEY_USER_PASS)
                    .apply();
            Log.d("SecurePrefs", "Credenciais de biometria apagadas.");
        } catch (GeneralSecurityException | IOException e) {
            Log.e("SecurePrefs", "Erro ao apagar credenciais seguras", e);
        }
    }

    private void ajustarLayout() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View bottomNav = binding.bottomNavigation;
        View toolbar = binding.toolbarLayout.toolbar;
        View container = binding.fragmentContainer;

        if (toolbar.getTag() == null) {
            toolbar.setTag(toolbar.getPaddingTop());
        }
        final int originalToolbarTop = (int) toolbar.getTag();

        if (bottomNav.getTag() == null) {
            bottomNav.setTag(bottomNav.getPaddingBottom());
        }
        final int originalBottomNavBottom = (int) bottomNav.getTag();

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    originalToolbarTop + statusBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    originalBottomNavBottom + navBars.bottom
            );
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            return insets;
        });
    }

    // --- MÉTODO DE LAYOUT CORRIGIDO ---
    private void configurarLayoutImersivoHibrido() {
        // 1. Diz ao sistema que vamos cuidar do layout
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View mainView = binding.getRoot();
        int originalPaddingLeft = mainView.getPaddingLeft();
        int originalPaddingTop = mainView.getPaddingTop();
        int originalPaddingRight = mainView.getPaddingRight();
        int originalPaddingBottom = mainView.getPaddingBottom();

        // (A linha 'extraPaddingBottom' foi REMOVIDA)

        // 2. Ouve as mudanças de insets (barras e teclado)
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {

            // Pega o tamanho da barra de status (topo)
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            // Pega o tamanho do teclado (para o rodapé)
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Calcula o padding
            int paddingLeft = originalPaddingLeft + statusBars.left;
            int paddingTop = originalPaddingTop + statusBars.top; // Respeita a barra de status
            int paddingRight = originalPaddingRight + statusBars.right;

            // O padding de baixo agora é SÓ o original + o teclado
            int paddingBottom = originalPaddingBottom + imeInsets.bottom;

            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            return insets;
        });

        // 3. Esconde a barra de navegação (embaixo)
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), mainView);
        //controller.hide(WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    // --- MÉTODO dpToPx REMOVIDO ---
    // (Você pode apagar o método dpToPx, pois não o usamos mais)
    // private int dpToPx(int dp) { ... }
}