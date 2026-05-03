# Projet Universitaire 2025-2026
## Système de Gestion des Réservations de Terrains Sportifs
### Cahier des Charges Technique

---

## 1. Introduction

### 1.1 Contexte

Les clubs sportifs (football, tennis, basketball, volleyball, etc.) gèrent souvent les
réservations des terrains de manière manuelle — par téléphone ou registre papier — ce qui
engendre :

- Des **conflits d'horaires** entre plusieurs réservations simultanées
- Des **erreurs de saisie** et enregistrements perdus
- Une **désorganisation** des créneaux disponibles
- Une **perte de temps** pour le personnel et les clients

Il devient donc nécessaire de développer une application informatique afin d'automatiser
et d'optimiser ce processus.

### 1.2 Objectif du Projet

Développer une application Java complète permettant :

- La gestion complète des terrains sportifs (CRUD)
- La gestion des utilisateurs avec authentification sécurisée (BCrypt)
- La réservation et l'annulation de créneaux horaires avec vérification des conflits
- L'affichage des disponibilités et de l'historique des réservations
- La consultation de statistiques pour les administrateurs (taux d'occupation, chiffre d'affaires)

---

## 2. Description Générale

### 2.1 Acteurs du Système

**1. Administrateur**

| Action | Description |
|--------|-------------|
| Gérer les terrains | Ajouter, modifier, supprimer, activer/désactiver la disponibilité |
| Gérer les réservations | Consulter toutes les réservations, annuler n'importe laquelle |
| Gérer les utilisateurs | Consulter la liste, supprimer un compte client |
| Consulter les statistiques | Taux d'occupation, chiffre d'affaires, répartition par statut |

**2. Client (Joueur)**

| Action | Description |
|--------|-------------|
| S'inscrire | Créer un compte avec email unique et mot de passe sécurisé |
| Se connecter / déconnecter | Session sécurisée |
| Consulter les terrains | Filtrage par type (Football, Tennis, Basketball, Volleyball) |
| Réserver un terrain | Sélection → date → heure → durée → confirmation + calcul du montant |
| Annuler une réservation | Uniquement ses propres réservations, uniquement si futures |
| Consulter son historique | Toutes ses réservations (tous statuts) |
| Modifier son profil | Nom, email, mot de passe |

### 2.2 Environnement Technique

| Élément | Technologie |
|---------|-------------|
| Langage | Java 17+ (JDK 25 recommandé) |
| Base de données | MySQL 8.0 |
| Connexion BD | JDBC avec `PreparedStatement` (anti-injection SQL) |
| Architecture | MVC + DAO + Singleton |
| Interface | Application Console avec couleurs ANSI |
| Sécurité mots de passe | BCrypt — bibliothèque jBCrypt 0.4 (10 rounds) |
| Gestion des dépendances | Apache Maven 3.9+ |
| Tests | JUnit 5 |

---

## 3. Spécifications Fonctionnelles

### 3.1 Gestion des Utilisateurs

Le système permet :

- L'inscription d'un client avec vérification du format de l'email et de son unicité
- La connexion via email + mot de passe (comparaison du hash BCrypt)
- La modification du profil (nom, email, et mot de passe avec confirmation)
- La suppression d'un compte par l'administrateur uniquement
  *(un administrateur ne peut pas supprimer un autre compte admin, ni le sien)*
- La déconnexion avec effacement de la session

**Attributs d'un utilisateur :**

| Attribut | Type | Contrainte |
|----------|------|-----------|
| `id_utilisateur` | INT | PK, AUTO_INCREMENT |
| `nom` | VARCHAR(100) | NOT NULL, minimum 2 caractères |
| `email` | VARCHAR(255) | UNIQUE, format valide (regex) |
| `mot_de_passe_hash` | VARCHAR(255) | Hashé BCrypt (10 rounds) |
| `role` | ENUM | `ADMIN` ou `CLIENT` |
| `date_inscription` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

### 3.2 Gestion des Terrains *(Administrateur uniquement)*

L'administrateur peut effectuer les opérations CRUD suivantes :

- **Ajouter** un terrain avec validation (nom unique, prix > 0, type parmi les 4 autorisés)
- **Modifier** les informations d'un terrain existant
- **Supprimer** un terrain
- **Lister** tous les terrains, avec possibilité de filtrer par type ou disponibilité
- **Activer / désactiver** la disponibilité d'un terrain

**Attributs d'un terrain :**

| Attribut | Type | Contrainte |
|----------|------|-----------|
| `id_terrain` | INT | PK, AUTO_INCREMENT |
| `nom` | VARCHAR(100) | UNIQUE, minimum 2 caractères |
| `type` | ENUM | `FOOTBALL`, `TENNIS`, `BASKETBALL`, `VOLLEYBALL` |
| `prix_par_heure` | DECIMAL(10,2) | > 0 |
| `disponibilite` | BOOLEAN | DEFAULT TRUE |
| `description` | TEXT | Optionnel |

### 3.3 Gestion des Réservations

Le système permet :

- Consulter les **créneaux déjà occupés** pour un terrain à une date donnée
- **Réserver** un terrain en 5 étapes guidées :
  1. Sélection du terrain parmi les disponibles
  2. Saisie de la date (format JJ/MM/AAAA)
  3. Affichage des créneaux pris — saisie de l'heure de début
  4. Saisie de la durée (1 à 4 heures)
  5. Récapitulatif et confirmation
- **Annuler** une réservation — client sur ses propres réservations, admin sur toutes
- Consulter l'**historique complet** (tous statuts : CONFIRMÉE, ANNULÉE, TERMINÉE)
- **Mise à jour automatique** des réservations passées en statut TERMINÉE au démarrage

**Attributs d'une réservation :**

| Attribut | Type | Contrainte |
|----------|------|-----------|
| `id_reservation` | INT | PK, AUTO_INCREMENT |
| `id_utilisateur` | INT | FK → `utilisateur` |
| `id_terrain` | INT | FK → `terrain` |
| `date_reservation` | DATE | ≥ date du jour |
| `heure_debut` | TIME | Entre 08:00 et 22:00 |
| `duree_heures` | INT | Entre 1 et 4 heures |
| `statut` | ENUM | `CONFIRMEE`, `ANNULEE`, `TERMINEE` |
| `montant_total` | DECIMAL(10,2) | = `prix_par_heure × duree_heures` |

### 3.4 Règles de Gestion

| # | Règle |
|---|-------|
| **R1** | Un terrain ne peut pas être réservé deux fois au même horaire. **Algorithme de chevauchement :** deux intervalles `[A, B]` et `[C, D]` se chevauchent si **A < D ET C < B**. Seules les réservations CONFIRMÉES sont prises en compte. |
| **R2** | Une réservation ne peut être effectuée que par un utilisateur connecté (session active). |
| **R3** | L'annulation est possible uniquement si la date de réservation est future, ou si la date est aujourd'hui mais l'heure de début est encore à venir. |
| **R4** | Les réservations avec le statut TERMINÉE ne peuvent être ni modifiées, ni annulées. |
| **R5** | Le montant total est calculé automatiquement : **montant = prix_par_heure × duree_heures**. |
| **R6** | Un terrain dont la disponibilité est désactivée (`disponibilite = FALSE`) n'accepte aucune nouvelle réservation. |
| **R7** | Plage horaire autorisée : **08h00 à 23h00**. Le dernier créneau doit se terminer au plus tard à 23h00 (ex. : 22h00 + 1h = 23h00 ✓ ; 22h00 + 2h = 24h00 ✗). |

---

## 4. Architecture Logicielle

### 4.1 Structure des Packages

```
src/main/java/com/clubsportif/
├── Main.java                           Point d'entrée — câblage des services
├── config/
│   ├── AppConfig.java                  Constantes : URL BD, BCrypt rounds, horaires
│   ├── DatabaseConfig.java             Singleton — connexion JDBC unique et réutilisable
│   └── SecurityConfig.java             Utilitaires BCrypt (hash + vérification)
├── model/                              POJO — Plain Old Java Objects
│   ├── Utilisateur.java
│   ├── Terrain.java
│   ├── Reservation.java
│   └── enums/
│       ├── Role.java                   ADMIN, CLIENT
│       ├── TypeTerrain.java            FOOTBALL, TENNIS, BASKETBALL, VOLLEYBALL
│       └── StatutReservation.java      CONFIRMEE, ANNULEE, TERMINEE
├── dao/                                Pattern DAO — accès base de données uniquement
│   ├── interfaces/
│   │   ├── IUtilisateurDAO.java
│   │   ├── ITerrainDAO.java
│   │   └── IReservationDAO.java
│   └── impl/
│       ├── UtilisateurDAOImpl.java     JDBC pur, PreparedStatement
│       ├── TerrainDAOImpl.java
│       └── ReservationDAOImpl.java
├── service/                            Logique métier et validation
│   ├── interfaces/
│   │   ├── IAuthService.java
│   │   ├── ITerrainService.java
│   │   └── IReservationService.java
│   ├── impl/
│   │   ├── AuthServiceImpl.java        Authentification, session utilisateur
│   │   ├── TerrainServiceImpl.java     CRUD terrains avec validation
│   │   └── ReservationServiceImpl.java Algorithme anti-conflit (R1), calcul prix (R5)
│   └── validators/
│       ├── EmailValidator.java         Validation format email (regex)
│       └── ReservationValidator.java   Validation horaires (R3, R5, R7)
├── controller/                         Orchestration vue ↔ service
│   ├── ConsoleController.java          Routage principal selon rôle et état de session
│   ├── AuthController.java             Formulaires connexion, inscription, profil
│   ├── TerrainController.java          Menus CRUD terrains
│   └── ReservationController.java      Formulaires réservation, annulation, stats
├── view/console/                       Présentation uniquement (aucune logique métier)
│   ├── ConsoleView.java                Affichage tableaux, messages, bannière
│   ├── MenuBuilder.java                Construction fluide des menus ANSI
│   └── TableFormatter.java             Tableaux alignés avec gestion des couleurs ANSI
├── util/
│   ├── ConsoleColors.java              Codes couleur ANSI (RED, GREEN, CYAN, etc.)
│   ├── DateTimeUtil.java               Parsing et formatage des dates/heures
│   ├── ScannerSingleton.java           Scanner partagé (Singleton) — évite les conflits buffer
│   └── Logger.java                     Journalisation console horodatée
└── exception/
    ├── BusinessException.java          Classe mère des exceptions métier
    ├── AuthenticationException.java    Identifiants invalides, session expirée
    ├── ReservationConflictException.java Conflit de créneaux (R1)
    ├── EntityNotFoundException.java    Ressource introuvable en base
    ├── ValidationException.java        Données saisies invalides
    └── UnauthorizedException.java      Accès refusé (rôle insuffisant)
```

### 4.2 Patterns de Conception Utilisés

| Pattern | Classe(s) | Justification |
|---------|-----------|--------------|
| **Singleton** | `DatabaseConfig`, `ScannerSingleton` | Une seule connexion BD / un seul Scanner |
| **DAO** | `*DAOImpl` | Sépare l'accès aux données de la logique métier |
| **MVC** | `controller/`, `view/`, `service/` | Découplage présentation / logique / données |
| **Interface / Implémentation** | `I*DAO`, `I*Service` | Facilite les tests et la substitution |

---

## 5. Modélisation UML

### 5.1 Diagramme de Cas d'Utilisation

```
┌─────────────────────────────────────────────────────┐
│                    Système                          │
│                                                     │
│  [S'authentifier] ◄──── include ────── [Réserver]  │
│  [S'authentifier] ◄──── include ────── [Annuler]   │
│                                                     │
│  ADMIN :                                            │
│    • Gérer terrains (CRUD + disponibilité)          │
│    • Gérer toutes les réservations                  │
│    • Supprimer un compte client                     │
│    • Consulter les statistiques                     │
│                                                     │
│  CLIENT :                                           │
│    • S'inscrire                                     │
│    • Consulter les terrains disponibles             │
│    • Réserver un terrain                            │
│    • Annuler sa réservation (si future)             │
│    • Consulter son historique                       │
│    • Modifier son profil                            │
└─────────────────────────────────────────────────────┘
```

### 5.2 Diagramme de Classes (Relations principales)

```
Utilisateur ─────────────────────── Reservation
  - idUtilisateur : int               - idReservation : int
  - nom : String          1    0..*   - idUtilisateur : int (FK)
  - email : String     ───────────►  - idTerrain : int (FK)
  - motDePasseHash : String           - dateReservation : LocalDate
  - role : Role                       - heureDebut : LocalTime
  - dateInscription : LocalDateTime   - dureeHeures : int
                                      - statut : StatutReservation
                                      - montantTotal : BigDecimal
Terrain ─────────────────────────────────────┘
  - idTerrain : int               1    0..*
  - nom : String               ───────────►
  - type : TypeTerrain
  - prixParHeure : BigDecimal
  - disponibilite : boolean
  - description : String

Enums : Role {ADMIN, CLIENT}
        TypeTerrain {FOOTBALL, TENNIS, BASKETBALL, VOLLEYBALL}
        StatutReservation {CONFIRMEE, ANNULEE, TERMINEE}
```

### 5.3 Diagramme de Séquence — Réserver un terrain

```
Client   ConsoleController   ReservationController   ReservationService   DAO         BDD
  │             │                     │                      │              │            │
  │──Réserver──►│                     │                      │              │            │
  │             │────────────────────►│                      │              │            │
  │             │                     │──getCurrentUser()───►│(AuthService) │            │
  │             │                     │◄─session active──────│              │            │
  │             │                     │──creerReservation()─►│              │            │
  │             │                     │                      │──findById()─►│            │
  │             │                     │                      │◄─terrain─────│            │
  │             │                     │                      │──isCreneauDisponible()    │
  │             │                     │                      │  (algorithme R1 : A<D∧C<B)│
  │             │                     │               Si OK  │──save()─────►│            │
  │             │                     │                      │              │──INSERT────►│
  │             │                     │                      │              │◄─id────────│
  │             │◄────confirmation────│◄────réservation──────│              │            │
  │◄─succès─────│                     │                      │              │            │
  │             │                     │         Si conflit   │              │            │
  │             │◄──erreur────────────│◄─ReservationConflictException       │            │
```

---

## 6. Base de Données

### 6.1 Script SQL — Création des tables

```sql
CREATE DATABASE IF NOT EXISTS club_sportif
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE club_sportif;

-- Utilisateurs
CREATE TABLE utilisateur (
    id_utilisateur    INT AUTO_INCREMENT PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role              ENUM('ADMIN', 'CLIENT') NOT NULL DEFAULT 'CLIENT',
    date_inscription  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_nom_length    CHECK (CHAR_LENGTH(TRIM(nom)) >= 2),
    CONSTRAINT chk_email_format  CHECK (email REGEXP '^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$')
) ENGINE = InnoDB;

-- Terrains
CREATE TABLE terrain (
    id_terrain     INT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL UNIQUE,
    type           ENUM('FOOTBALL', 'TENNIS', 'BASKETBALL', 'VOLLEYBALL') NOT NULL,
    prix_par_heure DECIMAL(10,2) NOT NULL CHECK (prix_par_heure > 0),
    disponibilite  BOOLEAN NOT NULL DEFAULT TRUE,
    description    TEXT,
    date_ajout     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Réservations
CREATE TABLE reservation (
    id_reservation   INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur   INT NOT NULL,
    id_terrain       INT NOT NULL,
    date_reservation DATE NOT NULL,
    heure_debut      TIME NOT NULL CHECK (heure_debut >= '08:00:00' AND heure_debut <= '22:00:00'),
    duree_heures     INT NOT NULL CHECK (duree_heures BETWEEN 1 AND 4),
    statut           ENUM('CONFIRMEE', 'ANNULEE', 'TERMINEE') NOT NULL DEFAULT 'CONFIRMEE',
    montant_total    DECIMAL(10,2) NOT NULL CHECK (montant_total > 0),
    date_creation    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_res_terrain     FOREIGN KEY (id_terrain)     REFERENCES terrain(id_terrain)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;
```

### 6.2 Index et Vues

```sql
-- Index pour les requêtes fréquentes
CREATE INDEX idx_utilisateur_email        ON utilisateur(email);
CREATE INDEX idx_terrain_type             ON terrain(type);
CREATE INDEX idx_reservation_statut       ON reservation(statut);
CREATE INDEX idx_reservation_composite    ON reservation(id_terrain, date_reservation, statut);

-- Vue : réservations actives avec détails client et terrain
CREATE VIEW v_reservations_actives AS
SELECT r.*, u.nom AS nom_client, u.email, t.nom AS nom_terrain, t.type
FROM reservation r
JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur
JOIN terrain     t ON r.id_terrain     = t.id_terrain
WHERE r.statut = 'CONFIRMEE';

-- Vue : statistiques par terrain
CREATE VIEW v_stats_terrain AS
SELECT t.nom AS nom_terrain, t.type,
       COUNT(r.id_reservation) AS total_reservations,
       SUM(CASE WHEN r.statut != 'ANNULEE' THEN r.montant_total ELSE 0 END) AS chiffre_affaires
FROM terrain t
LEFT JOIN reservation r ON t.id_terrain = r.id_terrain
GROUP BY t.id_terrain;
```

### 6.3 Données de Test

| Compte | Email | Mot de passe | Rôle |
|--------|-------|-------------|------|
| Administrateur Club | admin@clubsportif.com | Test@1234 | ADMIN |
| Jean Dupont | jean.dupont@email.com | Test@1234 | CLIENT |
| Marie Martin | marie.martin@email.com | Test@1234 | CLIENT |

---

## 7. Spécifications Non Fonctionnelles

### 7.1 Performance

- Temps de réponse inférieur à 3 secondes pour toutes les opérations
- Requêtes optimisées avec index sur `email`, `type`, `date_reservation`, `statut`
- Index composite sur `(id_terrain, date_reservation, statut)` pour la vérification des conflits

### 7.2 Sécurité

- Authentification obligatoire pour toute réservation ou modification (R2)
- Mots de passe hashés avec **BCrypt** (10 rounds) — stockage sécurisé
- Toutes les requêtes SQL utilisent des **`PreparedStatement`** — protection contre l'injection SQL (OWASP A03)
- Séparation stricte des rôles **ADMIN / CLIENT** côté service et contrôleur
- Session stockée en mémoire uniquement (pas de stockage persistant des tokens)

### 7.3 Maintenabilité

- Architecture en couches (MVC + DAO) permettant d'isoler les modifications
- Interfaces et implémentations séparées (facilite les tests unitaires)
- Exceptions personnalisées pour une gestion précise des erreurs métier

### 7.4 Fiabilité

- Vérification de la connexion à la base de données au démarrage de l'application
- Reconnexion automatique JDBC si la connexion est fermée (`DatabaseConfig`)
- Messages d'erreur clairs et informatifs pour l'utilisateur final

---

## 8. Planning Prévisionnel

| Phase | Durée | Livrables |
|-------|-------|-----------|
| Analyse des besoins | 1 semaine | Cahier des charges, règles de gestion |
| Conception UML | 1 semaine | Diagrammes : classes, cas d'utilisation, séquence |
| Développement — couche données | 1 semaine | Modèles, enums, DAO, script SQL |
| Développement — couche service | 1 semaine | Services, validators, exceptions, algorithme R1 |
| Développement — couche présentation | 1 semaine | Contrôleurs, vues console, menus ANSI |
| Tests et corrections | 1 semaine | Tests JUnit 5, correction de bugs, validation règles |
| **Total** | **6 semaines** | **Application fonctionnelle et testée** |

---

## 9. Conclusion

Ce projet met en œuvre une solution complète et moderne pour la gestion des réservations de
terrains sportifs. L'application :

- **Automatise** intégralement la gestion des créneaux en éliminant les conflits d'horaires
  grâce à l'algorithme de chevauchement d'intervalles (R1)
- **Sécurise** les données utilisateurs grâce au hashage BCrypt et aux `PreparedStatement`
- **Respecte** les principes de la Programmation Orientée Objet avec une architecture MVC + DAO + Singleton
- **Offre** une interface console intuitive avec retour visuel coloré (ANSI)
- **Est extensible** pour une future migration vers une interface graphique (JavaFX / Swing)
  ou une API REST (Spring Boot)
