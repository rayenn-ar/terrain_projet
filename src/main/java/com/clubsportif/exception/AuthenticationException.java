package com.clubsportif.exception;

/** Levée lors d'un échec d'authentification (email/mot de passe invalides, session expirée). */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }
}
