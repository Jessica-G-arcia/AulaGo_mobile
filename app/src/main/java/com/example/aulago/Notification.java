package com.example.aulago;

public class Notification {
    private String title;
    private String message;
    private String date;
    private String timeAgo;
    private boolean isRead;

    public Notification() {} // construtor vazioo obrigatório para o firebase

    public Notification(String title, String message, String date, String timeAgo, boolean isRead) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.timeAgo = timeAgo;
        this.isRead = isRead;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
    public String getTimeAgo() { return timeAgo; }
    public boolean getIsRead() { return isRead; }
}