package com.clubsportif.model;

import com.clubsportif.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation {

    private int idReservation;
    private int idUtilisateur;
    private int idTerrain;
    private LocalDate dateReservation;
    private LocalTime heureDebut;
    private int dureeHeures;
    private StatutReservation statut;
    private BigDecimal montantTotal;

    // Champs issus des jointures SQL (affichage uniquement)
    private String nomClient;
    private String emailClient;
    private String nomTerrain;
    private String typeTerrain;

    public Reservation() {}

    public Reservation(int idReservation, int idUtilisateur, int idTerrain,
                       LocalDate dateReservation, LocalTime heureDebut,
                       int dureeHeures, StatutReservation statut, BigDecimal montantTotal) {
        this.idReservation = idReservation;
        this.idUtilisateur = idUtilisateur;
        this.idTerrain = idTerrain;
        this.dateReservation = dateReservation;
        this.heureDebut = heureDebut;
        this.dureeHeures = dureeHeures;
        this.statut = statut;
        this.montantTotal = montantTotal;
    }

    /** Calcule l'heure de fin du créneau. */
    public LocalTime getHeureFin() {
        return heureDebut != null ? heureDebut.plusHours(dureeHeures) : null;
    }

    // ---- Getters & Setters ----

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getIdTerrain() { return idTerrain; }
    public void setIdTerrain(int idTerrain) { this.idTerrain = idTerrain; }

    public LocalDate getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public int getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(int dureeHeures) { this.dureeHeures = dureeHeures; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public String getNomTerrain() { return nomTerrain; }
    public void setNomTerrain(String nomTerrain) { this.nomTerrain = nomTerrain; }

    public String getTypeTerrain() { return typeTerrain; }
    public void setTypeTerrain(String typeTerrain) { this.typeTerrain = typeTerrain; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return idReservation == that.idReservation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReservation);
    }

    @Override
    public String toString() {
        return "Reservation{id=" + idReservation +
               ", idUtilisateur=" + idUtilisateur +
               ", idTerrain=" + idTerrain +
               ", date=" + dateReservation +
               ", heure=" + heureDebut +
               ", duree=" + dureeHeures + "h" +
               ", statut=" + statut +
               ", montant=" + montantTotal + "}";
    }
}
