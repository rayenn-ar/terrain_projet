package com.clubsportif.service.impl;

import com.clubsportif.dao.impl.TerrainDAOImpl;
import com.clubsportif.dao.interfaces.ITerrainDAO;
import com.clubsportif.exception.BusinessException;
import com.clubsportif.exception.EntityNotFoundException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.TypeTerrain;
import com.clubsportif.service.interfaces.ITerrainService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logique métier de gestion des terrains sportifs.
 */
public class TerrainServiceImpl implements ITerrainService {

    private final ITerrainDAO terrainDAO;

    public TerrainServiceImpl() {
        this.terrainDAO = new TerrainDAOImpl();
    }

    /* Package-private : injection pour les tests unitaires */
    TerrainServiceImpl(ITerrainDAO terrainDAO) {
        this.terrainDAO = terrainDAO;
    }

    @Override
    public Terrain ajouterTerrain(String nom, TypeTerrain type, BigDecimal prix,
                                  boolean disponibilite, String description) {
        validerDonneesTerrain(nom, type, prix, -1);

        Terrain t = new Terrain();
        t.setNom(nom.trim());
        t.setType(type);
        t.setPrixParHeure(prix);
        t.setDisponibilite(disponibilite);
        t.setDescription(description != null ? description.trim() : "");
        return terrainDAO.save(t);
    }

    @Override
    public Terrain modifierTerrain(int id, String nom, TypeTerrain type, BigDecimal prix,
                                   boolean disponibilite, String description) {
        Terrain terrain = terrainDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terrain #" + id + " introuvable."));

        validerDonneesTerrain(nom, type, prix, id);

        terrain.setNom(nom.trim());
        terrain.setType(type);
        terrain.setPrixParHeure(prix);
        terrain.setDisponibilite(disponibilite);
        terrain.setDescription(description != null ? description.trim() : "");
        terrainDAO.update(terrain);
        return terrain;
    }

    @Override
    public void supprimerTerrain(int id) {
        if (!terrainDAO.findById(id).isPresent()) {
            throw new EntityNotFoundException("Terrain #" + id + " introuvable.");
        }
        if (!terrainDAO.delete(id)) {
            throw new BusinessException(
                "Impossible de supprimer le terrain #" + id
                + ". Il est peut-être référencé par des réservations.");
        }
    }

    @Override
    public Terrain findById(int id) {
        return terrainDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terrain #" + id + " introuvable."));
    }

    @Override
    public List<Terrain> findAll() {
        return terrainDAO.findAll();
    }

    @Override
    public List<Terrain> findDisponibles() {
        return terrainDAO.findDisponibles();
    }

    @Override
    public List<Terrain> findByType(TypeTerrain type) {
        return terrainDAO.findByType(type);
    }

    @Override
    public void toggleDisponibilite(int id) {
        Terrain terrain = terrainDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terrain #" + id + " introuvable."));
        terrain.setDisponibilite(!terrain.isDisponibilite());
        terrainDAO.update(terrain);
    }

    /* ---- Validation interne ---- */

    private void validerDonneesTerrain(String nom, TypeTerrain type, BigDecimal prix, int excludeId) {
        if (nom == null || nom.trim().length() < 2) {
            throw new ValidationException("Le nom du terrain doit contenir au moins 2 caractères.");
        }
        if (type == null) {
            throw new ValidationException("Le type de terrain est obligatoire.");
        }
        if (prix == null || prix.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Le prix par heure doit être supérieur à 0.");
        }
        // Vérification unicité du nom (en excluant l'entrée courante lors d'une modification)
        if (terrainDAO.existsByNom(nom.trim())) {
            if (excludeId < 0) {
                throw new ValidationException("Un terrain nommé '" + nom.trim() + "' existe déjà.");
            }
            // Pour une modification : vérifier si le nom appartient au terrain courant
            boolean nameIsOwnedByCurrent = terrainDAO.findById(excludeId)
                    .map(t -> t.getNom().equalsIgnoreCase(nom.trim()))
                    .orElse(false);
            if (!nameIsOwnedByCurrent) {
                throw new ValidationException("Un autre terrain nommé '" + nom.trim() + "' existe déjà.");
            }
        }
    }
}
