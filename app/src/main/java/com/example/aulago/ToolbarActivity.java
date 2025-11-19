package com.example.aulago;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.aulago.databinding.ActivityToolbarBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userStatus = "aluno";

    // --- VARIÁVEIS DE NOTIFICAÇÃO ---
    private static final String CHANNEL_ID = "notificacoes_aulago";
    private ListenerRegistration notificationListener;
    private boolean isFirstLoad = true; // Evita spam ao abrir o app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityToolbarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configura o layout edge-to-edge
        ajustarLayout();

        setSupportActionBar(binding.toolbarLayout.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle("");
        }

        loadUserDataAndSetupUI();

        // --- INICIALIZAÇÃO DAS NOTIFICAÇÕES ---
        createNotificationChannel();
        pedirPermissaoNotificacao();
        iniciarOuvinteDeNotificacoes();
    }

    // ========================================================================================
    // --- LÓGICA DE NOTIFICAÇÕES (ADICIONADO) ---
    // ========================================================================================

    private void iniciarOuvinteDeNotificacoes() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String meuUid = user.getUid();

        // Ouve a coleção "notificacoes" filtrando pelo ID do usuário
        notificationListener = db.collection("notificacoes")
                .whereEqualTo("userId", meuUid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("Notif", "Erro ao ouvir notificações", e);
                        return;
                    }

                    if (snapshots != null) {
                        // Se for a primeira carga (ao abrir o app), marcamos como feito e ignoramos
                        // para não notificar mensagens antigas que já estão no banco.
                        if (isFirstLoad) {
                            isFirstLoad = false;
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            // Só queremos saber de documentos NOVOS (Type.ADDED)
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    Notification model = dc.getDocument().toObject(Notification.class);
                                    // Se a notificação ainda não foi lida, mostramos no topo
                                    if (!model.getIsRead()) {
                                        mostrarNotificacaoNoCelular(model.getTitle(), model.getMessage());
                                    }
                                } catch (Exception ex) {
                                    Log.e("Notif", "Erro ao converter notificação", ex);
                                }
                            }
                        }
                    }
                });
    }

    private void mostrarNotificacaoNoCelular(String titulo, String mensagem) {
        // Verifica permissão no Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Configura o clique na notificação para abrir esta Activity
        Intent intent = new Intent(this, ToolbarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Cria a notificação visual
        // DICA: Troque 'android.R.drawable.ic_dialog_info' pelo seu ícone (ex: R.drawable.ic_notification)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // Usa o tempo atual como ID para permitir múltiplas notificações
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        // O Canal é obrigatório no Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificações AulaGo";
            String description = "Avisos sobre aulas e mensagens";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void pedirPermissaoNotificacao() {
        // Pede permissão no Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove o ouvinte para economizar bateria quando fechar o app
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    // ========================================================================================
    // --- FIM DA LÓGICA DE NOTIFICAÇÕES ---
    // ========================================================================================

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

        binding.toolbarLayout.ivChatbot.setOnClickListener(v ->
                replaceFragment(new ChatFragment())
        );

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
}