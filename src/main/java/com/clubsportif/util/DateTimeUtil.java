package com.clubsportif.util;

import com.clubsportif.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilitaires pour le parsing et le formatage des dates et heures.
 */
public final class DateTimeUtil {

    private DateTimeUtil() {}

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Parse une date au format dd/MM/yyyy.
     *
     * @param input La chaîne à parser (ex: "25/12/2025")
     * @return La LocalDate correspondante
     * @throws ValidationException si le format est invalide
     */
    public static LocalDate parseDate(String input) {
        if (input == null || input.isBlank()) {
            throw new ValidationException("La date ne peut pas être vide.");
        }
        try {
            return LocalDate.parse(input.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Format de date invalide. Utilisez le format JJ/MM/AAAA (ex: 25/12/2025).");
        }
    }

    /**
     * Parse une heure au format HH:mm.
     *
     * @param input La chaîne à parser (ex: "14:00")
     * @return La LocalTime correspondante
     * @throws ValidationException si le format est invalide
     */
    public static LocalTime parseTime(String input) {
        if (input == null || input.isBlank()) {
            throw new ValidationException("L'heure ne peut pas être vide.");
        }
        try {
            return LocalTime.parse(input.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Format d'heure invalide. Utilisez le format HH:MM (ex: 14:00).");
        }
    }

    /** Formate une date en dd/MM/yyyy. */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    /** Formate une heure en HH:mm. */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "N/A";
    }
}
