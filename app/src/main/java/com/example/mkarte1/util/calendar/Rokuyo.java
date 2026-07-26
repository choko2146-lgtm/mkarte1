package com.example.mkarte1.util.calendar;

public enum Rokuyo {
    TAIAN("大安"),
    SHAKKO("赤口"),
    SENSHO("先勝"),
    TOMOBIKI("友引"),
    SENBU("先負"),
    BUTSUMETSU("仏滅");

    private final String displayName;

    Rokuyo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    static Rokuyo fromRemainder(int remainder) {
        switch (remainder) {
            case 0:
                return TAIAN;
            case 1:
                return SHAKKO;
            case 2:
                return SENSHO;
            case 3:
                return TOMOBIKI;
            case 4:
                return SENBU;
            case 5:
                return BUTSUMETSU;
            default:
                return null;
        }
    }
}
