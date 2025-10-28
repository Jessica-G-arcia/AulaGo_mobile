package com.example.aulago;

public class Aluno {
    // Atributos para guardar as informações de cada aluno
    private final int fotoResourceId;
    private final float rating;
    private final String nome;
    private final String idioma;
    private final String citacao;
    private final String autorCitacao;

    public Aluno(int fotoResourceId, float rating, String nome, String idioma, String citacao, String autorCitacao) {
        this.fotoResourceId = fotoResourceId;
        this.rating = rating;
        this.nome = nome;
        this.idioma = idioma;
        this.citacao = citacao;
        this.autorCitacao = autorCitacao;
    }

    public int getFotoResourceId() { return fotoResourceId; }
    public float getRating() { return rating; }
    public String getNome() { return nome; }
    public String getIdioma() { return idioma; }
    public String getCitacao() { return citacao; }
    public String getAutorCitacao() { return autorCitacao; }
}