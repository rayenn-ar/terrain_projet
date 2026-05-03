package com.clubsportif.model.enums;

public enum TypeTerrain {
    FOOTBALL("Football"),
    TENNIS("Tennis"),
    BASKETBALL("Basketball"),
    VOLLEYBALL("Volleyball");

    private final String libelle;

    TypeTerrain(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
