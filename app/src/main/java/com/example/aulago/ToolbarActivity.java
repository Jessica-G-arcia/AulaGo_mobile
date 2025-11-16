package com.example.aulago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.bumptech.glide.Glide;
import com.example.aulago.databinding.ActivityToolbarBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

        // --- Chamada para o layout ---
        ajustarLayout();

        setSupportActionBar(binding.toolbarLayout.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle("");
        }

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
                    String userRole = ROLE_ALUNO;
                    String userPhotoUrl = null;
                    this.userStatus = "aluno"; // Padrão

                    if (document.exists()) {
                        String status = document.getString("statusSolicitacao");

                        if (status != null && !status.isEmpty()) {
                            this.userStatus = status;
                        }

                        if ("aprovado".equals(status)) {
                            userRole = ROLE_PROFESSOR;
                        }
                        userPhotoUrl = document.getString("urlFotoPerfil");
                    }

                    if (binding != null) {
                        Glide.with(this)
                                .load(userPhotoUrl)
                                .placeholder(R.drawable.img_avatar_circle)
                                .error(R.drawable.img_avatar_circle)
                                .into(binding.toolbarLayout.ivUserAvatar);
                    }

                    setupUIWithRole(userRole);
                })
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        Glide.with(this)
                                .load((String) null)
                                .placeholder(R.drawable.img_avatar_circle)
                                .error(R.drawable.img_avatar_circle)
                                .into(binding.toolbarLayout.ivUserAvatar);
                    }
                    setupUIWithRole(ROLE_ALUNO);
                });
    }

    /**
     * Configura toda a UI da Activity depois que os dados do usuário foram buscados.
     */
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

        // Listeners da barra de ferramentas superior
        binding.toolbarLayout.ivNotifications.setOnClickListener(v ->
                replaceFragment(new NotificationsFragment())
        );

        // OPÇÃO A: Se você quer REMOVER os ícones antigos e usar APENAS o dropdown
        // Comente ou remova estas linhas:
    /*
    binding.toolbarLayout.ivSettings.setOnClickListener(v ->
        replaceFragment(new EditarDadosPessoaisFragment())
    );
    binding.toolbarLayout.ivLogout.setOnClickListener(v ->
        showLogoutConfirmationDialog()
    );
    */

        // OPÇÃO B: Se você quer MANTER os ícones antigos E adicionar o dropdown
        // Mantenha as linhas acima descomentadas

        // NOVO: Listener para o avatar que mostra o PopupMenu
        binding.toolbarLayout.ivUserAvatar.setOnClickListener(v ->
                showProfileMenu(v)
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

    // --- MÉTODO DE LAYOUT CORRIGIDO (VERSÃO FINAL) ---
    private void ajustarLayout() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View bottomNav = binding.bottomNavigation;
        View toolbar = binding.toolbarLayout.toolbar;
        View container = binding.fragmentContainer;

        // Salva os paddings originais
        if (toolbar.getTag() == null) {
            toolbar.setTag(toolbar.getPaddingTop());
        }
        final int originalToolbarTop = (int) toolbar.getTag();

        if (bottomNav.getTag() == null) {
            bottomNav.setTag(bottomNav.getPaddingBottom());
        }
        final int originalBottomNavBottom = (int) bottomNav.getTag();

        // Toolbar - Reage apenas à barra de status
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    originalToolbarTop + statusBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets; // ✅ NÃO consome
        });

        // Bottom Navigation - Reage apenas à barra de navegação (NÃO ao teclado)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    originalBottomNavBottom + navBars.bottom
            );
            return insets; // ✅ NÃO consome
        });

        // Container - NÃO aplica padding para o teclado
        // Deixa o ScrollView dentro do Fragment lidar com isso
        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            // Simplesmente retorna os insets sem modificar
            return insets; // ✅ Permite que o ScrollView reaja
        });
    }

    /**
     * Mostra o menu dropdown quando clica na foto de perfil
     * @param anchor View que serve como âncora para o menu (o avatar)
     */
//    private void showProfileMenu(View anchor) {
//        PopupMenu popupMenu = new PopupMenu(this, anchor);
//        popupMenu.getMenuInflater().inflate(R.menu.profile_dropdown_menu, popupMenu.getMenu());
//
//        popupMenu.setOnMenuItemClickListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.menu_edit_profile) {
//                replaceFragment(new EditarDadosPessoaisFragment());
//                return true;
//
//            } else if (id == R.id.menu_logout) {
//                showLogoutConfirmationDialog();
//                return true;
//            }
//
//            return false;
//        });
//
//        popupMenu.show();
//    }

    private void showProfileMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);

        // Adiciona itens manualmente
        popupMenu.getMenu().add(0, 1, 0, "Editar cadastro");
        popupMenu.getMenu().add(0, 2, 1, "Sair");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                replaceFragment(new EditarDadosPessoaisFragment());
                return true;
            } else if (item.getItemId() == 2) {
                showLogoutConfirmationDialog();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }






}