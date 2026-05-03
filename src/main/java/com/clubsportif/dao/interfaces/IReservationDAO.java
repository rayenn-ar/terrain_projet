package com.clubsportif.dao.interfaces;

import com.clubsportif.model.Reservation;
import com.clubsportif.model.enums.StatutReservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IReservationDAO {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(int id);

    List<Reservation> findAll();

    List<Reservation> findByUtilisateur(int idUtilisateur);

    List<Reservation> findByTerrain(int idTerrain);

    List<Reservation> findByTerrainAndDate(int idTerrain, LocalDate date);

    List<Reservation> findByTerrainAndDateAndStatut(int idTerrain, LocalDate date, StatutReservation statut);

    boolean updateStatut(int idReservation, StatutReservation statut);

    boolean delete(int id);

    /** Retourne toutes les réservations CONFIRMEE dont le créneau est passé. */
    List<Reservation> findConfirmeesPassees();
}
