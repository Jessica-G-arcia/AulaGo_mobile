package com.example.aulago;

public class Language {
    private String name;
    private String flagRef; // Usa o ID do drawable

    public Language () {}

    public Language(String name, String flagRef) {
        this.name = name;
        this.flagRef = flagRef;
    }

    public String getName() {
        return name;
    }

    public String getFlagRef() {
        return flagRef;
    }
}