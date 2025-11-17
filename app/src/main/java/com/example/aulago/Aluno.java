package com.example.aulago;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

public class Aluno implements Parcelable {

    // 1. Variável para o ID do aluno
    private String id;

    // Variáveis do seu documento no Firestore (Mantenha o mesmo nome)
    private String nome;
    private String email;
    private String urlFotoPerfil;
    private String bio;
    private String userType;
    private int idade;
    private double ratingMedia;
    private long ratingCount;


    // 2. Construtor vazio (OBRIGATÓRIO para o Firebase)
    public Aluno() {
    }

    // 3. Getters (Usados pelo Adapter e Fragment)
    public String getId() {
        return this.id;
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

    public int getIdade() {
        return idade;
    }


    // 4. Setter (USADO PARA DEFINIR O ID MANUALMENTE)
    // Este é o método que você chama no SearchFragment
    public void setId(String id) {
        this.id = id;
    }


    // --- Lógica do Parcelable (Usada para enviar o objeto entre telas) ---

    protected Aluno(Parcel in) {
        id = in.readString();
        nome = in.readString();
        email = in.readString();
        urlFotoPerfil = in.readString();
        bio = in.readString();
        userType = in.readString();
        idade = in.readInt();
        ratingCount = in.readLong();
        ratingMedia = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nome);
        dest.writeString(email);
        dest.writeString(urlFotoPerfil);
        dest.writeString(bio);
        dest.writeString(userType);
        dest.writeInt(idade);
        dest.writeLong(ratingCount);
        dest.writeDouble(ratingMedia);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Aluno> CREATOR = new Creator<Aluno>() {
        @Override
        public Aluno createFromParcel(Parcel in) {
            return new Aluno(in);
        }

        @Override
        public Aluno[] newArray(int size) {
            return new Aluno[size];
        }
    };

    public double getRatingMedia() {
        return ratingMedia;
    }

    public void setRatingMedia(double ratingMedia) {
        this.ratingMedia = ratingMedia;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }
}