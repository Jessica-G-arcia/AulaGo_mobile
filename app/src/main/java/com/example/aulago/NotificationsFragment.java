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
import java.util.Calendar; // Import
import java.util.Date; // Import
import java.util.List;
import android.util.Log;

// imports do firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Import
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> allNotificationsList; // Lista com TODAS as notificações

    private CheckBox checkUnread, checkRead, checkRecent;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        checkUnread = view.findViewById(R.id.checkbox_unread);
        checkRead = view.findViewById(R.id.checkbox_read);
        checkRecent = view.findViewById(R.id.checkbox_recent);

        db = FirebaseFirestore.getInstance();
        allNotificationsList = new ArrayList<>();

        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupCheckboxListeners();
        loadNotificationsFromFirebase();

        // Mantendo "Não lidas" como o filtro inicial
        checkUnread.setChecked(true);

        return view;
    }

    private void setupCheckboxListeners() {
        View.OnClickListener checkboxListener = v -> {
            // Desmarca todos primeiro
            checkUnread.setChecked(false);
            checkRead.setChecked(false);
            checkRecent.setChecked(false);

            // Marca apenas o que foi clicado
            CheckBox clickedCheckbox = (CheckBox) v;
            clickedCheckbox.setChecked(true);

            // Aplica o filtro
            filterNotifications();
        };

        checkUnread.setOnClickListener(checkboxListener);
        checkRead.setOnClickListener(checkboxListener);
        checkRecent.setOnClickListener(checkboxListener);
    }

    // --- LÓGICA DE FILTRO CORRIGIDA ---
    private void filterNotifications() {
        List<Notification> filteredList = new ArrayList<>();

        if (checkUnread.isChecked()) {
            for (Notification notification : allNotificationsList) {
                if (!notification.getIsRead()) { // Simplesmente checa o booleano
                    filteredList.add(notification);
                }
            }
        } else if (checkRead.isChecked()) {
            for (Notification notification : allNotificationsList) {
                if (notification.getIsRead()) { // Simplesmente checa o booleano
                    filteredList.add(notification);
                }
            }
        } else if (checkRecent.isChecked()) {
            for (Notification notification : allNotificationsList) {
                // Checa se a data da notificação é "hoje"
                if (isToday(notification.getDataCriacao())) {
                    filteredList.add(notification);
                }
            }
        }

        // Atualiza a lista visível no adapter
        adapter.updateList(filteredList);
    }

    // --- CONSULTA AO FIREBASE CORRIGIDA ---
    private void loadNotificationsFromFirebase() {
        allNotificationsList.clear();

        db.collection("notificacoes")
                // Ordena pela data de criação, da mais nova para a mais antiga
                .orderBy("dataCriacao", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Notification notification = document.toObject(Notification.class);
                            allNotificationsList.add(notification);
                        }

                        // Após carregar TUDO, aplica o filtro (que por padrão é "Não lidas")
                        filterNotifications();

                    } else {
                        Log.e("FirebaseError", "Erro ao buscar notificações: ", task.getException());
                    }
                });
    }

    // --- NOVO MÉTODO HELPER ---
    /**
     * Verifica se uma data (Date) é do dia de "hoje".
     */
    private boolean isToday(Date date) {
        if (date == null) return false;

        Calendar cal1 = Calendar.getInstance(); // Data de hoje
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date); // Data da notificação

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}