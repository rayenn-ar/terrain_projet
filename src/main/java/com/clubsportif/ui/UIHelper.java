package com.clubsportif.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Design System — "SportsPro Ultra".
 * Pro 3D-inspired: gradients, colored glow shadows, depth cues.
 */
public final class UIHelper {

    private UIHelper() {}

        public enum ThemePreset {
                OCEAN("Ocean Pro"),
                EMERALD("Emerald Field"),
                SUNSET("Sunset Arena");

                private final String label;

                ThemePreset(String label) {
                        this.label = label;
                }

                @Override
                public String toString() {
                        return label;
                }
        }

    // ══════════════════════════════════════════════════════════════════
    // PALETTE
    // ══════════════════════════════════════════════════════════════════
        public static String PRIMARY        = "#1D4ED8";
        public static String PRIMARY_DARK   = "#1E3A8A";
        public static String PRIMARY_LIGHT  = "#EFF6FF";
        public static String ACCENT         = "#059669";
        public static String ACCENT_DARK    = "#065F46";
        public static String DANGER         = "#DC2626";
        public static String DANGER_DARK    = "#991B1B";
        public static String WARNING        = "#D97706";
        public static String WARNING_DARK   = "#92400E";
        public static String SUCCESS        = "#059669";
        public static String PURPLE         = "#7C3AED";
        public static String BG             = "#EEF2FF";
        public static String SURFACE        = "white";
        public static String TEXT1          = "#0F172A";
        public static String TEXT2          = "#475569";
        public static String BORDER         = "#E2E8F0";
        public static final String CURRENCY_UNIT = "dt";

        private static ThemePreset currentTheme = ThemePreset.OCEAN;
        private static boolean notificationsEnabled = true;
        private static boolean compactMode = false;
        private static int autoRefreshSeconds = 30;

    // Depth shadows — simulates 3D elevation
    public static final String SHADOW_XS =
            "dropshadow(gaussian,rgba(15,23,42,0.08),6,0,0,2)";
    public static final String SHADOW_SM =
            "dropshadow(gaussian,rgba(15,23,42,0.12),12,0,0,4)";
    public static final String SHADOW_MD =
            "dropshadow(gaussian,rgba(15,23,42,0.18),22,0,0,7)";
    public static final String SHADOW_LG =
            "dropshadow(gaussian,rgba(15,23,42,0.26),36,0,0,12)";
    public static final String SHADOW_XL =
            "dropshadow(gaussian,rgba(15,23,42,0.38),52,0,0,18)";

    // Colored glow — neon depth effect on hover
    public static final String GLOW_BLUE   =
            "dropshadow(gaussian,rgba(29,78,216,0.50),22,0.3,0,0)";
    public static final String GLOW_GREEN  =
            "dropshadow(gaussian,rgba(5,150,105,0.45),22,0.3,0,0)";
    public static final String GLOW_RED    =
            "dropshadow(gaussian,rgba(220,38,38,0.50),22,0.3,0,0)";
    public static final String GLOW_AMBER  =
            "dropshadow(gaussian,rgba(217,119,6,0.50),22,0.3,0,0)";
    public static final String GLOW_PURPLE =
            "dropshadow(gaussian,rgba(124,58,237,0.50),22,0.3,0,0)";

        public static ThemePreset getCurrentTheme() {
                return currentTheme;
        }

        public static void applyTheme(ThemePreset theme) {
                currentTheme = theme;
                switch (theme) {
                        case OCEAN -> {
                                PRIMARY = "#1D4ED8";
                                PRIMARY_DARK = "#1E3A8A";
                                PRIMARY_LIGHT = "#EFF6FF";
                                ACCENT = "#059669";
                                ACCENT_DARK = "#065F46";
                                WARNING = "#D97706";
                                BG = "#EEF2FF";
                                TEXT1 = "#0F172A";
                                TEXT2 = "#475569";
                                BORDER = "#E2E8F0";
                        }
                        case EMERALD -> {
                                PRIMARY = "#047857";
                                PRIMARY_DARK = "#065F46";
                                PRIMARY_LIGHT = "#ECFDF5";
                                ACCENT = "#0EA5E9";
                                ACCENT_DARK = "#0369A1";
                                WARNING = "#CA8A04";
                                BG = "#F0FDF4";
                                TEXT1 = "#052E16";
                                TEXT2 = "#166534";
                                BORDER = "#BBF7D0";
                        }
                        case SUNSET -> {
                                PRIMARY = "#C2410C";
                                PRIMARY_DARK = "#9A3412";
                                PRIMARY_LIGHT = "#FFF7ED";
                                ACCENT = "#B91C1C";
                                ACCENT_DARK = "#7F1D1D";
                                WARNING = "#A16207";
                                BG = "#FFF1F2";
                                TEXT1 = "#431407";
                                TEXT2 = "#7C2D12";
                                BORDER = "#FED7AA";
                        }
                }
        }

        public static boolean isNotificationsEnabled() {
                return notificationsEnabled;
        }

        public static void setNotificationsEnabled(boolean notificationsEnabled) {
                UIHelper.notificationsEnabled = notificationsEnabled;
        }

        public static boolean isCompactMode() {
                return compactMode;
        }

        public static void setCompactMode(boolean compactMode) {
                UIHelper.compactMode = compactMode;
        }

        public static int getAutoRefreshSeconds() {
                return autoRefreshSeconds;
        }

        public static void setAutoRefreshSeconds(int autoRefreshSeconds) {
                UIHelper.autoRefreshSeconds = autoRefreshSeconds;
        }

    // ══════════════════════════════════════════════════════════════════
    // DIALOGS
    // ══════════════════════════════════════════════════════════════════
    public static void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    // ══════════════════════════════════════════════════════════════════
    // BUTTONS — gradient + colored glow hover + 3D press depth
    // ══════════════════════════════════════════════════════════════════
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        String base  = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#3B82F6," + PRIMARY + ");" +
                "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-padding:11 26;-fx-background-radius:10;-fx-cursor:hand;" +
                "-fx-effect:" + SHADOW_SM + ";";
        String hover = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#60A5FA,#2563EB);" +
                "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-padding:11 26;-fx-background-radius:10;-fx-cursor:hand;" +
                "-fx-effect:" + GLOW_BLUE + ";";
        String press = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%," + PRIMARY + ",#1E3A8A);" +
                "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-padding:12 26 10 26;-fx-background-radius:10;-fx-cursor:hand;" +
                "-fx-effect:" + SHADOW_XS + ";";
        btn.setStyle(base);
        btn.setOnMouseEntered(e  -> btn.setStyle(hover));
        btn.setOnMouseExited(e   -> btn.setStyle(base));
        btn.setOnMousePressed(e  -> btn.setStyle(press));
        btn.setOnMouseReleased(e -> btn.setStyle(hover));
        return btn;
    }

    public static Button dangerButton(String text) {
        Button btn = new Button(text);
        String base  = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#EF4444," + DANGER + ");" +
                "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:8 16;-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-effect:" + SHADOW_XS + ";";
        String hover = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#F87171,#E53E3E);" +
                "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:8 16;-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-effect:" + GLOW_RED + ";";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    public static Button successButton(String text) {
        Button btn = new Button(text);
        String base  = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#10B981," + ACCENT + ");" +
                "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:9 18;-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-effect:" + SHADOW_XS + ";";
        String hover = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#34D399,#059669);" +
                "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:9 18;-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-effect:" + GLOW_GREEN + ";";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    public static Button warningButton(String text) {
        Button btn = new Button(text);
        String base  = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#FBBF24," + WARNING + ");" +
                "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:8 16;-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-effect:" + SHADOW_XS + ";";
        String hover = "-fx-background-color:linear-gradient(from 0% 0% to 0% 100%,#FCD34D,#D97706);" +
                "-fx-text-fill:white;-fx-font-size:12px;-fx-font-weight:bold;" +
                "-fx-padding:8 16;-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-effect:" + GLOW_AMBER + ";";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    public static Button ghostButton(String text) {
        Button btn = new Button(text);
        String base  = "-fx-background-color:transparent;-fx-text-fill:" + PRIMARY + ";" +
                "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:8 16;" +
                "-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-border-color:" + PRIMARY + ";-fx-border-radius:8;-fx-border-width:1.5;";
        String hover = "-fx-background-color:" + PRIMARY_LIGHT + ";-fx-text-fill:" + PRIMARY + ";" +
                "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:8 16;" +
                "-fx-background-radius:8;-fx-cursor:hand;" +
                "-fx-border-color:" + PRIMARY + ";-fx-border-radius:8;-fx-border-width:1.5;" +
                "-fx-effect:" + GLOW_BLUE + ";";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════
    // LABELS
    // ══════════════════════════════════════════════════════════════════
    public static Label titleLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:25px;-fx-font-weight:bold;-fx-text-fill:" + TEXT1 + ";" +
                "-fx-effect:" + SHADOW_XS + ";");
        return l;
    }

    public static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:" + TEXT1 + ";");
        return l;
    }

    public static Label smallLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-text-fill:" + TEXT2 + ";-fx-font-weight:bold;-fx-padding:0 0 2 0;");
        return l;
    }

    public static Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill:" + DANGER + ";-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:4 0;");
        l.setWrapText(true);
        l.setMaxWidth(400);
        return l;
    }

    public static Label statusBadge(String text, String bgColor, String textColor) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:" + bgColor + ";-fx-text-fill:" + textColor + ";" +
                "-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:3 12;" +
                "-fx-background-radius:20;");
        return l;
    }

    // ══════════════════════════════════════════════════════════════════
    // INPUTS — glow focus border
    // ══════════════════════════════════════════════════════════════════
    private static String fieldBase() {
        return "-fx-padding:10 13;-fx-border-color:" + BORDER + ";"
                + "-fx-border-radius:9;-fx-background-radius:9;-fx-border-width:1.5;"
                + "-fx-background-color:white;-fx-font-size:13px;-fx-text-fill:" + TEXT1 + ";"
                + "-fx-effect:" + SHADOW_XS + ";";
    }

    private static String fieldFocus() {
        return "-fx-padding:10 13;-fx-border-color:" + PRIMARY + ";"
                + "-fx-border-radius:9;-fx-background-radius:9;-fx-border-width:2;"
                + "-fx-background-color:white;-fx-font-size:13px;-fx-text-fill:" + TEXT1 + ";"
                + "-fx-effect:" + GLOW_BLUE + ";";
    }

    public static TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(fieldBase());
        f.setPrefWidth(340);
        f.focusedProperty().addListener((obs, o, n) ->
                f.setStyle(n ? fieldFocus() : fieldBase()));
        return f;
    }

    public static PasswordField styledPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(fieldBase());
        f.setPrefWidth(340);
        f.focusedProperty().addListener((obs, o, n) ->
                f.setStyle(n ? fieldFocus() : fieldBase()));
        return f;
    }

    public static HBox passwordFieldWithToggle(String prompt) {
        String pfBase  = "-fx-padding:10 13;-fx-border-color:" + BORDER + ";" +
                "-fx-border-radius:9 0 0 9;-fx-background-radius:9 0 0 9;" +
                "-fx-border-width:1.5 0 1.5 1.5;" +
                "-fx-background-color:white;-fx-font-size:13px;";
        String pfFocus = "-fx-padding:10 13;-fx-border-color:" + PRIMARY + ";" +
                "-fx-border-radius:9 0 0 9;-fx-background-radius:9 0 0 9;" +
                "-fx-border-width:2 0 2 2;" +
                "-fx-background-color:white;-fx-font-size:13px;";

        PasswordField passField = new PasswordField();
        passField.setPromptText(prompt);
        passField.setStyle(pfBase);
        passField.setPrefWidth(310);
        passField.focusedProperty().addListener((obs, o, n) ->
                passField.setStyle(n ? pfFocus : pfBase));

        TextField visibleField = new TextField();
        visibleField.setPromptText(prompt);
        visibleField.setStyle(pfBase);
        visibleField.setPrefWidth(310);
        visibleField.setManaged(false);
        visibleField.setVisible(false);
        visibleField.textProperty().bindBidirectional(passField.textProperty());

        Button toggleBtn = new Button("👁");
        String tBase  = "-fx-padding:10 12;-fx-border-color:" + BORDER + ";" +
                "-fx-border-width:1.5 1.5 1.5 0;-fx-border-radius:0 9 9 0;" +
                "-fx-background-radius:0 9 9 0;-fx-background-color:white;" +
                "-fx-cursor:hand;-fx-font-size:14px;-fx-text-fill:" + TEXT2 + ";";
        String tHover = "-fx-padding:10 12;-fx-border-color:" + PRIMARY + ";" +
                "-fx-border-width:2 2 2 0;-fx-border-radius:0 9 9 0;" +
                "-fx-background-radius:0 9 9 0;-fx-background-color:" + PRIMARY_LIGHT + ";" +
                "-fx-cursor:hand;-fx-font-size:14px;-fx-text-fill:" + PRIMARY + ";";
        toggleBtn.setStyle(tBase);
        toggleBtn.setOnMouseEntered(e -> toggleBtn.setStyle(tHover));
        toggleBtn.setOnMouseExited(e  -> toggleBtn.setStyle(tBase));
        toggleBtn.setOnAction(e -> {
            boolean show = visibleField.isVisible();
            visibleField.setVisible(!show);
            visibleField.setManaged(!show);
            passField.setVisible(show);
            passField.setManaged(show);
            toggleBtn.setText(show ? "👁" : "🙈");
        });

        HBox box = new HBox(0, passField, visibleField, toggleBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static String getPasswordFromToggle(HBox toggleBox) {
        return ((PasswordField) toggleBox.getChildren().get(0)).getText();
    }

    public static void clearPasswordToggle(HBox toggleBox) {
        ((PasswordField) toggleBox.getChildren().get(0)).clear();
    }

    // ══════════════════════════════════════════════════════════════════
    // CARDS
    // ══════════════════════════════════════════════════════════════════
    public static VBox card(double padding) {
        VBox c = new VBox(14);
        c.setPadding(new Insets(padding));
        c.setStyle("-fx-background-color:white;-fx-background-radius:16;" +
                "-fx-effect:" + SHADOW_MD + ";");
        return c;
    }

    public static VBox accentCard(double padding, String accentColor) {
        VBox c = new VBox(14);
        c.setPadding(new Insets(padding, padding, padding, padding + 4));
        c.setStyle("-fx-background-color:white;-fx-background-radius:0 14 14 0;" +
                "-fx-border-color:" + accentColor + " transparent transparent transparent;" +
                "-fx-border-width:0 0 0 5;-fx-effect:" + SHADOW_MD + ";");
        return c;
    }

    public static HBox sectionHeader(String icon, String title, String accentColor) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(13, 18, 13, 18));
        h.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                "-fx-border-color:" + accentColor + " transparent transparent transparent;" +
                "-fx-border-width:3 0 0 0;-fx-border-radius:12 12 0 0;" +
                "-fx-effect:" + SHADOW_SM + ";");
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:20px;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + TEXT1 + ";");
        h.getChildren().addAll(ico, lbl);
        return h;
    }

    public static Label infoBanner(String text, String bgColor, String textColor) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:" + bgColor + ";-fx-text-fill:" + textColor + ";" +
                "-fx-padding:11 16;-fx-background-radius:10;-fx-font-size:13px;" +
                "-fx-effect:" + SHADOW_XS + ";");
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    // ══════════════════════════════════════════════════════════════════
    // NAVIGATION BAR — deep dark gradient + glow
    // ══════════════════════════════════════════════════════════════════
    public static HBox navBar(String username, boolean isAdmin, Runnable onLogout) {
        HBox nav = new HBox(14);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(0, 26, 0, 26));
        nav.setPrefHeight(62);
        nav.setStyle(
                "-fx-background-color:linear-gradient(from 0% 0% to 100% 0%,#060E2B,#0D205A,#1D4ED8 280%);" +
                "-fx-effect:" + SHADOW_LG + ";");

        Label logo = new Label("🏟");
        logo.setStyle("-fx-font-size:26px;");
        Label appName = new Label("SportsPro");
        appName.setStyle("-fx-text-fill:white;-fx-font-size:19px;-fx-font-weight:bold;" +
                "-fx-effect:dropshadow(gaussian,rgba(96,165,250,0.60),8,0.3,0,0);");
        Label appSub = new Label("Terrains Sportifs");
        appSub.setStyle("-fx-text-fill:rgba(148,163,184,0.80);-fx-font-size:10.5px;");
        VBox logoBox = new VBox(-1, appName, appSub);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Separator divider = new Separator(javafx.geometry.Orientation.VERTICAL);
        divider.setMaxHeight(30);
        divider.setStyle("-fx-background-color:rgba(255,255,255,0.18);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String roleColor = isAdmin ? "#F59E0B" : "#10B981";
        String roleText  = isAdmin ? "👑  ADMIN" : "👤  Client";
        Label badge = new Label(roleText);
        badge.setStyle("-fx-background-color:" + roleColor + ";-fx-text-fill:white;" +
                "-fx-padding:4 13;-fx-background-radius:20;-fx-font-size:10.5px;" +
                "-fx-font-weight:bold;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.35),6,0,0,2);");

        String initials = username.length() >= 2
                ? username.substring(0, 2).toUpperCase() : username.toUpperCase();
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-background-color:rgba(255,255,255,0.16);-fx-text-fill:white;" +
                "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:8 10;" +
                "-fx-background-radius:50;-fx-min-width:36;-fx-min-height:36;" +
                "-fx-border-color:rgba(255,255,255,0.30);-fx-border-radius:50;-fx-border-width:1;" +
                "-fx-alignment:center;");
        avatar.setAlignment(Pos.CENTER);

        Label userLbl = new Label(username.length() > 18 ? username.substring(0, 16) + "…" : username);
        userLbl.setStyle("-fx-text-fill:rgba(226,232,240,0.92);-fx-font-size:13px;-fx-font-weight:bold;");

        String logBase  = "-fx-background-color:rgba(255,255,255,0.09);-fx-text-fill:white;" +
                "-fx-border-color:rgba(255,255,255,0.30);-fx-border-radius:8;-fx-background-radius:8;" +
                "-fx-cursor:hand;-fx-font-size:12px;-fx-padding:7 15;-fx-font-weight:bold;";
        String logHover = "-fx-background-color:rgba(220,38,38,0.72);-fx-text-fill:white;" +
                "-fx-border-color:rgba(239,68,68,0.55);-fx-border-radius:8;-fx-background-radius:8;" +
                "-fx-cursor:hand;-fx-font-size:12px;-fx-padding:7 15;-fx-font-weight:bold;" +
                "-fx-effect:" + GLOW_RED + ";";
        Button logoutBtn = new Button("⎋  Déconnexion");
        logoutBtn.setStyle(logBase);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logHover));
        logoutBtn.setOnMouseExited(e  -> logoutBtn.setStyle(logBase));
        logoutBtn.setOnAction(e -> onLogout.run());

        nav.getChildren().addAll(logo, logoBox, divider, spacer, badge, avatar, userLbl, logoutBtn);
        return nav;
    }

    // ══════════════════════════════════════════════════════════════════
    // STAT CARD — KPI with gradient header + lift hover
    // ══════════════════════════════════════════════════════════════════
    public static VBox statCard(String icon, String label, String value, String accentColor) {
        HBox header = new HBox(9);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(11, 16, 11, 16));
        header.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%," +
                accentColor + ",rgba(0,0,0,0.18) 250%);" +
                "-fx-background-radius:13 13 0 0;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:22px;");
        Label nameLbl = new Label(label.toUpperCase());
        nameLbl.setStyle("-fx-font-size:10.5px;-fx-font-weight:bold;-fx-text-fill:rgba(255,255,255,0.92);" +
                "-fx-letter-spacing:0.5;");
        header.getChildren().addAll(iconLbl, nameLbl);

        VBox body = new VBox(3);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(13, 16, 17, 16));
        body.setStyle("-fx-background-color:white;-fx-background-radius:0 0 13 13;");
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size:33px;-fx-font-weight:bold;-fx-text-fill:" + TEXT1 + ";");
        body.getChildren().add(valLbl);

        VBox card = new VBox(0, header, body);
        card.setMinWidth(172);
        String normal = "-fx-background-radius:13;-fx-effect:" + SHADOW_MD + ";";
        String lifted = "-fx-background-radius:13;-fx-effect:" + SHADOW_LG + ";-fx-translate-y:-3;";
        card.setStyle(normal);
        card.setOnMouseEntered(e -> card.setStyle(lifted));
        card.setOnMouseExited(e  -> card.setStyle(normal));
        return card;
    }
}
