package com.example.aulago;

import java.util.List;

public class UserModel {

    // Os nomes dos campos devem ser IDÊNTICOS aos do seu Firestore
    private String nome;
    private String fotoUrl;
    private String userType;
    private double ratingMedia;
    private String especialidade; // Pode manter como fallback
    private List<String> idiomas; // Mais flexível para o Adapter
    private String quote;
    private String quoteAuthor; // <<< CORRIGIDO: nome do campo correto

    // Construtor vazio obrigatório para o Firebase
    public UserModel() {}

    // --- Getters ---
    public String getNome() { return nome; }
    public String getFotoUrl() { return fotoUrl; }
    public String getUserType() { return userType; }
    public double getRatingMedia() { return ratingMedia; }
    public String getEspecialidade() { return especialidade; }
    public List<String> getIdiomas() { return idiomas; } // Getter para a lista
    public String getQuote() { return quote; }
    public String getQuoteAuthor() { return quoteAuthor; } // <<< CORRIGIDO: getter agora busca o campo certo
}