package com.clubsportif.service.interfaces;

import com.clubsportif.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface IReservationService {

    /** Crée une réservation pour l'utilisateur actuellement connecté. */
    Reservation creerReservation(int idTerrain, LocalDate date, LocalTime heureDebut, int dureeHeures);

    /** Annule une réservation (par le client propriétaire). */
    void annulerReservation(int idReservation);

    /** Annule n'importe quelle réservation (admin). */
    void annulerReservationAdmin(int idReservation);

    /** Historique des réservations de l'utilisateur connecté. */
    List<Reservation> getHistoriqueUtilisateur();

    /** Toutes les réservations (admin). */
    List<Reservation> getAllReservations();

    Reservation findById(int id);

    List<Reservation> getReservationsParTerrain(int idTerrain);

    /** Passe en TERMINEE les réservations CONFIRMEE dont le créneau est passé. */
    void updateReservationsTerminees();

    /** Statistiques pour le tableau de bord admin. */
    Map<String, Object> getStatistiques();
}
