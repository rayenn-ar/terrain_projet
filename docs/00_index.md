# Documentation du code source — SportsPro

Ce dossier contient la documentation de chaque fonctionnalité du projet.

## Fichiers disponibles

| Fichier | Ce qu'il explique |
|---|---|
| [01_architecture.md](01_architecture.md) | Comment le projet est organisé (couches, packages, rôles) |
| [02_crud_utilisateurs.md](02_crud_utilisateurs.md) | Inscription, connexion, modification de profil, suppression de compte |
| [03_crud_terrains.md](03_crud_terrains.md) | Ajouter, modifier, supprimer, activer/désactiver un terrain |
| [04_crud_reservations.md](04_crud_reservations.md) | Créer, annuler, lister des réservations + algorithme de conflit + statistiques |
| [05_securite_connexion.md](05_securite_connexion.md) | BCrypt, connexion MySQL (Singleton), validation e-mail/mot de passe, injection SQL |
| [06_interface_javafx.md](06_interface_javafx.md) | Comment fonctionne l'interface : navigation, tableaux, filtres, export CSV |
| [07_tests.md](07_tests.md) | Comment les tests unitaires sont écrits et comment Mockito est utilisé |

## Par où commencer ?

1. Commence par **[01_architecture.md](01_architecture.md)** pour comprendre la structure globale
2. Lis ensuite **[05_securite_connexion.md](05_securite_connexion.md)** pour comprendre DatabaseConfig et BCrypt
3. Lis les fichiers CRUD dans l'ordre (02 → 03 → 04)
4. Lis **[06_interface_javafx.md](06_interface_javafx.md)** pour comprendre l'UI
5. Lis **[07_tests.md](07_tests.md)** en dernier pour comprendre les tests
