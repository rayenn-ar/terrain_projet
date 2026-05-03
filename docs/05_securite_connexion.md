# Sécurité et connexion à la base de données

## Les fichiers concernés

```
config/AppConfig.java
config/DatabaseConfig.java
config/SecurityConfig.java
service/validators/EmailValidator.java
service/validators/PasswordValidator.java
```

---

## 1. Configuration de la base de données : `AppConfig.java`

Toutes les constantes sont centralisées ici. C'est le seul fichier à modifier pour adapter l'application à une nouvelle machine.

```java
public static final String DB_HOST     = "localhost";
public static final int    DB_PORT     = 3306;
public static final String DB_NAME     = "club_sportif";
public static final String DB_USER     = "root";
public static final String DB_PASSWORD = "rayno1234@1234";

// L'URL est construite automatiquement à partir des constantes ci-dessus
public static final String DB_URL =
    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
    + "?useSSL=false"
    + "&serverTimezone=UTC"
    + "&allowPublicKeyRetrieval=true"
    + "&characterEncoding=UTF-8"
    + "&useUnicode=true";
```

**Règles métier configurables :**
```java
public static final int HEURE_OUVERTURE  = 8;   // 08h00
public static final int HEURE_FERMETURE  = 23;  // 23h00
public static final int DUREE_MIN_HEURES = 1;
public static final int DUREE_MAX_HEURES = 4;
public static final int BCRYPT_ROUNDS    = 10;
```

---

## 2. Connexion JDBC : `DatabaseConfig.java`

### Le patron Singleton

Le Singleton garantit qu'il n'y a **qu'une seule connexion** à la base de données dans toute l'application.

```java
private static DatabaseConfig instance;  // l'unique instance

public static synchronized DatabaseConfig getInstance() {
    if (instance == null) {
        instance = new DatabaseConfig();  // créée une seule fois
    }
    return instance;
}
```

`synchronized` évite les problèmes si deux threads appellent `getInstance()` en même temps.

### Comment les DAO utilisent la connexion

Chaque DAO appelle `DatabaseConfig.getInstance().getConnection()` pour obtenir la connexion :

```java
private Connection getConnection() throws SQLException {
    return DatabaseConfig.getInstance().getConnection();
}
```

### Reconnexion automatique

Si la connexion est fermée (timeout MySQL), elle est recréée automatiquement :

```java
public Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        connection = createConnection();  // reconnecte si nécessaire
    }
    return connection;
}
```

### Comment la connexion est créée

```java
private Connection createConnection() throws SQLException {
    return DriverManager.getConnection(
        AppConfig.DB_URL,
        AppConfig.DB_USER,
        AppConfig.DB_PASSWORD
    );
}
```

---

## 3. Hachage des mots de passe : `SecurityConfig.java`

Les mots de passe ne sont **jamais** stockés en clair. On utilise l'algorithme **BCrypt**.

### Hacher un mot de passe

```java
public static String hashPassword(String plaintext) {
    return BCrypt.hashpw(plaintext, BCrypt.gensalt(AppConfig.BCRYPT_ROUNDS));
}
```

- `BCrypt.gensalt(10)` génère un "sel" aléatoire avec 10 rounds
- Plus les rounds sont élevés, plus c'est lent à casser
- Chaque hash est unique même pour le même mot de passe (grâce au sel)

**Exemple :**
```
"Admin1234"  →  "$2a$10$N9qo8uLOickgx2ZMRZoMyeRF3UQwse0jJI0GfFZ89..."
"Admin1234"  →  "$2a$10$X8kp2mLqRtN4yTvW6nHpAO..."  (hash différent !)
```

### Vérifier un mot de passe

```java
public static boolean checkPassword(String plaintext, String hashed) {
    return BCrypt.checkpw(plaintext, hashed);
}
```

BCrypt peut vérifier si le mot de passe correspond au hash **sans le décoder** (c'est impossible). Il re-simule le calcul avec le sel inclus dans le hash et compare.

### Pourquoi BCrypt et pas MD5/SHA1 ?

- MD5 et SHA1 sont trop rapides → faciles à attaquer par "force brute"
- BCrypt est intentionnellement lent → chaque tentative prend du temps
- Le "sel" aléatoire empêche les attaques par tables précalculées (rainbow tables)

---

## 4. Validation des e-mails : `EmailValidator.java`

```java
public static boolean isValid(String email) {
    // Regex vérifiant le format standard d'un e-mail
    return email != null && email.matches(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
}

public static String normalize(String email) {
    // Mise en minuscules + suppression des espaces
    return email == null ? null : email.trim().toLowerCase();
}
```

**Exemples :**
- `"Jean.DUPONT@Gmail.com"` → normalisé en `"jean.dupont@gmail.com"` → valide ✓
- `"pas-un-email"` → invalide ✗
- `"a@b.c"` → valide ✓

---

## 5. Validation des mots de passe : `PasswordValidator.java`

```java
public static void valider(String motDePasse) {
    if (motDePasse == null || motDePasse.length() < 8) {
        throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères.");
    }
    if (!motDePasse.matches(".*[A-Z].*")) {
        throw new ValidationException("Le mot de passe doit contenir au moins une lettre majuscule.");
    }
    if (!motDePasse.matches(".*[0-9].*")) {
        throw new ValidationException("Le mot de passe doit contenir au moins un chiffre.");
    }
}
```

**Règles :**
- Minimum 8 caractères
- Au moins 1 lettre majuscule
- Au moins 1 chiffre

**Exemples :**
- `"Admin1234"` → valide ✓
- `"admin1234"` → invalide ✗ (pas de majuscule)
- `"AdminABCD"` → invalide ✗ (pas de chiffre)
- `"Ad1"` → invalide ✗ (trop court)

---

## 6. Protection contre les injections SQL

Toutes les requêtes SQL utilisent des `PreparedStatement` avec des paramètres `?`.

**Mauvaise façon (JAMAIS faire ça) :**
```java
// DANGEREUX : si email = "' OR 1=1 --", ça retourne tous les utilisateurs !
String sql = "SELECT * FROM utilisateur WHERE email = '" + email + "'";
```

**Bonne façon (ce qu'on fait) :**
```java
// SÛRE : le paramètre est traité comme une chaîne de caractères,
// jamais comme du code SQL
String sql = "SELECT * FROM utilisateur WHERE email = ?";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setString(1, email);  // le "?" est remplacé de façon sécurisée
```

Avec `PreparedStatement`, même si l'utilisateur tape `' OR 1=1 --` dans le champ e-mail, MySQL traite ça comme du texte et non comme du SQL.
