package com.clubsportif;

import com.clubsportif.config.DatabaseConfig;
import com.clubsportif.controller.ConsoleController;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import com.clubsportif.service.impl.AuthServiceImpl;
import com.clubsportif.service.impl.ReservationServiceImpl;
import com.clubsportif.service.impl.TerrainServiceImpl;
import com.clubsportif.service.interfaces.IAuthService;
import com.clubsportif.service.interfaces.IReservationService;
import com.clubsportif.service.interfaces.ITerrainService;
import com.clubsportif.util.ConsoleColors;
import com.clubsportif.util.Logger;
import com.clubsportif.util.ScannerSingleton;

/**
 * Point d'entrée de l'application de gestion des terrains sportifs.
 *
 * Prérequis :
 *  1. MySQL 8.0 en cours d'exécution
 *  2. Base de données 'club_sportif' créée (voir database.sql)
 *  3. Configurer DB_USER / DB_PASSWORD dans AppConfig.java si nécessaire
 *
 * Comptes de test (définis dans database.sql) :
 *  - admin@clubsportif.com  / Admin1234  (Administrateur)
 *  - jean.dupont@email.com  / Admin1234  (Client)
 *  - marie.martin@email.com / Admin1234  (Client)
 *
 * Si la connexion échoue, inscrivez un nouveau compte via l'application,
 * puis mettez à jour son rôle en base : UPDATE utilisateur SET role='ADMIN' WHERE email='...';
 */
public class Main {

    public static void main(String[] args) {
        // Activation des couleurs ANSI sur Windows (Windows 10+)
        enableWindowsAnsiSupport();

        Logger.info("Démarrage de l'application...");

        // Vérification de la connexion à la base de données
        try {
            DatabaseConfig.getInstance().getConnection();
            Logger.success("Connexion à la base de données établie.");
        } catch (SQLException | RuntimeException e) {
            System.err.println(ConsoleColors.RED_BOLD
                + "\n  ✘ ERREUR CRITIQUE : Impossible de se connecter à MySQL.\n"
                + "  Vérifiez que MySQL est démarré et que les paramètres dans AppConfig.java sont corrects.\n"
                + "  Détail : " + e.getMessage()
                + ConsoleColors.RESET);
            System.exit(1);
        }

        // Instanciation des services (injection de dépendances manuelle)
        IAuthService        authService        = new AuthServiceImpl();
        ITerrainService     terrainService     = new TerrainServiceImpl();
        IReservationService reservationService = new ReservationServiceImpl(authService);

        // Démarrage du contrôleur principal
        ConsoleController controller = new ConsoleController(authService, terrainService, reservationService);

        try {
            controller.demarrer();
        } finally {
            ScannerSingleton.getInstance().close();
            DatabaseConfig.getInstance().closeConnection();
            Logger.info("Application fermée proprement.");
        }
    }

    /**
     * Active le support ANSI sur la console Windows (mode virtuel ANSI).
     * Sans effet sur Linux/macOS où ANSI est natif.
     */
    private static void enableWindowsAnsiSupport() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            try {
                Process p = new ProcessBuilder("cmd", "/c", "").inheritIO().start();
                if (!p.waitFor(2, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }
            } catch (IOException ignored) {
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
