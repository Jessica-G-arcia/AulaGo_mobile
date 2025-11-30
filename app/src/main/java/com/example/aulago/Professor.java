package com.example.aulago;

import android.media.Rating;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

public class Professor implements Parcelable {

    // 1. Variável para o ID (usada no SearchFragment)
    private String id;

    // Variáveis que devem corresponder aos campos do Firestore
    private String nome;
    private String email;
    private String urlFotoPerfil;
    private String bio;
    private String userType;
    private Integer idade;
    private String especialidade; // Exemplo de campo específico de Professor
    private double rating;
    private String idioma;
    private long reviewCount;
    private double ratingMedia;
    private long ratingCount;
    private String preferenciaModalidade;
    private GeoPoint localizacao;
    private double distanciaCalculada;
    private boolean isExpanded;






    // 2. Construtor vazio (OBRIGATÓRIO para o Firebase)
    public Professor() {
    }

    // --- Getters e Setters ---
    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public String getBio() {
        return bio;
    }

    public String getUserType() {
        return userType;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public double getRating() {
        return rating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getRatingMedia() {
        return ratingMedia;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingMedia(double ratingMedia) {
        this.ratingMedia = ratingMedia;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    // --- Lógica do Parcelable ---

    protected Professor(Parcel in) {
        id = in.readString();
        nome = in.readString();
        email = in.readString();
        urlFotoPerfil = in.readString();
        bio = in.readString();
        userType = in.readString();
        especialidade = in.readString();
        rating = in.readDouble();
        reviewCount = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nome);
        dest.writeString(email);
        dest.writeString(urlFotoPerfil);
        dest.writeString(bio);
        dest.writeString(userType);
        dest.writeString(especialidade);
        dest.writeDouble(rating);
        dest.writeLong(reviewCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Professor> CREATOR = new Creator<Professor>() {
        @Override
        public Professor createFromParcel(Parcel in) {
            return new Professor(in);
        }

        @Override
        public Professor[] newArray(int size) {
            return new Professor[size];
        }
    };

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }


    public GeoPoint getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(GeoPoint localizacao) {
        this.localizacao = localizacao;
    }

    public double getDistanciaCalculada() {
        return distanciaCalculada;
    }

    public void setDistanciaCalculada(double distanciaCalculada) {
        this.distanciaCalculada = distanciaCalculada;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }


    public String getPreferenciaModalidade() {
        return preferenciaModalidade;
    }

    public void setPreferenciaModalidade(String preferenciaModalidade) {
        this.preferenciaModalidade = preferenciaModalidade;
    }
}