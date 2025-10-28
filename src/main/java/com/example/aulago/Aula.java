package com.example.aulago;

public class Aula {
    private String aluno;
    private String local;
    private String horario;
    private String data;

    // Novos campos para a tela de Aulas
    private String idioma;
    private float avaliacao;
    private boolean concluida;

    public Aula() {} // construtor vazioo obrigatório para o firebase

    public Aula(String aluno, String local, String horario, String data, String idioma, float avaliacao, boolean concluida) {
        this.aluno = aluno;
        this.local = local;
        this.horario = horario;
        this.data = data;
        this.idioma = idioma;
        this.avaliacao = avaliacao;
        this.concluida = concluida;
    }

    // Getters
    public String getAluno() { return aluno; }
    public String getLocal() { return local; }
    public String getHorario() { return horario; }
    public String getData() { return data; }
    public String getIdioma() { return idioma; }
    public float getAvaliacao() { return avaliacao; }
    public boolean isConcluida() { return concluida; }
}