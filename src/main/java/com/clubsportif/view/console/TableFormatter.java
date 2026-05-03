package com.clubsportif.view.console;

import com.clubsportif.util.ConsoleColors;

/**
 * Utilitaire d'affichage de tableaux alignés en console.
 */
public class TableFormatter {

    private final String[] headers;
    private final int[]    widths;

    public TableFormatter(String[] headers, int[] widths) {
        this.headers = headers;
        this.widths  = widths;
    }

    public void printHeader() {
        System.out.println(ConsoleColors.BLUE_BOLD + buildSeparator() + ConsoleColors.RESET);
        System.out.println(ConsoleColors.BLUE_BOLD + buildRow(headers) + ConsoleColors.RESET);
        System.out.println(ConsoleColors.BLUE_BOLD + buildSeparator() + ConsoleColors.RESET);
    }

    public void printRow(String... cells) {
        System.out.println(buildRow(cells));
    }

    public void printFooter() {
        System.out.println(ConsoleColors.BLUE + buildSeparator() + ConsoleColors.RESET);
    }

    private String buildSeparator() {
        StringBuilder sb = new StringBuilder("  +");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private String buildRow(String[] cells) {
        StringBuilder sb = new StringBuilder("  |");
        for (int i = 0; i < widths.length; i++) {
            String cell = (i < cells.length && cells[i] != null) ? cells[i] : "";
            sb.append(" ").append(pad(cell, widths[i])).append(" |");
        }
        return sb.toString();
    }

    private String pad(String text, int width) {
        // Strip ANSI codes for length calculation
        String stripped = text.replaceAll("\033\\[[;\\d]*m", "");
        int len = stripped.length();
        if (len >= width) {
            return text.substring(0, Math.min(text.length(), width));
        }
        return text + " ".repeat(width - len);
    }

    /** Affiche un message "aucun résultat" centré. */
    public static void printEmpty(String message) {
        System.out.println(ConsoleColors.YELLOW + "  " + message + ConsoleColors.RESET);
    }
}
