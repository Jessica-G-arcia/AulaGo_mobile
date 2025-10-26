package com.example.aulago;

import java.io.Serializable;

public class DadosUsuario implements Serializable {
    public String nome;
    public String cpf;
    public String dtNasc;
    public String telefone;
    public String email;
    public String senha;
    public String endereco;
    public String numero;
    public String complemento;
    public String cep;
    public String bairro;
    public String cidade;
    public String genero;

    public DadosUsuario(String nome, String cpf, String dtNasc, String telefone, String email, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.dtNasc = dtNasc;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
    }

    public DadosUsuario(){
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    public String getDtNasc() {
        return dtNasc;
    }
    public void setDtNasc(String dtNasc) {
        this.dtNasc = dtNasc;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    public String getNumero() {
        return numero;
    }
    public void setNumero(String numero) {
        this.numero = numero;
    }
    public String getComplemento() {
        return complemento;
    }
    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
    public String getCep() {
        return cep;
    }
    public void setCep(String cep) {
        this.cep = cep;
    }
    public String getBairro() {
        return bairro;
    }
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
    public String getCidade() {
        return cidade;
    }
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    public String getGenero() {
        return genero;
    }
    public void setGenero(String genero) {
        this.genero = genero;
    }


}