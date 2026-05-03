package com.clubsportif.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.clubsportif.exception.BusinessException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Reservation;
import com.clubsportif.model.Terrain;
import com.clubsportif.service.interfaces.IReservationService;
import com.clubsportif.service.interfaces.ITerrainService;
import com.clubsportif.util.ConsoleColors;
import com.clubsportif.util.DateTimeUtil;
import com.clubsportif.util.ScannerSingleton;
import com.clubsportif.view.console.ConsoleView;
import com.clubsportif.view.console.MenuBuilder;

/**
 * Contrôleur des réservations (création, annulation, historique).
 */
public class ReservationController {

    private final IReservationService reservationService;
    private final ITerrainService     terrainService;
    private final ScannerSingleton    scanner = ScannerSingleton.getInstance();

    public ReservationController(IReservationService reservationService, ITerrainService terrainService) {
        this.reservationService = reservationService;
        this.terrainService     = terrainService;
    }

    // ================================================================
    //  MENUS CLIENT
    // ================================================================

    /** Menu réservation complet pour le client. */
    public void menuReservationClient() {
        boolean retour = false;
        while (!retour) {
            new MenuBuilder()
                .titre("MES RÉSERVATIONS")
                .option(1, "📅", "Réserver un terrain")
                .option(2, "📋", "Voir mon historique")
                .option(3, "❌", "Annuler une réservation")
                .separateur()
                .option(0, "↩", "Retour")
                .afficher();

            MenuBuilder.promptChoix();
            int choix = scanner.nextInt();

            switch (choix) {
                case 1 -> reserverTerrain();
                case 2 -> afficherHistoriqueClient();
                case 3 -> annulerReservationClient();
                case 0 -> retour = true;
                default -> ConsoleView.avertissement("Choix invalide.");
            }
            if (!retour) scanner.attendreEntree();
        }
    }

    // ================================================================
    //  MENUS ADMIN
    // ================================================================

    /** Menu de gestion des réservations pour l'administrateur. */
    public void menuGestionReservationsAdmin() {
        boolean retour = false;
        while (!retour) {
            new MenuBuilder()
                .titre("GESTION DES RÉSERVATIONS")
                .option(1, "📋", "Toutes les réservations")
                .option(2, "🏟", "Réservations par terrain")
                .option(3, "❌", "Annuler une réservation")
                .option(4, "📊", "Statistiques")
                .separateur()
                .option(0, "↩", "Retour")
                .afficher();

            MenuBuilder.promptChoix();
            int choix = scanner.nextInt();

            switch (choix) {
                case 1 -> afficherToutesReservations();
                case 2 -> reservationsParTerrain();
                case 3 -> annulerReservationAdmin();
                case 4 -> afficherStatistiques();
                case 0 -> retour = true;
                default -> ConsoleView.avertissement("Choix invalide.");
            }
            if (!retour) scanner.attendreEntree();
        }
    }

    // ================================================================
    //  ACTIONS CLIENT
    // ================================================================

    /** Formulaire complet de réservation. */
    private void reserverTerrain() {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Nouvelle réservation ──" + ConsoleColors.RESET);

        // Étape 1 : sélection du terrain
        List<Terrain> disponibles = terrainService.findDisponibles();
        if (disponibles.isEmpty()) {
            ConsoleView.avertissement("Aucun terrain disponible pour le moment.");
            return;
        }
        ConsoleView.afficherTerrains(disponibles);
        int idTerrain = scanner.nextInt("  ID du terrain à réserver (0 = annuler) : ");
        if (idTerrain == 0) return;

        Terrain terrain;
        try {
            terrain = terrainService.findById(idTerrain);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
            return;
        }
        if (!terrain.isDisponibilite()) {
            ConsoleView.erreur("Ce terrain est actuellement indisponible.");
            return;
        }

        // Étape 2 : saisie de la date
        LocalDate date;
        try {
            String dateStr = scanner.nextLine("  Date (JJ/MM/AAAA)         : ");
            date = DateTimeUtil.parseDate(dateStr);
        } catch (ValidationException e) {
            ConsoleView.erreur(e.getMessage());
            return;
        }

        // Affichage des créneaux déjà pris pour information
        afficherCreneauxOccupes(idTerrain, date);

        // Étape 3 : saisie de l'heure de début
        LocalTime heureDebut;
        try {
            String heureStr = scanner.nextLine("  Heure de début (HH:MM, entre 08:00 et 22:00) : ");
            heureDebut = DateTimeUtil.parseTime(heureStr);
        } catch (ValidationException e) {
            ConsoleView.erreur(e.getMessage());
            return;
        }

        // Étape 4 : durée
        System.out.println(ConsoleColors.CYAN + "  Durée disponible : 1 à 4 heures" + ConsoleColors.RESET);
        int duree = scanner.nextInt("  Durée (heures)                              : ");

        // Étape 5 : récapitulatif et confirmation
        if (heureDebut != null) {
            LocalTime fin = heureDebut.plusHours(duree);
            double montantPrevu = terrain.getPrixParHeure().doubleValue() * duree;
            System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Récapitulatif ──" + ConsoleColors.RESET);
            ConsoleView.info("Terrain   : " + terrain.getNom() + " (" + terrain.getType().getLibelle() + ")");
            ConsoleView.info("Date      : " + DateTimeUtil.formatDate(date));
            ConsoleView.info("Créneau   : " + DateTimeUtil.formatTime(heureDebut) + " → " + DateTimeUtil.formatTime(fin));
            ConsoleView.info("Durée     : " + duree + " heure(s)");
            ConsoleView.info("Montant   : " + String.format("%.2f dt", montantPrevu));
        }

        System.out.print(ConsoleColors.YELLOW_BOLD
            + "\n  Confirmer la réservation ? (O/N) : " + ConsoleColors.RESET);
        if (!scanner.nextLine().equalsIgnoreCase("O")) {
            ConsoleView.info("Réservation annulée.");
            return;
        }

        try {
            Reservation r = reservationService.creerReservation(idTerrain, date, heureDebut, duree);
            ConsoleView.succes("Réservation #" + r.getIdReservation() + " confirmée ! Montant: " + r.getMontantTotal() + " dt");
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    /** Affiche les créneaux déjà occupés pour un terrain à une date donnée. */
    private void afficherCreneauxOccupes(int idTerrain, LocalDate date) {
        List<Reservation> reservations = reservationService.getReservationsParTerrain(idTerrain)
                .stream()
                .filter(r -> r.getDateReservation().equals(date)
                          && r.getStatut() == com.clubsportif.model.enums.StatutReservation.CONFIRMEE)
                .toList();

        if (reservations.isEmpty()) {
            ConsoleView.info("Aucun créneau réservé le " + DateTimeUtil.formatDate(date) + " pour ce terrain.");
        } else {
            System.out.println(ConsoleColors.YELLOW + "\n  Créneaux déjà réservés le " + DateTimeUtil.formatDate(date) + ":" + ConsoleColors.RESET);
            for (Reservation r : reservations) {
                System.out.println(ConsoleColors.RED + "    ✘ " + DateTimeUtil.formatTime(r.getHeureDebut())
                    + " → " + DateTimeUtil.formatTime(r.getHeureFin()) + ConsoleColors.RESET);
            }
        }
        System.out.println();
    }

    private void afficherHistoriqueClient() {
        try {
            List<Reservation> historique = reservationService.getHistoriqueUtilisateur();
            System.out.println(ConsoleColors.BLUE_BOLD
                + "\n  ── Mon Historique (" + historique.size() + " réservation(s)) ──"
                + ConsoleColors.RESET);
            ConsoleView.afficherReservations(historique);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void annulerReservationClient() {
        afficherHistoriqueClient();
        int id = scanner.nextInt("  ID de la réservation à annuler (0 = retour) : ");
        if (id == 0) return;

        System.out.print(ConsoleColors.YELLOW_BOLD
            + "  Confirmer l'annulation de la réservation #" + id + " ? (O/N) : "
            + ConsoleColors.RESET);
        if (!scanner.nextLine().equalsIgnoreCase("O")) {
            ConsoleView.info("Annulation abandonnée.");
            return;
        }
        try {
            reservationService.annulerReservation(id);
            ConsoleView.succes("Réservation #" + id + " annulée avec succès.");
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    // ================================================================
    //  ACTIONS ADMIN
    // ================================================================

    private void afficherToutesReservations() {
        try {
            List<Reservation> all = reservationService.getAllReservations();
            System.out.println(ConsoleColors.BLUE_BOLD
                + "\n  ── Toutes les réservations (" + all.size() + ") ──"
                + ConsoleColors.RESET);
            ConsoleView.afficherReservations(all);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void reservationsParTerrain() {
        ConsoleView.afficherTerrains(terrainService.findAll());
        int id = scanner.nextInt("  ID terrain (0 = retour) : ");
        if (id == 0) return;
        try {
            List<Reservation> list = reservationService.getReservationsParTerrain(id);
            Terrain t = terrainService.findById(id);
            System.out.println(ConsoleColors.BLUE_BOLD
                + "\n  ── Réservations : " + t.getNom() + " (" + list.size() + ") ──"
                + ConsoleColors.RESET);
            ConsoleView.afficherReservations(list);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void annulerReservationAdmin() {
        afficherToutesReservations();
        int id = scanner.nextInt("  ID réservation à annuler (0 = retour) : ");
        if (id == 0) return;

        try {
            Reservation r = reservationService.findById(id);
            ConsoleView.afficherDetailReservation(r);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
            return;
        }

        System.out.print(ConsoleColors.YELLOW_BOLD
            + "  Confirmer l'annulation ? (O/N) : " + ConsoleColors.RESET);
        if (!scanner.nextLine().equalsIgnoreCase("O")) {
            ConsoleView.info("Annulation abandonnée.");
            return;
        }
        try {
            reservationService.annulerReservationAdmin(id);
            ConsoleView.succes("Réservation #" + id + " annulée.");
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    public void afficherStatistiques() {
        try {
            Map<String, Object> stats = reservationService.getStatistiques();
            ConsoleView.afficherStatistiques(stats);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }
}
