package com.example.aulago;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.view.View; // <-- ADICIONADO PARA OPÇÃO 1

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

// --- IMPORTS ADICIONADOS PARA OPÇÃO 1 ---
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
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
// MUDANÇA 1: Import corrigido
import com.example.aulago.databinding.ActivityToolbarBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ToolbarActivity extends AppCompatActivity {

    // MUDANÇA 2: Declaração da variável corrigida
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MUDANÇA 3: Inflação do layout corrigida
        binding = ActivityToolbarBinding.inflate(getLayoutInflater());
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
                    // Em caso de falha, carrega como aluno
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

        // Carrega o fragmento inicial se não houver um
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
        // 1. Encerrar a sessão do Firebase
        auth.signOut();

        // 2. Limpar APENAS a flag da sessão "Lembrar Senha"
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();

        // 3. NÃO APAGAR AS CREDENCIAIS DE BIOMETRIA.
        // 4. Voltar para a tela de login com a flag
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