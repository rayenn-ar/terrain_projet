package com.clubsportif.service.impl;

import com.clubsportif.dao.impl.ReservationDAOImpl;
import com.clubsportif.dao.impl.TerrainDAOImpl;
import com.clubsportif.dao.interfaces.IReservationDAO;
import com.clubsportif.dao.interfaces.ITerrainDAO;
import com.clubsportif.exception.AuthenticationException;
import com.clubsportif.exception.BusinessException;
import com.clubsportif.exception.EntityNotFoundException;
import com.clubsportif.exception.ReservationConflictException;
import com.clubsportif.exception.UnauthorizedException;
import com.clubsportif.model.Reservation;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.StatutReservation;
import com.clubsportif.service.interfaces.IAuthService;
import com.clubsportif.service.interfaces.IReservationService;
import com.clubsportif.service.validators.ReservationValidator;
import com.clubsportif.util.Logger;
import com.clubsportif.util.OverlapUtils;

import com.clubsportif.model.enums.TypeTerrain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Logique métier des réservations.
 * Contient l'algorithme de détection de chevauchement temporel.
 */
public class ReservationServiceImpl implements IReservationService {

    private final IReservationDAO reservationDAO;
    private final ITerrainDAO terrainDAO;
    private final IAuthService authService;

    public ReservationServiceImpl(IAuthService authService) {
        this.reservationDAO = new ReservationDAOImpl();
        this.terrainDAO     = new TerrainDAOImpl();
        this.authService    = authService;
    }

    /* Package-private : injection pour les tests unitaires */
    ReservationServiceImpl(IAuthService authService, IReservationDAO reservationDAO, ITerrainDAO terrainDAO) {
        this.authService    = authService;
        this.reservationDAO = reservationDAO;
        this.terrainDAO     = terrainDAO;
    }

    /**
     * Crée une réservation après vérification de toutes les règles métier.
     *
     * RÈGLES VÉRIFIÉES :
     *  R1 - Pas de chevauchement horaire sur le terrain
     *  R2 - Utilisateur connecté
     *  R5 - Calcul du prix = PrixParHeure × Durée
     *  R6 - Terrain disponible
     *  R7 - Plage horaire 08h-23h
     */
    @Override
    public Reservation creerReservation(int idTerrain, LocalDate date, LocalTime heureDebut, int dureeHeures) {
        // RÈGLE 2 : vérification session
        if (!authService.isLoggedIn()) {
            throw new AuthenticationException("Vous devez être connecté pour effectuer une réservation.");
        }

        // Validation des données saisies
        ReservationValidator.valider(date, heureDebut, dureeHeures);

        // Vérification existence et disponibilité du terrain (RÈGLES 6)
        Terrain terrain = terrainDAO.findById(idTerrain)
                .orElseThrow(() -> new EntityNotFoundException("Terrain #" + idTerrain + " introuvable."));
        if (!terrain.isDisponibilite()) {
            throw new BusinessException(
                "Le terrain '" + terrain.getNom() + "' est actuellement indisponible pour de nouvelles réservations.");
        }

        // RÈGLE 1 : vérification des chevauchements temporels
        if (!isCreneauDisponible(idTerrain, date, heureDebut, dureeHeures)) {
            throw new ReservationConflictException(
                "Ce créneau est déjà réservé pour le terrain '" + terrain.getNom()
                + "' le " + date + " de " + heureDebut + " à " + heureDebut.plusHours(dureeHeures) + ".");
        }

        // RÈGLE 5 : calcul du montant
        BigDecimal montant = terrain.getPrixParHeure()
                .multiply(BigDecimal.valueOf(dureeHeures));

        Reservation r = new Reservation();
        r.setIdUtilisateur(authService.getCurrentUser().getIdUtilisateur());
        r.setIdTerrain(idTerrain);
        r.setDateReservation(date);
        r.setHeureDebut(heureDebut);
        r.setDureeHeures(dureeHeures);
        r.setStatut(StatutReservation.CONFIRMEE);
        r.setMontantTotal(montant);

        return reservationDAO.save(r);
    }

    /**
     * Algorithme de détection de chevauchement d'intervalles (Règle R1).
     * Délègue à {@link OverlapUtils#chevauchement} pour permettre les tests unitaires.
     *
     * Deux intervalles [A, B] et [C, D] se chevauchent si : A < D ET C < B
     */
    private boolean isCreneauDisponible(int idTerrain, LocalDate date,
                                         LocalTime heureDebut, int dureeHeures) {
        LocalTime heureFin = heureDebut.plusHours(dureeHeures);

        List<Reservation> existantes = reservationDAO.findByTerrainAndDateAndStatut(
                idTerrain, date, StatutReservation.CONFIRMEE);

        for (Reservation r : existantes) {
            if (OverlapUtils.chevauchement(heureDebut, heureFin, r.getHeureDebut(), r.getHeureFin())) {
                return false; // Conflit détecté
            }
        }
        return true;
    }

    /**
     * Annulation par le client (RÈGLE 3 : uniquement si date future).
     */
    @Override
    public void annulerReservation(int idReservation) {
        if (!authService.isLoggedIn()) {
            throw new AuthenticationException("Vous devez être connecté.");
        }
        Reservation r = reservationDAO.findById(idReservation)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Réservation #" + idReservation + " introuvable."));

        // Vérification d'appartenance
        if (r.getIdUtilisateur() != authService.getCurrentUser().getIdUtilisateur()) {
            throw new UnauthorizedException("Vous ne pouvez annuler que vos propres réservations.");
        }
        verifierAnnulable(r);
        reservationDAO.updateStatut(idReservation, StatutReservation.ANNULEE);
    }

    /**
     * Annulation par l'administrateur (RÈGLE 3 : uniquement si date future).
     */
    @Override
    public void annulerReservationAdmin(int idReservation) {
        if (!authService.isAdmin()) {
            throw new UnauthorizedException("Action réservée à l'administrateur.");
        }
        Reservation r = reservationDAO.findById(idReservation)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Réservation #" + idReservation + " introuvable."));

        verifierAnnulable(r);
        reservationDAO.updateStatut(idReservation, StatutReservation.ANNULEE);
    }

    /**
     * RÈGLE 3 : Annulation uniquement si DateRéservation > TODAY
     *           OU (Date = TODAY ET HeureDébut > NOW).
     * RÈGLE 4 : Les réservations TERMINEE ne peuvent pas être annulées.
     */
    private void verifierAnnulable(Reservation r) {
        if (r.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessException("La réservation #" + r.getIdReservation() + " est déjà annulée.");
        }
        if (r.getStatut() == StatutReservation.TERMINEE) {
            throw new BusinessException(
                "Impossible d'annuler la réservation #" + r.getIdReservation()
                + " : elle est déjà terminée.");
        }
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        boolean annulable = r.getDateReservation().isAfter(today)
                || (r.getDateReservation().isEqual(today) && r.getHeureDebut().isAfter(now));

        if (!annulable) {
            throw new BusinessException(
                "Impossible d'annuler la réservation #" + r.getIdReservation()
                + " : le créneau est passé ou en cours.");
        }
    }

    @Override
    public List<Reservation> getHistoriqueUtilisateur() {
        if (!authService.isLoggedIn()) {
            throw new AuthenticationException("Vous devez être connecté.");
        }
        return reservationDAO.findByUtilisateur(authService.getCurrentUser().getIdUtilisateur());
    }

    @Override
    public List<Reservation> getAllReservations() {
        if (!authService.isAdmin()) {
            throw new UnauthorizedException("Action réservée à l'administrateur.");
        }
        return reservationDAO.findAll();
    }

    @Override
    public Reservation findById(int id) {
        return reservationDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Réservation #" + id + " introuvable."));
    }

    @Override
    public List<Reservation> getReservationsParTerrain(int idTerrain) {
        return reservationDAO.findByTerrain(idTerrain);
    }

    /**
     * Met à jour en base les réservations CONFIRMEE dont le créneau est passé → TERMINEE.
     * À appeler au démarrage ou à chaque affichage de menu.
     */
    @Override
    public void updateReservationsTerminees() {
        List<Reservation> aTerminer = reservationDAO.findConfirmeesPassees();
        int count = 0;
        for (Reservation r : aTerminer) {
            reservationDAO.updateStatut(r.getIdReservation(), StatutReservation.TERMINEE);
            count++;
        }
        if (count > 0) {
            Logger.info(count + " réservation(s) automatiquement passée(s) au statut TERMINEE.");
        }
    }

    /**
     * Calcule et retourne les statistiques pour le tableau de bord admin.
     */
    @Override
    public Map<String, Object> getStatistiques() {
        List<Reservation> toutes = reservationDAO.findAll();
        Map<String, Object> stats = new HashMap<>();

        long totalRes      = toutes.size();
        long confirmees    = toutes.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count();
        long terminees     = toutes.stream().filter(r -> r.getStatut() == StatutReservation.TERMINEE).count();
        long annulees      = toutes.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count();
        BigDecimal ca      = toutes.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .map(Reservation::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long nbTerrains    = toutes.stream().map(Reservation::getIdTerrain).distinct().count();

        // Répartition des réservations par type de terrain
        Map<String, Long> parType = toutes.stream()
                .filter(r -> r.getTypeTerrain() != null)
                .collect(Collectors.groupingBy(r -> {
                    try {
                        return TypeTerrain.valueOf(r.getTypeTerrain()).getLibelle();
                    } catch (IllegalArgumentException ignored) {
                        return r.getTypeTerrain();
                    }
                }, Collectors.counting()));

        stats.put("totalReservations",    totalRes);
        stats.put("reservationsConfirmees", confirmees);
        stats.put("reservationsTerminees",  terminees);
        stats.put("reservationsAnnulees",   annulees);
        stats.put("chiffreAffaires",        ca);
        stats.put("terrainsUtilises",       nbTerrains);
        stats.put("reservationsParType",    parType);

        return stats;
    }
}
