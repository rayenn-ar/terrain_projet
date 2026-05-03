# SportsPro — Documentation Complète
## Système de Gestion des Réservations de Terrains Sportifs

---

## Table des matières

1. [Vue d'ensemble](#1-vue-densemble)
2. [Prérequis](#2-prérequis)
3. [Installation et démarrage](#3-installation-et-démarrage)
4. [Architecture du projet](#4-architecture-du-projet)
5. [Base de données](#5-base-de-données)
6. [Comptes utilisateurs par défaut](#6-comptes-utilisateurs-par-défaut)
7. [Guide d'utilisation — Client](#7-guide-dutilisation--client)
8. [Guide d'utilisation — Administrateur](#8-guide-dutilisation--administrateur)
9. [Structure des packages Java](#9-structure-des-packages-java)
10. [Classes principales](#10-classes-principales)
11. [Sécurité](#11-sécurité)
12. [Packaging et déploiement JAR](#12-packaging-et-déploiement-jar)
13. [Tests](#13-tests)
14. [Configuration](#14-configuration)
15. [FAQ / Dépannage](#15-faq--dépannage)

---

## 1. Vue d'ensemble

**SportsPro** est une application de bureau Java/JavaFX pour la gestion des réservations de terrains sportifs dans un club. Elle permet à des clients de réserver des créneaux horaires sur des terrains (football, tennis, basketball, volleyball), et à des administrateurs de gérer l'ensemble du système.

| Caractéristique | Valeur |
|---|---|
| Nom application | SportsPro — Gestion des Terrains Sportifs |
| Langage | Java 25 |
| Interface graphique | JavaFX 21.0.5 |
| Base de données | MySQL 8.0 |
| Gestionnaire de build | Apache Maven 3.9+ |
| Architecture | MVC + DAO + Service (couches strictes) |
| Hachage MDP | BCrypt (jBCrypt 0.4, 10 rounds) |

---

## 2. Prérequis

Avant de lancer l'application, assurez-vous d'avoir installé :

| Logiciel | Version minimale | Vérification |
|---|---|---|
| Java JDK | 17+ (testé avec Java 25) | `java -version` |
| Apache Maven | 3.8+ | `mvn -version` |
| MySQL Server | 8.0+ | `mysql --version` |

> **Note :** JavaFX est inclus automatiquement dans le JAR (dépendances Maven).

---

## 3. Installation et démarrage

### 3.1 Cloner/obtenir le projet

Placer le dossier `terrain_projet` dans le répertoire souhaité.

### 3.2 Configurer la base de données

**Étape 1 — Créer la base et les tables :**

```bash
mysql -u root -p < database.sql
```

Cela crée la base `club_sportif` avec :
- 3 tables : `utilisateur`, `terrain`, `reservation`
- 2 vues : `vue_reservations_details`, `vue_stats_terrains`
- Index de performance
- Données de test (1 admin + 1 client + 3 terrains + 3 réservations)

**Étape 2 — Adapter `AppConfig.java` si nécessaire :**

Fichier : `src/main/java/com/clubsportif/config/AppConfig.java`

```java
public static final String DB_HOST     = "localhost";
public static final int    DB_PORT     = 3306;
public static final String DB_NAME     = "club_sportif";
public static final String DB_USER     = "root";
public static final String DB_PASSWORD = "votre_mot_de_passe_mysql";
```

### 3.3 Compiler et lancer avec Maven

```bash
cd terrain_projet
mvn clean javafx:run
```

### 3.4 Lancer via JAR exécutable

**Construire le JAR :**

```bash
mvn clean package -DskipTests
```

Le JAR se trouve dans `target/terrain-projet-1.0.0-jar-with-dependencies.jar`.

**Lancer le JAR :**

```bash
java -jar target/terrain-projet-1.0.0-jar-with-dependencies.jar
```

> Le point d'entrée est `Launcher.java` (contourne la restriction JavaFX sur `java -jar`).

---

## 4. Architecture du projet

### 4.1 Architecture en couches (MVC enrichi)

```
┌──────────────────────────────────────────┐
│  VUE (View) — JavaFX                     │
│  LoginView · ClientView · AdminView      │
│  UIHelper                                │
└──────────────────┬───────────────────────┘
                   │ événements UI
┌──────────────────▼───────────────────────┐
│  CONTRÔLEUR                              │
│  AuthController · TerrainController      │
│  ReservationController                   │
└──────────────────┬───────────────────────┘
                   │ appels service
┌──────────────────▼───────────────────────┐
│  SERVICE (Logique métier)                │
│  AuthServiceImpl · TerrainServiceImpl    │
│  ReservationServiceImpl                  │
│  Validators : Email · Password · Resa    │
└──────────────────┬───────────────────────┘
                   │ accès données
┌──────────────────▼───────────────────────┐
│  DAO (Accès aux données — JDBC)          │
│  UtilisateurDAOImpl · TerrainDAOImpl     │
│  ReservationDAOImpl                      │
└──────────────────┬───────────────────────┘
                   │ SQL
┌──────────────────▼───────────────────────┐
│  MODÈLE (POJO + Enums)                   │
│  Utilisateur · Terrain · Reservation     │
│  Role · TypeTerrain · StatutReservation  │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│  BASE DE DONNÉES                         │
│  MySQL 8.0 — club_sportif                │
└──────────────────────────────────────────┘
```

### 4.2 Règles de communication

- Chaque couche ne dépend **que de la couche directement inférieure**
- Les couches DAO et Service exposent des **interfaces Java** (couplage faible)
- La Vue ne connaît pas les DAO ; les DAO ne connaissent pas la Vue

---

## 5. Base de données

### 5.1 Table `utilisateur`

| Colonne | Type | Contrainte |
|---|---|---|
| id_utilisateur | INT | PK, AUTO_INCREMENT |
| nom | VARCHAR(100) | NOT NULL, min 2 caractères |
| email | VARCHAR(255) | NOT NULL, UNIQUE, format validé |
| mot_de_passe_hash | VARCHAR(255) | NOT NULL (BCrypt) |
| role | ENUM('ADMIN','CLIENT') | NOT NULL |
| date_inscription | TIMESTAMP | DEFAULT NOW() |

### 5.2 Table `terrain`

| Colonne | Type | Contrainte |
|---|---|---|
| id_terrain | INT | PK, AUTO_INCREMENT |
| nom | VARCHAR(100) | NOT NULL, UNIQUE |
| type | ENUM('FOOTBALL','TENNIS','BASKETBALL','VOLLEYBALL') | NOT NULL |
| prix_par_heure | DECIMAL(10,2) | NOT NULL, > 0 |
| disponibilite | BOOLEAN | DEFAULT TRUE |
| description | TEXT | Optionnel |
| date_ajout | TIMESTAMP | DEFAULT NOW() |

### 5.3 Table `reservation`

| Colonne | Type | Contrainte |
|---|---|---|
| id_reservation | INT | PK, AUTO_INCREMENT |
| id_utilisateur | INT | FK → utilisateur |
| id_terrain | INT | FK → terrain |
| date_reservation | DATE | NOT NULL |
| heure_debut | TIME | 08:00–22:00 |
| duree_heures | INT | 1 à 4 |
| statut | ENUM('CONFIRMEE','ANNULEE','TERMINEE') | NOT NULL |
| montant_total | DECIMAL(10,2) | > 0 |
| date_creation | TIMESTAMP | DEFAULT NOW() |

### 5.4 Calcul du montant

```
montant_total = prix_par_heure × duree_heures
```

### 5.5 Vues SQL incluses

- **`vue_reservations_details`** : jointure reservation + utilisateur + terrain avec tous les champs d'affichage
- **`vue_stats_terrains`** : statistiques agrégées par terrain (nb réservations, CA total)

### 5.6 Détection des conflits de créneaux

Deux créneaux `[A, B]` et `[C, D]` se chevauchent si `A < D ET C < B`.

Implémentée dans `OverlapUtils.java`, appelée par `ReservationServiceImpl` avant chaque insertion.

---

## 6. Comptes utilisateurs par défaut

| Rôle | Email | Mot de passe |
|---|---|---|
| Administrateur | admin@sportspro.com | Admin1234 |
| Client | client@sportspro.com | Client1234 |

> Ces comptes sont créés par le script `database.sql`. Les mots de passe sont hachés avec BCrypt dans la base.

---

## 7. Guide d'utilisation — Client

### 7.1 Connexion / Inscription

1. Lancer l'application : l'écran de connexion s'affiche.
2. **Onglet Connexion** : saisir e-mail + mot de passe → **Se connecter**.
3. **Onglet Inscription** : saisir nom, e-mail, mot de passe (8+ car., 1 majuscule, 1 chiffre) → **S'inscrire**.

### 7.2 Onglet "Mes Réservations"

- Statistiques en haut : Total / À venir / Confirmées / Dépense totale
- Tableau avec colonnes : Date, Horaire, Terrain, Durée, Montant, Statut
- **Filtres** : par statut (Toutes / Confirmée / Annulée / Terminée), par période, recherche libre
- **Annuler** : bouton rouge pour les réservations CONFIRMEE à venir
- **Exporter CSV** : télécharger l'historique complet

### 7.3 Onglet "Nouvelle Réservation"

1. Sélectionner un terrain (liste des terrains disponibles uniquement)
2. Choisir la date (DatePicker, pas de date passée)
3. Sélectionner l'heure de début (08h00 – 22h00)
4. Choisir la durée (1, 2, 3 ou 4 heures)
5. Le **montant estimé** s'affiche en temps réel
6. L'indicateur de disponibilité vérifie les conflits de créneaux
7. Cliquer **Confirmer la réservation**

### 7.4 Onglet "Terrains"

- Tableau de tous les terrains : Nom, Type, Prix/h, Disponibilité, Description
- Affiche même les terrains indisponibles (information uniquement)

### 7.5 Onglet "Mon Profil"

- Bannière avec e-mail et date d'inscription
- Formulaire de modification : Nom, E-mail, Mot de passe (avec confirmation)

### 7.6 Onglet "Paramètres"

- **Thème** : Ocean Pro / Emerald Field / Sunset Arena
- **Auto-refresh** : 15 / 30 / 60 / 120 secondes
- **Notifications** : activer/désactiver
- **Mode compact** : interface réduite
- **Changer de mot de passe** : ancien + nouveau requis

---

## 8. Guide d'utilisation — Administrateur

### 8.1 Onglet "Statistiques" (tableau de bord)

**4 cartes KPI :**
- Terrains utilisés (nb terrains avec au moins 1 réservation confirmée)
- Total réservations
- Réservations confirmées
- Chiffre d'affaires total (somme des montants CONFIRMEE)

**Graphiques :**
- BarChart : nombre de réservations par type de terrain
- PieChart : répartition par statut (Confirmées / Annulées / Terminées)

**En bas :**
- Mini-cartes de répartition par type de terrain
- Tableau des réservations CONFIRMEE en cours

### 8.2 Onglet "Réservations"

- Tableau de **toutes** les réservations de tous les clients
- Colonnes : ID, Client, Terrain, Date, Horaire (début–fin), Montant, Statut, Action
- Filtres : statut, période, recherche textuelle
- **Annuler** une réservation confirmée
- **Exporter CSV** complet
- **Actualiser** manuellement

### 8.3 Onglet "Terrains"

- Liste de tous les terrains avec filtres (type, disponibilité, recherche)
- **Désactiver / Activer** un terrain (toggle disponibilité)
- **Supprimer** un terrain (si sans réservations futures confirmées)
- **Panneau "Ajouter un terrain"** (expansible) : Nom, Type, Prix/h, Description

### 8.4 Onglet "Utilisateurs"

- Liste de tous les comptes (Admin + Client)
- Filtres par rôle et recherche textuelle
- **Supprimer** un compte Client uniquement (le bouton n'apparaît pas pour Admin)

### 8.5 Onglet "Paramètres" (Admin)

Identique au client + les mêmes options :
- Thème, Auto-refresh, Notifications, Mode compact, Changement de mot de passe

---

## 9. Structure des packages Java

```
src/main/java/com/clubsportif/
├── Launcher.java            ← Point d'entrée JAR (java -jar)
├── MainApp.java             ← Application JavaFX (étend Application)
├── config/
│   ├── AppConfig.java       ← Constantes (BDD, règles métier)
│   ├── DatabaseConfig.java  ← Connexion JDBC Singleton
│   └── SecurityConfig.java  ← BCrypt utilitaires
├── model/
│   ├── Utilisateur.java
│   ├── Terrain.java
│   ├── Reservation.java
│   └── enums/
│       ├── Role.java              (ADMIN, CLIENT)
│       ├── TypeTerrain.java       (FOOTBALL, TENNIS, BASKETBALL, VOLLEYBALL)
│       └── StatutReservation.java (CONFIRMEE, ANNULEE, TERMINEE)
├── dao/
│   ├── interfaces/
│   │   ├── IUtilisateurDAO.java
│   │   ├── ITerrainDAO.java
│   │   └── IReservationDAO.java
│   └── impl/
│       ├── UtilisateurDAOImpl.java
│       ├── TerrainDAOImpl.java
│       └── ReservationDAOImpl.java
├── service/
│   ├── interfaces/
│   │   ├── IAuthService.java
│   │   ├── ITerrainService.java
│   │   └── IReservationService.java
│   ├── impl/
│   │   ├── AuthServiceImpl.java
│   │   ├── TerrainServiceImpl.java
│   │   └── ReservationServiceImpl.java
│   └── validators/
│       ├── EmailValidator.java
│       ├── PasswordValidator.java
│       └── ReservationValidator.java
├── ui/
│   ├── LoginView.java      ← Écran connexion/inscription
│   ├── ClientView.java     ← 5 onglets client
│   ├── AdminView.java      ← 5 onglets admin
│   └── UIHelper.java       ← Composants communs, thèmes CSS
├── exception/
│   ├── AuthException.java
│   ├── TerrainException.java
│   └── ReservationException.java
└── util/
    ├── Logger.java
    ├── DateTimeUtil.java
    └── OverlapUtils.java   ← Détection chevauchement créneaux
```

---

## 10. Classes principales

### `AppConfig.java`
Constantes globales de l'application. À modifier pour adapter les paramètres MySQL.

```java
DB_HOST = "localhost"
DB_PORT = 3306
DB_NAME = "club_sportif"
DB_USER = "root"
DB_PASSWORD = "..."
BCRYPT_ROUNDS = 10
HEURE_OUVERTURE = 8    // 08h00
HEURE_FERMETURE = 23   // 23h00
DUREE_MIN_HEURES = 1
DUREE_MAX_HEURES = 4
```

### `DatabaseConfig.java`
Connexion JDBC via **patron Singleton**. Produit une instance unique de `Connection`.

### `SecurityConfig.java`
Méthodes utilitaires BCrypt :
- `hashPassword(String plaintext) → String`
- `checkPassword(String plaintext, String hash) → boolean`

### `OverlapUtils.java`
Détection de chevauchement de créneaux :
```java
// Retourne true si [startA, endA[ et [startB, endB[ se chevauchent
public static boolean overlaps(LocalTime startA, LocalTime endA,
                               LocalTime startB, LocalTime endB)
```

### `Launcher.java`
Point d'entrée pour `java -jar`. Délègue à `MainApp.main(args)` sans étendre `Application`.

---

## 11. Sécurité

| Mécanisme | Implémentation |
|---|---|
| Hachage des mots de passe | BCrypt, 10 rounds (jBCrypt 0.4) |
| Protection SQL injection | `PreparedStatement` dans tous les DAO |
| Validation des e-mails | Regex + `EmailValidator` |
| Validation mot de passe | 8+ car., 1 majuscule, 1 chiffre (`PasswordValidator`) |
| Séparation des rôles | Vérification `Role.ADMIN` côté service avant toute action admin |
| Pas de mot de passe en clair | Aucun mot de passe n'est stocké ou transmis en clair |

---

## 12. Packaging et déploiement JAR

### Construire le JAR

```bash
mvn clean package -DskipTests
```

### Résultat

```
target/terrain-projet-1.0.0-jar-with-dependencies.jar
```

Le JAR embarque toutes les dépendances (JavaFX, jBCrypt, MySQL Connector/J).

### Lancer

```bash
java -jar target/terrain-projet-1.0.0-jar-with-dependencies.jar
```

### Pré-requis machine cible

- Java 17+ installé
- MySQL 8.0 démarré avec la base `club_sportif` initialisée
- Fichier `AppConfig.java` recompilé avec les bons paramètres de connexion

---

## 13. Tests

### Lancer les tests

```bash
mvn test
```

### Framework

- **JUnit 5** (5.10.1) : tests unitaires
- **Mockito** (5.17.0) : mocks pour tester la couche Service sans base de données

### Couverture

Les tests couvrent principalement :
- `OverlapUtils` : cas de chevauchement/non-chevauchement
- `EmailValidator` : formats valides/invalides
- `PasswordValidator` : règles de complexité
- `ReservationServiceImpl` : logique métier (avec mock DAO)
- `AuthServiceImpl` : authentification et inscription (avec mock DAO)

---

## 14. Configuration

### `pom.xml` — Dépendances principales

| Artefact | Version | Usage |
|---|---|---|
| javafx-controls | 21.0.5 | Composants UI |
| javafx-fxml | 21.0.5 | FXML (si utilisé) |
| mysql-connector-j | 8.0.33 | Driver JDBC MySQL |
| jbcrypt | 0.4 | Hachage BCrypt |
| junit-jupiter | 5.10.1 | Tests unitaires |
| mockito-core | 5.17.0 | Mocking |

### Plugin Maven Assembly

Le plugin `maven-assembly-plugin` crée le fat-JAR avec toutes les dépendances.

Le `mainClass` du manifest pointe sur `com.clubsportif.Launcher`.

---

## 15. FAQ / Dépannage

### L'application ne démarre pas — erreur de connexion MySQL

**Symptôme :** `Access denied for user 'root'@'localhost'`

**Solutions :**
1. Vérifier que MySQL est démarré : `mysql -u root -p`
2. Vérifier/corriger `DB_PASSWORD` dans `AppConfig.java`
3. Recompiler et relancer : `mvn clean javafx:run`

### Deux instances MySQL sur la même machine (XAMPP + MySQL autonome)

- XAMPP MySQL tourne sur le port 3306 par défaut
- MySQL 8.0 autonome tourne aussi sur 3306

Pour vérifier quelle instance répond :
```bash
mysql -u root -p --port=3306
```

Pour arrêter XAMPP MySQL et utiliser MySQL 8.0 : stopper le service `MySQL` dans XAMPP et démarrer le service Windows `MySQL80`.

### Erreur `caching_sha2_password could not be loaded`

Le client MySQL de XAMPP est ancien et incompatible avec l'authentification MySQL 8.0+.

**Solution :** utiliser `mysql.exe` de MySQL 8.0 :
```
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

### Voir la structure de la base de données

Utiliser **MySQL Workbench** (installé à `C:\Program Files\MySQL\MySQL Workbench 8.0\`).

1. Ouvrir MySQL Workbench
2. Cliquer sur la connexion locale (localhost:3306)
3. Dans le panneau gauche → Schemas → `club_sportif`
4. Explorer Tables, Views, etc.

### Maven ne télécharge pas les dépendances JavaFX

```bash
mvn dependency:resolve
```

Vérifier la connexion Internet. Les artefacts JavaFX nécessitent Maven Central.

### La table `club_sportif` n'existe pas

```bash
mysql -u root -p < database.sql
```

### Erreur `Duplicate key name 'idx_...'` lors de l'import SQL

Non-critique. Signifie que l'index existe déjà. La base est correctement créée.

---

*SportsPro v1.0.0 — Projet Universitaire 2025-2026*
