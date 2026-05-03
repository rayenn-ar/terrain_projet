package com.clubsportif.controller;

import com.clubsportif.service.interfaces.IAuthService;
import com.clubsportif.service.interfaces.IReservationService;
import com.clubsportif.service.interfaces.ITerrainService;
import com.clubsportif.util.ConsoleColors;
import com.clubsportif.util.ScannerSingleton;
import com.clubsportif.view.console.ConsoleView;
import com.clubsportif.view.console.MenuBuilder;

/**
 * Contrôleur principal : routing des menus selon l'état de session et le rôle.
 */
public class ConsoleController {

    private final IAuthService        authService;
    private final IReservationService reservationService;

    private final AuthController        authController;
    private final TerrainController     terrainController;
    private final ReservationController reservationController;

    private final ScannerSingleton scanner = ScannerSingleton.getInstance();

    public ConsoleController(IAuthService authService,
                             ITerrainService terrainService,
                             IReservationService reservationService) {
        this.authService        = authService;
        this.reservationService = reservationService;

        this.authController        = new AuthController(authService);
        this.terrainController     = new TerrainController(terrainService);
        this.reservationController = new ReservationController(reservationService, terrainService);
    }

    /**
     * Boucle principale de l'application.
     * Affiche le menu adapté selon l'état de connexion.
     */
    public void demarrer() {
        ConsoleView.afficherBanniere();

        // Mise à jour automatique des statuts à chaque démarrage
        try {
            reservationService.updateReservationsTerminees();
        } catch (Exception e) {
            // Ne pas bloquer si la connexion DB échoue ici
        }

        boolean continuer = true;
        while (continuer) {
            if (!authService.isLoggedIn()) {
                continuer = menuPublic();
            } else if (authService.isAdmin()) {
                menuAdmin();
            } else {
                menuClient();
            }
        }
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  Merci d'avoir utilisé " + com.clubsportif.config.AppConfig.APP_NAME + ". À bientôt !" + ConsoleColors.RESET);
    }

    // ================================================================
    //  MENU PUBLIC (non connecté)
    // ================================================================

    private boolean menuPublic() {
        new MenuBuilder()
            .titre("MENU PRINCIPAL")
            .option(1, "🔑", "Se connecter")
            .option(2, "📝", "S'inscrire")
            .separateur()
            .option(0, "🚪", "Quitter")
            .afficher();

        MenuBuilder.promptChoix();
        int choix = scanner.nextInt();

        switch (choix) {
            case 1 -> authController.connexion();
            case 2 -> {
                authController.inscription();
                scanner.attendreEntree();
            }
            case 0 -> { return false; }
            default -> {
                ConsoleView.avertissement("Choix invalide. Veuillez saisir 0, 1 ou 2.");
                scanner.attendreEntree();
            }
        }
        return true;
    }

    // ================================================================
    //  MENU CLIENT
    // ================================================================

    private void menuClient() {
        String nom = authService.getCurrentUser().getNom();
        new MenuBuilder()
            .titre("ESPACE CLIENT - " + nom)
            .option(1, "🏟", "Voir les terrains disponibles")
            .option(2, "📅", "Mes réservations")
            .option(3, "👤", "Mon profil")
            .separateur()
            .option(0, "🚪", "Se déconnecter")
            .afficher();

        MenuBuilder.promptChoix();
        int choix = scanner.nextInt();

        switch (choix) {
            case 1 -> {
                terrainController.listerTerrainsAvecFiltre();
                scanner.attendreEntree();
            }
            case 2 -> reservationController.menuReservationClient();
            case 3 -> {
                authController.gererProfil();
                scanner.attendreEntree();
            }
            case 0 -> {
                authController.deconnexion();
                scanner.attendreEntree();
            }
            default -> {
                ConsoleView.avertissement("Choix invalide.");
                scanner.attendreEntree();
            }
        }

        // Actualisation des statuts à chaque retour au menu
        try { reservationService.updateReservationsTerminees(); } catch (Exception ignored) {}
    }

    // ================================================================
    //  MENU ADMIN
    // ================================================================

    private void menuAdmin() {
        String nom = authService.getCurrentUser().getNom();
        new MenuBuilder()
            .titre("ESPACE ADMINISTRATEUR - " + nom)
            .option(1, "🏟", "Gestion des terrains")
            .option(2, "📅", "Gestion des réservations")
            .option(3, "👥", "Gestion des utilisateurs")
            .option(4, "📊", "Statistiques")
            .separateur()
            .option(0, "🚪", "Se déconnecter")
            .afficher();

        MenuBuilder.promptChoix();
        int choix = scanner.nextInt();

        switch (choix) {
            case 1 -> terrainController.menuGestionTerrains();
            case 2 -> reservationController.menuGestionReservationsAdmin();
            case 3 -> {
                authController.gererUtilisateurs();
                scanner.attendreEntree();
            }
            case 4 -> {
                reservationController.afficherStatistiques();
                scanner.attendreEntree();
            }
            case 0 -> {
                authController.deconnexion();
                scanner.attendreEntree();
            }
            default -> {
                ConsoleView.avertissement("Choix invalide.");
                scanner.attendreEntree();
            }
        }

        // Actualisation des statuts à chaque retour au menu
        try { reservationService.updateReservationsTerminees(); } catch (Exception ignored) {}
    }
}
