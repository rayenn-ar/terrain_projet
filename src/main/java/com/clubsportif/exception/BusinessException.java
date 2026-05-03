package com.clubsportif.exception;

/** Classe mère de toutes les exceptions métier de l'application. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
