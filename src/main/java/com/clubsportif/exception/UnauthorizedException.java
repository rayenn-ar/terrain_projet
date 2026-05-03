package com.clubsportif.exception;

/** Levée lorsqu'un utilisateur tente une action non autorisée selon son rôle. */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
