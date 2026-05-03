package com.clubsportif.dao.impl;

import com.clubsportif.config.DatabaseConfig;
import com.clubsportif.dao.interfaces.ITerrainDAO;
import com.clubsportif.exception.BusinessException;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.TypeTerrain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de l'accès aux données Terrain.
 */
public class TerrainDAOImpl implements ITerrainDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Terrain mapRow(ResultSet rs) throws SQLException {
        Terrain t = new Terrain();
        t.setIdTerrain(rs.getInt("id_terrain"));
        t.setNom(rs.getString("nom"));
        t.setType(TypeTerrain.valueOf(rs.getString("type")));
        t.setPrixParHeure(rs.getBigDecimal("prix_par_heure"));
        t.setDisponibilite(rs.getBoolean("disponibilite"));
        t.setDescription(rs.getString("description"));
        return t;
    }

    @Override
    public Terrain save(Terrain terrain) {
        String sql = "INSERT INTO terrain (nom, type, prix_par_heure, disponibilite, description) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, terrain.getNom());
            ps.setString(2, terrain.getType().name());
            ps.setBigDecimal(3, terrain.getPrixParHeure());
            ps.setBoolean(4, terrain.isDisponibilite());
            ps.setString(5, terrain.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) terrain.setIdTerrain(keys.getInt(1));
            }
            return terrain;
        } catch (SQLException e) {
            throw new BusinessException("Erreur création terrain: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Terrain> findById(int id) {
        String sql = "SELECT * FROM terrain WHERE id_terrain = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche terrain: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Terrain> findAll() {
        List<Terrain> list = new ArrayList<>();
        String sql = "SELECT * FROM terrain ORDER BY type, nom";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new BusinessException("Erreur récupération terrains: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Terrain> findByType(TypeTerrain type) {
        List<Terrain> list = new ArrayList<>();
        String sql = "SELECT * FROM terrain WHERE type = ? ORDER BY nom";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche terrains par type: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Terrain> findDisponibles() {
        List<Terrain> list = new ArrayList<>();
        String sql = "SELECT * FROM terrain WHERE disponibilite = TRUE ORDER BY type, nom";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche terrains disponibles: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Terrain terrain) {
        String sql = "UPDATE terrain SET nom = ?, type = ?, prix_par_heure = ?, "
                   + "disponibilite = ?, description = ? WHERE id_terrain = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, terrain.getNom());
            ps.setString(2, terrain.getType().name());
            ps.setBigDecimal(3, terrain.getPrixParHeure());
            ps.setBoolean(4, terrain.isDisponibilite());
            ps.setString(5, terrain.getDescription());
            ps.setInt(6, terrain.getIdTerrain());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Erreur mise à jour terrain: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM terrain WHERE id_terrain = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Erreur suppression terrain: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByNom(String nom) {
        String sql = "SELECT COUNT(*) FROM terrain WHERE nom = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur vérification nom terrain: " + e.getMessage(), e);
        }
        return false;
    }
}
