package com.example.aulago;

public class Language {
    private String name;
    private int flagResourceId; // Usa o ID do drawable

    public Language(String name, int flagResourceId) {
        this.name = name;
        this.flagResourceId = flagResourceId;
    }

    public String getName() {
        return name;
    }

    public int getFlagResourceId() {
        return flagResourceId;
    }
}