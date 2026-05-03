package com.clubsportif.model.enums;

public enum StatutReservation {
    CONFIRMEE("Confirmée"),
    ANNULEE("Annulée"),
    TERMINEE("Terminée");

    private final String libelle;

    StatutReservation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
