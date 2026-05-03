package com.clubsportif.dao.interfaces;

import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.TypeTerrain;

import java.util.List;
import java.util.Optional;

public interface ITerrainDAO {

    Terrain save(Terrain terrain);

    Optional<Terrain> findById(int id);

    List<Terrain> findAll();

    List<Terrain> findByType(TypeTerrain type);

    List<Terrain> findDisponibles();

    boolean update(Terrain terrain);

    boolean delete(int id);

    boolean existsByNom(String nom);
}
