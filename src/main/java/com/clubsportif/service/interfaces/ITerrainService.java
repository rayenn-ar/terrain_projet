package com.clubsportif.service.interfaces;

import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.TypeTerrain;

import java.math.BigDecimal;
import java.util.List;

public interface ITerrainService {

    Terrain ajouterTerrain(String nom, TypeTerrain type, BigDecimal prix,
                           boolean disponibilite, String description);

    Terrain modifierTerrain(int id, String nom, TypeTerrain type, BigDecimal prix,
                            boolean disponibilite, String description);

    void supprimerTerrain(int id);

    Terrain findById(int id);

    List<Terrain> findAll();

    List<Terrain> findDisponibles();

    List<Terrain> findByType(TypeTerrain type);

    /** Inverse l'état de disponibilité d'un terrain. */
    void toggleDisponibilite(int id);
}
