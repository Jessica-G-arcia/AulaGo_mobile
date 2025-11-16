package com.example.aulago;

import java.util.List;

public class UserModel {

    // Os nomes dos campos devem ser IDÊNTICOS aos do seu Firestore
    private String nome;
    private String urlFotoPerfil;
    private String userType;
    private String nivel;
    private double ratingMedia;
    private long ratingCount;
    private String especialidade; // Pode manter como fallback
    private List<String> idiomas; // Mais flexível para o Adapter
    private String comentarioMelhorAvaliado;
    private String autorComentarioMelhorAvaliado;
    private int totalAvaliacoes;


    // Construtor vazio obrigatório para o Firebase
    public UserModel() {
    }

    // --- Getters ---
    public String getNome() {
        return nome;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }



    public String getUserType() {
        return userType;
    }

    public double getRatingMedia() {
        return ratingMedia;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public List<String> getIdiomas() {
        return idiomas;
    } // Getter para a lista


    public void setUrlFotoPerfil(String urlFotoPerfil) {
        this.urlFotoPerfil = urlFotoPerfil;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getComentarioMelhorAvaliado() {
        return comentarioMelhorAvaliado;
    }

    public void setComentarioMelhorAvaliado(String comentarioMelhorAvaliado) {
        this.comentarioMelhorAvaliado = comentarioMelhorAvaliado;
    }

    public String getAutorComentarioMelhorAvaliado() {
        return autorComentarioMelhorAvaliado;
    }

    public void setAutorComentarioMelhorAvaliado(String autorComentarioMelhorAvaliado) {
        this.autorComentarioMelhorAvaliado = autorComentarioMelhorAvaliado;
    }

    public int getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(int totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setRatingMedia(double ratingMedia) {
        this.ratingMedia = ratingMedia;
    }
}