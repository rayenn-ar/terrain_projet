-- ============================================================
-- SCRIPT SQL - Système de Gestion des Réservations de Terrains Sportifs
-- Projet Universitaire 2025-2026
-- Base de données: MySQL 8.0
-- ============================================================

-- Création et sélection de la base de données
CREATE DATABASE IF NOT EXISTS club_sportif
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE club_sportif;

-- ============================================================
-- TABLE UTILISATEUR
-- ============================================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role           ENUM('ADMIN', 'CLIENT') NOT NULL DEFAULT 'CLIENT',
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_nom_length CHECK (CHAR_LENGTH(TRIM(nom)) >= 2),
    CONSTRAINT chk_email_format CHECK (email REGEXP '^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$')
) ENGINE = InnoDB;

CREATE INDEX idx_utilisateur_email ON utilisateur (email);

-- ============================================================
-- TABLE TERRAIN
-- ============================================================
CREATE TABLE IF NOT EXISTS terrain (
    id_terrain     INT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL UNIQUE,
    type           ENUM('FOOTBALL', 'TENNIS', 'BASKETBALL', 'VOLLEYBALL') NOT NULL,
    prix_par_heure DECIMAL(10, 2) NOT NULL,
    disponibilite  BOOLEAN NOT NULL DEFAULT TRUE,
    description    TEXT,
    date_ajout     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_terrain_nom_length CHECK (CHAR_LENGTH(TRIM(nom)) >= 2),
    CONSTRAINT chk_prix_positif CHECK (prix_par_heure > 0)
) ENGINE = InnoDB;

CREATE INDEX idx_terrain_type ON terrain (type);
CREATE INDEX idx_terrain_disponibilite ON terrain (disponibilite);

-- ============================================================
-- TABLE RESERVATION
-- ============================================================
CREATE TABLE IF NOT EXISTS reservation (
    id_reservation  INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur  INT NOT NULL,
    id_terrain      INT NOT NULL,
    date_reservation DATE NOT NULL,
    heure_debut      TIME NOT NULL,
    duree_heures     INT NOT NULL,
    statut           ENUM('CONFIRMEE', 'ANNULEE', 'TERMINEE') NOT NULL DEFAULT 'CONFIRMEE',
    montant_total    DECIMAL(10, 2) NOT NULL,
    date_creation    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_date_min CHECK (date_reservation >= '2025-01-01'),
    CONSTRAINT chk_heure_plage CHECK (heure_debut >= '08:00:00' AND heure_debut <= '22:00:00'),
    CONSTRAINT chk_duree CHECK (duree_heures BETWEEN 1 AND 4),
    CONSTRAINT chk_montant_positif CHECK (montant_total > 0),
    CONSTRAINT fk_reservation_utilisateur
        FOREIGN KEY (id_utilisateur)
            REFERENCES utilisateur (id_utilisateur)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    CONSTRAINT fk_reservation_terrain
        FOREIGN KEY (id_terrain)
            REFERENCES terrain (id_terrain)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_reservation_utilisateur ON reservation (id_utilisateur);
CREATE INDEX idx_reservation_terrain ON reservation (id_terrain);
CREATE INDEX idx_reservation_date ON reservation (date_reservation);
CREATE INDEX idx_reservation_statut ON reservation (statut);
CREATE INDEX idx_reservation_composite ON reservation (id_terrain, date_reservation, statut);

-- ============================================================
-- VUE : Réservations actives (CONFIRMEE) avec détails
-- ============================================================
CREATE OR REPLACE VIEW v_reservations_actives AS
SELECT r.id_reservation,
       r.id_utilisateur,
       r.id_terrain,
       r.date_reservation,
       r.heure_debut,
       r.duree_heures,
       r.statut,
       r.montant_total,
       r.date_creation,
       u.nom  AS nom_client,
       u.email,
       t.nom  AS nom_terrain,
       t.type AS type_terrain
FROM reservation r
         JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur
         JOIN terrain t ON r.id_terrain = t.id_terrain
WHERE r.statut = 'CONFIRMEE';

-- ============================================================
-- VUE : Statistiques par terrain
-- ============================================================
CREATE OR REPLACE VIEW v_stats_terrain AS
SELECT t.id_terrain,
       t.nom                                                         AS nom_terrain,
       t.type,
       COUNT(r.id_reservation)                                       AS total_reservations,
       SUM(CASE WHEN r.statut = 'CONFIRMEE' THEN 1 ELSE 0 END)      AS reservations_confirmees,
       SUM(CASE WHEN r.statut = 'TERMINEE' THEN 1 ELSE 0 END)       AS reservations_terminees,
       SUM(CASE WHEN r.statut = 'ANNULEE' THEN 1 ELSE 0 END)        AS reservations_annulees,
       COALESCE(SUM(CASE WHEN r.statut != 'ANNULEE' THEN r.montant_total END), 0) AS chiffre_affaires
FROM terrain t
         LEFT JOIN reservation r ON t.id_terrain = r.id_terrain
GROUP BY t.id_terrain, t.nom, t.type;

-- ============================================================
-- DONNÉES DE TEST
-- ============================================================

-- ============================================================
-- INSTRUCTIONS POUR LES COMPTES DE TEST
-- ============================================================
-- Les mots de passe sont hashés avec BCrypt (rounds=10).
-- Le hash ci-dessous correspond au mot de passe : Test@1234
-- (généré via SecurityConfig.hashPassword("Test@1234"))
--
-- Comptes disponibles :
--   admin@clubsportif.com  / Test@1234  (ADMIN)
--   jean.dupont@email.com  / Test@1234  (CLIENT)
--   marie.martin@email.com / Test@1234  (CLIENT)
--
-- Si vous souhaitez changer les mots de passe, utilisez l'application
-- (menu "S'inscrire" pour les clients, puis modifiez le rôle en base)
-- ou régénérez le hash avec : SecurityConfig.hashPassword("votre_mdp")
-- ============================================================
-- Comptes de test : mot de passe par défaut = Admin1234
INSERT INTO utilisateur (nom, email, mot_de_passe_hash, role)
VALUES ('Administrateur Club', 'admin@clubsportif.com',
    '$2a$10$vssnXnHUmXO5fPenVPawq.OGeLWN7OoRstZ1yyOuY294h5p8rJ/My', 'ADMIN'),
       ('Jean Dupont', 'jean.dupont@email.com',
    '$2a$10$vssnXnHUmXO5fPenVPawq.OGeLWN7OoRstZ1yyOuY294h5p8rJ/My', 'CLIENT'),
       ('Marie Martin', 'marie.martin@email.com',
    '$2a$10$vssnXnHUmXO5fPenVPawq.OGeLWN7OoRstZ1yyOuY294h5p8rJ/My', 'CLIENT');
-- NOTE : Si la connexion échoue avec ces comptes, inscrivez un nouveau compte
-- via l'application puis exécutez : UPDATE utilisateur SET role='ADMIN' WHERE email='votre@email.com';

-- Terrains
INSERT INTO terrain (nom, type, prix_par_heure, disponibilite, description)
VALUES ('Terrain A - Football', 'FOOTBALL', 50.00, TRUE,
        'Terrain football 11 joueurs, pelouse synthétique haute qualité, vestiaires inclus'),
       ('Court Central Tennis', 'TENNIS', 35.00, TRUE,
        'Court tennis terre battue, éclairage LED, tribunes 50 places'),
       ('Salle Basketball Intérieur', 'BASKETBALL', 40.00, TRUE,
        'Salle parquet avec paniers professionnels NBA standard, climatisée'),
       ('Beach Volley Extérieur', 'VOLLEYBALL', 25.00, TRUE,
        'Terrain sable naturel avec filet réglable, douches extérieures');

-- Réservations de test (dates relatives)
INSERT INTO reservation (id_utilisateur, id_terrain, date_reservation, heure_debut, duree_heures, statut,
                         montant_total)
VALUES (2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 2, 'CONFIRMEE', 100.00),
       (2, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', 1, 'CONFIRMEE', 35.00),
       (3, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '16:00:00', 2, 'TERMINEE', 80.00),
       (3, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:00:00', 1, 'ANNULEE', 50.00),
       (2, 4, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '09:00:00', 3, 'CONFIRMEE', 75.00);
