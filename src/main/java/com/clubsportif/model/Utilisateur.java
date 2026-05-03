package com.clubsportif.model;

import com.clubsportif.model.enums.Role;

import java.time.LocalDateTime;
import java.util.Objects;

public class Utilisateur {

    private int idUtilisateur;
    private String nom;
    private String email;
    private String motDePasseHash;
    private Role role;
    private LocalDateTime dateInscription;

    public Utilisateur() {}

    public Utilisateur(int idUtilisateur, String nom, String email,
                       String motDePasseHash, Role role, LocalDateTime dateInscription) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
        this.dateInscription = dateInscription;
    }

    // ---- Getters & Setters ----

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    // ---- Méthodes utilitaires ----

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur)) return false;
        Utilisateur that = (Utilisateur) o;
        return idUtilisateur == that.idUtilisateur;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtilisateur);
    }

    @Override
    public String toString() {
        return "Utilisateur{id=" + idUtilisateur + ", nom='" + nom +
               "', email='" + email + "', role=" + role + "}";
    }
}
