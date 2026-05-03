package com.clubsportif.util;

/**
 * Codes couleur ANSI pour l'affichage console coloré.
 */
public final class ConsoleColors {

    private ConsoleColors() {}

    // Réinitialisation
    public static final String RESET  = "\033[0m";

    // Couleurs de texte
    public static final String BLACK   = "\033[0;30m";
    public static final String RED     = "\033[0;31m";
    public static final String GREEN   = "\033[0;32m";
    public static final String YELLOW  = "\033[0;33m";
    public static final String BLUE    = "\033[0;34m";
    public static final String PURPLE  = "\033[0;35m";
    public static final String CYAN    = "\033[0;36m";
    public static final String WHITE   = "\033[0;37m";

    // Couleurs en gras
    public static final String BLACK_BOLD   = "\033[1;30m";
    public static final String RED_BOLD     = "\033[1;31m";
    public static final String GREEN_BOLD   = "\033[1;32m";
    public static final String YELLOW_BOLD  = "\033[1;33m";
    public static final String BLUE_BOLD    = "\033[1;34m";
    public static final String PURPLE_BOLD  = "\033[1;35m";
    public static final String CYAN_BOLD    = "\033[1;36m";
    public static final String WHITE_BOLD   = "\033[1;37m";

    // Fonds colorés
    public static final String BG_RED    = "\033[41m";
    public static final String BG_GREEN  = "\033[42m";
    public static final String BG_YELLOW = "\033[43m";
    public static final String BG_BLUE   = "\033[44m";
    public static final String BG_CYAN   = "\033[46m";

    // Soulignement
    public static final String UNDERLINE = "\033[4m";

    // Helpers
    public static String success(String msg)  { return GREEN_BOLD  + msg + RESET; }
    public static String error(String msg)    { return RED_BOLD    + msg + RESET; }
    public static String warning(String msg)  { return YELLOW_BOLD + msg + RESET; }
    public static String info(String msg)     { return CYAN        + msg + RESET; }
    public static String title(String msg)    { return BLUE_BOLD   + msg + RESET; }
    public static String highlight(String msg){ return PURPLE_BOLD + msg + RESET; }
}
