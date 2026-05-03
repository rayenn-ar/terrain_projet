package com.clubsportif.service.impl;

import com.clubsportif.dao.interfaces.ITerrainDAO;
import com.clubsportif.exception.BusinessException;
import com.clubsportif.exception.EntityNotFoundException;
import com.clubsportif.exception.ValidationException;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.TypeTerrain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests du service de gestion des terrains.
 * Couvre les scénarios SC-TER-01 à SC-TER-23.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class TerrainServiceImplTest {

    @Mock
    private ITerrainDAO terrainDAO;

    private TerrainServiceImpl terrainService;

    private Terrain terrain;
    @BeforeEach
    void setUp() {
        terrainService = new TerrainServiceImpl(terrainDAO);
        terrain = new Terrain(1, "Court A", TypeTerrain.TENNIS,
                BigDecimal.valueOf(50), true, "Terre battue");
    }

    // ═══════════════════════════════════════════════
    // AJOUT DE TERRAIN
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Ajout de terrain")
    class AjouterTerrainTests {

        @Test
        @DisplayName("SC-TER-01 : Ajout valide → terrain créé")
        void ajouterTerrain_valid() {
            when(terrainDAO.existsByNom("Nouveau Terrain")).thenReturn(false);
            when(terrainDAO.save(any())).thenAnswer(inv -> {
                Terrain t = inv.getArgument(0);
                t.setIdTerrain(99);
                return t;
            });

            Terrain result = terrainService.ajouterTerrain(
                    "Nouveau Terrain", TypeTerrain.FOOTBALL, BigDecimal.valueOf(30), true, "Desc");
            assertEquals("Nouveau Terrain", result.getNom());
            assertEquals(TypeTerrain.FOOTBALL, result.getType());
        }

        @Test
        @DisplayName("SC-TER-02 : Nom trop court → ValidationException")
        void ajouterTerrain_shortName() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain("A", TypeTerrain.TENNIS,
                            BigDecimal.valueOf(50), true, null)));
        }

        @Test
        @DisplayName("SC-TER-03 : Nom null → ValidationException")
        void ajouterTerrain_nullName() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain(null, TypeTerrain.TENNIS,
                            BigDecimal.valueOf(50), true, null)));
        }

        @Test
        @DisplayName("SC-TER-04 : Type null → ValidationException")
        void ajouterTerrain_nullType() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain("Terrain X", null,
                            BigDecimal.valueOf(50), true, null)));
        }

        @Test
        @DisplayName("SC-TER-05 : Prix négatif → ValidationException")
        void ajouterTerrain_negativePrice() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain("Terrain X", TypeTerrain.TENNIS,
                            BigDecimal.valueOf(-10), true, null)));
        }

        @Test
        @DisplayName("SC-TER-06 : Prix zéro → ValidationException")
        void ajouterTerrain_zeroPrice() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain("Terrain X", TypeTerrain.TENNIS,
                            BigDecimal.ZERO, true, null)));
        }

        @Test
        @DisplayName("SC-TER-07 : Prix null → ValidationException")
        void ajouterTerrain_nullPrice() {
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain("Terrain X", TypeTerrain.TENNIS,
                            null, true, null)));
        }

        @Test
        @DisplayName("SC-TER-08 : Nom déjà existant → ValidationException")
        void ajouterTerrain_duplicateName() {
            when(terrainDAO.existsByNom("Court A")).thenReturn(true);
            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.ajouterTerrain("Court A", TypeTerrain.TENNIS,
                            BigDecimal.valueOf(50), true, null)));
        }
    }

    // ═══════════════════════════════════════════════
    // MODIFICATION DE TERRAIN
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Modification de terrain")
    class ModifierTerrainTests {

        @Test
        @DisplayName("SC-TER-09 : Modification valide → terrain mis à jour")
        void modifierTerrain_valid() {
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.existsByNom("Court B")).thenReturn(false);
            when(terrainDAO.update(any())).thenReturn(true);

            Terrain result = terrainService.modifierTerrain(1, "Court B", TypeTerrain.BASKETBALL,
                    BigDecimal.valueOf(60), false, "Indoor");
            assertEquals("Court B", result.getNom());
            assertEquals(TypeTerrain.BASKETBALL, result.getType());
        }

        @Test
        @DisplayName("SC-TER-10 : Terrain inexistant → EntityNotFoundException")
        void modifierTerrain_notFound() {
            when(terrainDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> terrainService.modifierTerrain(999, "X", TypeTerrain.TENNIS,
                            BigDecimal.valueOf(50), true, null)));
        }

        @Test
        @DisplayName("SC-TER-11 : Nom dupliqué (autre terrain) → ValidationException")
        void modifierTerrain_duplicateName() {
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.existsByNom("Court B")).thenReturn(true);
            Terrain other = new Terrain(2, "Court B", TypeTerrain.FOOTBALL,
                    BigDecimal.valueOf(40), true, "");
            when(terrainDAO.findAll()).thenReturn(List.of(terrain, other));

            assertNotNull(assertThrows(ValidationException.class,
                    () -> terrainService.modifierTerrain(1, "Court B", TypeTerrain.TENNIS,
                            BigDecimal.valueOf(50), true, null)));
        }

        @Test
        @DisplayName("SC-TER-12 : Même nom (terrain courant) → succès")
        void modifierTerrain_sameName() {
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.existsByNom("Court A")).thenReturn(true);
            when(terrainDAO.findAll()).thenReturn(List.of(terrain));
            when(terrainDAO.update(any())).thenReturn(true);

            assertDoesNotThrow(() -> terrainService.modifierTerrain(1, "Court A",
                    TypeTerrain.TENNIS, BigDecimal.valueOf(55), true, "Updated"));
        }
    }

    // ═══════════════════════════════════════════════
    // SUPPRESSION DE TERRAIN
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Suppression de terrain")
    class SupprimerTerrainTests {

        @Test
        @DisplayName("SC-TER-13 : Suppression valide → succès")
        void supprimerTerrain_valid() {
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.delete(1)).thenReturn(true);

            assertDoesNotThrow(() -> terrainService.supprimerTerrain(1));
            verify(terrainDAO).delete(1);
        }

        @Test
        @DisplayName("SC-TER-14 : Terrain inexistant → EntityNotFoundException")
        void supprimerTerrain_notFound() {
            when(terrainDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> terrainService.supprimerTerrain(999)));
        }

        @Test
        @DisplayName("SC-TER-15 : Terrain référencé → BusinessException")
        void supprimerTerrain_referenced() {
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.delete(1)).thenReturn(false);
            assertNotNull(assertThrows(BusinessException.class,
                    () -> terrainService.supprimerTerrain(1)));
        }
    }

    // ═══════════════════════════════════════════════
    // DISPONIBILITÉ
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Toggle disponibilité")
    class ToggleDisponibiliteTests {

        @Test
        @DisplayName("SC-TER-16 : Toggle disponible → indisponible")
        void toggleDisponibilite_toUnavailable() {
            terrain.setDisponibilite(true);
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.update(any())).thenReturn(true);

            terrainService.toggleDisponibilite(1);
            assertFalse(terrain.isDisponibilite());
        }

        @Test
        @DisplayName("SC-TER-17 : Toggle indisponible → disponible")
        void toggleDisponibilite_toAvailable() {
            terrain.setDisponibilite(false);
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            when(terrainDAO.update(any())).thenReturn(true);

            terrainService.toggleDisponibilite(1);
            assertTrue(terrain.isDisponibilite());
        }

        @Test
        @DisplayName("SC-TER-18 : Toggle terrain inexistant → EntityNotFoundException")
        void toggleDisponibilite_notFound() {
            when(terrainDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> terrainService.toggleDisponibilite(999)));
        }
    }

    // ═══════════════════════════════════════════════
    // RECHERCHE
    // ═══════════════════════════════════════════════
    @Nested
    @DisplayName("Recherche de terrains")
    class RechercheTests {

        @Test
        @DisplayName("SC-TER-19 : findById existant → terrain retourné")
        void findById_existing() {
            when(terrainDAO.findById(1)).thenReturn(Optional.of(terrain));
            assertEquals("Court A", terrainService.findById(1).getNom());
        }

        @Test
        @DisplayName("SC-TER-20 : findById inexistant → EntityNotFoundException")
        void findById_notFound() {
            when(terrainDAO.findById(999)).thenReturn(Optional.empty());
            assertNotNull(assertThrows(EntityNotFoundException.class,
                    () -> terrainService.findById(999)));
        }

        @Test
        @DisplayName("SC-TER-21 : findAll → liste complète")
        void findAll() {
            Terrain t2 = new Terrain(2, "Court B", TypeTerrain.FOOTBALL,
                    BigDecimal.valueOf(40), true, "");
            when(terrainDAO.findAll()).thenReturn(List.of(terrain, t2));

            List<Terrain> result = terrainService.findAll();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("SC-TER-22 : findDisponibles → uniquement les disponibles")
        void findDisponibles() {
            when(terrainDAO.findDisponibles()).thenReturn(List.of(terrain));
            List<Terrain> result = terrainService.findDisponibles();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isDisponibilite());
        }

        @Test
        @DisplayName("SC-TER-23 : findByType → terrains du type spécifié")
        void findByType() {
            when(terrainDAO.findByType(TypeTerrain.TENNIS)).thenReturn(List.of(terrain));
            List<Terrain> result = terrainService.findByType(TypeTerrain.TENNIS);
            assertEquals(1, result.size());
            assertEquals(TypeTerrain.TENNIS, result.get(0).getType());
        }
    }
}
