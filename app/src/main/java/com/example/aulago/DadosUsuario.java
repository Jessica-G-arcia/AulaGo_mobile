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
}