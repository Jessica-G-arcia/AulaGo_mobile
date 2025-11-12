package com.example.aulago;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

// imports do firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> allNotificationsList;

    private CheckBox checkUnread, checkRead, checkRecent;

    // instancia do firebase
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        checkUnread = view.findViewById(R.id.checkbox_unread);
        checkRead = view.findViewById(R.id.checkbox_read);
        checkRecent = view.findViewById(R.id.checkbox_recent);

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance();

        allNotificationsList = new ArrayList<>();
//        loadDummyData();

        // O Adapter será notificado quando os dados chegarem
        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupCheckboxListeners();

        // Carregua os dados do Firebase
        loadNotificationsFromFirebase();

        // Mantendo "Não lidas" como o filtro inicial
        checkUnread.setChecked(true);

        return view;
    }

    private void setupCheckboxListeners() {
        View.OnClickListener checkboxListener = v -> {
            checkUnread.setChecked(false);
            checkRead.setChecked(false);
            checkRecent.setChecked(false);

            CheckBox clickedCheckbox = (CheckBox) v;
            clickedCheckbox.setChecked(true);

            filterNotifications();
        };

        checkUnread.setOnClickListener(checkboxListener);
        checkRead.setOnClickListener(checkboxListener);
        checkRecent.setOnClickListener(checkboxListener);
    }

    private void filterNotifications() {
        List<Notification> filteredList = new ArrayList<>();
        // Variável para guardar o nome do filtro ativo
        String activeFilter = "";

        if (checkUnread.isChecked()) {
            activeFilter = "unread"; // Define o filtro como "não lido"
            for (Notification notification : allNotificationsList) {
                if (!notification.getIsRead() && !"Agora".equals(notification.getTimeAgo())) {
                    filteredList.add(notification);
                }
            }
        } else if (checkRead.isChecked()) {
            activeFilter = "read"; // Define o filtro como "lido"
            for (Notification notification : allNotificationsList) {
                if (notification.getIsRead() && !"Agora".equals(notification.getTimeAgo())) {
                    filteredList.add(notification);
                }
            }
        } else if (checkRecent.isChecked()) {
            activeFilter = "recent"; // Define o filtro como "recente"
            for (Notification notification : allNotificationsList) {
                if ("Agora".equals(notification.getTimeAgo())) {
                    filteredList.add(notification);
                }
            }
        }

        // Antes de atualizar a lista, avisa ao adaptador qual filtro está ativo
        adapter.setCurrentFilter(activeFilter);

        // atualiza a lista
        adapter.updateList(filteredList);
    }

//    private void loadDummyData() {
//        allNotificationsList.add(new Notification("Pagamento recebido!", "O valor da aula agendada por Maria já foi depositado em sua conta com sucesso: R$150,00.", "25/03/2025", "Agora", false));
//        allNotificationsList.add(new Notification("Nova aula agendada", "Ana Paula agendou uma aula online com você para o dia 17/05/2025 às 15:30.", "24/05/2025", "14:00", true));
//        allNotificationsList.add(new Notification("Nova avaliação recebida", "João acabou de avaliar sua última aula. Confira o feedback e veja como você está impactando seus alunos!", "25/03/2025", "08:00", true));
//        allNotificationsList.add(new Notification("Nova avaliação recebida", "Eduardo acabou de avaliar sua última aula. Confira o feedback e veja como você está impactando seus alunos!", "25/03/2025", "10:30", false));
//    }

    private void loadNotificationsFromFirebase() {
        allNotificationsList.clear(); // Limpa a lista antes de carregar

        db.collection("notificacoes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loop por CADA documento que o Firebase retornou
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte o documento em um objeto Notification
                            Notification notification = document.toObject(Notification.class);
                            allNotificationsList.add(notification);
                        }

                        filterNotifications();

                    } else {
                        // Se der erro, mostra no Logcat
                        Log.e("FirebaseError", "Erro ao buscar notificações: ", task.getException());
                    }
                });
    }
}