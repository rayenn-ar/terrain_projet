package com.clubsportif;

import com.clubsportif.service.impl.AuthServiceImpl;
import com.clubsportif.service.impl.ReservationServiceImpl;
import com.clubsportif.service.impl.TerrainServiceImpl;
import com.clubsportif.service.interfaces.IAuthService;
import com.clubsportif.service.interfaces.IReservationService;
import com.clubsportif.service.interfaces.ITerrainService;
import com.clubsportif.ui.LoginView;
import com.clubsportif.ui.UIHelper;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX de l'application.
 * Lancer avec : mvn javafx:run
 */
public class MainApp extends Application {

    public static IAuthService authService;
    public static IReservationService reservationService;
    public static ITerrainService terrainService;
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            authService        = new AuthServiceImpl();
            reservationService = new ReservationServiceImpl(authService);
            terrainService     = new TerrainServiceImpl();
        } catch (Exception e) {
            UIHelper.showError("Erreur de connexion",
                """
                Impossible de se connecter à la base de données MySQL.

                Vérifiez que MySQL est démarré (XAMPP).

                Détail : %s""".formatted(e.getMessage()));
            stage.close();
            return;
        }

        stage.setTitle("Gestion des Terrains Sportifs");
        stage.setWidth(1050);
        stage.setHeight(720);
        stage.setMinWidth(850);
        stage.setMinHeight(580);
        stage.setScene(LoginView.createScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
