package com.example.aulago;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat; // Import
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Notification> newList) {
        notificationList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());

        // --- LÓGICA DE DATA/HORA CORRIGIDA ---
        if (notification.getDataCriacao() != null) {
            // Formato da Data (ex: 25/03/2025)
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.date.setText(sdfDate.format(notification.getDataCriacao()));

            // Formato da Hora (ex: 10:30)
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.timeAgo.setText(sdfTime.format(notification.getDataCriacao()));
        } else {
            // Se a data for nula, esconde os campos
            holder.date.setText("");
            holder.timeAgo.setText("");
        }

        // --- LÓGICA DO PONTO DE "NÃO LIDO" CORRIGIDA ---
        // A bolinha deve aparecer se a notificação NÃO for lida.
        // O filtro (no Fragment) decide se o item aparece na lista ou não.
        if (!notification.getIsRead()) {
            holder.unreadDot.setVisibility(View.VISIBLE);
        } else {
            holder.unreadDot.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, date, timeAgo;
        View unreadDot;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_notification_title);
            message = itemView.findViewById(R.id.tv_notification_message);
            date = itemView.findViewById(R.id.tv_notification_date);
            timeAgo = itemView.findViewById(R.id.tv_notification_time_ago);
            unreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}