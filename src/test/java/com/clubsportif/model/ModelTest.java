package com.clubsportif.model;

import com.clubsportif.model.enums.Role;
import com.clubsportif.model.enums.StatutReservation;
import com.clubsportif.model.enums.TypeTerrain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests des modèles et des enums.
 * Couvre les scénarios SC-MOD-01 à SC-MOD-11.
 */
@SuppressWarnings("unused")
class ModelTest {

    // ---- Reservation ----

    @Test
    @DisplayName("SC-MOD-01 : getHeureFin() retourne heureDebut + dureeHeures")
    void reservation_getHeureFin() {
        Reservation r = new Reservation();
        r.setHeureDebut(LocalTime.of(10, 0));
        r.setDureeHeures(2);
        assertEquals(LocalTime.of(12, 0), r.getHeureFin());
    }

    @Test
    @DisplayName("SC-MOD-02 : getHeureFin() avec heureDebut null → null")
    void reservation_getHeureFin_null() {
        Reservation r = new Reservation();
        r.setDureeHeures(2);
        assertNull(r.getHeureFin());
    }

    @Test
    @DisplayName("SC-MOD-06 : Reservation constructeur complet")
    void reservation_fullConstructor() {
        Reservation r = new Reservation(1, 10, 20,
                LocalDate.of(2025, 6, 15), LocalTime.of(14, 0),
                2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
        assertEquals(1, r.getIdReservation());
        assertEquals(10, r.getIdUtilisateur());
        assertEquals(20, r.getIdTerrain());
        assertEquals(LocalDate.of(2025, 6, 15), r.getDateReservation());
        assertEquals(LocalTime.of(14, 0), r.getHeureDebut());
        assertEquals(2, r.getDureeHeures());
        assertEquals(StatutReservation.CONFIRMEE, r.getStatut());
        assertEquals(BigDecimal.valueOf(100), r.getMontantTotal());
    }

    // ---- Utilisateur ----

    @Test
    @DisplayName("SC-MOD-03 : isAdmin() avec rôle ADMIN → true")
    void utilisateur_isAdmin_true() {
        Utilisateur u = new Utilisateur();
        u.setRole(Role.ADMIN);
        assertTrue(u.isAdmin());
    }

    @Test
    @DisplayName("SC-MOD-04 : isAdmin() avec rôle CLIENT → false")
    void utilisateur_isAdmin_false() {
        Utilisateur u = new Utilisateur();
        u.setRole(Role.CLIENT);
        assertFalse(u.isAdmin());
    }

    @Test
    @DisplayName("SC-MOD-05 : isAdmin() avec rôle null → false")
    void utilisateur_isAdmin_nullRole() {
        Utilisateur u = new Utilisateur();
        assertFalse(u.isAdmin());
    }

    @Test
    @DisplayName("SC-MOD-08 : Utilisateur constructeur complet")
    void utilisateur_fullConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Utilisateur u = new Utilisateur(5, "Alice", "alice@test.com",
                "hash123", Role.CLIENT, now);
        assertEquals(5, u.getIdUtilisateur());
        assertEquals("Alice", u.getNom());
        assertEquals("alice@test.com", u.getEmail());
        assertEquals("hash123", u.getMotDePasseHash());
        assertEquals(Role.CLIENT, u.getRole());
        assertEquals(now, u.getDateInscription());
    }

    // ---- Terrain ----

    @Test
    @DisplayName("SC-MOD-07 : Terrain constructeur complet")
    void terrain_fullConstructor() {
        Terrain t = new Terrain(3, "Court A", TypeTerrain.TENNIS,
                BigDecimal.valueOf(50), true, "Gazon synthétique");
        assertEquals(3, t.getIdTerrain());
        assertEquals("Court A", t.getNom());
        assertEquals(TypeTerrain.TENNIS, t.getType());
        assertEquals(BigDecimal.valueOf(50), t.getPrixParHeure());
        assertTrue(t.isDisponibilite());
        assertEquals("Gazon synthétique", t.getDescription());
    }

    // ---- Enums ----

    @Test
    @DisplayName("SC-MOD-09 : Role enum — libellés corrects")
    void role_libelles() {
        assertEquals("Administrateur", Role.ADMIN.getLibelle());
        assertEquals("Client", Role.CLIENT.getLibelle());
        assertEquals(2, Role.values().length);
    }

    @Test
    @DisplayName("SC-MOD-10 : StatutReservation enum — libellés corrects")
    void statutReservation_libelles() {
        assertEquals("Confirmée", StatutReservation.CONFIRMEE.getLibelle());
        assertEquals("Annulée", StatutReservation.ANNULEE.getLibelle());
        assertEquals("Terminée", StatutReservation.TERMINEE.getLibelle());
        assertEquals(3, StatutReservation.values().length);
    }

    @Test
    @DisplayName("SC-MOD-11 : TypeTerrain enum — tous les types présents")
    void typeTerrain_libelles() {
        assertEquals("Football", TypeTerrain.FOOTBALL.getLibelle());
        assertEquals("Tennis", TypeTerrain.TENNIS.getLibelle());
        assertEquals("Basketball", TypeTerrain.BASKETBALL.getLibelle());
        assertEquals("Volleyball", TypeTerrain.VOLLEYBALL.getLibelle());
        assertEquals(4, TypeTerrain.values().length);
    }
}
