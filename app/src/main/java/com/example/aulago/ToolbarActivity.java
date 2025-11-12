package com.example.aulago;

import android.os.Bundle;
import android.content.Intent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.fragment.app.Fragment;

import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper;

// IMPORTS ADICIONADOS PARA A CORREÇÃO DE COR
import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper; // <-- 1. NOVO IMPORT

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

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AppBarMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        loadUserProfileImage(userPhotoUrl);

        ChipNavigationBar bottomNav = binding.bottomNavigation;
        if (ROLE_PROFESSOR.equals(userRole)) {
            bottomNav.setMenuResource(R.menu.bottom_menu_professor);
        } else {
            bottomNav.setMenuResource(R.menu.bottom_menu_aluno);
        }

        setupNavigationListener(bottomNav, userRole);
        setupAvatarClickListener(userRole); // <-- CORREÇÃO ESTÁ AQUI DENTRO

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

    /**
     * O 'setupAvatarClickListener' AGORA USA O NOVO ESTILO
     */
    private void setupAvatarClickListener(String userRole) {
        CircleImageView avatarIcon = binding.toolbarLayout.ivUserAvatar;
        avatarIcon.setOnClickListener(view -> {

            Context wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenuStyle); // <-- MUDANÇA
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

            // Truque para forçar ícones (sem mudanças)
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}