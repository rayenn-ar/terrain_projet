package com.clubsportif.service.validators;

import com.clubsportif.config.AppConfig;
import com.clubsportif.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Validateur des règles métier de réservation.
 */
public final class ReservationValidator {

    private ReservationValidator() {}

    /**
     * Valide les paramètres d'une nouvelle réservation.
     *
     * @throws ValidationException si une règle est violée
     */
    public static void valider(LocalDate date, LocalTime heureDebut, int dureeHeures) {
        LocalDate today = LocalDate.now();

        // La date doit être aujourd'hui ou dans le futur
        if (date.isBefore(today)) {
            throw new ValidationException(
                "La date de réservation ne peut pas être dans le passé (date saisie: "
                + date + ").");
        }

        // Vérification plage horaire (RÈGLE 7)
        int heure = heureDebut.getHour();
        if (heure < AppConfig.HEURE_OUVERTURE) {
            throw new ValidationException(
                "L'heure de début ne peut pas être avant " + AppConfig.HEURE_OUVERTURE + "h00.");
        }

        // Vérification durée (RÈGLE 5 sous règles)
        if (dureeHeures < AppConfig.DUREE_MIN_HEURES || dureeHeures > AppConfig.DUREE_MAX_HEURES) {
            throw new ValidationException(
                "La durée doit être comprise entre " + AppConfig.DUREE_MIN_HEURES
                + " et " + AppConfig.DUREE_MAX_HEURES + " heures.");
        }

        // L'heure de fin ne doit pas dépasser HEURE_FERMETURE (RÈGLE 7)
        // Utilisation d'une arithmétique entière pour éviter le dépassement minuit de LocalTime
        int finHeure = heureDebut.getHour() + dureeHeures;
        if (finHeure > AppConfig.HEURE_FERMETURE) {
            throw new ValidationException(
                "La réservation se terminerait à " + finHeure + "h00 mais le terrain ferme à "
                + AppConfig.HEURE_FERMETURE + "h00.");
        }

        // Si la date est aujourd'hui, l'heure doit être dans le futur
        if (date.isEqual(today) && !heureDebut.isAfter(LocalTime.now())) {
            throw new ValidationException(
                "Pour une réservation aujourd'hui, l'heure de début doit être dans le futur.");
        }
    }
}
