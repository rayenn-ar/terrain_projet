package com.clubsportif.service.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du validateur d'adresses e-mail.
 */
@DisplayName("EmailValidator - Validation du format e-mail")
class EmailValidatorTest {

    // ── Emails VALIDES ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Email standard : user@domain.com")
    void emailStandard_retourneTrue() {
        assertTrue(EmailValidator.isValid("jean.dupont@email.com"));
    }

    @Test
    @DisplayName("Email avec point dans le domaine")
    void emailAvecSousDomaineRetourneTrue() {
        assertTrue(EmailValidator.isValid("admin@clubsportif.fr"));
    }

    @Test
    @DisplayName("Email avec chiffres")
    void emailAvecChiffres_retourneTrue() {
        assertTrue(EmailValidator.isValid("user42@example.org"));
    }

    @Test
    @DisplayName("Email avec tirets et underscores")
    void emailAvecCaracteresSpeciaux_retourneTrue() {
        assertTrue(EmailValidator.isValid("jean_dupont@mon-club.com"));
    }

    @Test
    @DisplayName("Email majuscules normalisé")
    void emailMajuscules_retourneTrue() {
        // isValid est sensible à la casse, normalize() doit être appelé avant
        String normalized = EmailValidator.normalize("JEAN@DOMAIN.COM");
        assertTrue(EmailValidator.isValid(normalized));
    }

    // ── Emails INVALIDES ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Null : retourne false")
    void null_retourneFalse() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    @DisplayName("Chaîne vide : retourne false")
    void chaineVide_retourneFalse() {
        assertFalse(EmailValidator.isValid(""));
    }

    @Test
    @DisplayName("Sans @: retourne false")
    void sansArobase_retourneFalse() {
        assertFalse(EmailValidator.isValid("jeanexample.com"));
    }

    @Test
    @DisplayName("Sans domaine : retourne false")
    void sansDomaine_retourneFalse() {
        assertFalse(EmailValidator.isValid("jean@"));
    }

    @Test
    @DisplayName("Sans extension de domaine : retourne false")
    void sansTLD_retourneFalse() {
        assertFalse(EmailValidator.isValid("jean@domaine"));
    }

    @Test
    @DisplayName("Double @ : retourne false")
    void doubleArobase_retourneFalse() {
        assertFalse(EmailValidator.isValid("jean@@domain.com"));
    }

    @Test
    @DisplayName("Espaces dans l'email : retourne false")
    void avecEspaces_retourneFalse() {
        assertFalse(EmailValidator.isValid("jean dupont@domain.com"));
    }

    // ── normalize() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("normalize : trim + lowercase")
    void normalize_trimEtLowercase() {
        assertEquals("jean@example.com", EmailValidator.normalize("  JEAN@Example.COM  "));
    }

    @Test
    @DisplayName("normalize : null retourne chaîne vide")
    void normalize_nullRetourneVide() {
        assertEquals("", EmailValidator.normalize(null));
    }
}
