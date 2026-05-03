# Tests unitaires — Documentation du code

## Les fichiers de test

```
src/test/java/com/clubsportif/
├── util/
│   ├── OverlapUtilsTest.java
│   └── DateTimeUtilTest.java
├── model/
│   └── ModelTest.java
├── config/
│   └── SecurityConfigTest.java
└── service/
    ├── impl/
    │   ├── AuthServiceImplTest.java
    │   ├── TerrainServiceImplTest.java
    │   └── ReservationServiceImplTest.java
    └── validators/
        ├── EmailValidatorTest.java
        ├── PasswordValidatorTest.java
        └── ReservationValidatorTest.java
```

---

## Comment lancer les tests

```bash
mvn test
```

---

## 1. Pourquoi des tests unitaires ?

Un test unitaire vérifie qu'une méthode fonctionne correctement. On teste chaque cas possible : le cas normal (ça marche), et les cas d'erreur (exception attendue).

**Avantage :** si on modifie une méthode et qu'un test casse, on sait immédiatement que quelque chose est cassé, sans avoir besoin de lancer toute l'application.

---

## 2. Mockito — Simuler les dépendances

Les tests de la couche Service ont besoin d'une base de données. Mais on ne veut pas dépendre d'une vraie base MySQL pour les tests. On utilise **Mockito** pour créer un "faux DAO" qui retourne des données définies dans le test.

```java
// On crée un faux DAO
IUtilisateurDAO mockDAO = Mockito.mock(IUtilisateurDAO.class);

// On lui dit quoi retourner quand on appelle findByEmail("test@test.com")
Mockito.when(mockDAO.findByEmail("test@test.com"))
       .thenReturn(Optional.of(unUtilisateur));

// On injecte le faux DAO dans le service (via le constructeur package-private)
AuthServiceImpl service = new AuthServiceImpl(mockDAO);
```

Le constructeur utilisé pour les tests (visible uniquement dans le même package) :
```java
// Dans AuthServiceImpl.java :
AuthServiceImpl(IUtilisateurDAO utilisateurDAO) {  // package-private
    this.utilisateurDAO = utilisateurDAO;
    ...
}
```

---

## 3. `OverlapUtilsTest.java`

Teste l'algorithme de détection de chevauchement. C'est le test le plus important car cet algorithme est au cœur de la réservation.

```java
@Test
void deuxCreneauxQui_seChevauchent() {
    // [10h-12h] et [11h-13h] → chevauchement attendu
    assertTrue(OverlapUtils.chevauchement(
        LocalTime.of(10, 0), LocalTime.of(12, 0),
        LocalTime.of(11, 0), LocalTime.of(13, 0)
    ));
}

@Test
void deuxCreneauxQui_neSeChevauchentPas() {
    // [10h-12h] et [12h-14h] → pas de chevauchement (12h = 12h, aucun croisement)
    assertFalse(OverlapUtils.chevauchement(
        LocalTime.of(10, 0), LocalTime.of(12, 0),
        LocalTime.of(12, 0), LocalTime.of(14, 0)
    ));
}

@Test
void creneauInclusDansUnAutre() {
    // [10h-14h] contient [11h-12h] → chevauchement
    assertTrue(OverlapUtils.chevauchement(
        LocalTime.of(10, 0), LocalTime.of(14, 0),
        LocalTime.of(11, 0), LocalTime.of(12, 0)
    ));
}
```

---

## 4. `EmailValidatorTest.java`

```java
@Test
void emailValide() {
    assertTrue(EmailValidator.isValid("jean.dupont@gmail.com"));
    assertTrue(EmailValidator.isValid("test+tag@domain.fr"));
}

@Test
void emailInvalide() {
    assertFalse(EmailValidator.isValid("pasun@email"));  // pas d'extension
    assertFalse(EmailValidator.isValid("@gmail.com"));   // pas de nom
    assertFalse(EmailValidator.isValid(null));
}

@Test
void normalisation() {
    assertEquals("jean@gmail.com", EmailValidator.normalize("  JEAN@Gmail.COM  "));
}
```

---

## 5. `PasswordValidatorTest.java`

```java
@Test
void motDePasseValide() {
    assertDoesNotThrow(() -> PasswordValidator.valider("Admin1234"));
}

@Test
void tropCourt() {
    assertThrows(ValidationException.class, () -> PasswordValidator.valider("Ab1"));
}

@Test
void pasDeMajuscule() {
    assertThrows(ValidationException.class, () -> PasswordValidator.valider("admin1234"));
}

@Test
void pasDeChiffre() {
    assertThrows(ValidationException.class, () -> PasswordValidator.valider("AdminABCD"));
}
```

---

## 6. `SecurityConfigTest.java`

```java
@Test
void hashEtVerification() {
    String hash = SecurityConfig.hashPassword("Admin1234");
    // Le hash n'est jamais en clair
    assertFalse(hash.equals("Admin1234"));
    // Mais checkPassword retrouve la correspondance
    assertTrue(SecurityConfig.checkPassword("Admin1234", hash));
    // Un mauvais mot de passe ne correspond pas
    assertFalse(SecurityConfig.checkPassword("mauvais", hash));
}

@Test
void deuxHashDiffeents() {
    // Même mot de passe → deux hashes différents (grâce au sel)
    String h1 = SecurityConfig.hashPassword("Admin1234");
    String h2 = SecurityConfig.hashPassword("Admin1234");
    assertNotEquals(h1, h2);
}
```

---

## 7. `AuthServiceImplTest.java`

```java
@Test
void connexionReussie() {
    // Préparation du faux DAO
    IUtilisateurDAO dao = mock(IUtilisateurDAO.class);
    String hash = SecurityConfig.hashPassword("Admin1234");
    Utilisateur user = new Utilisateur(1, "Admin", "admin@test.com", hash, Role.ADMIN);
    when(dao.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

    AuthServiceImpl service = new AuthServiceImpl(dao);
    Utilisateur result = service.login("admin@test.com", "Admin1234");

    // Vérifications
    assertNotNull(result);
    assertTrue(service.isLoggedIn());
    assertTrue(service.isAdmin());
}

@Test
void connexionMauvaisMdp() {
    // Le même faux DAO
    // ...
    assertThrows(AuthenticationException.class,
        () -> service.login("admin@test.com", "mauvaisMdp")
    );
}

@Test
void inscriptionEmailDejaPris() {
    when(dao.existsByEmail("pris@test.com")).thenReturn(true);
    assertThrows(ValidationException.class,
        () -> service.register("Nom", "pris@test.com", "Admin1234")
    );
}
```

---

## 8. `ReservationServiceImplTest.java`

```java
@Test
void creerReservation_sansCconflit() {
    // Le DAO retourne une liste vide = pas de réservations existantes
    when(reservationDAO.findByTerrainAndDateAndStatut(1, date, CONFIRMEE))
        .thenReturn(Collections.emptyList());
    when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));

    Reservation r = service.creerReservation(1, date, LocalTime.of(10, 0), 2);
    assertNotNull(r);
    assertEquals(StatutReservation.CONFIRMEE, r.getStatut());
}

@Test
void creerReservation_avecConflit() {
    // Le DAO retourne une réservation existante de 10h à 12h
    Reservation existante = new Reservation();
    existante.setHeureDebut(LocalTime.of(10, 0));
    existante.setDureeHeures(2);  // finit à 12h

    when(reservationDAO.findByTerrainAndDateAndStatut(1, date, CONFIRMEE))
        .thenReturn(List.of(existante));

    // Essayer de réserver de 11h à 13h → conflit !
    assertThrows(ReservationConflictException.class,
        () -> service.creerReservation(1, date, LocalTime.of(11, 0), 2)
    );
}
```

---

## Structure d'un test (modèle)

```java
@Test
void nomDuTest_ceQueTesteCeTest() {
    // 1. ARRANGE — préparer les données
    Utilisateur user = new Utilisateur(...);

    // 2. ACT — appeler la méthode à tester
    boolean result = user.isAdmin();

    // 3. ASSERT — vérifier le résultat
    assertTrue(result);
}
```

Cette structure s'appelle **AAA : Arrange, Act, Assert**.
