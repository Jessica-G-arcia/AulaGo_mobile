package com.example.aulago;

public class Aluno {
    // Atributos para guardar as informações de cada aluno
    private String fotoRef;
    private float rating;
    private String nome;
    private String idioma;
    private String citacao;
    private String autorCitacao;

    public Aluno () {}

    public Aluno(String fotoRef, float rating, String nome, String idioma, String citacao, String autorCitacao) {
        this.fotoRef = fotoRef;
        this.rating = rating;
        this.nome = nome;
        this.idioma = idioma;
        this.citacao = citacao;
        this.autorCitacao = autorCitacao;
    }

    public String getFotoRef() { return fotoRef; }
    public float getRating() { return rating; }
    public String getNome() { return nome; }
    public String getIdioma() { return idioma; }
    public String getCitacao() { return citacao; }
    public String getAutorCitacao() { return autorCitacao; }
}