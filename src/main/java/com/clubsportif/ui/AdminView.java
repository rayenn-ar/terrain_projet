package com.clubsportif.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.clubsportif.MainApp;
import com.clubsportif.config.SecurityConfig;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.Role;
import com.clubsportif.model.enums.StatutReservation;
import com.clubsportif.model.enums.TypeTerrain;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

/**
 * Tableau de bord administrateur JavaFX.
 */
public final class AdminView {

    private AdminView() {}

    public static Scene createScene(Utilisateur admin) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIHelper.BG + ";");

        root.setTop(UIHelper.navBar(admin.getNom(), true, () -> {
            MainApp.authService.logout();
            MainApp.primaryStage.setScene(LoginView.createScene());
        }));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-tab-min-width:148;-fx-tab-min-height:38;");
        tabs.getTabs().addAll(
            new Tab("📊  Statistiques",   buildStatsTab()),
            new Tab("📋  Réservations",   buildReservationsTab()),
            new Tab("🏟  Terrains",        buildTerrainsTab()),
            new Tab("👥  Utilisateurs",    buildUsersTab()),
            new Tab("⚙  Paramètres",      buildSettingsTab(admin))
        );

        root.setCenter(tabs);
        return new Scene(root, 1050, 720);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 1 : Statistiques
    // ═══════════════════════════════════════════════════════════════════════
    private static ScrollPane buildStatsTab() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(28));

    // Admin banner with gradient header
    VBox headerBanner = new VBox(2);
    headerBanner.setPadding(new Insets(16, 24, 16, 24));
    headerBanner.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 100% 0%," +
        "#060E2B,#0D205A,#1D4ED8 300%);" +
        "-fx-background-radius:14;-fx-effect:" + UIHelper.SHADOW_MD + ";");
    Label bannerTitle = new Label("📊  Tableau de Bord Administrateur");
    bannerTitle.setStyle("-fx-font-size:21px;-fx-font-weight:bold;-fx-text-fill:white;");
    Label bannerSub = new Label("Vue d'ensemble des activités et statistiques");
    bannerSub.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(147,197,253,0.82);");
    headerBanner.getChildren().addAll(bannerTitle, bannerSub);
    content.getChildren().add(headerBanner);

        try {
            Map<String, Object> stats = MainApp.reservationService.getStatistiques();

            // ── Cartes de stats ──────────────────────────────────────────────
            HBox cards = new HBox(18);
            cards.setAlignment(Pos.CENTER_LEFT);
            cards.getChildren().addAll(
                        UIHelper.statCard("🏟", "Terrains utilisés",
                            String.valueOf(stats.getOrDefault("terrainsUtilises", 0)), UIHelper.PRIMARY),
                        UIHelper.statCard("📅", "Réservations",
                            String.valueOf(stats.getOrDefault("totalReservations", 0)), "#2E7D32"),
                        UIHelper.statCard("✅", "Confirmées",
                            String.valueOf(stats.getOrDefault("reservationsConfirmees", 0)), UIHelper.ACCENT),
                        UIHelper.statCard("💰", "Chiffre d'affaires",
                            stats.getOrDefault("chiffreAffaires", "0") + " " + UIHelper.CURRENCY_UNIT, UIHelper.WARNING)
            );

            content.getChildren().add(cards);

            HBox charts = new HBox(14);
            charts.setAlignment(Pos.CENTER_LEFT);
            charts.getChildren().addAll(buildReservationsByTypeChart(stats), buildStatusChart());
            content.getChildren().add(charts);

            // ── Répartition par type ──────────────────────────────────────────
            Object byType = stats.get("reservationsParType");
            if (byType instanceof Map<?, ?> map && !map.isEmpty()) {
                content.getChildren().add(UIHelper.sectionLabel("Répartition par type de terrain :"));
                HBox typeCards = new HBox(12);
                typeCards.setAlignment(Pos.CENTER_LEFT);
                String[] colors = {"#1565C0", "#2E7D32", "#6A1B9A", "#E65100"};
                int[] idx = {0};
                map.forEach((k, v) -> {
                    typeCards.getChildren().add(
                            UIHelper.statCard("📌", k.toString(),
                                    v + " rés.", colors[idx[0]++ % colors.length]));
                });
                content.getChildren().add(typeCards);
            }

            // ── Réservations récentes ─────────────────────────────────────────
            content.getChildren().add(UIHelper.sectionLabel("Réservations actives (CONFIRMÉES) :"));
            TableView<com.clubsportif.model.Reservation> recentTable = buildMiniRecentTable();
            recentTable.setMaxHeight(200);
            content.getChildren().add(recentTable);

        } catch (Exception e) {
                content.getChildren().add(
                    new Label("Erreur de chargement des statistiques : " + e.getMessage()));
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        return sp;
    }

    private static TableView<com.clubsportif.model.Reservation> buildMiniRecentTable() {
        TableView<com.clubsportif.model.Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucune réservation confirmée."));

        TableColumn<com.clubsportif.model.Reservation, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomClient() != null ? d.getValue().getNomClient() : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colTerrain = new TableColumn<>("Terrain");
        colTerrain.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomTerrain() != null ? d.getValue().getNomTerrain() : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateReservation() != null
                        ? d.getValue().getDateReservation().toString() : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colHoraire = new TableColumn<>("Horaire");
        colHoraire.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHeureDebut() + " → " + d.getValue().getHeureFin()));

        table.getColumns().add(colClient);
        table.getColumns().add(colTerrain);
        table.getColumns().add(colDate);
        table.getColumns().add(colHoraire);

        try {
            var all = MainApp.reservationService.getAllReservations();
            var confirmed = all.stream()
                    .filter(r -> r.getStatut() == com.clubsportif.model.enums.StatutReservation.CONFIRMEE)
                    .toList();
            table.setItems(FXCollections.observableArrayList(confirmed));
        } catch (Exception e) {
            // silencieux dans la mini-table
        }
        return table;
    }

    private static VBox buildReservationsByTypeChart(Map<String, Object> stats) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Réservations par type");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefSize(430, 260);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Object byType = stats.get("reservationsParType");
        if (byType instanceof Map<?, ?> map) {
            map.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k.toString(), toNumber(v))));
        }
        chart.getData().add(series);

        VBox box = UIHelper.card(10);
        box.setPrefSize(450, 280);
        box.getChildren().add(chart);
        return box;
    }

    private static VBox buildStatusChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Répartition par statut");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setPrefSize(430, 260);

        try {
            List<com.clubsportif.model.Reservation> all = MainApp.reservationService.getAllReservations();
            long c = all.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count();
            long a = all.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count();
            long t = all.stream().filter(r -> r.getStatut() == StatutReservation.TERMINEE).count();
            chart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Confirmées", c),
                    new PieChart.Data("Annulées", a),
                    new PieChart.Data("Terminées", t)
            ));
        } catch (Exception ignored) {
            chart.setData(FXCollections.observableArrayList());
        }

        VBox box = UIHelper.card(10);
        box.setPrefSize(450, 280);
        box.getChildren().add(chart);
        return box;
    }

    private static Number toNumber(Object value) {
        if (value instanceof Number n) {
            return n;
        }
        if (value instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 2 : Toutes les réservations
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildReservationsTab() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label title = UIHelper.sectionLabel("📋 Toutes les Réservations");

        var table = buildAdminReservationTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField searchField = UIHelper.styledField("Rechercher: client, terrain, date...");
        searchField.setPrefWidth(260);

        ComboBox<String> statutFilter = new ComboBox<>();
        statutFilter.getItems().addAll("Tous", "Confirmée", "Annulée", "Terminée");
        statutFilter.setValue("Tous");
        statutFilter.setPrefWidth(130);

        ComboBox<String> periodeFilter = new ComboBox<>();
        periodeFilter.getItems().addAll("Toutes", "Aujourd'hui", "7 prochains jours", "30 prochains jours", "Passées");
        periodeFilter.setValue("Toutes");
        periodeFilter.setPrefWidth(175);

        Label stats = new Label();
        stats.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIHelper.TEXT2 + ";-fx-font-weight:bold;");

        Runnable applyFilters = () -> {
            try {
                String query  = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
                String statut = statutFilter.getValue();
                String periode = periodeFilter.getValue();
                LocalDate today = LocalDate.now();

                List<com.clubsportif.model.Reservation> filtered =
                        MainApp.reservationService.getAllReservations().stream()
                        .filter(r -> {
                            if (query.isBlank()) return true;
                            String client  = r.getNomClient()  == null ? "" : r.getNomClient().toLowerCase();
                            String terrain = r.getNomTerrain() == null ? "" : r.getNomTerrain().toLowerCase();
                            String date    = r.getDateReservation() == null ? "" : r.getDateReservation().toString();
                            return client.contains(query) || terrain.contains(query) || date.contains(query);
                        })
                        .filter(r -> "Tous".equals(statut) || (r.getStatut() != null &&
                                r.getStatut().getLibelle().equalsIgnoreCase(statut)))
                        .filter(r -> {
                            if (r.getDateReservation() == null || "Toutes".equals(periode)) return true;
                            LocalDate d = r.getDateReservation();
                            return switch (periode) {
                                case "Aujourd'hui"        -> d.isEqual(today);
                                case "7 prochains jours"  -> !d.isBefore(today) && !d.isAfter(today.plusDays(7));
                                case "30 prochains jours" -> !d.isBefore(today) && !d.isAfter(today.plusDays(30));
                                case "Passées"            -> d.isBefore(today);
                                default -> true;
                            };
                        })
                        .sorted(Comparator
                                .comparing(com.clubsportif.model.Reservation::getDateReservation,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(com.clubsportif.model.Reservation::getHeureDebut,
                                        Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.toList());

                table.setItems(FXCollections.observableArrayList(filtered));
                table.getProperties().put("currentFiltered", filtered);

                long confirmees = filtered.stream()
                        .filter(r -> r.getStatut() == com.clubsportif.model.enums.StatutReservation.CONFIRMEE)
                        .count();
                BigDecimal revenue = filtered.stream()
                        .filter(r -> r.getStatut() == com.clubsportif.model.enums.StatutReservation.CONFIRMEE)
                        .map(com.clubsportif.model.Reservation::getMontantTotal)
                        .filter(m -> m != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setText("Total: " + filtered.size() + " | Confirmées: " + confirmees + " | CA: "
                        + revenue + " " + UIHelper.CURRENCY_UNIT);
            } catch (Exception ex) {
                UIHelper.showError("Erreur", ex.getMessage());
            }
        };

        table.getProperties().put("refreshAction", applyFilters);

        Button resetBtn = UIHelper.ghostButton("Réinitialiser");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            statutFilter.setValue("Tous");
            periodeFilter.setValue("Toutes");
            applyFilters.run();
        });

        Button refreshBtn = UIHelper.primaryButton("🔄 Actualiser");
        refreshBtn.setOnAction(e -> applyFilters.run());

        Button exportBtn = UIHelper.ghostButton("Exporter CSV");
        exportBtn.setOnAction(e -> exportAdminReservationsCsv(table));

        searchField.textProperty().addListener((obs, o, n) -> applyFilters.run());
        statutFilter.valueProperty().addListener((obs, o, n) -> applyFilters.run());
        periodeFilter.valueProperty().addListener((obs, o, n) -> applyFilters.run());
        applyFilters.run();

        HBox toolbar = new HBox(10, UIHelper.smallLabel("Statut:"), statutFilter,
                UIHelper.smallLabel("Période:"), periodeFilter, searchField, resetBtn, exportBtn, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(title, stats, table, toolbar);
        return content;
    }

    private static TableView<com.clubsportif.model.Reservation> buildAdminReservationTable() {
        TableView<com.clubsportif.model.Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucune réservation."));

        TableColumn<com.clubsportif.model.Reservation, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getIdReservation())));
        colId.setMaxWidth(55);

        TableColumn<com.clubsportif.model.Reservation, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomClient() != null ? d.getValue().getNomClient() : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colTerrain = new TableColumn<>("Terrain");
        colTerrain.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomTerrain() != null ? d.getValue().getNomTerrain() : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateReservation() != null
                        ? d.getValue().getDateReservation().toString() : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colHoraire = new TableColumn<>("Horaire");
        colHoraire.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHeureDebut() + "  →  " + d.getValue().getHeureFin()));

        TableColumn<com.clubsportif.model.Reservation, String> colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMontantTotal() != null
                ? d.getValue().getMontantTotal() + " " + UIHelper.CURRENCY_UNIT : "—"));

        TableColumn<com.clubsportif.model.Reservation, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatut().getLibelle()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String bg, fg;
                if (v.equalsIgnoreCase("Confirmée"))      { bg = "#D1FAE5"; fg = "#065F46"; }
                else if (v.equalsIgnoreCase("Annulée"))   { bg = "#FEE2E2"; fg = "#7F1D1D"; }
                else                                       { bg = "#F3F4F6"; fg = "#374151"; }
                setGraphic(UIHelper.statusBadge(v, bg, fg));
            }
        });

        TableColumn<com.clubsportif.model.Reservation, Void> colAction = new TableColumn<>("Action");
        colAction.setMaxWidth(100);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = UIHelper.dangerButton("Annuler");
            {
                btn.setOnAction(e -> {
                    var r = getTableView().getItems().get(getIndex());
                    if (UIHelper.showConfirm("Annuler", "Annuler la réservation #" +
                            r.getIdReservation() + " ?")) {
                        try {
                            MainApp.reservationService.annulerReservationAdmin(r.getIdReservation());
                            Object act = getTableView().getProperties().get("refreshAction");
                            if (act instanceof Runnable run) run.run();
                            else ClientView.loadAllReservations(getTableView());
                        } catch (Exception ex) {
                            UIHelper.showError("Erreur", ex.getMessage());
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                var r = getTableView().getItems().get(getIndex());
                setGraphic(r.getStatut() == com.clubsportif.model.enums.StatutReservation.CONFIRMEE
                        ? btn : null);
            }
        });

        table.getColumns().add(colId);
        table.getColumns().add(colClient);
        table.getColumns().add(colTerrain);
        table.getColumns().add(colDate);
        table.getColumns().add(colHoraire);
        table.getColumns().add(colMontant);
        table.getColumns().add(colStatut);
        table.getColumns().add(colAction);
        return table;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 3 : Gestion des terrains
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildTerrainsTab() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label title = UIHelper.sectionLabel("🏟 Gestion des Terrains");

        TableView<Terrain> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucun terrain enregistré."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField terrainSearchField = UIHelper.styledField("Rechercher par nom ou description...");
        terrainSearchField.setPrefWidth(240);

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("Tous");
        for (TypeTerrain t : TypeTerrain.values()) typeFilter.getItems().add(t.getLibelle());
        typeFilter.setValue("Tous");
        typeFilter.setPrefWidth(140);

        ComboBox<String> dispoFilter = new ComboBox<>();
        dispoFilter.getItems().addAll("Tous", "Disponibles", "Indisponibles");
        dispoFilter.setValue("Tous");
        dispoFilter.setPrefWidth(130);

        Label terrainStats = new Label();
        terrainStats.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIHelper.TEXT2 + ";-fx-font-weight:bold;");

        Runnable applyTerrainFilters = () -> {
            try {
                String q = terrainSearchField.getText() == null ? "" : terrainSearchField.getText().trim().toLowerCase();
                String type = typeFilter.getValue();
                String dispo = dispoFilter.getValue();
                List<Terrain> filtered = MainApp.terrainService.findAll().stream()
                        .filter(t -> q.isBlank() || t.getNom().toLowerCase().contains(q)
                                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
                        .filter(t -> "Tous".equals(type) || t.getType().getLibelle().equals(type))
                        .filter(t -> "Tous".equals(dispo)
                                || ("Disponibles".equals(dispo) && t.isDisponibilite())
                                || ("Indisponibles".equals(dispo) && !t.isDisponibilite()))
                        .collect(Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
                long dispos = filtered.stream().filter(Terrain::isDisponibilite).count();
                terrainStats.setText("Total: " + filtered.size() + " | Disponibles: " + dispos
                        + " | Indisponibles: " + (filtered.size() - dispos));
            } catch (Exception ex) {
                UIHelper.showError("Erreur", ex.getMessage());
            }
        };

        table.getProperties().put("refreshAction", applyTerrainFilters);
        terrainSearchField.textProperty().addListener((obs, o, n) -> applyTerrainFilters.run());
        typeFilter.valueProperty().addListener((obs, o, n) -> applyTerrainFilters.run());
        dispoFilter.valueProperty().addListener((obs, o, n) -> applyTerrainFilters.run());

        TableColumn<Terrain, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getIdTerrain())));
        colId.setMaxWidth(55);

        TableColumn<Terrain, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Terrain, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getType().getLibelle()));

        TableColumn<Terrain, String> colPrix = new TableColumn<>("Prix/h");
        colPrix.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPrixParHeure() + " " + UIHelper.CURRENCY_UNIT));
        colPrix.setMaxWidth(90);

        TableColumn<Terrain, String> colDispo = new TableColumn<>("Statut");
        colDispo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isDisponibilite() ? "✅ Disponible" : "❌ Indisponible"));
        colDispo.setMaxWidth(120);

        TableColumn<Terrain, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Terrain, Void> colActions = new TableColumn<>("Actions");
        colActions.setMaxWidth(200);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnToggle = UIHelper.warningButton("Toggle dispo");
            private final Button btnDel    = UIHelper.dangerButton("Supprimer");
            private final HBox   box       = new HBox(6, btnToggle, btnDel);
            { box.setAlignment(Pos.CENTER); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Terrain t = getTableView().getItems().get(getIndex());
                btnToggle.setText(t.isDisponibilite() ? "⛔ Désactiver" : "✅ Activer");
                btnToggle.setOnAction(e -> {
                    try {
                        MainApp.terrainService.toggleDisponibilite(t.getIdTerrain());
                        Object act = getTableView().getProperties().get("refreshAction");
                        if (act instanceof Runnable run) run.run();
                        else loadTerrainsTable(getTableView());
                    } catch (Exception ex) { UIHelper.showError("Erreur", ex.getMessage()); }
                });
                btnDel.setOnAction(e -> {
                    if (UIHelper.showConfirm("Supprimer",
                            "Supprimer définitivement le terrain \"" + t.getNom() + "\" ?")) {
                        try {
                            MainApp.terrainService.supprimerTerrain(t.getIdTerrain());
                            Object act = getTableView().getProperties().get("refreshAction");
                            if (act instanceof Runnable run) run.run();
                            else loadTerrainsTable(getTableView());
                        } catch (Exception ex) { UIHelper.showError("Erreur", ex.getMessage()); }
                    }
                });
                setGraphic(box);
            }
        });

        table.getColumns().add(colId);
        table.getColumns().add(colNom);
        table.getColumns().add(colType);
        table.getColumns().add(colPrix);
        table.getColumns().add(colDispo);
        table.getColumns().add(colDesc);
        table.getColumns().add(colActions);
        applyTerrainFilters.run();

        HBox terrainToolbar = new HBox(10, UIHelper.smallLabel("Type:"), typeFilter,
                UIHelper.smallLabel("Dispo:"), dispoFilter, terrainSearchField);
        terrainToolbar.setAlignment(Pos.CENTER_RIGHT);

        // ── Formulaire d'ajout ─────────────────────────────────────────────
        TitledPane addPane = buildAddTerrainPane(applyTerrainFilters);

        content.getChildren().addAll(title, terrainStats, table, terrainToolbar, addPane);
        return content;
    }

    private static TitledPane buildAddTerrainPane(Runnable refreshAction) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField nomField = UIHelper.styledField("Nom du terrain");

        ComboBox<TypeTerrain> combType = new ComboBox<>(FXCollections.observableArrayList(TypeTerrain.values()));
        combType.setConverter(new StringConverter<>() {
            @Override public String toString(TypeTerrain t) { return t == null ? "" : t.getLibelle(); }
            @Override public TypeTerrain fromString(String s) { return null; }
        });
        combType.setValue(TypeTerrain.FOOTBALL);
        combType.setPrefWidth(300);

        Spinner<Double> spinPrix = new Spinner<>(0.0, 9999.0, 25.0, 5.0);
        spinPrix.setEditable(true);
        spinPrix.setPrefWidth(300);

        TextField descField = UIHelper.styledField("Description (optionnel)");

        CheckBox dispoCheck = new CheckBox("Disponible immédiatement");
        dispoCheck.setSelected(true);

        Label errLbl = UIHelper.errorLabel();

        Button btnAdd = UIHelper.successButton("➕  Ajouter le terrain");

        btnAdd.setOnAction(e -> {
            errLbl.setText("");
            try {
                MainApp.terrainService.ajouterTerrain(
                        nomField.getText(),
                        combType.getValue(),
                        BigDecimal.valueOf(spinPrix.getValue()),
                        dispoCheck.isSelected(),
                        descField.getText()
                );
                UIHelper.showInfo("Terrain ajouté", "Le terrain a été ajouté avec succès !");
                nomField.clear();
                descField.clear();
                spinPrix.getValueFactory().setValue(25.0);
                refreshAction.run();
            } catch (ValidationException ex) {
                errLbl.setText(ex.getMessage());
            } catch (Exception ex) {
                UIHelper.showError("Erreur", ex.getMessage());
            }
        });

        grid.addRow(0, UIHelper.smallLabel("Nom :"),              nomField);
        grid.addRow(1, UIHelper.smallLabel("Type :"),             combType);
        grid.addRow(2, UIHelper.smallLabel("Prix/heure (dt) :"),   spinPrix);
        grid.addRow(3, UIHelper.smallLabel("Description :"),      descField);
        grid.add(dispoCheck, 1, 4);
        grid.add(errLbl,     0, 5, 2, 1);
        grid.add(btnAdd,     1, 6);

        TitledPane pane = new TitledPane("➕  Ajouter un nouveau terrain", grid);
        pane.setExpanded(false);
        return pane;
    }

    private static void loadTerrainsTable(TableView<Terrain> table) {
        try {
            table.setItems(FXCollections.observableArrayList(MainApp.terrainService.findAll()));
        } catch (Exception e) {
            UIHelper.showError("Erreur", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void exportAdminReservationsCsv(TableView<com.clubsportif.model.Reservation> table) {
        try {
            List<com.clubsportif.model.Reservation> data =
                    (List<com.clubsportif.model.Reservation>) table.getProperties().get("currentFiltered");
            if (data == null || data.isEmpty()) {
                UIHelper.showInfo("Export CSV", "Aucune réservation à exporter.");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter les réservations");
            chooser.setInitialFileName("reservations-admin.csv");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            java.io.File out = chooser.showSaveDialog(MainApp.primaryStage);
            if (out == null) return;
            try (java.io.FileWriter fw = new java.io.FileWriter(out, java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write("id,client,terrain,date,heure_debut,heure_fin,duree,statut,montant\n");
                for (com.clubsportif.model.Reservation r : data) {
                    fw.write(r.getIdReservation() + ","
                            + adminSafeCsv(r.getNomClient()) + ","
                            + adminSafeCsv(r.getNomTerrain()) + ","
                            + r.getDateReservation() + ","
                            + r.getHeureDebut() + ","
                            + r.getHeureFin() + ","
                            + r.getDureeHeures() + ","
                            + (r.getStatut() != null ? r.getStatut().getLibelle() : "") + ","
                            + (r.getMontantTotal() != null ? r.getMontantTotal() : BigDecimal.ZERO)
                            + "\n");
                }
            }
            UIHelper.showInfo("Export CSV", "Export terminé : " + out.getAbsolutePath());
        } catch (java.io.IOException ex) {
            UIHelper.showError("Export CSV", ex.getMessage());
        }
    }

    private static String adminSafeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 4 : Gestion des utilisateurs
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildUsersTab() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label title = UIHelper.sectionLabel("👥 Gestion des Utilisateurs");

        TableView<Utilisateur> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucun utilisateur."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Utilisateur, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getIdUtilisateur())));
        colId.setMaxWidth(55);

        TableColumn<Utilisateur, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Utilisateur, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Utilisateur, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRole() == Role.ADMIN ? "👑 Admin" : "👤 Client"));
        colRole.setMaxWidth(100);

        TableColumn<Utilisateur, String> colDate = new TableColumn<>("Inscription");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateInscription() != null
                        ? d.getValue().getDateInscription().toLocalDate().toString() : "—"));

        TableColumn<Utilisateur, Void> colAction = new TableColumn<>("Action");
        colAction.setMaxWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = UIHelper.dangerButton("Supprimer");
            {
                btn.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    if (UIHelper.showConfirm("Supprimer l'utilisateur",
                            "Supprimer le compte de \"" + u.getNom() + "\" ?\n" +
                            "Toutes ses réservations seront également supprimées.")) {
                        try {
                            MainApp.authService.deleteAccount(u.getIdUtilisateur());
                            Object act = getTableView().getProperties().get("refreshAction");
                            if (act instanceof Runnable run) run.run();
                            else loadUsersTable(getTableView());
                        } catch (Exception ex) {
                            UIHelper.showError("Erreur", ex.getMessage());
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Utilisateur u = getTableView().getItems().get(getIndex());
                setGraphic(u.getRole() == Role.CLIENT ? btn : new Label("—"));
            }
        });

        table.getColumns().add(colId);
        table.getColumns().add(colNom);
        table.getColumns().add(colEmail);
        table.getColumns().add(colRole);
        table.getColumns().add(colDate);
        table.getColumns().add(colAction);

        TextField searchField = UIHelper.styledField("Rechercher par nom ou email...");
        searchField.setPrefWidth(240);

        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("Tous", "Admin", "Client");
        roleFilter.setValue("Tous");
        roleFilter.setPrefWidth(120);

        Label userStats = new Label();
        userStats.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIHelper.TEXT2 + ";-fx-font-weight:bold;");

        Runnable applyUserFilters = () -> {
            try {
                String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
                String role = roleFilter.getValue();
                List<Utilisateur> filtered = MainApp.authService.findAllUtilisateurs().stream()
                        .filter(u -> q.isBlank()
                                || u.getNom().toLowerCase().contains(q)
                                || u.getEmail().toLowerCase().contains(q))
                        .filter(u -> "Tous".equals(role)
                                || ("Admin".equals(role)  && u.getRole() == Role.ADMIN)
                                || ("Client".equals(role) && u.getRole() == Role.CLIENT))
                        .collect(Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
                long admins  = filtered.stream().filter(u -> u.getRole() == Role.ADMIN).count();
                long clients = filtered.stream().filter(u -> u.getRole() == Role.CLIENT).count();
                userStats.setText("Total: " + filtered.size() + " | Admins: " + admins + " | Clients: " + clients);
            } catch (Exception ex) {
                UIHelper.showError("Erreur", ex.getMessage());
            }
        };

        table.getProperties().put("refreshAction", applyUserFilters);
        searchField.textProperty().addListener((obs, o, n) -> applyUserFilters.run());
        roleFilter.valueProperty().addListener((obs, o, n) -> applyUserFilters.run());
        applyUserFilters.run();

        HBox toolbar = new HBox(10, UIHelper.smallLabel("Rôle:"), roleFilter, searchField);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        Button refreshBtn = UIHelper.primaryButton("🔄 Actualiser");
        refreshBtn.setOnAction(e -> applyUserFilters.run());

        content.getChildren().addAll(title, userStats, table, toolbar, refreshBtn);
        return content;
    }

    private static ScrollPane buildSettingsTab(Utilisateur admin) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setMaxWidth(560);

        Label title = UIHelper.sectionLabel("⚙ Paramètres Administrateur");

        ComboBox<UIHelper.ThemePreset> themeBox =
                new ComboBox<>(FXCollections.observableArrayList(UIHelper.ThemePreset.values()));
        themeBox.setValue(UIHelper.getCurrentTheme());
        themeBox.setPrefWidth(340);

        CheckBox notifications = new CheckBox("Notifications système actives");
        notifications.setSelected(UIHelper.isNotificationsEnabled());
        CheckBox compactMode = new CheckBox("Mode compact des tableaux admin");
        compactMode.setSelected(UIHelper.isCompactMode());

        ComboBox<Integer> refreshBox = new ComboBox<>();
        refreshBox.getItems().addAll(15, 30, 60, 120);
        refreshBox.setValue(UIHelper.getAutoRefreshSeconds());
        refreshBox.setPrefWidth(120);

        Label prefError = UIHelper.errorLabel();
        Button savePrefsBtn = UIHelper.primaryButton("Appliquer les préférences");
        savePrefsBtn.setOnAction(e -> {
            prefError.setText("");
            try {
                UIHelper.applyTheme(themeBox.getValue());
                UIHelper.setNotificationsEnabled(notifications.isSelected());
                UIHelper.setCompactMode(compactMode.isSelected());
                UIHelper.setAutoRefreshSeconds(refreshBox.getValue());
                UIHelper.showInfo("Paramètres", "Préférences mises à jour.");
                MainApp.primaryStage.setScene(AdminView.createScene(admin));
            } catch (RuntimeException ex) {
                prefError.setText(ex.getMessage());
            }
        });

        HBox oldPassBox = UIHelper.passwordFieldWithToggle("Mot de passe actuel");
        HBox newPassBox = UIHelper.passwordFieldWithToggle("Nouveau mot de passe");
        HBox confirmPassBox = UIHelper.passwordFieldWithToggle("Confirmer le nouveau mot de passe");
        Label passError = UIHelper.errorLabel();

        Button changePassBtn = UIHelper.warningButton("Changer le mot de passe admin");
        changePassBtn.setOnAction(e -> {
            passError.setText("");
            try {
                Utilisateur current = MainApp.authService.getCurrentUser();
                if (current == null) {
                    throw new IllegalStateException("Session expirée. Veuillez vous reconnecter.");
                }
                String oldPass = UIHelper.getPasswordFromToggle(oldPassBox);
                String newPass = UIHelper.getPasswordFromToggle(newPassBox);
                String confirm = UIHelper.getPasswordFromToggle(confirmPassBox);

                if (!SecurityConfig.checkPassword(oldPass, current.getMotDePasseHash())) {
                    throw new IllegalArgumentException("Mot de passe actuel incorrect.");
                }
                if (!newPass.equals(confirm)) {
                    throw new IllegalArgumentException("La confirmation ne correspond pas au nouveau mot de passe.");
                }

                MainApp.authService.updateProfil(current.getNom(), current.getEmail(), newPass);
                UIHelper.showInfo("Sécurité", "Mot de passe administrateur modifié.");
                UIHelper.clearPasswordToggle(oldPassBox);
                UIHelper.clearPasswordToggle(newPassBox);
                UIHelper.clearPasswordToggle(confirmPassBox);
            } catch (RuntimeException ex) {
                passError.setText(ex.getMessage());
            }
        });

        content.getChildren().addAll(
                title,
                UIHelper.smallLabel("Thème du dashboard :"), themeBox,
                UIHelper.smallLabel("Auto-refresh par défaut (secondes) :"), refreshBox,
                notifications, compactMode, prefError, savePrefsBtn,
                new Separator(),
                UIHelper.smallLabel("Mot de passe actuel :"), oldPassBox,
                UIHelper.smallLabel("Nouveau mot de passe :"), newPassBox,
                UIHelper.smallLabel("Confirmation :"), confirmPassBox,
                passError, changePassBtn
        );

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        return sp;
    }

    private static void loadUsersTable(TableView<Utilisateur> table) {
        try {
            table.setItems(FXCollections.observableArrayList(
                    MainApp.authService.findAllUtilisateurs()));
        } catch (Exception e) {
            UIHelper.showError("Erreur", e.getMessage());
        }
    }
}
