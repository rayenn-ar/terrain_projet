package com.clubsportif.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests du module sécurité — SecurityConfig (BCrypt).
 * Couvre les scénarios SC-SEC-01 à SC-SEC-09.
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored", "ThrowableResultOfMethodCallIgnored"})
class SecurityConfigTest {

    @Test
    @DisplayName("SC-SEC-01 : Hashage d'un mot de passe retourne un hash non null")
    void hashPassword_valid() {
        String hash = SecurityConfig.hashPassword("Test@1234");
        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    @DisplayName("SC-SEC-02 : Vérification mot de passe correct → true")
    void checkPassword_correct() {
        String hash = SecurityConfig.hashPassword("MonMotDePasse1");
        assertTrue(SecurityConfig.checkPassword("MonMotDePasse1", hash));
    }

    @Test
    @DisplayName("SC-SEC-03 : Vérification mot de passe incorrect → false")
    void checkPassword_incorrect() {
        String hash = SecurityConfig.hashPassword("MonMotDePasse1");
        assertFalse(SecurityConfig.checkPassword("MauvaisMDP1", hash));
    }

    @Test
    @DisplayName("SC-SEC-04 : Deux hashages du même mot de passe → hashes différents (salt)")
    void hashPassword_differentSalts() {
        String hash1 = SecurityConfig.hashPassword("IdentiqueMDP1");
        String hash2 = SecurityConfig.hashPassword("IdentiqueMDP1");
        assertNotEquals(hash1, hash2, "Les deux hashes doivent être différents grâce au salt");
        // Mais les deux doivent correspondre au même mot de passe
        assertTrue(SecurityConfig.checkPassword("IdentiqueMDP1", hash1));
        assertTrue(SecurityConfig.checkPassword("IdentiqueMDP1", hash2));
    }

    @Test
    @DisplayName("SC-SEC-05 : Hashage avec mot de passe null → IllegalArgumentException")
    void hashPassword_null() {
        assertNotNull(assertThrows(IllegalArgumentException.class, () -> SecurityConfig.hashPassword(null)));
    }

    @Test
    @DisplayName("SC-SEC-06 : Hashage avec mot de passe vide → IllegalArgumentException")
    void hashPassword_blank() {
        assertNotNull(assertThrows(IllegalArgumentException.class, () -> SecurityConfig.hashPassword("   ")));
    }

    @Test
    @DisplayName("SC-SEC-07 : Vérification avec password null → false")
    void checkPassword_nullPassword() {
        String hash = SecurityConfig.hashPassword("Valid1234");
        assertFalse(SecurityConfig.checkPassword(null, hash));
    }

    @Test
    @DisplayName("SC-SEC-08 : Vérification avec hash null → false")
    void checkPassword_nullHash() {
        assertFalse(SecurityConfig.checkPassword("Valid1234", null));
    }

    @Test
    @DisplayName("SC-SEC-09 : Vérification avec hash invalide → false (pas d'exception)")
    void checkPassword_invalidHash() {
        assertFalse(SecurityConfig.checkPassword("Valid1234", "not-a-bcrypt-hash"));
    }
}
