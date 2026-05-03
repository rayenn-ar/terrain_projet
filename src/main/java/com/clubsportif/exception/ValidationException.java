package com.clubsportif.exception;

/** Levée lorsqu'une donnée saisie ne respecte pas les règles de validation. */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }
}
