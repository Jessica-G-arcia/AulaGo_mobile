package com.example.aulago;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName; // Importante para garantir o mapeamento

public class ClassModel {

    // Atores
    private String alunoId;
    private String professorId;
    private String alunoNome;
    private String professorNome;
    private String alunoAvatarUrl;
    private String professorAvatarUrl;

    // Detalhes da Aula
    private String idioma;
    private String local;

    // Agendamento
    private Timestamp dataTimestamp;
    private String horarioInicio;
    private String horarioFim;

    // Status e Avaliação
    private String status;
    private float avaliacao;
    private String modalidade;

    // Construtor vazio - ESSENCIAL para o Firestore
    public ClassModel() {
    }

    // --- Getters e Setters ---

    // IMPORTANTE: Anotações @PropertyName garantem que o filtro do banco funcione
    @PropertyName("dataTimestamp")
    public Timestamp getDataTimestamp() {
        return dataTimestamp;
    }

    @PropertyName("dataTimestamp")
    public void setDataTimestamp(Timestamp dataTimestamp) {
        this.dataTimestamp = dataTimestamp;
    }

    @PropertyName("alunoId")
    public String getAlunoId() {
        return alunoId;
    }

    @PropertyName("alunoId")
    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    @PropertyName("professorId")
    public String getProfessorId() {
        return professorId;
    }

    @PropertyName("professorId")
    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    // --- Outros Getters e Setters (padrão) ---

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

    public float getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(float avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getAlunoAvatarUrl() {
        return alunoAvatarUrl;
    }

    public void setAlunoAvatarUrl(String alunoAvatarUrl) {
        this.alunoAvatarUrl = alunoAvatarUrl;
    }

    public String getProfessorAvatarUrl() {
        return professorAvatarUrl;
    }

    public void setProfessorAvatarUrl(String professorAvatarUrl) {
        this.professorAvatarUrl = professorAvatarUrl;
    }
}