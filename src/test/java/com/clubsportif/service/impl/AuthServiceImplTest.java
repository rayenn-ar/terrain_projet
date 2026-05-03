package com.clubsportif.service.impl;

import com.clubsportif.config.SecurityConfig;
import com.clubsportif.dao.interfaces.IUtilisateurDAO;
import com.clubsportif.exception.AuthenticationException;
import com.clubsportif.exception.EntityNotFoundException;
import com.clubsportif.exception.UnauthorizedException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests du service d'authentification.
 * Couvre les scénarios SC-AUTH-01 à SC-AUTH-38.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class AuthServiceImplTest {

    @Mock
    private IUtilisateurDAO utilisateurDAO;

    private AuthServiceImpl authService;

    private Utilisateur clientUser;
    private Utilisateur adminUser;
    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(utilisateurDAO);
        clientUser = new Utilisateur(1, "Jean Dupont", "jean@test.com",
                SecurityConfig.hashPassword("Test@1234"), Role.CLIENT, LocalDateTime.now());
        adminUser = new Utilisateur(2, "Admin", "admin@test.com",
                SecurityConfig.hashPassword("Admin@1234"), Role.ADMIN, LocalDateTime.now());
    }

    // ═══════════════════════════════════════════════
    // INSCRIPTION (register)
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Inscription")
    class RegisterTests {

        @Test
        @DisplayName("SC-AUTH-01 : Inscription valide → utilisateur créé avec rôle CLIENT")
        void register_valid() {
            when(utilisateurDAO.existsByEmail("nouveau@test.com")).thenReturn(false);
            when(utilisateurDAO.save(any(Utilisateur.class))).thenAnswer(inv -> {
                Utilisateur u = inv.getArgument(0);
                u.setIdUtilisateur(99);
                return u;
            });

            Utilisateur result = authService.register("Nouveau User", "nouveau@test.com", "Password1");
            assertEquals(Role.CLIENT, result.getRole());
            assertEquals("Nouveau User", result.getNom());
            verify(utilisateurDAO).save(any());
        }

        @Test
        @DisplayName("SC-AUTH-02 : Inscription avec nom trop court → ValidationException")
        void register_shortName() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register("A", "a@test.com", "Password1")));
        }

        @Test
        @DisplayName("SC-AUTH-03 : Inscription avec nom null → ValidationException")
        void register_nullName() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register(null, "a@test.com", "Password1")));
        }

        @Test
        @DisplayName("SC-AUTH-04 : Inscription avec email invalide → ValidationException")
        void register_invalidEmail() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register("User", "invalid-email", "Password1")));
        }

        @Test
        @DisplayName("SC-AUTH-05 : Inscription avec mot de passe trop court → ValidationException")
        void register_shortPassword() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register("User", "u@test.com", "Ab1")));
        }

        @Test
        @DisplayName("SC-AUTH-06 : Inscription avec mot de passe sans majuscule → ValidationException")
        void register_noUppercase() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register("User", "u@test.com", "nouppercase1")));
        }

        @Test
        @DisplayName("SC-AUTH-07 : Inscription avec mot de passe sans chiffre → ValidationException")
        void register_noDigit() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register("User", "u@test.com", "NoDigitHere")));
        }

        @Test
        @DisplayName("SC-AUTH-08 : Inscription avec email déjà utilisé → ValidationException")
        void register_duplicateEmail() {
            when(utilisateurDAO.existsByEmail("jean@test.com")).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.register("User", "jean@test.com", "Password1")));
        }

        @Test
        @DisplayName("SC-AUTH-09 : Inscription normalise l'email (trim + lowercase)")
        void register_emailNormalized() {
            when(utilisateurDAO.existsByEmail("test@test.com")).thenReturn(false);
            when(utilisateurDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Utilisateur result = authService.register("User", "  TEST@Test.COM  ", "Password1");
            assertEquals("test@test.com", result.getEmail());
        }
    }

    // ═══════════════════════════════════════════════
    // CONNEXION (login)
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Connexion")
    class LoginTests {

        @Test
        @DisplayName("SC-AUTH-10 : Connexion avec identifiants valides → session active")
        void login_valid() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));

            Utilisateur result = authService.login("jean@test.com", "Test@1234");
            assertNotNull(result);
            assertEquals("Jean Dupont", result.getNom());
            assertTrue(authService.isLoggedIn());
            assertEquals(result, authService.getCurrentUser());
        }

        @Test
        @DisplayName("SC-AUTH-11 : Connexion avec email null → AuthenticationException")
        void login_nullEmail() {
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> authService.login(null, "Test@1234")));
        }

        @Test
        @DisplayName("SC-AUTH-12 : Connexion avec mot de passe null → AuthenticationException")
        void login_nullPassword() {
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> authService.login("jean@test.com", null)));
        }

        @Test
        @DisplayName("SC-AUTH-13 : Connexion avec email inexistant → AuthenticationException")
        void login_unknownEmail() {
            when(utilisateurDAO.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> authService.login("inconnu@test.com", "Test@1234")));
        }

        @Test
        @DisplayName("SC-AUTH-14 : Connexion avec mot de passe incorrect → AuthenticationException")
        void login_wrongPassword() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> authService.login("jean@test.com", "WrongPass1")));
        }
    }

    // ═══════════════════════════════════════════════
    // DÉCONNEXION & SESSION
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Déconnexion et session")
    class SessionTests {

        @Test
        @DisplayName("SC-AUTH-15 : Déconnexion → session effacée")
        void logout() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            authService.login("jean@test.com", "Test@1234");
            assertTrue(authService.isLoggedIn());

            authService.logout();
            assertNull(authService.getCurrentUser());
            assertFalse(authService.isLoggedIn());
        }

        @Test
        @DisplayName("SC-AUTH-16 : isLoggedIn() retourne false quand non connecté")
        void isLoggedIn_false() {
            assertFalse(authService.isLoggedIn());
        }

        @Test
        @DisplayName("SC-AUTH-17 : isLoggedIn() retourne true après login")
        void isLoggedIn_true() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            authService.login("jean@test.com", "Test@1234");
            assertTrue(authService.isLoggedIn());
        }

        @Test
        @DisplayName("SC-AUTH-18 : isAdmin() retourne true pour ADMIN")
        void isAdmin_true() {
            when(utilisateurDAO.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            authService.login("admin@test.com", "Admin@1234");
            assertTrue(authService.isAdmin());
        }

        @Test
        @DisplayName("SC-AUTH-19 : isAdmin() retourne false pour CLIENT")
        void isAdmin_false() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            authService.login("jean@test.com", "Test@1234");
            assertFalse(authService.isAdmin());
        }

        @Test
        @DisplayName("SC-AUTH-20 : isAdmin() retourne false quand non connecté")
        void isAdmin_notLoggedIn() {
            assertFalse(authService.isAdmin());
        }
    }

    // ═══════════════════════════════════════════════
    // MISE À JOUR PROFIL (updateProfil)
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Mise à jour du profil")
    class UpdateProfilTests {
        @BeforeEach
        void loginClient() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            authService.login("jean@test.com", "Test@1234");
        }

        @Test
        @DisplayName("SC-AUTH-21 : Mise à jour du nom valide")
        void updateProfil_validName() {
            when(utilisateurDAO.update(any())).thenReturn(true);
            authService.updateProfil("Nouveau Nom", null, null);
            assertEquals("Nouveau Nom", authService.getCurrentUser().getNom());
        }

        @Test
        @DisplayName("SC-AUTH-22 : Mise à jour de l'email valide non dupliqué")
        void updateProfil_validEmail() {
            when(utilisateurDAO.existsByEmail("new@test.com")).thenReturn(false);
            when(utilisateurDAO.update(any())).thenReturn(true);
            authService.updateProfil(null, "new@test.com", null);
            assertEquals("new@test.com", authService.getCurrentUser().getEmail());
        }

        @Test
        @DisplayName("SC-AUTH-23 : Mise à jour du mot de passe fort")
        void updateProfil_validPassword() {
            String oldHash = authService.getCurrentUser().getMotDePasseHash();
            when(utilisateurDAO.update(any())).thenReturn(true);
            authService.updateProfil(null, null, "NewPass123");
            assertNotEquals(oldHash, authService.getCurrentUser().getMotDePasseHash());
        }

        @Test
        @DisplayName("SC-AUTH-24 : Mise à jour sans être connecté → AuthenticationException")
        void updateProfil_notLoggedIn() {
            authService.logout();
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> authService.updateProfil("Nom", null, null)));
        }

        @Test
        @DisplayName("SC-AUTH-25 : Mise à jour avec nom trop court → ValidationException")
        void updateProfil_shortName() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.updateProfil("X", null, null)));
        }

        @Test
        @DisplayName("SC-AUTH-26 : Mise à jour avec email invalide → ValidationException")
        void updateProfil_invalidEmail() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.updateProfil(null, "bad-email", null)));
        }

        @Test
        @DisplayName("SC-AUTH-27 : Mise à jour avec email dupliqué → ValidationException")
        void updateProfil_duplicateEmail() {
            when(utilisateurDAO.existsByEmail("other@test.com")).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.updateProfil(null, "other@test.com", null)));
        }

        @Test
        @DisplayName("SC-AUTH-28 : Mise à jour avec mot de passe faible → ValidationException")
        void updateProfil_weakPassword() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.updateProfil(null, null, "weak")));
        }

        @Test
        @DisplayName("SC-AUTH-29 : Mise à jour avec même email → pas d'erreur de doublon")
        void updateProfil_sameEmail() {
            when(utilisateurDAO.update(any())).thenReturn(true);
            // L'email courant est "jean@test.com" — pas besoin de vérifier le doublon
            assertDoesNotThrow(() -> authService.updateProfil(null, "jean@test.com", null));
        }
    }

    // ═══════════════════════════════════════════════
    // SUPPRESSION DE COMPTE (deleteAccount)
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Suppression de compte")
    class DeleteAccountTests {

        @Test
        @DisplayName("SC-AUTH-30 : Suppression d'un client par admin → succès")
        void deleteAccount_adminDeletesClient() {
            when(utilisateurDAO.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            authService.login("admin@test.com", "Admin@1234");

            when(utilisateurDAO.findById(1)).thenReturn(Optional.of(clientUser));
            when(utilisateurDAO.delete(1)).thenReturn(true);

            assertDoesNotThrow(() -> authService.deleteAccount(1));
            verify(utilisateurDAO).delete(1);
        }

        @Test
        @DisplayName("SC-AUTH-31 : Suppression par non-admin → UnauthorizedException")
        void deleteAccount_byNonAdmin() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            authService.login("jean@test.com", "Test@1234");

            assertNotNull(assertThrows(UnauthorizedException.class, () -> authService.deleteAccount(99)));
        }

        @Test
        @DisplayName("SC-AUTH-32 : Suppression sans être connecté → UnauthorizedException")
        void deleteAccount_notLoggedIn() {
            assertNotNull(assertThrows(UnauthorizedException.class, () -> authService.deleteAccount(1)));
        }

        @Test
        @DisplayName("SC-AUTH-33 : Admin supprime son propre compte → ValidationException")
        void deleteAccount_self() {
            when(utilisateurDAO.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            authService.login("admin@test.com", "Admin@1234");

            assertNotNull(assertThrows(ValidationException.class,
                    () -> authService.deleteAccount(adminUser.getIdUtilisateur())));
        }

        @Test
        @DisplayName("SC-AUTH-34 : Admin supprime un autre admin → UnauthorizedException")
        void deleteAccount_anotherAdmin() {
            when(utilisateurDAO.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            authService.login("admin@test.com", "Admin@1234");

            Utilisateur otherAdmin = new Utilisateur(99, "Admin2", "admin2@test.com",
                    "hash", Role.ADMIN, LocalDateTime.now());
            when(utilisateurDAO.findById(99)).thenReturn(Optional.of(otherAdmin));

            assertNotNull(assertThrows(UnauthorizedException.class, () -> authService.deleteAccount(99)));
        }

        @Test
        @DisplayName("SC-AUTH-35 : Suppression utilisateur inexistant → EntityNotFoundException")
        void deleteAccount_notFound() {
            when(utilisateurDAO.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            authService.login("admin@test.com", "Admin@1234");

            when(utilisateurDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class, () -> authService.deleteAccount(999)));
        }
    }

    // ═══════════════════════════════════════════════
    // LISTE DES UTILISATEURS (findAllUtilisateurs)
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Liste des utilisateurs")
    class FindAllUtilisateursTests {

        @Test
        @DisplayName("SC-AUTH-36 : Liste par admin → retourne tous les utilisateurs")
        void findAll_admin() {
            when(utilisateurDAO.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
            authService.login("admin@test.com", "Admin@1234");

            when(utilisateurDAO.findAll()).thenReturn(List.of(adminUser, clientUser));
            List<Utilisateur> result = authService.findAllUtilisateurs();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("SC-AUTH-37 : Liste par non-admin → UnauthorizedException")
        void findAll_nonAdmin() {
            when(utilisateurDAO.findByEmail("jean@test.com")).thenReturn(Optional.of(clientUser));
            authService.login("jean@test.com", "Test@1234");

            assertNotNull(assertThrows(UnauthorizedException.class, () -> authService.findAllUtilisateurs()));
        }

        @Test
        @DisplayName("SC-AUTH-38 : Liste sans être connecté → UnauthorizedException")
        void findAll_notLoggedIn() {
            assertNotNull(assertThrows(UnauthorizedException.class, () -> authService.findAllUtilisateurs()));
        }
    }
}
