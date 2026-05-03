package com.clubsportif.config;

public final class AppConfig {

    private AppConfig() {}

    // ---- Base de données ----
    public static final String DB_HOST     = "localhost";
    public static final int    DB_PORT     = 3306;
    public static final String DB_NAME     = "club_sportif";
    public static final String DB_USER     = "root";
    public static final String DB_PASSWORD =
            System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "rayno1234@1234";

    public static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false"
            + "&serverTimezone=UTC"
            + "&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF-8"
            + "&useUnicode=true";

    // ---- Sécurité ----
    public static final int BCRYPT_ROUNDS = 10;

    // ---- Règles métier ----
    public static final int HEURE_OUVERTURE   = 8;   // 08h00
    public static final int HEURE_FERMETURE   = 23;  // 23h00 (dernier créneau se termine à 23h max)
    public static final int DUREE_MIN_HEURES  = 1;
    public static final int DUREE_MAX_HEURES  = 4;

    // ---- Informations application ----
    public static final String APP_NAME    = "Gestion des Terrains Sportifs";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_YEAR    = "2025-2026";
    public static final String APP_AUTHOR  = "Projet Universitaire";
}
