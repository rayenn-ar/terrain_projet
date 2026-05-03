package com.clubsportif.ui;

import com.clubsportif.MainApp;
import com.clubsportif.config.SecurityConfig;
import com.clubsportif.model.Reservation;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.StatutReservation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tableau de bord client JavaFX.
 */
public final class ClientView {

    private ClientView() {}

    public static Scene createScene(Utilisateur user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIHelper.BG + ";");

        // NavBar
        root.setTop(UIHelper.navBar(user.getNom(), false, () -> {
            MainApp.authService.logout();
            MainApp.primaryStage.setScene(LoginView.createScene());
        }));

        // Table partagée pour garantir la cohérence entre onglets.
        TableView<Reservation> mesReservationsTable = buildReservationTable(false);

        // Onglets principaux
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-tab-min-width:148;-fx-tab-min-height:38;");
        Tab mesReservationsTab = new Tab("📅  Mes Réservations", buildMesReservationsTab(mesReservationsTable));
        Tab nouvelleReservationTab = new Tab("➕  Nouvelle Réservation", buildReservationFormTab(mesReservationsTable));
        Tab terrainsTab = new Tab("🏟  Terrains", buildTerrainsTab());
        Tab profilTab = new Tab("👤  Mon Profil", buildProfilTab(user));
        Tab settingsTab = new Tab("⚙  Paramètres", buildSettingsTab(user));

        // Rechargement automatique à l'ouverture de l'onglet "Mes Réservations".
        mesReservationsTab.setOnSelectionChanged(e -> {
            if (mesReservationsTab.isSelected()) {
                refreshMesReservationsTable(mesReservationsTable);
            }
        });

        tabs.getTabs().addAll(mesReservationsTab, nouvelleReservationTab, terrainsTab, profilTab, settingsTab);

        root.setCenter(tabs);
        return new Scene(root, 1050, 720);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 1 : Mes Réservations
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildMesReservationsTab(TableView<Reservation> table) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

    Label title = UIHelper.sectionLabel("📅 Mes Réservations");
    title.setStyle("-fx-font-size:19px;-fx-font-weight:bold;-fx-text-fill:" + UIHelper.TEXT1 + ";" +
        "-fx-padding:0 0 4 0;");

        VBox.setVgrow(table, Priority.ALWAYS);

        TextField searchField = UIHelper.styledField("Rechercher: terrain, date, statut");
        searchField.setPrefWidth(320);

        ComboBox<String> statutFilter = new ComboBox<>();
        statutFilter.getItems().addAll("Tous", "Confirmée", "Annulée", "Terminée");
        statutFilter.setValue("Tous");
        statutFilter.setPrefWidth(140);

        ComboBox<String> periodeFilter = new ComboBox<>();
        periodeFilter.getItems().addAll("Toutes", "Aujourd'hui", "7 prochains jours", "30 prochains jours", "Passées");
        periodeFilter.setValue("Toutes");
        periodeFilter.setPrefWidth(180);

        Label stats = new Label();
        stats.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIHelper.TEXT2 + ";-fx-font-weight:bold;");

        Runnable applyFilters = () -> {
            try {
                String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
                String statut = statutFilter.getValue();
                String periode = periodeFilter.getValue();
                LocalDate today = LocalDate.now();

                List<Reservation> filtered = MainApp.reservationService.getHistoriqueUtilisateur().stream()
                        .filter(r -> {
                            if (query.isBlank()) return true;
                            String terrain = r.getNomTerrain() == null ? "" : r.getNomTerrain().toLowerCase();
                            String date = r.getDateReservation() == null ? "" : r.getDateReservation().toString();
                            String st = r.getStatut() == null ? "" : r.getStatut().getLibelle().toLowerCase();
                            return terrain.contains(query) || date.contains(query) || st.contains(query);
                        })
                        .filter(r -> "Tous".equals(statut) || (r.getStatut() != null && r.getStatut().getLibelle().equalsIgnoreCase(statut)))
                        .filter(r -> {
                            if (r.getDateReservation() == null || "Toutes".equals(periode)) return true;
                            LocalDate d = r.getDateReservation();
                            return switch (periode) {
                                case "Aujourd'hui" -> d.isEqual(today);
                                case "7 prochains jours" -> !d.isBefore(today) && !d.isAfter(today.plusDays(7));
                                case "30 prochains jours" -> !d.isBefore(today) && !d.isAfter(today.plusDays(30));
                                case "Passées" -> d.isBefore(today);
                                default -> true;
                            };
                        })
                        .sorted(Comparator
                                .comparing(Reservation::getDateReservation, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(Reservation::getHeureDebut, Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.toList());

                table.setItems(FXCollections.observableArrayList(filtered));
                table.getProperties().put("currentFiltered", filtered);

                long aVenir = filtered.stream()
                        .filter(r -> r.getDateReservation() != null && !r.getDateReservation().isBefore(today))
                        .count();
                long confirmees = filtered.stream()
                        .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                        .count();
                BigDecimal totalDepense = filtered.stream()
                        .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                        .map(Reservation::getMontantTotal)
                        .filter(m -> m != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                stats.setText("Total: " + filtered.size() + " | A venir: " + aVenir
                        + " | Confirmées: " + confirmees + " | Dépense: " + totalDepense + " " + UIHelper.CURRENCY_UNIT);
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
        exportBtn.setOnAction(e -> exportReservationsCsv(table));

        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters.run());
        statutFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters.run());
        periodeFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters.run());

        applyFilters.run();
        HBox toolbar = new HBox(10, UIHelper.smallLabel("Statut:"), statutFilter,
                UIHelper.smallLabel("Période:"), periodeFilter, searchField, resetBtn, exportBtn, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().addAll(title, stats, table, toolbar);
        return content;
    }

    @SuppressWarnings("unchecked")
    private static void exportReservationsCsv(TableView<Reservation> table) {
        try {
            List<Reservation> data = (List<Reservation>) table.getProperties().get("currentFiltered");
            if (data == null || data.isEmpty()) {
                UIHelper.showInfo("Export CSV", "Aucune réservation à exporter.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter mes réservations");
            chooser.setInitialFileName("mes-reservations.csv");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File out = chooser.showSaveDialog(MainApp.primaryStage);
            if (out == null) return;

            try (FileWriter fw = new FileWriter(out, StandardCharsets.UTF_8)) {
                fw.write("id,date,heure_debut,heure_fin,terrain,duree,statut,montant\n");
                for (Reservation r : data) {
                    fw.write(r.getIdReservation() + ","
                            + safeCsv(r.getDateReservation() != null ? r.getDateReservation().toString() : "") + ","
                            + safeCsv(r.getHeureDebut() != null ? r.getHeureDebut().toString() : "") + ","
                            + safeCsv(r.getHeureFin() != null ? r.getHeureFin().toString() : "") + ","
                            + safeCsv(r.getNomTerrain()) + ","
                            + r.getDureeHeures() + ","
                            + safeCsv(r.getStatut() != null ? r.getStatut().getLibelle() : "") + ","
                            + (r.getMontantTotal() != null ? r.getMontantTotal() : BigDecimal.ZERO)
                            + "\n");
                }
            }
            UIHelper.showInfo("Export CSV", "Export terminé: " + out.getAbsolutePath());
        } catch (IOException ex) {
            UIHelper.showError("Export CSV", ex.getMessage());
        }
    }

    private static String safeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static void refreshMesReservationsTable(TableView<Reservation> table) {
        Object action = table.getProperties().get("refreshAction");
        if (action instanceof Runnable run) {
            run.run();
        } else {
            loadMesReservations(table);
        }
    }

    private static TableView<Reservation> buildReservationTable(boolean isAdmin) {
        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucune réservation."));

        if (isAdmin) {
            TableColumn<Reservation, String> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(d -> new SimpleStringProperty(
                    String.valueOf(d.getValue().getIdReservation())));
            colId.setMaxWidth(55);
            table.getColumns().add(colId);

            TableColumn<Reservation, String> colClient = new TableColumn<>("Client");
            colClient.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getNomClient() != null ? d.getValue().getNomClient() : "—"));
            table.getColumns().add(colClient);
        }

        TableColumn<Reservation, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateReservation() != null
                        ? d.getValue().getDateReservation().toString() : "—"));

        TableColumn<Reservation, String> colHoraire = new TableColumn<>("Horaire");
        colHoraire.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHeureDebut() + "  →  " + d.getValue().getHeureFin()));

        TableColumn<Reservation, String> colTerrain = new TableColumn<>("Terrain");
        colTerrain.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomTerrain() != null ? d.getValue().getNomTerrain() : "—"));

        TableColumn<Reservation, String> colDuree = new TableColumn<>("Durée");
        colDuree.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDureeHeures() + " h"));
        colDuree.setMaxWidth(70);

        TableColumn<Reservation, String> colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMontantTotal() != null
                ? d.getValue().getMontantTotal() + " " + UIHelper.CURRENCY_UNIT : "—"));

        TableColumn<Reservation, String> colStatut = new TableColumn<>("Statut");
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

        TableColumn<Reservation, Void> colAction = new TableColumn<>("Action");
        colAction.setMaxWidth(100);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = UIHelper.dangerButton("Annuler");
            {
                btn.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    if (UIHelper.showConfirm("Annuler la réservation",
                            "Confirmer l'annulation du créneau " +
                            r.getHeureDebut() + " → " + r.getHeureFin() + " ?")) {
                        try {
                            if (isAdmin) {
                                MainApp.reservationService.annulerReservationAdmin(r.getIdReservation());
                                loadAllReservations(getTableView());
                            } else {
                                MainApp.reservationService.annulerReservation(r.getIdReservation());
                                loadMesReservations(getTableView());
                            }
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
                Reservation r = getTableView().getItems().get(getIndex());
                setGraphic(r.getStatut() == StatutReservation.CONFIRMEE ? btn : null);
            }
        });

        table.getColumns().add(colDate);
        table.getColumns().add(colHoraire);
        table.getColumns().add(colTerrain);
        table.getColumns().add(colDuree);
        table.getColumns().add(colMontant);
        table.getColumns().add(colStatut);
        table.getColumns().add(colAction);
        return table;
    }

    private static void loadMesReservations(TableView<Reservation> table) {
        try {
            table.setItems(FXCollections.observableArrayList(
                    MainApp.reservationService.getHistoriqueUtilisateur()));
        } catch (Exception e) {
            UIHelper.showError("Erreur", e.getMessage());
        }
    }

    static void loadAllReservations(TableView<Reservation> table) {
        try {
            table.setItems(FXCollections.observableArrayList(
                    MainApp.reservationService.getAllReservations()));
        } catch (Exception e) {
            UIHelper.showError("Erreur", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 2 : Nouvelle Réservation
    // ═══════════════════════════════════════════════════════════════════════
    private static ScrollPane buildReservationFormTab(TableView<Reservation> mesReservationsTable) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(25));
        form.setMaxWidth(540);
        form.setStyle("-fx-background-color:transparent;");

        Label title = UIHelper.sectionLabel("➕ Nouvelle Réservation");

        // Terrain
        ComboBox<Terrain> combTerrain = new ComboBox<>();
        combTerrain.setConverter(new StringConverter<>() {
            @Override public String toString(Terrain t) {
                return t == null ? "" : t.getNom() + " - " +
                    t.getType().getLibelle() + "  (" + t.getPrixParHeure() + " " + UIHelper.CURRENCY_UNIT + "/h)";
            }
            @Override public Terrain fromString(String s) { return null; }
        });
        combTerrain.setPrefWidth(420);
        loadTerrains(combTerrain);

        // Date
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(420);

        // Heure
        ComboBox<LocalTime> combHeure = new ComboBox<>();
        for (int h = 8; h <= 22; h++) combHeure.getItems().add(LocalTime.of(h, 0));
        combHeure.setValue(LocalTime.of(10, 0));
        combHeure.setConverter(new StringConverter<>() {
            @Override public String toString(LocalTime t) { return t == null ? "" : t.toString(); }
            @Override public LocalTime fromString(String s) { return LocalTime.parse(s); }
        });
        combHeure.setPrefWidth(420);

        // Durée
        Spinner<Integer> spinDuree = new Spinner<>(1, 4, 1);
        spinDuree.setPrefWidth(420);

        // Résumé prix
        Label prixInfo = new Label();
        prixInfo.setStyle("-fx-background-color:" + UIHelper.PRIMARY_LIGHT + ";-fx-padding:13 16;" +
            "-fx-background-radius:10;-fx-text-fill:" + UIHelper.PRIMARY + ";-fx-font-size:14px;" +
            "-fx-font-weight:bold;-fx-border-color:" + UIHelper.PRIMARY + ";" +
            "-fx-border-radius:10;-fx-border-width:1.5;" +
            "-fx-effect:" + UIHelper.SHADOW_SM + ";");

        Label disponibiliteInfo = new Label("Choisissez un terrain et une date pour voir les créneaux déjà pris.");
        disponibiliteInfo.setWrapText(true);
        disponibiliteInfo.setStyle("-fx-background-color:#F8FAFC;-fx-padding:10 12;" +
            "-fx-background-radius:8;-fx-border-color:" + UIHelper.BORDER + ";" +
            "-fx-border-radius:8;-fx-text-fill:" + UIHelper.TEXT2 + ";-fx-font-size:12px;");

        // Mise à jour du résumé prix en temps réel
        Runnable updatePrix = () -> {
            Terrain t = combTerrain.getValue();
            if (t != null && spinDuree.getValue() != null) {
                BigDecimal total = t.getPrixParHeure()
                        .multiply(BigDecimal.valueOf(spinDuree.getValue()));
                prixInfo.setText("💰 Montant estimé : " + total + " " + UIHelper.CURRENCY_UNIT + "  ("
                    + spinDuree.getValue() + "h × " + t.getPrixParHeure() + " " + UIHelper.CURRENCY_UNIT + "/h)");
            }
        };

        Runnable updateDisponibilite = () -> {
            Terrain t = combTerrain.getValue();
            LocalDate d = datePicker.getValue();
            LocalTime h = combHeure.getValue();
            Integer duree = spinDuree.getValue();

            if (t == null || d == null) {
                disponibiliteInfo.setText("Choisissez un terrain et une date pour voir les créneaux déjà pris.");
                return;
            }

            try {
                List<Reservation> occupees = MainApp.reservationService.getReservationsParTerrain(t.getIdTerrain()).stream()
                        .filter(r -> d.equals(r.getDateReservation()))
                        .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                        .sorted(Comparator.comparing(Reservation::getHeureDebut))
                        .collect(Collectors.toList());

                String slots = occupees.isEmpty()
                        ? "Aucun créneau réservé ce jour."
                        : occupees.stream()
                            .map(r -> r.getHeureDebut() + "-" + r.getHeureFin())
                            .collect(Collectors.joining(" | "));

                boolean conflit = false;
                if (h != null && duree != null) {
                    LocalTime fin = h.plusHours(duree);
                    conflit = occupees.stream().anyMatch(r -> h.isBefore(r.getHeureFin()) && r.getHeureDebut().isBefore(fin));
                }

                disponibiliteInfo.setText(
                        "Créneaux déjà pris: " + slots +
                        (h != null && duree != null
                                ? (conflit
                                    ? "\n⚠️ Votre sélection chevauche un créneau existant."
                                    : "\n✅ Votre sélection semble disponible.")
                                : ""));
            } catch (Exception ex) {
                disponibiliteInfo.setText("Impossible de vérifier la disponibilité: " + ex.getMessage());
            }
        };

        combTerrain.setOnAction(e -> {
            updatePrix.run();
            updateDisponibilite.run();
        });
        datePicker.valueProperty().addListener((obs, o, n) -> updateDisponibilite.run());
        combHeure.valueProperty().addListener((obs, o, n) -> updateDisponibilite.run());
        spinDuree.valueProperty().addListener((obs, o, n) -> updatePrix.run());
        spinDuree.valueProperty().addListener((obs, o, n) -> updateDisponibilite.run());

        Label errorLbl = UIHelper.errorLabel();

        Button bookBtn = UIHelper.primaryButton("✅  Confirmer la réservation");
        bookBtn.setPrefWidth(260);
        bookBtn.setOnAction(e -> {
            errorLbl.setText("");
            bookBtn.setDisable(true);
            Terrain terrain = combTerrain.getValue();
            if (terrain == null)                          { errorLbl.setText("Sélectionnez un terrain."); bookBtn.setDisable(false); return; }
            if (datePicker.getValue() == null)            { errorLbl.setText("Sélectionnez une date."); bookBtn.setDisable(false); return; }
            if (combHeure.getValue() == null)             { errorLbl.setText("Sélectionnez une heure."); bookBtn.setDisable(false); return; }
            try {
                MainApp.reservationService.creerReservation(
                        terrain.getIdTerrain(),
                        datePicker.getValue(),
                        combHeure.getValue(),
                        spinDuree.getValue());
                BigDecimal total = terrain.getPrixParHeure()
                        .multiply(BigDecimal.valueOf(spinDuree.getValue()));
                UIHelper.showInfo("✅ Réservation confirmée",
                        "Terrain : " + terrain.getNom() + "\n" +
                        "Date    : " + datePicker.getValue() + "\n" +
                        "Horaire : " + combHeure.getValue() +
                        "  →  " + combHeure.getValue().plusHours(spinDuree.getValue()) + "\n" +
                        "Montant : " + total + " " + UIHelper.CURRENCY_UNIT);

                    // Rafraîchit immédiatement l'onglet "Mes Réservations".
                    refreshMesReservationsTable(mesReservationsTable);
                    updateDisponibilite.run();

                // Reset
                datePicker.setValue(LocalDate.now().plusDays(1));
                combHeure.setValue(LocalTime.of(10, 0));
                spinDuree.getValueFactory().setValue(1);
            } catch (Exception ex) {
                errorLbl.setText(ex.getMessage());
            } finally {
                bookBtn.setDisable(false);
            }
        });

        // Labels de champ (supprime le bloc redondant)
        // On recrée le layout proprement
        form.getChildren().addAll(
                title,
                UIHelper.smallLabel("Terrain disponible :"),  combTerrain,
                UIHelper.smallLabel("Date :"),                 datePicker,
                UIHelper.smallLabel("Heure de début :"),       combHeure,
                UIHelper.smallLabel("Durée (1 à 4 heures) :"), spinDuree,
                prixInfo, disponibiliteInfo, errorLbl, bookBtn
        );

            updatePrix.run();
            updateDisponibilite.run();

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        return sp;
    }

    private static void loadTerrains(ComboBox<Terrain> combo) {
        try {
            List<Terrain> list = MainApp.terrainService.findDisponibles();
            combo.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) combo.setValue(list.get(0));
        } catch (Exception e) {
            UIHelper.showError("Erreur", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 3 : Terrains disponibles
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildTerrainsTab() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label title = UIHelper.sectionLabel("🏟 Terrains Disponibles");

        TableView<Terrain> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Aucun terrain disponible."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Terrain, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Terrain, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().getLibelle()));

        TableColumn<Terrain, String> colPrix = new TableColumn<>("Prix / heure");
        colPrix.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPrixParHeure() + " " + UIHelper.CURRENCY_UNIT));

        TableColumn<Terrain, String> colDispo = new TableColumn<>("Disponibilité");
        colDispo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isDisponibilite() ? "✅ Disponible" : "❌ Indisponible"));

        TableColumn<Terrain, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().add(colNom);
        table.getColumns().add(colType);
        table.getColumns().add(colPrix);
        table.getColumns().add(colDispo);
        table.getColumns().add(colDesc);

        try {
            table.setItems(FXCollections.observableArrayList(MainApp.terrainService.findAll()));
        } catch (Exception e) {
            UIHelper.showError("Erreur", e.getMessage());
        }

        content.getChildren().addAll(title, table);
        return content;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 4 : Mon Profil
    // ═══════════════════════════════════════════════════════════════════════
    private static ScrollPane buildProfilTab(Utilisateur user) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(25));
        content.setMaxWidth(420);

        Label title = UIHelper.sectionLabel("👤 Mon Profil");

        // Informations actuelles
        String dateInscr = user.getDateInscription() != null
                ? user.getDateInscription().toLocalDate().toString() : "—";
        Label infoBanner = UIHelper.infoBanner(
            "📧  " + user.getEmail() + "     🗓  Inscrit le " + dateInscr,
            UIHelper.PRIMARY_LIGHT, UIHelper.PRIMARY);

        TextField nomField  = UIHelper.styledField("Nom complet");
        nomField.setText(user.getNom());
        TextField mailField = UIHelper.styledField("Email");
        mailField.setText(user.getEmail());
        HBox passBox = UIHelper.passwordFieldWithToggle("Nouveau mot de passe (vide = inchangé)");

        Label errorLbl = UIHelper.errorLabel();

        Button saveBtn = UIHelper.primaryButton("💾  Enregistrer les modifications");
        saveBtn.setOnAction(e -> {
            errorLbl.setText("");
            try {
                String pwd = UIHelper.getPasswordFromToggle(passBox);
                String newPass = pwd.isBlank() ? null : pwd;
                MainApp.authService.updateProfil(
                        nomField.getText(), mailField.getText(), newPass);
                UIHelper.showInfo("Profil mis à jour",
                        "Vos informations ont été enregistrées avec succès.");
                UIHelper.clearPasswordToggle(passBox);
            } catch (Exception ex) {
                errorLbl.setText(ex.getMessage());
            }
        });

        content.getChildren().addAll(
                title, infoBanner,
                UIHelper.smallLabel("Nom :"),           nomField,
                UIHelper.smallLabel("Email :"),          mailField,
                UIHelper.smallLabel("Mot de passe :"),   passBox,
                errorLbl, saveBtn
        );

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        return sp;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Onglet 5 : Paramètres
    // ═══════════════════════════════════════════════════════════════════════
    private static ScrollPane buildSettingsTab(Utilisateur user) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setMaxWidth(520);

        Label title = UIHelper.sectionLabel("⚙ Paramètres");

        ComboBox<UIHelper.ThemePreset> themeBox =
                new ComboBox<>(FXCollections.observableArrayList(UIHelper.ThemePreset.values()));
        themeBox.setValue(UIHelper.getCurrentTheme());
        themeBox.setPrefWidth(320);

        CheckBox notifications = new CheckBox("Activer les notifications");
        notifications.setSelected(UIHelper.isNotificationsEnabled());
        CheckBox compactMode = new CheckBox("Mode compact des tableaux");
        compactMode.setSelected(UIHelper.isCompactMode());

        ComboBox<Integer> refreshBox = new ComboBox<>();
        refreshBox.getItems().addAll(15, 30, 60, 120);
        refreshBox.setValue(UIHelper.getAutoRefreshSeconds());
        refreshBox.setPrefWidth(120);

        Label prefError = UIHelper.errorLabel();
        Button savePrefsBtn = UIHelper.primaryButton("Appliquer le thème et préférences");
        savePrefsBtn.setOnAction(e -> {
            prefError.setText("");
            try {
                UIHelper.applyTheme(themeBox.getValue());
                UIHelper.setNotificationsEnabled(notifications.isSelected());
                UIHelper.setCompactMode(compactMode.isSelected());
                UIHelper.setAutoRefreshSeconds(refreshBox.getValue());
                UIHelper.showInfo("Paramètres", "Thème et préférences appliqués.");
                MainApp.primaryStage.setScene(ClientView.createScene(user));
            } catch (RuntimeException ex) {
                prefError.setText(ex.getMessage());
            }
        });

        HBox oldPassBox = UIHelper.passwordFieldWithToggle("Mot de passe actuel");
        HBox newPassBox = UIHelper.passwordFieldWithToggle("Nouveau mot de passe");
        HBox confirmPassBox = UIHelper.passwordFieldWithToggle("Confirmer le nouveau mot de passe");
        Label passError = UIHelper.errorLabel();

        Button changePassBtn = UIHelper.warningButton("Changer le mot de passe");
        changePassBtn.setOnAction(e -> {
            passError.setText("");
            try {
                String oldPass = UIHelper.getPasswordFromToggle(oldPassBox);
                String newPass = UIHelper.getPasswordFromToggle(newPassBox);
                String confirm = UIHelper.getPasswordFromToggle(confirmPassBox);

                Utilisateur current = MainApp.authService.getCurrentUser();
                if (current == null) {
                    throw new IllegalStateException("Session expirée. Veuillez vous reconnecter.");
                }
                if (!SecurityConfig.checkPassword(oldPass, current.getMotDePasseHash())) {
                    throw new IllegalArgumentException("Mot de passe actuel incorrect.");
                }
                if (!newPass.equals(confirm)) {
                    throw new IllegalArgumentException("La confirmation ne correspond pas au nouveau mot de passe.");
                }

                MainApp.authService.updateProfil(current.getNom(), current.getEmail(), newPass);
                UIHelper.showInfo("Sécurité", "Mot de passe modifié avec succès.");
                UIHelper.clearPasswordToggle(oldPassBox);
                UIHelper.clearPasswordToggle(newPassBox);
                UIHelper.clearPasswordToggle(confirmPassBox);
            } catch (RuntimeException ex) {
                passError.setText(ex.getMessage());
            }
        });

        content.getChildren().addAll(
                title,
                UIHelper.smallLabel("Thème de l'application :"), themeBox,
                UIHelper.smallLabel("Fréquence auto-refresh (secondes) :"), refreshBox,
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
}
