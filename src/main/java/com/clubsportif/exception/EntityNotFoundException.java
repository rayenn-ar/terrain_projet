package com.clubsportif.exception;

/** Levée lorsqu'une entité (terrain, réservation, utilisateur) est introuvable en base. */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
