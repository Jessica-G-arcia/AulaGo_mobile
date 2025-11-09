package com.example.aulago;

import java.util.Date;

// 1. REMOVEMOS os imports desnecessários.
// 2. ADICIONAMOS todos os novos campos da nossa tabela.
// 3. ADICIONAMOS um construtor vazio (obrigatório para o Firestore).
// 4. ADICIONAMOS Getters e Setters para todos os campos.

public class ClassModel {

    // Atores
    private String alunoId;
    private String professorId;
    private String alunoNome;     // Substitui o antigo campo 'aluno'
    private String professorNome; // Novo

    // Detalhes da Aula
    private String idioma;
    private String local;
    // Adicione outros que desejar (nivel, modalidade, etc.)

    // Agendamento
    private Date data;             // Mantemos como Date
    private String horarioInicio;  // Substitui 'horario'
    private String horarioFim;     // Substitui 'horario'

    // Status
    private String status;         // Substitui 'concluida' (boolean)

    // Construtor vazio - ESSENCIAL para o Firestore
    public ClassModel() {
    }

    // (Opcional) Você pode manter um construtor completo para testes,
    // mas não é mais usado pelo CalendarActivity.

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

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
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
}