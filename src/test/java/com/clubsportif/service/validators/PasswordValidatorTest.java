package com.clubsportif.service.validators;

import com.clubsportif.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du validateur de mots de passe.
 */
@DisplayName("PasswordValidator - Validation de la robustesse des mots de passe")
class PasswordValidatorTest {

    // ── Mots de passe VALIDES ──────────────────────────────────────────────────

    @Test
    @DisplayName("Mot de passe valide : majuscule + chiffre + 8 chars")
    void motDePasseValide_standard() {
        assertDoesNotThrow(() -> PasswordValidator.valider("Test1234"));
    }

    @Test
    @DisplayName("Mot de passe valide : format utilisé dans les données de test")
    void motDePasseValide_formatTest() {
        assertDoesNotThrow(() -> PasswordValidator.valider("Test@1234"));
    }

    @Test
    @DisplayName("Mot de passe valide : exactement 8 caractères")
    void motDePasseValide_longueurExacte() {
        assertDoesNotThrow(() -> PasswordValidator.valider("Abcdef1g"));
    }

    @Test
    @DisplayName("Mot de passe valide : long avec plusieurs majuscules")
    void motDePasseValide_long() {
        assertDoesNotThrow(() -> PasswordValidator.valider("MonSuperMotDePasse2025"));
    }

    @Test
    @DisplayName("estValide : retourne true pour un mot de passe valide")
    void estValide_retourneTrue() {
        assertTrue(PasswordValidator.estValide("Test@1234"));
    }

    // ── Mots de passe INVALIDES ────────────────────────────────────────────────

    @Test
    @DisplayName("Trop court : 7 characters")
    void tropCourt_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> PasswordValidator.valider("Test123"));
        assertTrue(ex.getMessage().contains("8"));
    }

    @Test
    @DisplayName("Null : lève une exception")
    void null_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> PasswordValidator.valider(null)));
    }

    @Test
    @DisplayName("Chaîne vide : lève une exception")
    void chaineVide_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> PasswordValidator.valider(""));
        assertNotNull(ex);
    }

    @Test
    @DisplayName("Aucune majuscule : lève une exception")
    void aucuneMajuscule_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> PasswordValidator.valider("test1234"));
        assertTrue(ex.getMessage().toLowerCase().contains("majuscule"));
    }

    @Test
    @DisplayName("Aucun chiffre : lève une exception")
    void aucunChiffre_lanceException() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> PasswordValidator.valider("TestMotDePasse"));
        assertTrue(ex.getMessage().toLowerCase().contains("chiffre"));
    }

    @Test
    @DisplayName("estValide : retourne false pour un mot de passe invalide")
    void estValide_retourneFalse() {
        assertFalse(PasswordValidator.estValide("abc"));
        assertFalse(PasswordValidator.estValide("alllowercase1"));
        assertFalse(PasswordValidator.estValide("ALLUPPERCASE"));
    }
}
