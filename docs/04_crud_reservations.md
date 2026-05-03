# CRUD Réservations — Documentation du code

## Les fichiers concernés

```
model/Reservation.java
model/enums/StatutReservation.java
dao/interfaces/IReservationDAO.java
dao/impl/ReservationDAOImpl.java
service/interfaces/IReservationService.java
service/impl/ReservationServiceImpl.java
util/OverlapUtils.java
service/validators/ReservationValidator.java
```

---

## 1. Le modèle : `Reservation.java`

Représente une réservation. Correspond à la table `reservation` en base.

```java
// Champs de la table
private int idReservation;
private int idUtilisateur;
private int idTerrain;
private LocalDate dateReservation;
private LocalTime heureDebut;
private int dureeHeures;
private StatutReservation statut;   // CONFIRMEE, ANNULEE ou TERMINEE
private BigDecimal montantTotal;    // = prix/h × dureeHeures

// Champs supplémentaires (viennent des JOIN SQL, pas dans la table)
private String nomClient;
private String emailClient;
private String nomTerrain;
private String typeTerrain;
```

**Méthode calculée :**
```java
public LocalTime getHeureFin() {
    return heureDebut.plusHours(dureeHeures);
}
// Exemple : début 10h00 + durée 2h → fin 12h00
```

### L'enum `StatutReservation`

```java
public enum StatutReservation {
    CONFIRMEE,   // réservation active
    ANNULEE,     // annulée par le client ou l'admin
    TERMINEE     // le créneau est passé
}
```

---

## 2. La couche DAO : `ReservationDAOImpl.java`

### La requête SELECT de base

Toutes les méthodes de lecture utilisent une requête avec JOIN pour récupérer aussi les noms du client et du terrain :

```java
private static final String SELECT_FULL =
    "SELECT r.*, u.nom AS nom_client, u.email, t.nom AS nom_terrain, t.type " +
    "FROM reservation r " +
    "JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur " +
    "JOIN terrain t ON r.id_terrain = t.id_terrain ";
```

C'est pour afficher directement "Jean Dupont a réservé Terrain Sud" sans avoir à faire une deuxième requête.

### La méthode `mapRow`

```java
private Reservation mapRow(ResultSet rs) throws SQLException {
    Reservation r = new Reservation();
    r.setIdReservation(rs.getInt("id_reservation"));
    r.setIdUtilisateur(rs.getInt("id_utilisateur"));
    r.setIdTerrain(rs.getInt("id_terrain"));
    r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
    r.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
    r.setDureeHeures(rs.getInt("duree_heures"));
    r.setStatut(StatutReservation.valueOf(rs.getString("statut")));
    r.setMontantTotal(rs.getBigDecimal("montant_total"));
    // Champs JOIN — si absents dans la requête, l'exception est ignorée
    try { r.setNomClient(rs.getString("nom_client")); }   catch (SQLException ignored) {}
    try { r.setNomTerrain(rs.getString("nom_terrain")); } catch (SQLException ignored) {}
    // ...
    return r;
}
```

### CREATE — `save(Reservation r)`

```java
String sql = "INSERT INTO reservation "
           + "(id_utilisateur, id_terrain, date_reservation, heure_debut, duree_heures, statut, montant_total) "
           + "VALUES (?, ?, ?, ?, ?, ?, ?)";
```

Le statut est toujours `CONFIRMEE` à la création.

### READ — `findAll()`

Toutes les réservations (admin uniquement), du plus récent au plus ancien.

```java
String sql = SELECT_FULL + "ORDER BY r.date_reservation DESC, r.heure_debut";
```

### READ — `findByUtilisateur(int idUtilisateur)`

Toutes les réservations d'un client précis (pour l'onglet "Mes Réservations").

```java
String sql = SELECT_FULL + "WHERE r.id_utilisateur = ? ORDER BY r.date_reservation DESC, r.heure_debut";
```

### READ — `findByTerrainAndDateAndStatut(idTerrain, date, statut)`

Utilisée pour la **vérification de conflit** : cherche toutes les réservations CONFIRMEE sur un terrain à une date donnée.

```java
String sql = SELECT_FULL
           + "WHERE r.id_terrain = ? AND r.date_reservation = ? AND r.statut = ? "
           + "ORDER BY r.heure_debut";
```

### READ — `findConfirmeesPassees()`

Cherche les réservations CONFIRMEE dont le créneau est déjà terminé. Utilisée pour mettre à jour le statut vers TERMINEE.

```java
"WHERE r.statut = 'CONFIRMEE' "
+ "AND (r.date_reservation < CURDATE() "
+ "     OR (r.date_reservation = CURDATE() "
+ "         AND ADDTIME(r.heure_debut, SEC_TO_TIME(r.duree_heures * 3600)) <= CURTIME()))"
```

En français : la date est dans le passé, OU (c'est aujourd'hui ET l'heure de fin est déjà passée).

### UPDATE — `updateStatut(int id, StatutReservation statut)`

Change uniquement le statut d'une réservation (ANNULEE ou TERMINEE).

```java
String sql = "UPDATE reservation SET statut = ? WHERE id_reservation = ?";
```

### DELETE — `delete(int id)`

Supprime une réservation. Peu utilisé dans l'interface (on annule, on ne supprime pas).

---

## 3. La couche Service : `ReservationServiceImpl.java`

### Créer une réservation — `creerReservation(idTerrain, date, heureDebut, dureeHeures)`

C'est la méthode la plus importante. Elle vérifie **toutes les règles métier** avant d'enregistrer.

**Étapes dans l'ordre :**

```
1. Vérifier que l'utilisateur est connecté
2. Valider les données (ReservationValidator.valider)
3. Vérifier que le terrain existe
4. Vérifier que le terrain est disponible (disponibilite = true)
5. Vérifier l'absence de conflit de créneau (isCreneauDisponible)
6. Calculer le montant : prix/h × dureeHeures
7. Créer l'objet Reservation avec statut CONFIRMEE
8. Appeler reservationDAO.save(r)
```

```java
BigDecimal montant = terrain.getPrixParHeure()
        .multiply(BigDecimal.valueOf(dureeHeures));
```

### Algorithme de détection de conflit — `isCreneauDisponible()`

```java
private boolean isCreneauDisponible(int idTerrain, LocalDate date,
                                     LocalTime heureDebut, int dureeHeures) {
    LocalTime heureFin = heureDebut.plusHours(dureeHeures);

    // Récupère les réservations CONFIRMEE sur ce terrain ce jour-là
    List<Reservation> existantes = reservationDAO.findByTerrainAndDateAndStatut(
            idTerrain, date, StatutReservation.CONFIRMEE);

    // Vérifie le chevauchement avec chacune
    for (Reservation r : existantes) {
        if (OverlapUtils.chevauchement(heureDebut, heureFin, r.getHeureDebut(), r.getHeureFin())) {
            return false;  // conflit trouvé
        }
    }
    return true;  // aucun conflit
}
```

### L'algorithme de chevauchement — `OverlapUtils.java`

```java
// Deux intervalles [A,B] et [C,D] se chevauchent si : A < D ET C < B
public static boolean chevauchement(LocalTime debutA, LocalTime finA,
                                     LocalTime debutB, LocalTime finB) {
    return debutA.isBefore(finB) && debutB.isBefore(finA);
}
```

**Exemples :**
- `[10h, 12h]` et `[11h, 13h]` → chevauchement ✓ (10 < 13 ET 11 < 12)
- `[10h, 12h]` et `[12h, 14h]` → pas de chevauchement ✗ (10 < 14 ET 12 < 12 = faux)
- `[10h, 12h]` et `[8h,  11h]` → chevauchement ✓ (10 < 11 ET 8 < 12)

### Annuler une réservation (client) — `annulerReservation(int idReservation)`

**Vérifications :**
1. L'utilisateur est connecté
2. La réservation appartient bien à cet utilisateur → sinon `UnauthorizedException`
3. La réservation est annulable (voir `verifierAnnulable`)

### Annuler une réservation (admin) — `annulerReservationAdmin(int idReservation)`

Même chose mais sans vérification d'appartenance (l'admin peut annuler n'importe quelle réservation). Vérifie uniquement que l'utilisateur est admin.

### La règle d'annulation — `verifierAnnulable(Reservation r)`

```java
// Règle : on peut annuler si la date est dans le futur
// OU si c'est aujourd'hui mais l'heure de début n'est pas encore passée

boolean annulable = r.getDateReservation().isAfter(today)
        || (r.getDateReservation().isEqual(today) && r.getHeureDebut().isAfter(now));
```

- Réservation déjà ANNULEE → `BusinessException`
- Réservation TERMINEE → `BusinessException`
- Créneau passé → `BusinessException`

### Mettre à jour les statuts — `updateReservationsTerminees()`

Cherche toutes les réservations CONFIRMEE dont le créneau est passé et les passe à TERMINEE.

```java
List<Reservation> aTerminer = reservationDAO.findConfirmeesPassees();
for (Reservation r : aTerminer) {
    reservationDAO.updateStatut(r.getIdReservation(), StatutReservation.TERMINEE);
}
```

> Cette méthode est appelée depuis `ConsoleController` mais pas depuis l'interface JavaFX.

### Statistiques — `getStatistiques()`

Retourne une `Map<String, Object>` avec :

| Clé | Valeur |
|---|---|
| `totalReservations` | nombre total de réservations |
| `reservationsConfirmees` | nombre avec statut CONFIRMEE |
| `reservationsTerminees` | nombre avec statut TERMINEE |
| `reservationsAnnulees` | nombre avec statut ANNULEE |
| `chiffreAffaires` | somme des montants (hors ANNULEE) |
| `terrainsUtilises` | nombre de terrains distincts ayant eu une réservation |
| `reservationsParType` | Map type → nombre (ex: "Football" → 5) |

---

## 4. Le validateur : `ReservationValidator.java`

Appelé avant toute création de réservation pour vérifier :

- La date n'est pas dans le passé
- L'heure de début est entre 08h00 et 22h00
- La durée est entre 1 et 4 heures
- L'heure de fin ne dépasse pas 23h00 (limite dans `AppConfig`)

---

## Résumé des méthodes

| Méthode Service | Ce qu'elle fait |
|---|---|
| `creerReservation(...)` | Valide toutes les règles + insère |
| `annulerReservation(id)` | Annulation par le client |
| `annulerReservationAdmin(id)` | Annulation par l'admin |
| `getHistoriqueUtilisateur()` | Réservations du client connecté |
| `getAllReservations()` | Toutes les réservations (admin) |
| `findById(id)` | Une réservation par ID |
| `getReservationsParTerrain(id)` | Réservations d'un terrain |
| `updateReservationsTerminees()` | Passe les créneaux passés à TERMINEE |
| `getStatistiques()` | Données pour le tableau de bord admin |
