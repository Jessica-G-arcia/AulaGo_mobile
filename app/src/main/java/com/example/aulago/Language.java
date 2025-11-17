package com.example.aulago;

public class Language {
    private String name;
    private String flagUrl; // Usa o ID do drawable

    public Language () {}

    public Language(String name, String flagUrl) {
        this.name = name;
        this.flagUrl = flagUrl;
    }

    public String getName() {
        return name;
    }

    public String getFlagUrl() {
        return flagUrl;
    }
}