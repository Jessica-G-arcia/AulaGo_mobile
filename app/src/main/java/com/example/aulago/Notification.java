package com.example.aulago;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String title;
    private String message;
    private boolean isRead;
    private Date dataCriacao;
    private String userId; // Campo novo: UID do destinatário

    // Construtor vazio - obrigatório para Firebase
    public Notification() {}

    // Construtor completo
    public Notification(String title, String message, boolean isRead, Date dataCriacao, String userId) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.dataCriacao = dataCriacao;
        this.userId = userId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean getIsRead() { return isRead; }
    public String getUserId() { return userId; }

    @ServerTimestamp
    public Date getDataCriacao() { return dataCriacao; }



    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setIsRead(boolean read) { isRead = read; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
    public void setUserId(String userId) { this.userId = userId; }
}
