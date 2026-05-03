package com.clubsportif.controller;

import com.clubsportif.exception.AuthenticationException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.service.interfaces.IAuthService;
import com.clubsportif.util.ConsoleColors;
import com.clubsportif.util.ScannerSingleton;
import com.clubsportif.view.console.ConsoleView;
import com.clubsportif.view.console.MenuBuilder;

/**
 * Contrôleur d'authentification : connexion, inscription, profil.
 */
public class AuthController {

    private final IAuthService authService;
    private final ScannerSingleton scanner = ScannerSingleton.getInstance();

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    /** Affiche le formulaire de connexion et tente l'authentification. */
    public boolean connexion() {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Connexion ──" + ConsoleColors.RESET);
        String email = scanner.nextLine("  Email       : ");
        // Masquage de mot de passe limité (console Java standard)
        String mdp   = scanner.nextLine("  Mot de passe: ");
        try {
            Utilisateur u = authService.login(email, mdp);
            ConsoleView.succes("Bienvenue, " + u.getNom() + " (" + u.getRole().getLibelle() + ") !");
            return true;
        } catch (AuthenticationException e) {
            ConsoleView.erreur(e.getMessage());
            return false;
        }
    }

    /** Affiche le formulaire d'inscription client. */
    public void inscription() {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Créer un compte ──" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  (Tous les champs sont obligatoires)" + ConsoleColors.RESET);

        String nom   = scanner.nextLine("  Nom complet  : ");
        String email = scanner.nextLine("  Email        : ");
        String mdp   = scanner.nextLine("  Mot de passe : ");
        String mdp2  = scanner.nextLine("  Confirmer MDP: ");

        if (!mdp.equals(mdp2)) {
            ConsoleView.erreur("Les mots de passe ne correspondent pas.");
            return;
        }
        try {
            Utilisateur u = authService.register(nom, email, mdp);
            ConsoleView.succes("Compte créé avec succès pour " + u.getNom() + " !");
            ConsoleView.info("Vous pouvez maintenant vous connecter.");
        } catch (ValidationException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    /** Affiche et gère le menu de modification du profil. */
    public void gererProfil() {
        Utilisateur user = authService.getCurrentUser();
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Mon Profil ──" + ConsoleColors.RESET);
        ConsoleView.info("Nom actuel   : " + user.getNom());
        ConsoleView.info("Email actuel : " + user.getEmail());
        System.out.println();
        ConsoleView.info("Appuyez sur Entrée pour conserver la valeur actuelle.");

        String nom   = scanner.nextLine("  Nouveau nom    (actuel: " + user.getNom()   + ") : ");
        String email = scanner.nextLine("  Nouveau email  (actuel: " + user.getEmail() + ") : ");
        String mdp   = scanner.nextLine("  Nouveau MDP    (laisser vide = inchangé)      : ");

        // Transmettre null si inchangé
        String newNom   = nom.isBlank()   ? null : nom;
        String newEmail = email.isBlank() ? null : email;
        String newMdp   = mdp.isBlank()   ? null : mdp;

        try {
            authService.updateProfil(newNom, newEmail, newMdp);
            ConsoleView.succes("Profil mis à jour avec succès !");
        } catch (ValidationException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    /** Déconnecte l'utilisateur courant. */
    public void deconnexion() {
        String nom = authService.getCurrentUser().getNom();
        authService.logout();
        ConsoleView.succes("Au revoir, " + nom + " ! À bientôt.");
    }

    /** ---- Section admin : gestion des utilisateurs ---- */
    public void gererUtilisateurs() {
        new MenuBuilder()
            .titre("GESTION DES UTILISATEURS")
            .option(1, "👥", "Lister tous les utilisateurs")
            .option(2, "🗑", "Supprimer un compte client")
            .separateur()
            .option(0, "↩", "Retour")
            .afficher();

        MenuBuilder.promptChoix();
        int choix = scanner.nextInt();

        switch (choix) {
            case 1 -> afficherTousLesUtilisateurs();
            case 2 -> supprimerCompte();
            case 0 -> { /* retour */ }
            default -> ConsoleView.avertissement("Choix invalide.");
        }
    }

    private void afficherTousLesUtilisateurs() {
        try {
            ConsoleView.afficherUtilisateurs(authService.findAllUtilisateurs());
        } catch (Exception e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void supprimerCompte() {
        afficherTousLesUtilisateurs();
        int id = scanner.nextInt("  ID utilisateur à supprimer (0 = annuler) : ");
        if (id == 0) return;

        System.out.print(ConsoleColors.YELLOW_BOLD
            + "  Confirmer la suppression du compte #" + id + " ? (O/N) : "
            + ConsoleColors.RESET);
        String conf = scanner.nextLine();
        if (!conf.equalsIgnoreCase("O") && !conf.equalsIgnoreCase("oui")) {
            ConsoleView.info("Suppression annulée.");
            return;
        }
        try {
            authService.deleteAccount(id);
            ConsoleView.succes("Compte #" + id + " supprimé avec succès.");
        } catch (Exception e) {
            ConsoleView.erreur(e.getMessage());
        }
    }
}
