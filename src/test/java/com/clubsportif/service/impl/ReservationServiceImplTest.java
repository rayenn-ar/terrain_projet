package com.clubsportif.service.impl;

import com.clubsportif.dao.interfaces.IReservationDAO;
import com.clubsportif.dao.interfaces.ITerrainDAO;
import com.clubsportif.exception.*;
import com.clubsportif.model.Reservation;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.Role;
import com.clubsportif.model.enums.StatutReservation;
import com.clubsportif.model.enums.TypeTerrain;
import com.clubsportif.service.interfaces.IAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests du service de réservation.
 * Couvre les scénarios SC-RES-01 à SC-RES-31.
 */
@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private IReservationDAO reservationDAO;
    @Mock
    private ITerrainDAO terrainDAO;
    @Mock
    private IAuthService authService;

    private ReservationServiceImpl reservationService;

    private Utilisateur clientUser;
    private Utilisateur adminUser;
    private Terrain terrain;
    private final LocalDate futureDate = LocalDate.now().plusDays(7);

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(authService, reservationDAO, terrainDAO);
        clientUser = new Utilisateur(1, "Jean", "jean@test.com", "hash", Role.CLIENT, null);
        adminUser = new Utilisateur(2, "Admin", "admin@test.com", "hash", Role.ADMIN, null);
        terrain = new Terrain(10, "Terrain A", TypeTerrain.FOOTBALL,
                BigDecimal.valueOf(50), true, "desc");
    }

    // ═══════════════════════════════════════════════
    // CRÉATION DE RÉSERVATION
    // ═══════════════════════════════════════════════

    @SuppressWarnings("unused")
    @Nested
    @DisplayName("Création de réservation")
    class CreerReservationTests {

        @Test
        @DisplayName("SC-RES-01 : Création valide → réservation CONFIRMEE avec montant calculé")
        void creerReservation_valid() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);
            when(terrainDAO.findById(10)).thenReturn(Optional.of(terrain));
            when(reservationDAO.findByTerrainAndDateAndStatut(eq(10), eq(futureDate),
                    eq(StatutReservation.CONFIRMEE))).thenReturn(Collections.emptyList());
            when(reservationDAO.save(any())).thenAnswer(inv -> {
                Reservation r = inv.getArgument(0);
                r.setIdReservation(100);
                return r;
            });

            Reservation result = reservationService.creerReservation(
                    10, futureDate, LocalTime.of(10, 0), 2);

            assertEquals(StatutReservation.CONFIRMEE, result.getStatut());
            assertEquals(BigDecimal.valueOf(100), result.getMontantTotal()); // 50 * 2
            assertEquals(1, result.getIdUtilisateur());
        }

        @Test
        @DisplayName("SC-RES-02 : Création sans être connecté → AuthenticationException")
        void creerReservation_notLoggedIn() {
            when(authService.isLoggedIn()).thenReturn(false);
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(10, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-03 : Création avec date passée → ValidationException")
        void creerReservation_pastDate() {
            when(authService.isLoggedIn()).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> reservationService.creerReservation(10,
                            LocalDate.now().minusDays(1), LocalTime.of(10, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-04 : Heure avant ouverture → ValidationException")
        void creerReservation_beforeOpening() {
            when(authService.isLoggedIn()).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(6, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-05 : Fin après fermeture → ValidationException")
        void creerReservation_afterClosing() {
            when(authService.isLoggedIn()).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(22, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-06 : Durée < minimum → ValidationException")
        void creerReservation_durationTooShort() {
            when(authService.isLoggedIn()).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(10, 0), 0)));
        }

        @Test
        @DisplayName("SC-RES-07 : Durée > maximum → ValidationException")
        void creerReservation_durationTooLong() {
            when(authService.isLoggedIn()).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(10, 0), 5)));
        }

        @Test
        @DisplayName("SC-RES-08 : Terrain inexistant → EntityNotFoundException")
        void creerReservation_terrainNotFound() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(terrainDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> reservationService.creerReservation(999, futureDate, LocalTime.of(10, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-09 : Terrain indisponible → BusinessException")
        void creerReservation_terrainUnavailable() {
            when(authService.isLoggedIn()).thenReturn(true);
            terrain.setDisponibilite(false);
            when(terrainDAO.findById(10)).thenReturn(Optional.of(terrain));
            assertNotNull(assertThrows(BusinessException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(10, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-10 : Chevauchement horaire → ReservationConflictException")
        void creerReservation_overlap() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(terrainDAO.findById(10)).thenReturn(Optional.of(terrain));

            Reservation existing = new Reservation(1, 2, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findByTerrainAndDateAndStatut(eq(10), eq(futureDate),
                    eq(StatutReservation.CONFIRMEE))).thenReturn(List.of(existing));

            assertNotNull(assertThrows(ReservationConflictException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(11, 0), 2)));
        }

        @Test
        @DisplayName("SC-RES-11 : Créneau adjacent (pas de chevauchement) → succès")
        void creerReservation_adjacent_noOverlap() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);
            when(terrainDAO.findById(10)).thenReturn(Optional.of(terrain));

            Reservation existing = new Reservation(1, 2, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findByTerrainAndDateAndStatut(eq(10), eq(futureDate),
                    eq(StatutReservation.CONFIRMEE))).thenReturn(List.of(existing));
            when(reservationDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // 12h-14h ne chevauche pas 10h-12h
            Reservation result = reservationService.creerReservation(
                    10, futureDate, LocalTime.of(12, 0), 2);
            assertNotNull(result);
        }

        @Test
        @DisplayName("SC-RES-12 : Chevauchement partiel → ReservationConflictException")
        void creerReservation_partialOverlap() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(terrainDAO.findById(10)).thenReturn(Optional.of(terrain));

            Reservation existing = new Reservation(1, 2, 10, futureDate,
                    LocalTime.of(14, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findByTerrainAndDateAndStatut(eq(10), eq(futureDate),
                    eq(StatutReservation.CONFIRMEE))).thenReturn(List.of(existing));

            // 13h-15h chevauche 14h-16h
            assertNotNull(assertThrows(ReservationConflictException.class,
                    () -> reservationService.creerReservation(10, futureDate, LocalTime.of(13, 0), 2)));
        }
    }

    // ═══════════════════════════════════════════════
    // ANNULATION PAR LE CLIENT
    // ═══════════════════════════════════════════════

    @SuppressWarnings("unused")
    @Nested
    @DisplayName("Annulation par le client")
    class AnnulerReservationTests {

        @Test
        @DisplayName("SC-RES-13 : Annulation réservation future propre → succès")
        void annulerReservation_valid() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);

            Reservation r = new Reservation(50, 1, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));
            when(reservationDAO.updateStatut(50, StatutReservation.ANNULEE)).thenReturn(true);

            assertDoesNotThrow(() -> reservationService.annulerReservation(50));
            verify(reservationDAO).updateStatut(50, StatutReservation.ANNULEE);
        }

        @Test
        @DisplayName("SC-RES-14 : Annulation sans être connecté → AuthenticationException")
        void annulerReservation_notLoggedIn() {
            when(authService.isLoggedIn()).thenReturn(false);
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> reservationService.annulerReservation(50)));
        }

        @Test
        @DisplayName("SC-RES-15 : Annulation réservation d'un autre → UnauthorizedException")
        void annulerReservation_otherUser() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);

            Reservation r = new Reservation(50, 999, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));

            assertNotNull(assertThrows(UnauthorizedException.class,
                    () -> reservationService.annulerReservation(50)));
        }

        @Test
        @DisplayName("SC-RES-16 : Annulation réservation déjà annulée → BusinessException")
        void annulerReservation_alreadyCancelled() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);

            Reservation r = new Reservation(50, 1, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.ANNULEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));

            assertNotNull(assertThrows(BusinessException.class,
                    () -> reservationService.annulerReservation(50)));
        }

        @Test
        @DisplayName("SC-RES-17 : Annulation réservation terminée → BusinessException")
        void annulerReservation_terminated() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);

            Reservation r = new Reservation(50, 1, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.TERMINEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));

            assertNotNull(assertThrows(BusinessException.class,
                    () -> reservationService.annulerReservation(50)));
        }

        @Test
        @DisplayName("SC-RES-18 : Annulation créneau passé → BusinessException")
        void annulerReservation_pastSlot() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);

            Reservation r = new Reservation(50, 1, 10, LocalDate.now().minusDays(1),
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));

            assertNotNull(assertThrows(BusinessException.class,
                    () -> reservationService.annulerReservation(50)));
        }

        @Test
        @DisplayName("SC-RES-19 : Annulation réservation inexistante → EntityNotFoundException")
        void annulerReservation_notFound() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(reservationDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> reservationService.annulerReservation(999)));
        }
    }

    // ═══════════════════════════════════════════════
    // ANNULATION PAR L'ADMIN
    // ═══════════════════════════════════════════════

    @SuppressWarnings("unused")
    @Nested
    @DisplayName("Annulation par l'admin")
    class AnnulerReservationAdminTests {

        @Test
        @DisplayName("SC-RES-20 : Admin annule une réservation future → succès")
        void annulerAdmin_valid() {
            when(authService.isAdmin()).thenReturn(true);
            Reservation r = new Reservation(50, 1, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));
            when(reservationDAO.updateStatut(50, StatutReservation.ANNULEE)).thenReturn(true);

            assertDoesNotThrow(() -> reservationService.annulerReservationAdmin(50));
        }

        @Test
        @DisplayName("SC-RES-21 : Non-admin tente annulation admin → UnauthorizedException")
        void annulerAdmin_notAdmin() {
            when(authService.isAdmin()).thenReturn(false);
            assertNotNull(assertThrows(UnauthorizedException.class,
                    () -> reservationService.annulerReservationAdmin(50)));
        }

        @Test
        @DisplayName("SC-RES-22 : Admin annule réservation inexistante → EntityNotFoundException")
        void annulerAdmin_notFound() {
            when(authService.isAdmin()).thenReturn(true);
            when(reservationDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> reservationService.annulerReservationAdmin(999)));
        }

        @Test
        @DisplayName("SC-RES-23 : Admin annule réservation terminée → BusinessException")
        void annulerAdmin_terminated() {
            when(authService.isAdmin()).thenReturn(true);
            Reservation r = new Reservation(50, 1, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.TERMINEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));
            assertNotNull(assertThrows(BusinessException.class,
                    () -> reservationService.annulerReservationAdmin(50)));
        }
    }

    // ═══════════════════════════════════════════════
    // HISTORIQUE ET LISTES
    // ═══════════════════════════════════════════════

    @SuppressWarnings("unused")
    @Nested
    @DisplayName("Historique et listes")
    class HistoriqueTests {

        @Test
        @DisplayName("SC-RES-24 : Historique utilisateur connecté → liste retournée")
        void getHistorique_loggedIn() {
            when(authService.isLoggedIn()).thenReturn(true);
            when(authService.getCurrentUser()).thenReturn(clientUser);
            when(reservationDAO.findByUtilisateur(1)).thenReturn(List.of(
                    new Reservation(1, 1, 10, futureDate, LocalTime.of(10, 0),
                            2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100))));

            List<Reservation> result = reservationService.getHistoriqueUtilisateur();
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("SC-RES-25 : Historique sans être connecté → AuthenticationException")
        void getHistorique_notLoggedIn() {
            when(authService.isLoggedIn()).thenReturn(false);
            assertNotNull(assertThrows(AuthenticationException.class,
                    () -> reservationService.getHistoriqueUtilisateur()));
        }

        @Test
        @DisplayName("SC-RES-26 : Toutes les réservations par admin → liste retournée")
        void getAllReservations_admin() {
            when(authService.isAdmin()).thenReturn(true);
            when(reservationDAO.findAll()).thenReturn(List.of(
                    new Reservation(1, 1, 10, futureDate, LocalTime.of(10, 0),
                            2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100))));

            List<Reservation> result = reservationService.getAllReservations();
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("SC-RES-27 : Toutes les réservations par non-admin → UnauthorizedException")
        void getAllReservations_notAdmin() {
            when(authService.isAdmin()).thenReturn(false);
            assertNotNull(assertThrows(UnauthorizedException.class,
                    () -> reservationService.getAllReservations()));
        }

        @Test
        @DisplayName("SC-RES-28 : Recherche par ID existant → réservation retournée")
        void findById_existing() {
            Reservation r = new Reservation(50, 1, 10, futureDate,
                    LocalTime.of(10, 0), 2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100));
            when(reservationDAO.findById(50)).thenReturn(Optional.of(r));
            assertEquals(50, reservationService.findById(50).getIdReservation());
        }

        @Test
        @DisplayName("SC-RES-29 : Recherche par ID inexistant → EntityNotFoundException")
        void findById_notFound() {
            when(reservationDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> reservationService.findById(999)));
        }
    }

    // ═══════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════

    @SuppressWarnings("unused")
    @Nested
    @DisplayName("Statistiques")
    class StatistiquesTests {

        @Test
        @DisplayName("SC-RES-30 : Statistiques avec réservations mixtes → comptages corrects")
        void getStatistiques_mixed() {
            List<Reservation> all = List.of(
                    new Reservation(1, 1, 10, futureDate, LocalTime.of(10, 0),
                            2, StatutReservation.CONFIRMEE, BigDecimal.valueOf(100)),
                    new Reservation(2, 1, 10, futureDate, LocalTime.of(14, 0),
                            1, StatutReservation.TERMINEE, BigDecimal.valueOf(50)),
                    new Reservation(3, 2, 20, futureDate, LocalTime.of(16, 0),
                            2, StatutReservation.ANNULEE, BigDecimal.valueOf(80))
            );
            when(reservationDAO.findAll()).thenReturn(all);

            Map<String, Object> stats = reservationService.getStatistiques();
            assertEquals(3L, stats.get("totalReservations"));
            assertEquals(1L, stats.get("reservationsConfirmees"));
            assertEquals(1L, stats.get("reservationsTerminees"));
            assertEquals(1L, stats.get("reservationsAnnulees"));
            // CA exclut les annulées: 100 + 50 = 150
            assertEquals(BigDecimal.valueOf(150), stats.get("chiffreAffaires"));
            assertEquals(2L, stats.get("terrainsUtilises"));
        }

        @Test
        @DisplayName("SC-RES-31 : Statistiques sans réservations → valeurs à zéro")
        void getStatistiques_empty() {
            when(reservationDAO.findAll()).thenReturn(Collections.emptyList());

            Map<String, Object> stats = reservationService.getStatistiques();
            assertEquals(0L, stats.get("totalReservations"));
            assertEquals(0L, stats.get("reservationsConfirmees"));
            assertEquals(BigDecimal.ZERO, stats.get("chiffreAffaires"));
        }
    }
}
