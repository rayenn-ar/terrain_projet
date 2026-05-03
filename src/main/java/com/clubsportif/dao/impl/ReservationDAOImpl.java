package com.clubsportif.dao.impl;

import com.clubsportif.config.DatabaseConfig;
import com.clubsportif.dao.interfaces.IReservationDAO;
import com.clubsportif.exception.BusinessException;
import com.clubsportif.model.Reservation;
import com.clubsportif.model.enums.StatutReservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de l'accès aux données Reservation.
 */
public class ReservationDAOImpl implements IReservationDAO {

    private static final String SELECT_FULL =
            "SELECT r.*, u.nom AS nom_client, u.email, t.nom AS nom_terrain, t.type " +
            "FROM reservation r " +
            "JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur " +
            "JOIN terrain t ON r.id_terrain = t.id_terrain ";

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setIdReservation(rs.getInt("id_reservation"));
        r.setIdUtilisateur(rs.getInt("id_utilisateur"));
        r.setIdTerrain(rs.getInt("id_terrain"));
        r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
        r.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        r.setDureeHeures(rs.getInt("duree_heures"));
        r.setStatut(StatutReservation.valueOf(rs.getString("statut")));
        r.setMontantTotal(rs.getBigDecimal("montant_total"));
        // Champs joints (présents dans toutes les requêtes via SELECT_FULL)
        r.setNomClient(rs.getString("nom_client"));
        r.setEmailClient(rs.getString("email"));
        r.setNomTerrain(rs.getString("nom_terrain"));
        r.setTypeTerrain(rs.getString("type"));
        return r;
    }

    @Override
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation "
                   + "(id_utilisateur, id_terrain, date_reservation, heure_debut, duree_heures, statut, montant_total) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getIdUtilisateur());
            ps.setInt(2, reservation.getIdTerrain());
            ps.setDate(3, Date.valueOf(reservation.getDateReservation()));
            ps.setTime(4, Time.valueOf(reservation.getHeureDebut()));
            ps.setInt(5, reservation.getDureeHeures());
            ps.setString(6, reservation.getStatut().name());
            ps.setBigDecimal(7, reservation.getMontantTotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) reservation.setIdReservation(keys.getInt(1));
            }
            return reservation;
        } catch (SQLException e) {
            throw new BusinessException("Erreur création réservation: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Reservation> findById(int id) {
        String sql = SELECT_FULL + "WHERE r.id_reservation = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche réservation: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_FULL + "ORDER BY r.date_reservation DESC, r.heure_debut";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new BusinessException("Erreur récupération réservations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByUtilisateur(int idUtilisateur) {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE r.id_utilisateur = ? ORDER BY r.date_reservation DESC, r.heure_debut";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur réservations utilisateur: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByTerrain(int idTerrain) {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE r.id_terrain = ? ORDER BY r.date_reservation DESC, r.heure_debut";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTerrain);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur réservations terrain: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByTerrainAndDate(int idTerrain, LocalDate date) {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE r.id_terrain = ? AND r.date_reservation = ? ORDER BY r.heure_debut";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTerrain);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche créneaux: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByTerrainAndDateAndStatut(int idTerrain, LocalDate date, StatutReservation statut) {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE r.id_terrain = ? AND r.date_reservation = ? AND r.statut = ? "
                   + "ORDER BY r.heure_debut";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTerrain);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, statut.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche créneaux confirmés: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean updateStatut(int idReservation, StatutReservation statut) {
        String sql = "UPDATE reservation SET statut = ? WHERE id_reservation = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, idReservation);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Erreur mise à jour statut réservation: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Erreur suppression réservation: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findConfirmeesPassees() {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_FULL
                   + "WHERE r.statut = 'CONFIRMEE' "
                   + "AND (r.date_reservation < CURDATE() "
                   + "     OR (r.date_reservation = CURDATE() "
                   + "         AND ADDTIME(r.heure_debut, SEC_TO_TIME(r.duree_heures * 3600)) <= CURTIME()))";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche réservations terminées: " + e.getMessage(), e);
        }
        return list;
    }
}
