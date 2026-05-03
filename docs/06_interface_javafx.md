# Interface JavaFX — Documentation du code

## Les fichiers concernés

```
ui/LoginView.java
ui/ClientView.java
ui/AdminView.java
ui/UIHelper.java
MainApp.java
Launcher.java
```

---

## 1. Comment l'application démarre

### `Launcher.java`

```java
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);  // délègue simplement à MainApp
    }
}
```

Pourquoi ce fichier existe : depuis Java 9, si la classe principale hérite de `Application` (JavaFX), `java -jar` refuse de la lancer. `Launcher` est une classe normale (sans JavaFX) qui contourne ce problème.

### `MainApp.java`

```java
public class MainApp extends Application {

    public static Stage primaryStage;       // la fenêtre principale
    public static IAuthService authService;
    public static ITerrainService terrainService;
    public static IReservationService reservationService;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        // Création des services (une seule fois pour toute l'application)
        authService        = new AuthServiceImpl();
        terrainService     = new TerrainServiceImpl();
        reservationService = new ReservationServiceImpl(authService);

        // Affichage du premier écran : la page de connexion
        primaryStage.setScene(LoginView.createScene());
        primaryStage.setTitle("SportsPro");
        primaryStage.show();
    }
}
```

Les services sont `public static` pour que toutes les vues puissent y accéder via `MainApp.authService`, `MainApp.terrainService`, etc.

---

## 2. Navigation entre les écrans

L'application utilise un seul `Stage` (une seule fenêtre). Changer d'écran = changer la `Scene`.

```
LoginView.createScene()
    │
    ├── Connexion admin  →  AdminView.createScene(user)
    └── Connexion client →  ClientView.createScene(user)
                                    │
                                    └── Déconnexion  →  LoginView.createScene()
```

**Exemple dans `LoginView.java` :**
```java
Utilisateur user = MainApp.authService.login(email, motDePasse);
if (user.isAdmin()) {
    MainApp.primaryStage.setScene(AdminView.createScene(user));
} else {
    MainApp.primaryStage.setScene(ClientView.createScene(user));
}
```

---

## 3. `UIHelper.java` — Les composants réutilisables

`UIHelper` fournit des méthodes statiques pour créer des composants JavaFX avec le bon style. Cela évite de répéter le même code CSS dans toutes les vues.

### Créer un bouton

```java
UIHelper.primaryButton("Confirmer")    // bouton bleu principal
UIHelper.dangerButton("Supprimer")     // bouton rouge
UIHelper.ghostButton("Réinitialiser")  // bouton transparent
UIHelper.smallLabel("Statut:")         // texte gris petit
```

### Les thèmes

3 thèmes sont disponibles, stockés dans un enum :

```java
public enum Theme {
    OCEAN("Ocean Pro"),
    EMERALD("Emerald Field"),
    SUNSET("Sunset Arena");
}
```

Le thème en cours est stocké dans `UIHelper.Settings` (classe interne statique) et appliqué à la scène sous forme de CSS inline.

### Les paramètres utilisateur

```java
// Tous dans UIHelper.Settings :
private static Theme currentTheme = Theme.OCEAN;
private static boolean notificationsEnabled = true;
private static boolean compactMode = false;
private static int autoRefreshSeconds = 30;  // valeurs : 15, 30, 60, 120
```

---

## 4. `LoginView.java` — Écran de connexion

### Structure de l'écran

```
HBox (split-panel)
├── VBox gauche (branding)  — logo, titre, liste des fonctionnalités
└── VBox droite (formulaire)
    ├── TabPane
    │   ├── Tab "Connexion"
    │   │   ├── TextField email
    │   │   ├── PasswordField motDePasse
    │   │   └── Button "Se connecter"
    │   └── Tab "Inscription"
    │       ├── TextField nom
    │       ├── TextField email
    │       ├── PasswordField motDePasse
    │       └── Button "S'inscrire"
    └── Label erreur (rouge, caché par défaut)
```

### La validation en temps réel

```java
emailField.textProperty().addListener((obs, oldVal, newVal) -> {
    // Appelé à chaque frappe dans le champ
    boolean valide = EmailValidator.isValid(EmailValidator.normalize(newVal));
    emailField.setStyle(valide ? "" : "-fx-border-color: red;");
});
```

### La connexion

```java
connectBtn.setOnAction(e -> {
    try {
        Utilisateur user = MainApp.authService.login(email, mdp);
        if (user.isAdmin()) {
            MainApp.primaryStage.setScene(AdminView.createScene(user));
        } else {
            MainApp.primaryStage.setScene(ClientView.createScene(user));
        }
    } catch (AuthenticationException ex) {
        erreurLabel.setText(ex.getMessage());  // affiche le message d'erreur
        erreurLabel.setVisible(true);
    }
});
```

---

## 5. `AdminView.java` — Interface administrateur

5 onglets dans un `TabPane` :

| Onglet | Contenu principal |
|---|---|
| Statistiques | 4 cartes KPI + BarChart + PieChart + tableau des réservations actives |
| Réservations | TableView de toutes les réservations + filtres + annulation + export CSV |
| Terrains | TableView des terrains + toggle disponibilité + suppression + ajout |
| Utilisateurs | TableView des utilisateurs + suppression des clients |
| Paramètres | Thème + auto-refresh + notifications + compact + changement MDP |

### Comment les données s'affichent (TableView)

```java
TableView<Reservation> table = new TableView<>();

TableColumn<Reservation, String> colClient = new TableColumn<>("Client");
colClient.setCellValueFactory(data ->
    new SimpleStringProperty(data.getValue().getNomClient())
);

table.getColumns().addAll(colClient, ...);
table.getItems().setAll(MainApp.reservationService.getAllReservations());
```

- `setCellValueFactory` dit à la colonne quel champ de l'objet afficher
- `getItems().setAll(liste)` remplace les données du tableau

### Les filtres

```java
void applyFilters() {
    List<Reservation> toutes = MainApp.reservationService.getAllReservations();
    List<Reservation> filtrees = toutes.stream()
        .filter(r -> statut.equals("Toutes") || r.getStatut().name().equals(statut))
        .filter(r -> recherche.isEmpty() || r.getNomClient().contains(recherche))
        .collect(Collectors.toList());
    table.getItems().setAll(filtrees);
}
```

Le filtrage est fait **côté Java** (en mémoire), pas en SQL.

### Export CSV

```java
exportBtn.setOnAction(e -> {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    File fichier = chooser.showSaveDialog(MainApp.primaryStage);
    if (fichier != null) {
        // Écrit les données dans le fichier CSV
        try (PrintWriter pw = new PrintWriter(fichier)) {
            pw.println("ID;Client;Terrain;Date;Début;Durée;Montant;Statut");
            for (Reservation r : table.getItems()) {
                pw.println(r.getIdReservation() + ";" + r.getNomClient() + ...);
            }
        }
    }
});
```

---

## 6. `ClientView.java` — Interface client

5 onglets :

| Onglet | Contenu principal |
|---|---|
| Mes Réservations | Stats personnelles + tableau des réservations + annulation + export CSV |
| Nouvelle Réservation | Formulaire de réservation avec calcul du montant en temps réel |
| Terrains | Tableau consultatif de tous les terrains |
| Mon Profil | Formulaire de modification du profil |
| Paramètres | Mêmes options que l'admin |

### Calcul du montant en temps réel

```java
// Quand le client change le terrain ou la durée, le montant se recalcule
terrainCombo.valueProperty().addListener((obs, old, terrain) -> updateMontant());
dureeSpinner.valueProperty().addListener((obs, old, duree) -> updateMontant());

void updateMontant() {
    Terrain t = terrainCombo.getValue();
    int duree = dureeSpinner.getValue();
    if (t != null && duree > 0) {
        BigDecimal montant = t.getPrixParHeure().multiply(BigDecimal.valueOf(duree));
        montantLabel.setText(montant + " DT");
    }
}
```

### Soumission du formulaire de réservation

```java
confirmerBtn.setOnAction(e -> {
    try {
        Terrain terrain = terrainCombo.getValue();
        LocalDate date  = datePicker.getValue();
        LocalTime heure = LocalTime.of(heureCombo.getValue(), 0);
        int duree       = dureeSpinner.getValue();

        MainApp.reservationService.creerReservation(
            terrain.getIdTerrain(), date, heure, duree
        );

        // Succès : message vert + retour à l'onglet "Mes Réservations"
        UIHelper.showSuccess("Réservation confirmée !");
        tabPane.getSelectionModel().select(0);

    } catch (ReservationConflictException ex) {
        UIHelper.showError("Créneau indisponible : " + ex.getMessage());
    } catch (Exception ex) {
        UIHelper.showError(ex.getMessage());
    }
});
```
