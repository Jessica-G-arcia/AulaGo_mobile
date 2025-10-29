package com.example.aulago;
public enum Modalidade {
    PRESENCIAL(R.color.aula_presencial),
    ONLINE(R.color.aula_online);

    private final int colorResId;

    Modalidade(int colorResId) {
        this.colorResId = colorResId;
    }

    public int getColorResId() {
        return colorResId;
    }
}
