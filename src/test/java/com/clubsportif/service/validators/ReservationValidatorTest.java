package com.clubsportif.service.validators;

import com.clubsportif.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du validateur de réservations (Règles R3, R5, R7).
 *
 * Ces tests sont 100% purs (pas de base de données, pas de mocks).
 */
@DisplayName("ReservationValidator - Règles de validation métier")
@SuppressWarnings("unused")
class ReservationValidatorTest {

    private LocalDate demain;
    private LocalDate hier;
    private LocalDate aujourd_hui;

    @BeforeEach
    void setUp() {
        aujourd_hui = LocalDate.now();
        demain      = aujourd_hui.plusDays(1);
        hier        = aujourd_hui.minusDays(1);
    }

    // ── Cas VALIDES ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Réservation valide : demain, 10h00, 2 heures")
    void reservationValide_casNominal() {
        assertDoesNotThrow(() ->
            ReservationValidator.valider(demain, LocalTime.of(10, 0), 2));
    }

    @Test
    @DisplayName("Réservation valide : heure limite 08h00, durée 1h")
    void reservationValide_heureOuverture() {
        assertDoesNotThrow(() ->
            ReservationValidator.valider(demain, LocalTime.of(8, 0), 1));
    }

    @Test
    @DisplayName("Réservation valide : 22h00 + 1h = 23h00 exactement (limite autorisée)")
    void reservationValide_limiteHeureFermeture() {
        assertDoesNotThrow(() ->
            ReservationValidator.valider(demain, LocalTime.of(22, 0), 1));
    }

    @Test
    @DisplayName("Réservation valide : durée maximale 4 heures")
    void reservationValide_dureMaximale() {
        assertDoesNotThrow(() ->
            ReservationValidator.valider(demain, LocalTime.of(10, 0), 4));
    }

    @Test
    @DisplayName("Réservation valide : durée minimale 1 heure")
    void reservationValide_dureMinimale() {
        assertDoesNotThrow(() ->
            ReservationValidator.valider(demain, LocalTime.of(15, 0), 1));
    }

    // ── Date invalide ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Date passée (hier) : lève ValidationException")
    void datePassee_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
            ReservationValidator.valider(hier, LocalTime.of(10, 0), 2));
        assertTrue(ex.getMessage().toLowerCase().contains("pass"));
    }

    // ── Heure de début invalide ──────────────────────────────────────────────────

    @Test
    @DisplayName("Heure avant ouverture (07h59) : lève ValidationException")
    void heureAvantOuverture_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
            ReservationValidator.valider(demain, LocalTime.of(7, 59), 1));
        assertTrue(ex.getMessage().contains("8") || ex.getMessage().toLowerCase().contains("avant"));
    }

    // ── Durée invalide ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Durée 0 : lève ValidationException")
    void dureeNulle_lanceException() {
        assertNotNull(assertThrows(ValidationException.class, () ->
            ReservationValidator.valider(demain, LocalTime.of(10, 0), 0)));
    }

    @Test
    @DisplayName("Durée 5 (supérieur à max) : lève ValidationException")
    void dureeTropLongue_lanceException() {
        assertNotNull(assertThrows(ValidationException.class, () ->
            ReservationValidator.valider(demain, LocalTime.of(10, 0), 5)));
    }

    // ── Dépassement de l'heure de fermeture (Règle R7) ──────────────────────────

    @Test
    @DisplayName("R7 : 22h00 + 2h = 24h — dépasse la fermeture à 23h")
    void heureFinDepasseFermeture_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
            ReservationValidator.valider(demain, LocalTime.of(22, 0), 2));
        assertTrue(ex.getMessage().contains("23") || ex.getMessage().toLowerCase().contains("ferme"));
    }

    @Test
    @DisplayName("R7 : 21h00 + 4h = 25h — dépasse la fermeture")
    void heureDebutTardive_avecDureMax_lanceException() {
        assertNotNull(assertThrows(ValidationException.class, () ->
            ReservationValidator.valider(demain, LocalTime.of(21, 0), 4)));
    }
}
