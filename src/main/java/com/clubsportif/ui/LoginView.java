package com.clubsportif.ui;

import com.clubsportif.MainApp;
import com.clubsportif.exception.AuthenticationException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

/**
 * Écran de connexion et d'inscription — design split-panel "SportsPro Ultra".
 */
public final class LoginView {

    private LoginView() {}

    public static Scene createScene() {
        HBox root = new HBox();
        root.setPrefSize(1100, 720);

        // ══════════════════════════════════════════════════════════════
        // PANNEAU GAUCHE — branding & hero
        // ══════════════════════════════════════════════════════════════
        StackPane leftPanel = new StackPane();
        leftPanel.setPrefWidth(440);
        leftPanel.setMinWidth(440);
        leftPanel.setMaxWidth(440);
        leftPanel.setStyle(
            "-fx-background-color:linear-gradient(from 0% 0% to 100% 100%,#020A1A,#0B1A42,#1D4ED8);");

        // Decorative depth circles — simulate 3D depth layers
        Circle c1 = new Circle(170, Color.web("#3B82F6", 0.08));
        Circle c2 = new Circle(120, Color.web("#60A5FA", 0.10));
        Circle c3 = new Circle(80,  Color.web("#93C5FD", 0.13));
        Circle c4 = new Circle(220, Color.web("#1D4ED8", 0.12));
        StackPane.setAlignment(c1, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(c2, Pos.TOP_LEFT);
        StackPane.setAlignment(c4, Pos.TOP_RIGHT);
        StackPane.setMargin(c1,  new Insets(0, -50, -50, 0));
        StackPane.setMargin(c2,  new Insets(-60, 0, 0, -60));
        StackPane.setMargin(c4,  new Insets(-80, -80, 0, 0));

        VBox brand = new VBox(20);
        brand.setAlignment(Pos.CENTER);
        brand.setPadding(new Insets(44, 38, 44, 38));

        // Logo with deep glow
        Label logoIcon = new Label("🏟");
        logoIcon.setStyle("-fx-font-size:84px;" +
            "-fx-effect:dropshadow(gaussian,rgba(29,78,216,0.70),28,0.4,0,0);");

        Label appName = new Label("SportsPro");
        appName.setStyle(
            "-fx-font-size:40px;-fx-font-weight:bold;-fx-text-fill:white;" +
            "-fx-effect:dropshadow(gaussian,rgba(96,165,250,0.60),14,0.4,0,0);");

        Label appSub = new Label("Gestion des Terrains Sportifs");
        appSub.setStyle("-fx-font-size:13.5px;-fx-text-fill:rgba(147,197,253,0.85);-fx-letter-spacing:0.5;");

        // Glowing divider
        Separator sep = new Separator();
        sep.setMaxWidth(180);
        sep.setStyle("-fx-background-color:linear-gradient(to right,transparent,rgba(96,165,250,0.5),transparent);");

        // Tagline
        Label tagline = new Label("« Le terrain idéal,\nau moment idéal. »");
        tagline.setStyle(
            "-fx-font-size:16.5px;-fx-font-style:italic;-fx-text-fill:rgba(186,230,253,0.78);" +
            "-fx-line-spacing:4;");
        tagline.setTextAlignment(TextAlignment.CENTER);
        tagline.setAlignment(Pos.CENTER);

        // Sports emojis with hover effect (done via scale trick)
        HBox sports = new HBox(16);
        sports.setAlignment(Pos.CENTER);
        for (String s : new String[]{"⚽", "🏀", "🎾", "🏐", "🏸"}) {
            Label sp = new Label(s);
            sp.setStyle("-fx-font-size:26px;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.40),8,0,0,2);");
            sports.getChildren().add(sp);
        }

        // Feature pills
        FlowPane pills = new FlowPane(8, 8);
        pills.setAlignment(Pos.CENTER);
        pills.getChildren().addAll(
            featurePill("✅ Réservation facile"),
            featurePill("📊 Tableaux de bord"),
            featurePill("🔒 Sécurisé"),
            featurePill("⚡ Temps réel")
        );

        // Copyright at bottom of hero
        Label copyright = new Label("© 2025 SportsPro · Tous droits réservés");
        copyright.setStyle("-fx-font-size:10.5px;-fx-text-fill:rgba(148,163,184,0.55);");

        brand.getChildren().addAll(logoIcon, appName, appSub, sep, tagline, sports, pills, copyright);
        leftPanel.getChildren().addAll(c4, c1, c2, c3, brand);

        // ══════════════════════════════════════════════════════════════
        // PANNEAU DROIT — formulaire
        // ══════════════════════════════════════════════════════════════
        StackPane rightPanel = new StackPane();
        rightPanel.setStyle("-fx-background-color:" + UIHelper.BG + ";");
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Form card with stronger shadow for depth
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 42, 38, 42));
        card.setMaxWidth(470);
        card.setStyle(
            "-fx-background-color:white;-fx-background-radius:18;" +
            "-fx-effect:" + UIHelper.SHADOW_XL + ";" +
            "-fx-border-color:" + UIHelper.BORDER + ";-fx-border-radius:18;-fx-border-width:1;");

        // Form header
        HBox formHead = new HBox(10);
        formHead.setAlignment(Pos.CENTER_LEFT);
        Label formIcon = new Label("🏟");
        formIcon.setStyle("-fx-font-size:30px;");
        VBox formHeadText = new VBox(1);
        Label formTitle = new Label("Bienvenue !");
        formTitle.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + UIHelper.TEXT1 + ";");
        Label formSub = new Label("Connectez-vous ou créez votre compte");
        formSub.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIHelper.TEXT2 + ";");
        formHeadText.getChildren().addAll(formTitle, formSub);
        formHead.getChildren().addAll(formIcon, formHeadText);

        // Separator with primary accent
        Separator formSep = new Separator();
        formSep.setStyle("-fx-background-color:" + UIHelper.PRIMARY + ";");

        // Tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
            "-fx-tab-min-width:130;" +
            "-fx-background-color:transparent;");
        tabs.getTabs().addAll(
            new Tab("  🔑  Connexion  ",  buildLoginPane()),
            new Tab("  ✍  Inscription  ", buildRegisterPane())
        );

        card.getChildren().addAll(formHead, formSep, tabs);
        rightPanel.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(32));

        root.getChildren().addAll(leftPanel, rightPanel);
        return new Scene(root, 1100, 720);
    }

    private static Label featurePill(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-background-color:rgba(255,255,255,0.11);-fx-text-fill:rgba(186,230,253,0.88);" +
            "-fx-padding:5 12;-fx-background-radius:20;-fx-font-size:11px;-fx-font-weight:bold;" +
            "-fx-border-color:rgba(147,197,253,0.25);-fx-border-radius:20;-fx-border-width:1;");
        l.setWrapText(true);
        return l;
    }

    // ── Onglet Connexion ──────────────────────────────────────────────────────
    private static VBox buildLoginPane() {
        VBox pane = new VBox(10);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(20, 15, 15, 15));

        TextField emailField = UIHelper.styledField("exemple@email.com");
        HBox      passBox    = UIHelper.passwordFieldWithToggle("Mot de passe");
        Label     errorLbl   = UIHelper.errorLabel();

        Button loginBtn = UIHelper.primaryButton("Se connecter");
        loginBtn.setPrefWidth(300);

        loginBtn.setOnAction(e -> {
            errorLbl.setText("");
            try {
                Utilisateur user = MainApp.authService.login(
                        emailField.getText().trim(), UIHelper.getPasswordFromToggle(passBox));
                UIHelper.clearPasswordToggle(passBox);
                if (user.getRole() == Role.ADMIN) {
                    MainApp.primaryStage.setScene(AdminView.createScene(user));
                } else {
                    MainApp.primaryStage.setScene(ClientView.createScene(user));
                }
            } catch (AuthenticationException | ValidationException ex) {
                errorLbl.setText(ex.getMessage());
            } catch (Exception ex) {
                errorLbl.setText("Erreur inattendue : " + ex.getMessage());
            }
        });

        // Appuyer sur Entrée dans le champ mot de passe déclenche la connexion
        PasswordField passField = (PasswordField) passBox.getChildren().get(0);
        passField.setOnAction(loginBtn.getOnAction());

        pane.getChildren().addAll(
                UIHelper.smallLabel("Email :"),    emailField,
                UIHelper.smallLabel("Mot de passe :"), passBox,
                errorLbl, loginBtn
        );
        return pane;
    }

    // ── Onglet Inscription ────────────────────────────────────────────────────
    private static VBox buildRegisterPane() {
        VBox pane = new VBox(9);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(20, 15, 15, 15));

        TextField nomField     = UIHelper.styledField("Votre nom complet");
        TextField emailField   = UIHelper.styledField("exemple@email.com");
        HBox      passBox      = UIHelper.passwordFieldWithToggle("8+ car., 1 majusc., 1 chiffre");
        HBox      confirmBox   = UIHelper.passwordFieldWithToggle("Confirmer le mot de passe");
        Label     errorLbl     = UIHelper.errorLabel();

        Button registerBtn = UIHelper.primaryButton("Créer mon compte");
        registerBtn.setPrefWidth(300);

        registerBtn.setOnAction(e -> {
            errorLbl.setText("");
            String pass    = UIHelper.getPasswordFromToggle(passBox);
            String confirm = UIHelper.getPasswordFromToggle(confirmBox);
            if (!pass.equals(confirm)) {
                errorLbl.setText("Les mots de passe ne correspondent pas.");
                return;
            }
            try {
                Utilisateur user = MainApp.authService.register(
                        nomField.getText(), emailField.getText(), pass);
                UIHelper.showInfo("Inscription réussie",
                        "Bienvenue, " + user.getNom() + " !\n" +
                        "Votre compte a été créé. Vous pouvez maintenant vous connecter.");
                nomField.clear();
                emailField.clear();
                UIHelper.clearPasswordToggle(passBox);
                UIHelper.clearPasswordToggle(confirmBox);
            } catch (ValidationException ex) {
                errorLbl.setText(ex.getMessage());
            } catch (Exception ex) {
                errorLbl.setText("Erreur : " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(
                UIHelper.smallLabel("Nom :"),              nomField,
                UIHelper.smallLabel("Email :"),            emailField,
                UIHelper.smallLabel("Mot de passe :"),     passBox,
                UIHelper.smallLabel("Confirmation :"),     confirmBox,
                errorLbl, registerBtn
        );
        return pane;
    }
}
