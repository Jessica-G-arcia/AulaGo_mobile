package com.example.aulago;

import java.io.Serializable;

public class DadosUsuario implements Serializable {
    // Campos da primeira tela
    public String nome;
    public String cpf;
    public String dtNasc;
    public String telefone;
    public String email;
    public String senha;

    // Campos da segunda tela
    public String endereco;
    public String numero;
    public String complemento;
    public String cep;
    public String bairro;
    public String cidade;
    public String genero;
    public String estado;

    // Construtor para os dados da primeira tela
    public DadosUsuario(String nome, String cpf, String dtNasc, String telefone, String email, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.dtNasc = dtNasc;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
    }

    // Construtor vazio necessário para alguns usos do Firebase
    public DadosUsuario() {
    }
}