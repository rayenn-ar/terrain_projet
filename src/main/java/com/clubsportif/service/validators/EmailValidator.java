package com.clubsportif.service.validators;

/**
 * Validateur d'adresses e-mail.
 */
public final class EmailValidator {

    private EmailValidator() {}

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";

    /**
     * Vérifie si l'adresse e-mail fournie est valide.
     *
     * @param email L'e-mail à vérifier
     * @return true si l'e-mail est valide
     */
    public static boolean isValid(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    /**
     * Normalise l'e-mail (trim + toLowerCase).
     */
    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
