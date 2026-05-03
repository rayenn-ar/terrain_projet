package com.clubsportif.view.console;

import com.clubsportif.config.AppConfig;
import com.clubsportif.model.Reservation;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.StatutReservation;
import com.clubsportif.model.enums.TypeTerrain;
import com.clubsportif.util.ConsoleColors;
import com.clubsportif.util.DateTimeUtil;

import java.util.List;
import java.util.Map;

/**
 * Composant de présentation console : affichage de messages, tableaux et formulaires.
 */
public class ConsoleView {

    // ---- Messages système ----

    public static void succes(String message) {
        System.out.println("\n  " + ConsoleColors.GREEN_BOLD + "✔ " + message + ConsoleColors.RESET);
    }

    public static void erreur(String message) {
        System.out.println("\n  " + ConsoleColors.RED_BOLD + "✘ Erreur : " + message + ConsoleColors.RESET);
    }

    public static void info(String message) {
        System.out.println("  " + ConsoleColors.CYAN + "ℹ " + message + ConsoleColors.RESET);
    }

    public static void avertissement(String message) {
        System.out.println("  " + ConsoleColors.YELLOW_BOLD + "⚠ " + message + ConsoleColors.RESET);
    }

    // ---- Bannière de démarrage ----

    public static void afficherBanniere() {
        System.out.println(ConsoleColors.BLUE_BOLD);
        System.out.println("  ╔══════════════════════════════════════════════════════════╗");
        System.out.println("  ║     🏟  GESTION DES TERRAINS SPORTIFS  🏟               ║");
        System.out.println("  ║         Projet Universitaire " + AppConfig.APP_YEAR + "                    ║");
        System.out.println("  ║         Version " + AppConfig.APP_VERSION + "                                     ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════╝");
        System.out.println(ConsoleColors.RESET);
    }

    // ---- Affichage des terrains ----

    public static void afficherTerrains(List<Terrain> terrains) {
        if (terrains.isEmpty()) {
            TableFormatter.printEmpty("Aucun terrain trouvé.");
            return;
        }
        TableFormatter tf = new TableFormatter(
            new String[]{"ID", "NOM", "TYPE", "PRIX/H", "DISPO", "DESCRIPTION"},
            new int[]   { 4,    28,    12,     8,        6,       35}
        );
        tf.printHeader();
        for (Terrain t : terrains) {
            String dispo = t.isDisponibilite()
                    ? ConsoleColors.GREEN_BOLD + "OUI" + ConsoleColors.RESET
                    : ConsoleColors.RED_BOLD   + "NON" + ConsoleColors.RESET;
            String desc  = t.getDescription() != null
                    ? (t.getDescription().length() > 33
                       ? t.getDescription().substring(0, 33) + ".."
                       : t.getDescription())
                    : "";
            tf.printRow(
                String.valueOf(t.getIdTerrain()),
                t.getNom(),
                t.getType().getLibelle(),
                t.getPrixParHeure() + " dt",
                dispo,
                desc
            );
        }
        tf.printFooter();
        System.out.println();
    }

    // ---- Affichage des réservations ----

    public static void afficherReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            TableFormatter.printEmpty("Aucune réservation trouvée.");
            return;
        }
        TableFormatter tf = new TableFormatter(
            new String[]{"ID",  "CLIENT",    "TERRAIN",           "DATE",       "DÉBUT", "FIN",  "DURÉE", "STATUT",    "MONTANT"},
            new int[]   { 4,     18,          22,                  12,           7,       7,      6,       11,          9}
        );
        tf.printHeader();
        for (Reservation r : reservations) {
            String statutColore = colorerStatut(r.getStatut());
            tf.printRow(
                String.valueOf(r.getIdReservation()),
                r.getNomClient() != null ? r.getNomClient() : "ID:" + r.getIdUtilisateur(),
                r.getNomTerrain() != null ? r.getNomTerrain() : "ID:" + r.getIdTerrain(),
                DateTimeUtil.formatDate(r.getDateReservation()),
                DateTimeUtil.formatTime(r.getHeureDebut()),
                DateTimeUtil.formatTime(r.getHeureFin()),
                r.getDureeHeures() + "h",
                statutColore,
                r.getMontantTotal() + " dt"
            );
        }
        tf.printFooter();
        System.out.println();
    }

    // ---- Affichage des utilisateurs ----

    public static void afficherUtilisateurs(List<Utilisateur> utilisateurs) {
        if (utilisateurs.isEmpty()) {
            TableFormatter.printEmpty("Aucun utilisateur trouvé.");
            return;
        }
        TableFormatter tf = new TableFormatter(
            new String[]{"ID", "NOM",         "EMAIL",                   "RÔLE",    "INSCRIPTION"},
            new int[]   { 4,    20,             30,                        12,        19}
        );
        tf.printHeader();
        for (Utilisateur u : utilisateurs) {
            String role = u.isAdmin()
                    ? ConsoleColors.PURPLE_BOLD + u.getRole().getLibelle() + ConsoleColors.RESET
                    : u.getRole().getLibelle();
            String dateIns = u.getDateInscription() != null
                    ? u.getDateInscription().toLocalDate().format(DateTimeUtil.DATE_FORMATTER)
                    : "N/A";
            tf.printRow(
                String.valueOf(u.getIdUtilisateur()),
                u.getNom(),
                u.getEmail(),
                role,
                dateIns
            );
        }
        tf.printFooter();
        System.out.println();
    }

    // ---- Statistiques ----

    public static void afficherStatistiques(Map<String, Object> stats) {
        System.out.println(ConsoleColors.BLUE_BOLD);
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║          TABLEAU DE BORD - STATISTIQUES      ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
        System.out.println(ConsoleColors.RESET);
        System.out.printf("  %-32s : %s%n",
                "Total réservations",
                ConsoleColors.WHITE_BOLD + stats.get("totalReservations") + ConsoleColors.RESET);
        System.out.printf("  %-32s : %s%n",
                "Réservations confirmées",
                ConsoleColors.GREEN_BOLD + stats.get("reservationsConfirmees") + ConsoleColors.RESET);
        System.out.printf("  %-32s : %s%n",
                "Réservations terminées",
                ConsoleColors.CYAN + stats.get("reservationsTerminees") + ConsoleColors.RESET);
        System.out.printf("  %-32s : %s%n",
                "Réservations annulées",
                ConsoleColors.RED + stats.get("reservationsAnnulees") + ConsoleColors.RESET);
        System.out.printf("  %-32s : %s%n",
                "Chiffre d'affaires (hors annulées)",
            ConsoleColors.GREEN_BOLD + stats.get("chiffreAffaires") + " dt" + ConsoleColors.RESET);
        System.out.printf("  %-32s : %s%n",
                "Terrains avec réservations",
                ConsoleColors.YELLOW + stats.get("terrainsUtilises") + ConsoleColors.RESET);
        System.out.println();
    }

    // ---- Types de terrain disponibles ----

    public static void afficherTypesTerrains() {
        System.out.println(ConsoleColors.CYAN + "  Types disponibles :" + ConsoleColors.RESET);
        int i = 1;
        for (TypeTerrain t : TypeTerrain.values()) {
            System.out.printf("    [%d] %s%n", i++, t.getLibelle());
        }
    }

    // ---- Détail d'une réservation ----

    public static void afficherDetailReservation(Reservation r) {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Détail de la réservation #" + r.getIdReservation() + " ──" + ConsoleColors.RESET);
        info("Client      : " + (r.getNomClient() != null ? r.getNomClient() : "ID " + r.getIdUtilisateur()));
        info("Terrain     : " + (r.getNomTerrain() != null ? r.getNomTerrain() : "ID " + r.getIdTerrain()));
        info("Date        : " + DateTimeUtil.formatDate(r.getDateReservation()));
        info("Créneau     : " + DateTimeUtil.formatTime(r.getHeureDebut()) + " → " + DateTimeUtil.formatTime(r.getHeureFin()));
        info("Durée       : " + r.getDureeHeures() + " heure(s)");
        info("Statut      : " + colorerStatut(r.getStatut()));
        info("Montant     : " + r.getMontantTotal() + " dt");
        System.out.println();
    }

    // ---- Helpers internes ----

    private static String colorerStatut(StatutReservation statut) {
        return switch (statut) {
            case CONFIRMEE -> ConsoleColors.GREEN_BOLD  + statut.getLibelle() + ConsoleColors.RESET;
            case ANNULEE   -> ConsoleColors.RED_BOLD    + statut.getLibelle() + ConsoleColors.RESET;
            case TERMINEE  -> ConsoleColors.CYAN        + statut.getLibelle() + ConsoleColors.RESET;
        };
    }

    public static void separateur() {
        System.out.println(ConsoleColors.BLUE + "  ──────────────────────────────────────────" + ConsoleColors.RESET);
    }
}
