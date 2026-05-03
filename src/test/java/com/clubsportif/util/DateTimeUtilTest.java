package com.clubsportif.util;

import com.clubsportif.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires des utilitaires de parsing et formatage de dates/heures.
 */
@DisplayName("DateTimeUtil - Parsing et formatage des dates et heures")
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored", "ThrowableResultOfMethodCallIgnored"})
class DateTimeUtilTest {

    // ── parseDate ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("parseDate : format valide JJ/MM/AAAA")
    void parseDate_formatValide() {
        LocalDate date = DateTimeUtil.parseDate("25/12/2025");
        assertEquals(LocalDate.of(2025, 12, 25), date);
    }

    @Test
    @DisplayName("parseDate : avec espaces autour (trim)")
    void parseDate_avecEspaces() {
        LocalDate date = DateTimeUtil.parseDate("  01/06/2026  ");
        assertEquals(LocalDate.of(2026, 6, 1), date);
    }

    @Test
    @DisplayName("parseDate : format invalide AAAA-MM-JJ lève ValidationException")
    void parseDate_formatIso_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseDate("2025-12-25")));
    }

    @Test
    @DisplayName("parseDate : date non valide 32/01/2025 lève ValidationException")
    void parseDate_jourInvalide_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseDate("32/01/2025")));
    }

    @Test
    @DisplayName("parseDate : null lève ValidationException")
    void parseDate_null_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseDate(null)));
    }

    @Test
    @DisplayName("parseDate : chaîne vide lève ValidationException")
    void parseDate_vide_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseDate("")));
    }

    // ── parseTime ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("parseTime : format valide HH:MM")
    void parseTime_formatValide() {
        LocalTime time = DateTimeUtil.parseTime("14:30");
        assertEquals(LocalTime.of(14, 30), time);
    }

    @Test
    @DisplayName("parseTime : minuit 00:00")
    void parseTime_minuit() {
        LocalTime time = DateTimeUtil.parseTime("00:00");
        assertEquals(LocalTime.MIDNIGHT, time);
    }

    @Test
    @DisplayName("parseTime : heure invalide 25:00 lève ValidationException")
    void parseTime_heureInvalide_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseTime("25:00")));
    }

    @Test
    @DisplayName("parseTime : format incorrect (sans deux-points) lève ValidationException")
    void parseTime_formatSansDeuxPoints_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseTime("1400")));
    }

    @Test
    @DisplayName("parseTime : null lève ValidationException")
    void parseTime_null_lanceException() {
        assertNotNull(assertThrows(ValidationException.class,
            () -> DateTimeUtil.parseTime(null)));
    }

    // ── formatDate / formatTime ───────────────────────────────────────────────

    @Test
    @DisplayName("formatDate : retourne le format JJ/MM/AAAA")
    void formatDate_retourneFormatFrancais() {
        assertEquals("25/12/2025", DateTimeUtil.formatDate(LocalDate.of(2025, 12, 25)));
    }

    @Test
    @DisplayName("formatDate : null retourne N/A")
    void formatDate_null_retourneNA() {
        assertEquals("N/A", DateTimeUtil.formatDate(null));
    }

    @Test
    @DisplayName("formatTime : retourne le format HH:MM")
    void formatTime_retourneFormatHeureMM() {
        assertEquals("08:00", DateTimeUtil.formatTime(LocalTime.of(8, 0)));
    }

    @Test
    @DisplayName("formatTime : null retourne N/A")
    void formatTime_null_retourneNA() {
        assertEquals("N/A", DateTimeUtil.formatTime(null));
    }

    @Test
    @DisplayName("Aller-retour : parseDate(formatDate(date)) == date")
    void allerRetour_date() {
        LocalDate original = LocalDate.of(2026, 4, 12);
        assertEquals(original, DateTimeUtil.parseDate(DateTimeUtil.formatDate(original)));
    }

    @Test
    @DisplayName("Aller-retour : parseTime(formatTime(time)) == time")
    void allerRetour_time() {
        LocalTime original = LocalTime.of(14, 30);
        assertEquals(original, DateTimeUtil.parseTime(DateTimeUtil.formatTime(original)));
    }
}
