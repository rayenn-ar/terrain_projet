package com.clubsportif.config;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Configuration de la sécurité : hashage et vérification des mots de passe avec BCrypt.
 */
public final class SecurityConfig {

    private SecurityConfig() {}

    /**
     * Hashe un mot de passe en clair avec BCrypt.
     *
     * @param plainPassword Le mot de passe en clair
     * @return Le hash BCrypt du mot de passe
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(AppConfig.BCRYPT_ROUNDS));
    }

    /**
     * Vérifie qu'un mot de passe en clair correspond à un hash BCrypt stocké.
     *
     * @param plainPassword  Le mot de passe saisi
     * @param hashedPassword Le hash stocké en base
     * @return true si le mot de passe correspond
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null
                || plainPassword.isBlank() || hashedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
