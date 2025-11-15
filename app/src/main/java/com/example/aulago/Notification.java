package com.example.aulago;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Notification {
    private String title;
    private String message;
    private boolean isRead;
    private Date dataCriacao;

    public Notification() {} // construtor vazioo obrigatório para o firebase

    public Notification(String title, String message, boolean isRead, Date dataCriacao) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.dataCriacao = dataCriacao;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean getIsRead() { return isRead; }

    @ServerTimestamp // Isso garante que o Firebase use a data do servidor ao criar
    public Date getDataCriacao() { return dataCriacao; }

    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setIsRead(boolean read) { isRead = read; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
}