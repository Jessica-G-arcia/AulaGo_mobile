package com.example.aulago;

public class UserModel {

    // Campos do Firebase (devem existir na coleção "users")
    private String nome;
    private String fotoUrl;
    private String userType; // "aluno" ou "professor"
    private double ratingMedia; // Ex: 4.8 (NECESSÁRIO PARA ORDENAR)
    private String especialidade; // Ex: "Idioma(s): Inglês"

    // Opcional: para o card da sua foto
    private String quote; // "Aluna sensacional!..."
    private String tvUserQuote; // "Rogério Lima"

    // Construtor vazio para Firebase
    public UserModel() {}

    // --- Getters ---
    public String getNome() { return nome; }
    public String getFotoUrl() { return fotoUrl; }
    public String getUserType() { return userType; }
    public double getRatingMedia() { return ratingMedia; }
    public String getEspecialidade() { return especialidade; }
    public String getQuote() { return quote; }
    public String getQuoteAuthor() { return tvUserQuote; }
}