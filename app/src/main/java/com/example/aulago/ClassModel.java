package com.example.aulago;

import com.google.firebase.Timestamp;

// Esta classe substitui a antiga classe "Aula"
public class ClassModel {

    // Atores
    private String alunoId;
    private String professorId;
    private String alunoNome;
    private String professorNome;

    // Detalhes da Aula
    private String idioma;
    private String local;

    // Agendamento
    private Timestamp dataTimestamp;
    private String horarioInicio;
    private String horarioFim;

    // Status e Avaliação
    private String status;         // Substitui 'concluida' (Ex: "Pendente", "Concluída")
    private float avaliacao; // Campo trazido da classe "Aula"
    private String modalidade;

    // Construtor vazio - ESSENCIAL para o Firestore
    public ClassModel() {
    }

    // --- Getters e Setters para TODOS os campos ---

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }

    public String getProfessorNome() {
        return professorNome;
    }

    public void setProfessorNome(String professorNome) {
        this.professorNome = professorNome;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }


    public String getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public String getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(String horarioFim) {
        this.horarioFim = horarioFim;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter e Setter para o novo campo
    public float getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(float avaliacao) {
        this.avaliacao = avaliacao;
    }


    public Timestamp getDataTimestamp() {
        return dataTimestamp;
    }

    public void setDataTimestamp(Timestamp dataTimestamp) {
        this.dataTimestamp = dataTimestamp;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }
}