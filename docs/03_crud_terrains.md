# CRUD Terrains — Documentation du code

## Les fichiers concernés

```
model/Terrain.java
model/enums/TypeTerrain.java
dao/interfaces/ITerrainDAO.java
dao/impl/TerrainDAOImpl.java
service/interfaces/ITerrainService.java
service/impl/TerrainServiceImpl.java
```

---

## 1. Le modèle : `Terrain.java`

Représente un terrain sportif. Correspond à la table `terrain` en base.

```java
private int idTerrain;
private String nom;
private TypeTerrain type;        // enum : FOOTBALL, TENNIS, BASKETBALL, VOLLEYBALL
private BigDecimal prixParHeure; // BigDecimal pour les calculs de prix précis
private boolean disponibilite;   // true = ouvert aux réservations, false = fermé
private String description;
```

`BigDecimal` est utilisé à la place de `double` pour éviter les erreurs d'arrondi sur les prix.

### L'enum `TypeTerrain`

```java
public enum TypeTerrain {
    FOOTBALL, TENNIS, BASKETBALL, VOLLEYBALL;

    public String getLibelle() { ... }  // retourne le nom lisible (ex: "Football")
}
```

---

## 2. La couche DAO : `TerrainDAOImpl.java`

### La méthode `mapRow`

Convertit une ligne SQL en objet `Terrain` :

```java
private Terrain mapRow(ResultSet rs) throws SQLException {
    Terrain t = new Terrain();
    t.setIdTerrain(rs.getInt("id_terrain"));
    t.setNom(rs.getString("nom"));
    t.setType(TypeTerrain.valueOf(rs.getString("type")));  // String → enum
    t.setPrixParHeure(rs.getBigDecimal("prix_par_heure"));
    t.setDisponibilite(rs.getBoolean("disponibilite"));
    t.setDescription(rs.getString("description"));
    return t;
}
```

### CREATE — `save(Terrain t)`

```java
String sql = "INSERT INTO terrain (nom, type, prix_par_heure, disponibilite, description) "
           + "VALUES (?, ?, ?, ?, ?)";
```

Retourne le terrain avec son ID auto-incrémenté renseigné.

### READ — `findById(int id)`

```java
String sql = "SELECT * FROM terrain WHERE id_terrain = ?";
```

Retourne `Optional<Terrain>` (vide si l'ID n'existe pas).

### READ — `findAll()`

Tous les terrains, triés par type puis par nom.

```java
String sql = "SELECT * FROM terrain ORDER BY type, nom";
```

### READ — `findDisponibles()`

Seulement les terrains avec `disponibilite = TRUE`. Utilisé dans le formulaire de réservation client.

```java
String sql = "SELECT * FROM terrain WHERE disponibilite = TRUE ORDER BY type, nom";
```

### READ — `findByType(TypeTerrain type)`

Filtre par type de terrain.

```java
String sql = "SELECT * FROM terrain WHERE type = ? ORDER BY nom";
```

### UPDATE — `update(Terrain t)`

Met à jour toutes les colonnes d'un terrain.

```java
String sql = "UPDATE terrain SET nom = ?, type = ?, prix_par_heure = ?, "
           + "disponibilite = ?, description = ? WHERE id_terrain = ?";
```

### DELETE — `delete(int id)`

```java
String sql = "DELETE FROM terrain WHERE id_terrain = ?";
```

> **Attention :** Si le terrain a des réservations en base (FK), MySQL bloque la suppression. La couche service gère cette erreur.

### CHECK — `existsByNom(String nom)`

Vérifie si un nom de terrain est déjà utilisé (unicité du nom).

```java
String sql = "SELECT COUNT(*) FROM terrain WHERE nom = ?";
```

---

## 3. La couche Service : `TerrainServiceImpl.java`

### Ajouter un terrain — `ajouterTerrain(nom, type, prix, disponibilite, description)`

**Étapes :**
1. Appelle `validerDonneesTerrain()` — vérifie nom, type, prix, unicité du nom
2. Crée l'objet `Terrain`
3. Appelle `terrainDAO.save(t)`

### Modifier un terrain — `modifierTerrain(id, nom, type, prix, disponibilite, description)`

**Étapes :**
1. Cherche le terrain par ID → sinon `EntityNotFoundException`
2. Appelle `validerDonneesTerrain()` en passant l'ID courant pour exclure ce terrain de la vérification d'unicité du nom
3. Met à jour les champs de l'objet
4. Appelle `terrainDAO.update(t)`

### Supprimer un terrain — `supprimerTerrain(int id)`

**Étapes :**
1. Vérifie que le terrain existe → sinon `EntityNotFoundException`
2. Appelle `terrainDAO.delete(id)` → si retourne `false`, lance `BusinessException` (terrain référencé par des réservations)

### Activer / Désactiver — `toggleDisponibilite(int id)`

Inverse la valeur de `disponibilite` (true → false ou false → true).

```java
terrain.setDisponibilite(!terrain.isDisponibilite());
terrainDAO.update(terrain);
```

C'est le bouton "Désactiver / Activer" dans l'interface admin.

### Lister — `findAll()`, `findDisponibles()`, `findByType(TypeTerrain)`

Délèguent directement au DAO sans logique supplémentaire.

---

## 4. La validation interne : `validerDonneesTerrain()`

Méthode privée appelée avant tout ajout ou modification :

```
nom null ou < 2 caractères   → ValidationException
type null                    → ValidationException
prix null ou <= 0            → ValidationException
nom déjà pris par un autre terrain → ValidationException
```

Pour la modification, `excludeId` permet de ne pas considérer le terrain lui-même comme doublon :

```java
// Si je renomme le terrain ID=5 "Terrain A" en "Terrain A" (même nom)
// → pas d'erreur car c'est le même terrain
```

---

## Résumé des méthodes

| Méthode Service | Ce qu'elle fait | DAO appelé |
|---|---|---|
| `ajouterTerrain(...)` | Valide + insère | `save()` |
| `modifierTerrain(...)` | Valide + met à jour | `update()` |
| `supprimerTerrain(id)` | Vérifie existence + supprime | `delete()` |
| `toggleDisponibilite(id)` | Inverse disponible/indisponible | `update()` |
| `findAll()` | Tous les terrains | `findAll()` |
| `findDisponibles()` | Terrains disponibles uniquement | `findDisponibles()` |
| `findByType(type)` | Filtre par sport | `findByType()` |
| `findById(id)` | Un terrain par ID | `findById()` |
