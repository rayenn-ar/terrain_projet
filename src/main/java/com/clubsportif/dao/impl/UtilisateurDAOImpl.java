package com.clubsportif.dao.impl;

import com.clubsportif.config.DatabaseConfig;
import com.clubsportif.dao.interfaces.IUtilisateurDAO;
import com.clubsportif.exception.BusinessException;
import com.clubsportif.model.Utilisateur;
import com.clubsportif.model.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de l'accès aux données Utilisateur.
 * Toutes les requêtes utilisent des PreparedStatement (protection anti-injection SQL).
 */
public class UtilisateurDAOImpl implements IUtilisateurDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("nom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null) {
            u.setDateInscription(ts.toLocalDateTime());
        }
        return u;
    }

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateur (nom, email, mot_de_passe_hash, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getEmail());
            ps.setString(3, utilisateur.getMotDePasseHash());
            ps.setString(4, utilisateur.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    utilisateur.setIdUtilisateur(keys.getInt(1));
                }
            }
            return utilisateur;
        } catch (SQLException e) {
            throw new BusinessException("Erreur création utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Utilisateur> findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche utilisateur par ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur recherche utilisateur par email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY date_inscription DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new BusinessException("Erreur récupération des utilisateurs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateur SET nom = ?, email = ?, mot_de_passe_hash = ?, role = ? "
                   + "WHERE id_utilisateur = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, utilisateur.getNom());
            ps.setString(2, utilisateur.getEmail());
            ps.setString(3, utilisateur.getMotDePasseHash());
            ps.setString(4, utilisateur.getRole().name());
            ps.setInt(5, utilisateur.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Erreur mise à jour utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BusinessException("Erreur suppression utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new BusinessException("Erreur vérification email: " + e.getMessage(), e);
        }
        return false;
    }
}
