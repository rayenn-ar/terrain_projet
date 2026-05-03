# CRUD Utilisateurs — Documentation du code

## Les fichiers concernés

```
model/Utilisateur.java
dao/interfaces/IUtilisateurDAO.java
dao/impl/UtilisateurDAOImpl.java
service/interfaces/IAuthService.java
service/impl/AuthServiceImpl.java
```

---

## 1. Le modèle : `Utilisateur.java`

C'est l'objet Java qui représente un utilisateur. Il correspond à la table `utilisateur` en base.

```java
private int idUtilisateur;
private String nom;
private String email;
private String motDePasseHash;   // jamais le mot de passe en clair
private Role role;               // Role.ADMIN ou Role.CLIENT
private LocalDateTime dateInscription;
```

**Méthode utile :**
```java
public boolean isAdmin() {
    return Role.ADMIN.equals(this.role);
}
```

---

## 2. La couche DAO : `UtilisateurDAOImpl.java`

Cette classe fait toutes les requêtes SQL sur la table `utilisateur`. Elle implémente `IUtilisateurDAO`.

### La méthode `mapRow`

Chaque fois qu'on lit une ligne de la base, on appelle `mapRow(ResultSet rs)` pour la convertir en objet `Utilisateur`.

```java
private Utilisateur mapRow(ResultSet rs) throws SQLException {
    Utilisateur u = new Utilisateur();
    u.setIdUtilisateur(rs.getInt("id_utilisateur"));
    u.setNom(rs.getString("nom"));
    u.setEmail(rs.getString("email"));
    u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
    u.setRole(Role.valueOf(rs.getString("role")));  // String → enum
    // ...
    return u;
}
```

### CREATE — `save(Utilisateur u)`

Insère un nouvel utilisateur dans la base et retourne l'objet avec son ID généré.

```java
String sql = "INSERT INTO utilisateur (nom, email, mot_de_passe_hash, role) VALUES (?, ?, ?, ?)";
```

- `Statement.RETURN_GENERATED_KEYS` : demande à MySQL de renvoyer l'ID auto-incrémenté
- `ps.getGeneratedKeys()` : récupère cet ID et le met dans l'objet Java

### READ — `findById(int id)`

Cherche un utilisateur par son ID. Retourne `Optional<Utilisateur>` (peut être vide si l'ID n'existe pas).

```java
String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
```

### READ — `findByEmail(String email)`

Cherche un utilisateur par son e-mail. Utilisé pendant la connexion.

```java
String sql = "SELECT * FROM utilisateur WHERE email = ?";
```

### READ — `findAll()`

Retourne tous les utilisateurs, du plus récent au plus ancien.

```java
String sql = "SELECT * FROM utilisateur ORDER BY date_inscription DESC";
```

### UPDATE — `update(Utilisateur u)`

Met à jour toutes les colonnes d'un utilisateur existant.

```java
String sql = "UPDATE utilisateur SET nom = ?, email = ?, mot_de_passe_hash = ?, role = ? "
           + "WHERE id_utilisateur = ?";
```

Retourne `true` si une ligne a été modifiée, `false` sinon.

### DELETE — `delete(int id)`

Supprime un utilisateur par son ID.

```java
String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
```

### CHECK — `existsByEmail(String email)`

Vérifie si un e-mail est déjà utilisé (avant l'inscription).

```java
String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
```

Retourne `true` si COUNT > 0.

---

## 3. La couche Service : `AuthServiceImpl.java`

La couche Service ajoute la **logique métier** autour des opérations DAO. Elle valide les données avant d'appeler le DAO.

### La session (utilisateur connecté)

La session est stockée en mémoire dans un champ privé :

```java
private Utilisateur currentUser;  // null si personne n'est connecté
```

- `login()` met `currentUser` → l'utilisateur connecté
- `logout()` met `currentUser` → `null`
- `getCurrentUser()` retourne l'utilisateur connecté
- `isLoggedIn()` retourne `true` si `currentUser != null`
- `isAdmin()` retourne `true` si connecté ET rôle == ADMIN

### Inscription — `register(nom, email, motDePasse)`

**Étapes dans l'ordre :**
1. Vérifie que le nom fait au moins 2 caractères → sinon `ValidationException`
2. Normalise l'e-mail (minuscules, trim) via `EmailValidator.normalize()`
3. Vérifie le format de l'e-mail → sinon `ValidationException`
4. Vérifie la complexité du mot de passe (8 car., 1 majuscule, 1 chiffre) → sinon `ValidationException`
5. Vérifie que l'e-mail n'est pas déjà utilisé → sinon `ValidationException`
6. Hash le mot de passe avec BCrypt : `SecurityConfig.hashPassword(motDePasse)`
7. Crée l'objet `Utilisateur` avec `Role.CLIENT`
8. Appelle `utilisateurDAO.save(u)` pour l'enregistrer

```java
Utilisateur u = new Utilisateur();
u.setNom(nom.trim());
u.setEmail(emailNorm);
u.setMotDePasseHash(SecurityConfig.hashPassword(motDePasse));
u.setRole(Role.CLIENT);
return utilisateurDAO.save(u);
```

### Connexion — `login(email, motDePasse)`

**Étapes dans l'ordre :**
1. Normalise l'e-mail
2. Cherche l'utilisateur par e-mail → sinon `AuthenticationException`
3. Vérifie le mot de passe avec BCrypt : `SecurityConfig.checkPassword(motDePasse, hash)` → sinon `AuthenticationException`
4. Met l'utilisateur dans `currentUser`

```java
if (!SecurityConfig.checkPassword(motDePasse, u.getMotDePasseHash())) {
    throw new AuthenticationException("Mot de passe incorrect.");
}
this.currentUser = u;
return u;
```

### Modification de profil — `updateProfil(nom, email, nouveauMotDePasse)`

L'utilisateur doit être connecté. Chaque paramètre est optionnel (null = pas de changement).

- Si `nom` fourni : vérifie ≥ 2 caractères, met à jour `currentUser.nom`
- Si `email` fourni : vérifie format et unicité, met à jour `currentUser.email`
- Si `nouveauMotDePasse` fourni : vérifie complexité, re-hash, met à jour `currentUser.motDePasseHash`
- Appelle `utilisateurDAO.update(currentUser)` pour sauvegarder

### Lister les utilisateurs — `findAllUtilisateurs()`

Réservé à l'admin. Lève `UnauthorizedException` si l'appelant n'est pas admin.

### Supprimer un compte — `deleteAccount(int idUtilisateur)`

**Protections :**
- Réservé à l'admin → sinon `UnauthorizedException`
- L'admin ne peut pas supprimer son propre compte → sinon `ValidationException`
- Ne peut pas supprimer un autre admin → sinon `UnauthorizedException`
- Si l'ID n'existe pas → `EntityNotFoundException`

---

## 4. Réparation automatique des comptes de test

Au démarrage de `AuthServiceImpl`, la méthode `repairSeedAccountsIfNeeded()` est appelée.

Elle vérifie les comptes de test (`admin@clubsportif.com`, `jean.dupont@email.com`, `marie.martin@email.com`). Si leur hash BCrypt est cassé ou invalide (commence par `$2` mais est corrompu), elle le recalcule avec le mot de passe par défaut `Admin1234`.

Cela évite que l'application bloque si la base de données a été recréée avec un mauvais script.

---

## Résumé des exceptions utilisées

| Exception | Quand elle est lancée |
|---|---|
| `ValidationException` | Données invalides (nom trop court, e-mail déjà utilisé...) |
| `AuthenticationException` | E-mail inconnu, mot de passe incorrect, pas connecté |
| `UnauthorizedException` | Action réservée à l'admin |
| `EntityNotFoundException` | L'utilisateur demandé n'existe pas en base |
