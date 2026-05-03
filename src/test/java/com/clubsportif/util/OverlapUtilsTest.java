package com.clubsportif.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de l'algorithme de chevauchement d'intervalles horaires (Règle R1).
 *
 * Algorithme : deux intervalles [A, B] et [C, D] se chevauchent si A < D ET C < B.
 */
@DisplayName("OverlapUtils - Algorithme de chevauchement (Règle R1)")
class OverlapUtilsTest {

    // ── Cas de CHEVAUCHEMENT (conflit attendu) ─────────────────────────────────

    @Test
    @DisplayName("Chevauchement partiel : B débute pendant A")
    void chevauchementPartiel_BdebutePendantA() {
        // A = [10:00, 12:00], B = [11:00, 13:00]
        assertTrue(OverlapUtils.chevauchement(
            LocalTime.of(10, 0), LocalTime.of(12, 0),
            LocalTime.of(11, 0), LocalTime.of(13, 0)
        ));
    }

    @Test
    @DisplayName("Chevauchement partiel : A débute pendant B")
    void chevauchementPartiel_AdebutePendantB() {
        // A = [11:00, 13:00], B = [10:00, 12:00]
        assertTrue(OverlapUtils.chevauchement(
            LocalTime.of(11, 0), LocalTime.of(13, 0),
            LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }

    @Test
    @DisplayName("Chevauchement total : B est entièrement inclus dans A")
    void chevauchementTotal_BinclsDansA() {
        // A = [09:00, 14:00], B = [10:00, 12:00]
        assertTrue(OverlapUtils.chevauchement(
            LocalTime.of(9, 0),  LocalTime.of(14, 0),
            LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }

    @Test
    @DisplayName("Chevauchement total : A est entièrement inclus dans B")
    void chevauchementTotal_AinclsDansB() {
        // A = [10:00, 12:00], B = [09:00, 14:00]
        assertTrue(OverlapUtils.chevauchement(
            LocalTime.of(10, 0), LocalTime.of(12, 0),
            LocalTime.of(9, 0),  LocalTime.of(14, 0)
        ));
    }

    @Test
    @DisplayName("Chevauchement : intervalles identiques")
    void chevauchementIdentiques() {
        // A = B = [10:00, 12:00]
        assertTrue(OverlapUtils.chevauchement(
            LocalTime.of(10, 0), LocalTime.of(12, 0),
            LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }

    // ── Cas SANS CHEVAUCHEMENT (pas de conflit) ─────────────────────────────────

    @Test
    @DisplayName("Pas de chevauchement : B commence exactement quand A termine")
    void pasChevauchement_adjacentsFin() {
        // A = [10:00, 12:00], B = [12:00, 14:00]
        // Créneau adjacent : 12h est la FIN de A et le DÉBUT de B — pas de conflit
        assertFalse(OverlapUtils.chevauchement(
            LocalTime.of(10, 0), LocalTime.of(12, 0),
            LocalTime.of(12, 0), LocalTime.of(14, 0)
        ));
    }

    @Test
    @DisplayName("Pas de chevauchement : A commence exactement quand B termine")
    void pasChevauchement_adjacentsDebut() {
        // A = [12:00, 14:00], B = [10:00, 12:00]
        assertFalse(OverlapUtils.chevauchement(
            LocalTime.of(12, 0), LocalTime.of(14, 0),
            LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }

    @Test
    @DisplayName("Pas de chevauchement : B est entièrement avant A")
    void pasChevauchement_BavantA() {
        // A = [14:00, 16:00], B = [10:00, 12:00]
        assertFalse(OverlapUtils.chevauchement(
            LocalTime.of(14, 0), LocalTime.of(16, 0),
            LocalTime.of(10, 0), LocalTime.of(12, 0)
        ));
    }

    @Test
    @DisplayName("Pas de chevauchement : B est entièrement après A")
    void pasChevauchement_BapresA() {
        // A = [08:00, 10:00], B = [15:00, 17:00]
        assertFalse(OverlapUtils.chevauchement(
            LocalTime.of(8, 0),  LocalTime.of(10, 0),
            LocalTime.of(15, 0), LocalTime.of(17, 0)
        ));
    }

    @Test
    @DisplayName("Cas réel : deux réservations non conflictuelles dans la même journée")
    void casReel_TroisCreneauxSansConflit() {
        // Terrain 1 le même jour : 08h-10h, 10h-12h, 14h-16h — aucun conflit entre eux
        LocalTime d1 = LocalTime.of(8, 0),  f1 = LocalTime.of(10, 0);
        LocalTime d2 = LocalTime.of(10, 0), f2 = LocalTime.of(12, 0);
        LocalTime d3 = LocalTime.of(14, 0), f3 = LocalTime.of(16, 0);

        assertFalse(OverlapUtils.chevauchement(d1, f1, d2, f2));
        assertFalse(OverlapUtils.chevauchement(d1, f1, d3, f3));
        assertFalse(OverlapUtils.chevauchement(d2, f2, d3, f3));
    }
}
