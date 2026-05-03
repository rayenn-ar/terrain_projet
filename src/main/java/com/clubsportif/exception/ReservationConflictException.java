package com.clubsportif.exception;

/** Levée lorsqu'un créneau de réservation est déjà occupé (chevauchement temporel). */
public class ReservationConflictException extends BusinessException {

    public ReservationConflictException(String message) {
        super(message);
    }
}
