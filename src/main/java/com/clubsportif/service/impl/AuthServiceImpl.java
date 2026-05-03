package com.clubsportif.service.impl;

import com.clubsportif.config.SecurityConfig;
import com.clubsportif.dao.impl.UtilisateurDAOImpl;
import com.clubsportif.dao.interfaces.IUtilisateurDAO;
import com.clubsportif.exception.AuthenticationException;
import com.clubsportif.exception.EntityNotFoundException;
import com.clubsportif.exception.UnauthorizedException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.Role;
import com.clubsportif.service.interfaces.IAuthService;
import com.clubsportif.service.validators.EmailValidator;
import com.clubsportif.service.validators.PasswordValidator;

import java.util.List;

/**
 * Gestion de l'authentification, de la session et des profils utilisateurs.
 */
public class AuthServiceImpl implements IAuthService {

    private static final List<String> SEED_EMAILS = List.of(
            "admin@clubsportif.com",
            "jean.dupont@email.com",
            "marie.martin@email.com"
    );

    private final IUtilisateurDAO utilisateurDAO;
    private Utilisateur currentUser;

    public AuthServiceImpl() {
        this.utilisateurDAO = new UtilisateurDAOImpl();
        repairSeedAccountsIfNeeded();
    }

    /* Package-private : injection pour les tests unitaires */
    AuthServiceImpl(IUtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    private void repairSeedAccountsIfNeeded() {
        for (String email : SEED_EMAILS) {
            try {
                utilisateurDAO.findByEmail(email).ifPresent(u -> {
                    String hash = u.getMotDePasseHash();
                    if (hash == null || hash.isBlank() || !hash.startsWith("$2")) {
                        u.setMotDePasseHash(SecurityConfig.hashPassword("Admin1234"));
                        utilisateurDAO.update(u);
                    }
                });
            } catch (Exception ignored) {
                // La réparation est opportuniste : ne pas bloquer le démarrage.
            }
        }
    }

    @Override
    public Utilisateur register(String nom, String email, String motDePasse) {
        // Validation des données
        if (nom == null || nom.trim().length() < 2) {
            throw new ValidationException("Le nom doit contenir au moins 2 caractères.");
        }
        String emailNorm = EmailValidator.normalize(email);
        if (!EmailValidator.isValid(emailNorm)) {
            throw new ValidationException("L'adresse e-mail '" + email + "' est invalide.");
        }
        PasswordValidator.valider(motDePasse);
        if (utilisateurDAO.existsByEmail(emailNorm)) {
            throw new ValidationException("L'e-mail '" + emailNorm + "' est déjà utilisé.");
        }

        Utilisateur u = new Utilisateur();
        u.setNom(nom.trim());
        u.setEmail(emailNorm);
        u.setMotDePasseHash(SecurityConfig.hashPassword(motDePasse));
        u.setRole(Role.CLIENT);
        return utilisateurDAO.save(u);
    }

    @Override
    public Utilisateur login(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            throw new AuthenticationException("E-mail et mot de passe requis.");
        }
        String emailNorm = EmailValidator.normalize(email);
        Utilisateur u = utilisateurDAO.findByEmail(emailNorm)
                .orElseThrow(() -> new AuthenticationException(
                        "Aucun compte trouvé pour l'e-mail '" + emailNorm + "'."));

        if (!SecurityConfig.checkPassword(motDePasse, u.getMotDePasseHash())) {
            throw new AuthenticationException("Mot de passe incorrect.");
        }
        this.currentUser = u;
        return u;
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    @Override
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    @Override
    public void updateProfil(String nom, String email, String nouveauMotDePasse) {
        if (currentUser == null) {
            throw new AuthenticationException("Vous devez être connecté pour modifier votre profil.");
        }
        if (nom != null && !nom.isBlank()) {
            if (nom.trim().length() < 2) {
                throw new ValidationException("Le nom doit contenir au moins 2 caractères.");
            }
            currentUser.setNom(nom.trim());
        }
        if (email != null && !email.isBlank()) {
            String emailNorm = EmailValidator.normalize(email);
            if (!EmailValidator.isValid(emailNorm)) {
                throw new ValidationException("L'adresse e-mail '" + email + "' est invalide.");
            }
            if (!emailNorm.equals(currentUser.getEmail()) && utilisateurDAO.existsByEmail(emailNorm)) {
                throw new ValidationException("L'e-mail '" + emailNorm + "' est déjà utilisé.");
            }
            currentUser.setEmail(emailNorm);
        }
        if (nouveauMotDePasse != null && !nouveauMotDePasse.isBlank()) {
            PasswordValidator.valider(nouveauMotDePasse);
            currentUser.setMotDePasseHash(SecurityConfig.hashPassword(nouveauMotDePasse));
        }
        utilisateurDAO.update(currentUser);
    }

    @Override
    public List<Utilisateur> findAllUtilisateurs() {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new UnauthorizedException("Action réservée à l'administrateur.");
        }
        return utilisateurDAO.findAll();
    }

    @Override
    public void deleteAccount(int idUtilisateur) {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new UnauthorizedException("Seul un administrateur peut supprimer un compte.");
        }
        if (currentUser.getIdUtilisateur() == idUtilisateur) {
            throw new ValidationException("Un administrateur ne peut pas supprimer son propre compte.");
        }
        Utilisateur cible = utilisateurDAO.findById(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur #" + idUtilisateur + " introuvable."));
        if (cible.isAdmin()) {
            throw new UnauthorizedException("Impossible de supprimer un autre compte administrateur.");
        }
        utilisateurDAO.delete(idUtilisateur);
    }
}
