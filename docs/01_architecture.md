# Architecture du projet SportsPro

## Comment le projet est organisé

Le projet est divisé en **5 couches**. Chaque couche a un rôle précis et ne parle qu'avec la couche juste en dessous.

```
LoginView / ClientView / AdminView   ← l'utilisateur clique ici
            ↓
    AuthController / TerrainController / ReservationController
            ↓
    AuthServiceImpl / TerrainServiceImpl / ReservationServiceImpl
            ↓
    UtilisateurDAOImpl / TerrainDAOImpl / ReservationDAOImpl
            ↓
         Base de données MySQL (club_sportif)
```

---

## Rôle de chaque couche

### 1. Vue (`ui/`)
Les classes JavaFX que l'utilisateur voit.

| Fichier | Ce qu'il fait |
|---|---|
| `LoginView.java` | Écran de connexion et d'inscription |
| `ClientView.java` | Interface complète du client (5 onglets) |
| `AdminView.java` | Interface complète de l'admin (5 onglets) |
| `UIHelper.java` | Composants réutilisables (boutons, tables, thèmes) |

### 2. Contrôleur (`controller/`)
Reçoit les actions de l'utilisateur et appelle le bon service.

| Fichier | Ce qu'il fait |
|---|---|
| `AuthController.java` | Gère connexion, inscription, profil, suppression compte |
| `TerrainController.java` | Gère les opérations CRUD sur les terrains |
| `ReservationController.java` | Gère la création, l'annulation et l'affichage des réservations |
| `ConsoleController.java` | Version console de l'application (non JavaFX) |

### 3. Service (`service/impl/`)
Contient **toute la logique métier** : validations, règles, calculs.

| Fichier | Ce qu'il fait |
|---|---|
| `AuthServiceImpl.java` | Connexion, inscription, modification de profil |
| `TerrainServiceImpl.java` | Ajouter, modifier, supprimer un terrain |
| `ReservationServiceImpl.java` | Créer, annuler, lister des réservations + statistiques |

### 4. DAO (`dao/impl/`)
Fait les requêtes SQL. Chaque classe correspond à une table.

| Fichier | Table MySQL |
|---|---|
| `UtilisateurDAOImpl.java` | `utilisateur` |
| `TerrainDAOImpl.java` | `terrain` |
| `ReservationDAOImpl.java` | `reservation` |

### 5. Modèle (`model/`)
Les objets Java qui représentent les données de la base.

| Fichier | Correspond à |
|---|---|
| `Utilisateur.java` | Un utilisateur (admin ou client) |
| `Terrain.java` | Un terrain sportif |
| `Reservation.java` | Une réservation |
| `enums/Role.java` | ADMIN ou CLIENT |
| `enums/TypeTerrain.java` | FOOTBALL, TENNIS, BASKETBALL, VOLLEYBALL |
| `enums/StatutReservation.java` | CONFIRMEE, ANNULEE, TERMINEE |

---

## Les interfaces

Chaque couche Service et DAO a une interface Java (dans `interfaces/`).

**Pourquoi ?** Pour pouvoir tester le code sans base de données réelle (on peut remplacer le DAO réel par un faux DAO dans les tests).

```
IAuthService  ←  AuthServiceImpl
ITerrainService  ←  TerrainServiceImpl
IReservationService  ←  ReservationServiceImpl

IUtilisateurDAO  ←  UtilisateurDAOImpl
ITerrainDAO  ←  TerrainDAOImpl
IReservationDAO  ←  ReservationDAOImpl
```

---

## Les classes utilitaires (`config/` et `util/`)

| Fichier | Rôle |
|---|---|
| `AppConfig.java` | Constantes : URL MySQL, mot de passe, règles métier |
| `DatabaseConfig.java` | Gère la connexion MySQL (Singleton : une seule connexion) |
| `SecurityConfig.java` | Hash et vérification des mots de passe (BCrypt) |
| `OverlapUtils.java` | Algorithme de détection de chevauchement de créneaux |
| `EmailValidator.java` | Vérifie le format d'une adresse e-mail |
| `PasswordValidator.java` | Vérifie la complexité d'un mot de passe |
| `ReservationValidator.java` | Vérifie les données d'une réservation |
| `Logger.java` | Affiche des messages colorés dans la console |
| `DateTimeUtil.java` | Formate les dates et heures |

---

## Le point d'entrée (comment l'application démarre)

```
java -jar app.jar
      ↓
  Launcher.java   (classe simple, pas JavaFX)
      ↓
  MainApp.java    (étend Application JavaFX)
      ↓
  LoginView.java  (premier écran affiché)
```

`Launcher.java` existe parce que depuis Java 9, `java -jar` ne fonctionne pas directement avec une classe JavaFX. C'est un contournement connu.
