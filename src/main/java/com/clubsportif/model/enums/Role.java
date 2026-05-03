package com.clubsportif.model.enums;

public enum Role {
    ADMIN("Administrateur"),
    CLIENT("Client");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
