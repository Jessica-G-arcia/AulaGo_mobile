package com.example.aulago;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DadosUsuario implements Serializable {
    private String uid;
    private String nome;
    private String email;
    private String telefone;
    private String genero;
    private String dtNasc;
    private String cpf;
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String cep;

    // Campos para roles
    private List<String> roles; // ["aluno", "professor"]
    private List<String> rolesPendentes; // ["professor"]

    // Campos de validação de professor
    private String tipoCertificacao;
    private String numeroCertificado;
    private String instituicaoCertificacao;
    private String nomeCompletoCertificado; // <-- CAMPO ADICIONADO
    private String pontuacaoCertificado; // Para TOEFL/IELTS
    private String certificadoUrl;
    private Long dataSolicitacao;
    private String statusSolicitacao; // "pendente_analise", "aprovado", "rejeitado"

    // Campos de Aprovação/Rejeição
    private boolean professorVerificado;
    private Long dataAprovacao;
    private String motivoRejeicao;
    private String idiomaProfessor; // "ingles"

    // Construtores
    public DadosUsuario() {
        this.roles = new ArrayList<>();
        this.roles.add("aluno"); // Default
        this.professorVerificado = false;
    }

    // Getters e Setters (Resumidos por brevidade)
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getDtNasc() { return dtNasc; }
    public void setDtNasc(String dtNasc) { this.dtNasc = dtNasc; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public List<String> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
            roles.add("aluno");
        }
        return roles;
    }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getRolesPendentes() { return rolesPendentes; }
    public void setRolesPendentes(List<String> rolesPendentes) { this.rolesPendentes = rolesPendentes; }

    public String getTipoCertificacao() { return tipoCertificacao; }
    public void setTipoCertificacao(String tipoCertificacao) { this.tipoCertificacao = tipoCertificacao; }

    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String numeroCertificado) { this.numeroCertificado = numeroCertificado; }

    public String getInstituicaoCertificacao() { return instituicaoCertificacao; }
    public void setInstituicaoCertificacao(String instituicaoCertificacao) { this.instituicaoCertificacao = instituicaoCertificacao; }

    // <-- GETTER E SETTER ADICIONADOS -->
    public String getNomeCompletoCertificado() { return nomeCompletoCertificado; }
    public void setNomeCompletoCertificado(String nomeCompletoCertificado) { this.nomeCompletoCertificado = nomeCompletoCertificado; }

    public String getPontuacaoCertificado() { return pontuacaoCertificado; }
    public void setPontuacaoCertificado(String pontuacaoCertificado) { this.pontuacaoCertificado = pontuacaoCertificado; }

    public String getCertificadoUrl() { return certificadoUrl; }
    public void setCertificadoUrl(String certificadoUrl) { this.certificadoUrl = certificadoUrl; }

    public String getStatusSolicitacao() { return statusSolicitacao; }
    public void setStatusSolicitacao(String statusSolicitacao) { this.statusSolicitacao = statusSolicitacao; }


    public boolean isProfessorVerificado() { return professorVerificado; }
    public void setProfessorVerificado(boolean professorVerificado) { this.professorVerificado = professorVerificado; }

    public Long getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(Long dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public Long getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(Long dataAprovacao) { this.dataAprovacao = dataAprovacao; }

    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }

    public String getIdiomaProfessor() { return idiomaProfessor; }
    public void setIdiomaProfessor(String idiomaProfessor) { this.idiomaProfessor = idiomaProfessor; }

    // Métodos auxiliares
    public boolean isProfessor() {
        return roles != null && roles.contains("professor");
    }

    public boolean isAluno() {
        return roles != null && roles.contains("aluno");
    }

    public boolean temSolicitacaoPendente() {
        return "pendente_analise".equals(statusSolicitacao);
    }
}