package com.example.aulago;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;

public class Professor implements Parcelable {

    // 1. Variável para o ID (usada no SearchFragment)
    private String id;

    // Variáveis que devem corresponder aos campos do Firestore
    private String nome;
    private String email;
    private String urlFotoPerfil;
    private String bio;
    private String userType;
    private String especialidade; // Exemplo de campo específico de Professor
    private double rating;
    private long reviewCount;

    // 2. Construtor vazio (OBRIGATÓRIO para o Firebase)
    public Professor() {}

    // --- Getters e Setters ---
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getUrlFotoPerfil() { return urlFotoPerfil; }
    public String getBio() { return bio; }
    public String getUserType() { return userType; }
    public String getEspecialidade() { return especialidade; }
    public double getRating() { return rating; }
    public long getReviewCount() { return reviewCount; }

    // Setter para o ID (Usado no fetchProfessoresFromFirestore)
    public void setId(String id) { this.id = id; }

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
}