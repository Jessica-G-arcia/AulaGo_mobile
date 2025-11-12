package com.example.aulago;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.view.View; // Import genérico, necessário

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

// --- IMPORTS ADICIONADOS PARA O NOVO LAYOUT ---
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;
// --- FIM DOS IMPORTS ---

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import androidx.fragment.app.Fragment;
import android.content.Context;
import androidx.appcompat.view.ContextThemeWrapper;

// IMPORTS PARA A BIOMETRIA
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.bumptech.glide.Glide;
import com.example.aulago.databinding.AppBarMainBinding;
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

        // --- CHAMADA DO NOVO MÉTODO DE LAYOUT ---
        configurarLayoutImersivoHibrido(); // <-- SUBSTITUI OS MÉTODOS ANTERIORES

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

    // --- MÉTODO DE LAYOUT HÍBRIDO (OPÇÃO 1 + OPÇÃO 2) ---
    private void configurarLayoutImersivoHibrido() {
        // 1. Diz ao sistema que vamos cuidar do layout
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View mainView = binding.getRoot();
        int originalPaddingLeft = mainView.getPaddingLeft();
        int originalPaddingTop = mainView.getPaddingTop();
        int originalPaddingRight = mainView.getPaddingRight();
        int originalPaddingBottom = mainView.getPaddingBottom();

        // 2. Ouve as mudanças de insets (barras e teclado)
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {

            // --- LÓGICA DA OPÇÃO 1 (APENAS PARA O TOPO) ---
            // Pega o tamanho da barra de status (topo)
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            // Pega o tamanho do teclado (para o rodapé)
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Calcula o padding
            int paddingLeft = originalPaddingLeft + statusBars.left;
            int paddingTop = originalPaddingTop + statusBars.top; // <-- Respeita a barra de status
            int paddingRight = originalPaddingRight + statusBars.right;

            // O padding de baixo só reage ao teclado, ignorando a barra de navegação
            int paddingBottom = originalPaddingBottom + imeInsets.bottom; // <-- Ignora a barra de navegação

            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            return insets;
        });

        // --- LÓGICA DA OPÇÃO 2 (APENAS PARA O RODAPÉ) ---
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), mainView);

        // Esconde APENAS a barra de navegação (embaixo)
        controller.hide(WindowInsetsCompat.Type.navigationBars());

        // Define o comportamento: a barra reaparece com um gesto de swipe
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }


    // --- Restante do seu código (sem alterações) ---

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
                    String userRole = ROLE_ALUNO; // Padrão
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

    private void setupUIWithRole(String userRole, String userPhotoUrl) {
        loadUserProfileImage(userPhotoUrl);

        ChipNavigationBar bottomNav = binding.bottomNavigation;
        if (ROLE_PROFESSOR.equals(userRole)) {
            bottomNav.setMenuResource(R.menu.bottom_menu_professor);
        } else {
            bottomNav.setMenuResource(R.menu.bottom_menu_aluno);
        }

        setupNavigationListener(bottomNav, userRole);
        setupAvatarClickListener(userRole);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new HomeFragment());
            bottomNav.setItemSelected(R.id.nav_home, true);
        }
    }

    private void loadUserProfileImage(String userPhotoUrl) {
        CircleImageView avatarIcon = binding.toolbarLayout.ivUserAvatar;
        if (userPhotoUrl != null && !userPhotoUrl.isEmpty()) {
            Glide.with(this)
                    .load(userPhotoUrl)
                    .placeholder(R.drawable.img_avatar_circle)
                    .error(R.drawable.img_avatar_circle)
                    .into(avatarIcon);
        } else {
            avatarIcon.setImageResource(R.drawable.img_avatar_circle);
        }
    }

    private void setupNavigationListener(ChipNavigationBar bottomNav, String userRole) {
        bottomNav.setOnItemSelectedListener(id -> {
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_classes) {
                replaceFragment(new AulasFragment());
            } else if (id == R.id.nav_calendar) {
                replaceFragment(new CalendarFragment());
            } else if (id == R.id.nav_search) {
                if (ROLE_PROFESSOR.equals(userRole)) {
                    replaceFragment(new SearchAlunosFragment());
                } else {
                    replaceFragment(new SearchProfessoresFragment());
                }
            } else if (id == R.id.nav_profile) {
                if (ROLE_PROFESSOR.equals(userRole)) {
                    replaceFragment(new ProfessorPerfilFragment());
                } else {
                    replaceFragment(new AlunoPerfilFragment());
                }
            }
        });
    }

    private void setupAvatarClickListener(String userRole) {
        CircleImageView avatarIcon = binding.toolbarLayout.ivUserAvatar;
        avatarIcon.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenuStyle);
            PopupMenu popup = new PopupMenu(wrapper, view);

            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.profile_dropdown_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    if (ROLE_PROFESSOR.equals(userRole)) {
                        replaceFragment(new ProfessorPerfilFragment());
                    } else {
                        replaceFragment(new AlunoPerfilFragment());
                    }
                    binding.bottomNavigation.setItemSelected(R.id.nav_profile, true);
                    return true;
                } else if (id == R.id.menu_settings) {
                    replaceFragment(new EditarDadosPessoaisFragment());
                    return true;
                } else if (id == R.id.menu_logout) {
                    showLogoutConfirmationDialog();
                    return true;
                }
                return false;
            });

            // Forçar ícones
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popup.show();
        });
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
}