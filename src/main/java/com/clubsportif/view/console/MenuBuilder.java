package com.clubsportif.view.console;

import com.clubsportif.util.ConsoleColors;

/**
 * Composant de construction des menus console.
 */
public class MenuBuilder {

    private final StringBuilder sb = new StringBuilder();

    public MenuBuilder titre(String texte) {
        sb.append("\n");
        sb.append(ConsoleColors.BLUE_BOLD);
        String ligne = "═".repeat(texte.length() + 4);
        sb.append("  ╔").append(ligne).append("╗\n");
        sb.append("  ║  ").append(texte).append("  ║\n");
        sb.append("  ╚").append(ligne).append("╝");
        sb.append(ConsoleColors.RESET);
        sb.append("\n");
        return this;
    }

    public MenuBuilder option(int num, String icone, String texte) {
        sb.append(ConsoleColors.WHITE)
          .append("  ").append(ConsoleColors.CYAN_BOLD)
          .append("[").append(num).append("] ")
          .append(ConsoleColors.RESET)
          .append(icone).append(" ").append(texte)
          .append("\n");
        return this;
    }

    public MenuBuilder separateur() {
        sb.append(ConsoleColors.BLUE).append("  ─────────────────────────────\n").append(ConsoleColors.RESET);
        return this;
    }

    public MenuBuilder vide() {
        sb.append("\n");
        return this;
    }

    public void afficher() {
        System.out.print(sb.toString());
        sb.setLength(0);
    }

    /** Affiche une invite de saisie de choix. */
    public static void promptChoix() {
        System.out.print(ConsoleColors.YELLOW_BOLD + "  Votre choix : " + ConsoleColors.RESET);
    }
}
