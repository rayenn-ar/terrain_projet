package com.clubsportif.model;

import com.clubsportif.model.enums.TypeTerrain;

import java.math.BigDecimal;
import java.util.Objects;

public class Terrain {

    private int idTerrain;
    private String nom;
    private TypeTerrain type;
    private BigDecimal prixParHeure;
    private boolean disponibilite;
    private String description;

    public Terrain() {}

    public Terrain(int idTerrain, String nom, TypeTerrain type,
                   BigDecimal prixParHeure, boolean disponibilite, String description) {
        this.idTerrain = idTerrain;
        this.nom = nom;
        this.type = type;
        this.prixParHeure = prixParHeure;
        this.disponibilite = disponibilite;
        this.description = description;
    }

    // ---- Getters & Setters ----

    public int getIdTerrain() { return idTerrain; }
    public void setIdTerrain(int idTerrain) { this.idTerrain = idTerrain; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public TypeTerrain getType() { return type; }
    public void setType(TypeTerrain type) { this.type = type; }

    public BigDecimal getPrixParHeure() { return prixParHeure; }
    public void setPrixParHeure(BigDecimal prixParHeure) { this.prixParHeure = prixParHeure; }

    public boolean isDisponibilite() { return disponibilite; }
    public void setDisponibilite(boolean disponibilite) { this.disponibilite = disponibilite; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terrain)) return false;
        Terrain that = (Terrain) o;
        return idTerrain == that.idTerrain;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTerrain);
    }

    @Override
    public String toString() {
        return "Terrain{id=" + idTerrain + ", nom='" + nom + "', type=" + type +
               ", prix=" + prixParHeure + "/h, dispo=" + disponibilite + "}";
    }
}
