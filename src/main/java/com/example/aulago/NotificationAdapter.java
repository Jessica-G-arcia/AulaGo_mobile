package com.example.aulago;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
   //Para saber qual filtro está ativo
    private String currentFilter = "";

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Notification> newList) {
        notificationList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // NOVO MÉTODO: O Fragment usará este método para nos dizer qual filtro está ativo
    public void setCurrentFilter(String filter) {
        this.currentFilter = filter;
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
        holder.date.setText(notification.getDate());
        holder.timeAgo.setText(notification.getTimeAgo());

        // A bolinha só aparece se a notificação NÃO for lida E o filtro ativo for "Não lidas"
        boolean isUnread = !notification.getIsRead();
        boolean isUnreadFilterActive = "unread".equals(currentFilter);

        if (isUnread && isUnreadFilterActive) {
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