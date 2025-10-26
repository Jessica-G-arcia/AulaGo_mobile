package com.example.aulago;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Aula {
    private String nomeAluno;
    private String horarioInicio;
    private String horarioFim;
    private String data; // Formato "yyyy-MM-dd"
    private Modalidade modalidade;

    // Construtor
    public Aula(String nomeAluno, String horarioInicio, String horarioFim, String data, Modalidade modalidade) {
        this.nomeAluno = nomeAluno;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.data = data;
        this.modalidade = modalidade;
    }

    // Getters e Setters
    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    // Método útil para exibir o horário completo
    public String getHorarioCompleto() {
        return horarioInicio + " - " + horarioFim;
    }

    // Método para verificar se a aula já passou
    public boolean jaOcorreu() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date dataDaAula = sdf.parse(this.data);
            Date hoje = new Date();
            // Compara se a data da aula é anterior à data de hoje (ignorando o horário)
            return dataDaAula.before(removeTime(hoje));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}



