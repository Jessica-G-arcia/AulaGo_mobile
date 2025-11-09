package com.example.aulago;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

// Este é o "Molde" (POJO) para o Firestore
public class ReviewModel {

    // --- Campos da Coleção "avaliacoes" ---
    private double rating; // Nota (ex: 4.5)
    private String comentario;
    private Date dataAvaliacao; // O Firebase Timestamp será convertido para Date

    private String alunoId;
    private String professorId;

    private String alunoNome;
    private String alunoAvatarUrl;

    private String professorNome;
    private String professorAvatarUrl;

    private String escritoPor; // "aluno" ou "professor"

    // Construtor vazio (OBRIGATÓRIO para o Firestore)
    public ReviewModel() {
    }

    // --- Getters ---
    // (O Firestore usa os getters para ler os dados)

    public double getRating() {
        return rating;
    }

    public String getComentario() {
        return comentario;
    }

    @ServerTimestamp // Pega a data do servidor do Firebase
    public Date getDataAvaliacao() {
        return dataAvaliacao;
    }

    public String getAlunoId() {
        return alunoId;
    }

    public String getProfessorId() {
        return professorId;
    }

    public String getAlunoNome() {
        return alunoNome;
    }

    public String getAlunoAvatarUrl() {
        return alunoAvatarUrl;
    }

    public String getProfessorNome() {
        return professorNome;
    }

    public String getProfessorAvatarUrl() {
        return professorAvatarUrl;
    }

    public String getEscritoPor() {
        return escritoPor;
    }
}