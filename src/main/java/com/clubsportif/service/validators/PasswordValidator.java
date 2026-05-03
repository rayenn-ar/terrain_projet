package com.clubsportif.service.validators;

import com.clubsportif.exception.ValidationException;

/**
 * Validateur de robustesse des mots de passe.
 *
 * Règles appliquées :
 *  - Minimum 8 caractères
 *  - Au moins 1 lettre majuscule
 *  - Au moins 1 chiffre
 */
public final class PasswordValidator {

    private PasswordValidator() {}

    /** Longueur minimale requise. */
    public static final int LONGUEUR_MIN = 8;

    /**
     * Valide la robustesse d'un mot de passe.
     *
     * @param motDePasse Le mot de passe en clair à valider
     * @throws ValidationException si une règle n'est pas respectée
     */
    public static void valider(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN) {
            throw new ValidationException(
                "Le mot de passe doit contenir au moins " + LONGUEUR_MIN + " caractères.");
        }
        boolean aMajuscule = false;
        boolean aChiffre   = false;
        for (char c : motDePasse.toCharArray()) {
            if (Character.isUpperCase(c)) aMajuscule = true;
            if (Character.isDigit(c))     aChiffre   = true;
        }
        if (!aMajuscule) {
            throw new ValidationException(
                "Le mot de passe doit contenir au moins une lettre majuscule.");
        }
        if (!aChiffre) {
            throw new ValidationException(
                "Le mot de passe doit contenir au moins un chiffre.");
        }
    }

    /**
     * Retourne true si le mot de passe est valide, false sinon.
     * Pratique pour les contrôles sans gestion d'exception.
     */
    public static boolean estValide(String motDePasse) {
        try {
            valider(motDePasse);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
