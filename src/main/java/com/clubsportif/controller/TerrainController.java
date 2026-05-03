package com.clubsportif.controller;

import java.math.BigDecimal;
import java.util.List;

import com.clubsportif.exception.BusinessException;
import com.clubsportif.model.Terrain;
import com.clubsportif.model.enums.TypeTerrain;
import com.clubsportif.service.interfaces.ITerrainService;
import com.clubsportif.util.ConsoleColors;
import com.clubsportif.util.ScannerSingleton;
import com.clubsportif.view.console.ConsoleView;
import com.clubsportif.view.console.MenuBuilder;

/**
 * Contrôleur de gestion des terrains (CRUD - admin uniquement).
 */
public class TerrainController {

    private final ITerrainService terrainService;
    private final ScannerSingleton scanner = ScannerSingleton.getInstance();

    public TerrainController(ITerrainService terrainService) {
        this.terrainService = terrainService;
    }

    /** Menu principal de gestion des terrains (admin). */
    public void menuGestionTerrains() {
        boolean retour = false;
        while (!retour) {
            new MenuBuilder()
                .titre("GESTION DES TERRAINS")
                .option(1, "📋", "Lister tous les terrains")
                .option(2, "➕", "Ajouter un terrain")
                .option(3, "✏",  "Modifier un terrain")
                .option(4, "🗑",  "Supprimer un terrain")
                .option(5, "🔄", "Changer la disponibilité")
                .separateur()
                .option(0, "↩", "Retour")
                .afficher();

            MenuBuilder.promptChoix();
            int choix = scanner.nextInt();

            switch (choix) {
                case 1 -> listerTerrains();
                case 2 -> ajouterTerrain();
                case 3 -> modifierTerrain();
                case 4 -> supprimerTerrain();
                case 5 -> toggleDisponibilite();
                case 0 -> retour = true;
                default -> ConsoleView.avertissement("Choix invalide. Veuillez saisir un nombre entre 0 et 5.");
            }
            if (!retour) scanner.attendreEntree();
        }
    }

    /** Affiche tous les terrains (accessible par tous). */
    public void listerTerrains() {
        List<Terrain> terrains = terrainService.findAll();
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Liste des terrains (" + terrains.size() + ") ──" + ConsoleColors.RESET);
        ConsoleView.afficherTerrains(terrains);
    }

    /** Affiche uniquement les terrains disponibles. */
    public void listerTerrainsDisponibles() {
        List<Terrain> terrains = terrainService.findDisponibles();
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Terrains disponibles (" + terrains.size() + ") ──" + ConsoleColors.RESET);
        ConsoleView.afficherTerrains(terrains);
    }

    /** Affiche les terrains filtrés par type avec option. */
    public void listerTerrainsAvecFiltre() {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Filtrer les terrains ──" + ConsoleColors.RESET);
        new MenuBuilder()
            .option(1, "⚽",  "Football")
            .option(2, "🎾", "Tennis")
            .option(3, "🏀", "Basketball")
            .option(4, "🏐", "Volleyball")
            .option(5, "📋", "Tous les terrains disponibles")
            .afficher();

        MenuBuilder.promptChoix();
        int choix = scanner.nextInt();

        switch (choix) {
            case 1 -> ConsoleView.afficherTerrains(terrainService.findByType(TypeTerrain.FOOTBALL));
            case 2 -> ConsoleView.afficherTerrains(terrainService.findByType(TypeTerrain.TENNIS));
            case 3 -> ConsoleView.afficherTerrains(terrainService.findByType(TypeTerrain.BASKETBALL));
            case 4 -> ConsoleView.afficherTerrains(terrainService.findByType(TypeTerrain.VOLLEYBALL));
            case 5 -> listerTerrainsDisponibles();
            default -> ConsoleView.avertissement("Choix invalide.");
        }
    }

    private void ajouterTerrain() {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n  ── Ajouter un terrain ──" + ConsoleColors.RESET);

        String nom = scanner.nextLine("  Nom du terrain       : ");
        TypeTerrain type = saisirTypeTerrain();
        if (type == null) return;

        BigDecimal prix = saisirPrix();
        if (prix == null) return;

        System.out.print("  Disponible dès maintenant ? (O/N) : ");
        boolean dispo = scanner.nextLine().equalsIgnoreCase("O");

        String desc = scanner.nextLine("  Description          : ");

        try {
            Terrain t = terrainService.ajouterTerrain(nom, type, prix, dispo, desc);
            ConsoleView.succes("Terrain '" + t.getNom() + "' ajouté avec succès (ID: #" + t.getIdTerrain() + ").");
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void modifierTerrain() {
        listerTerrains();
        int id = scanner.nextInt("  ID du terrain à modifier (0 = annuler) : ");
        if (id == 0) return;

        Terrain terrain;
        try {
            terrain = terrainService.findById(id);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
            return;
        }

        System.out.println(ConsoleColors.CYAN + "  (Entrée pour conserver la valeur actuelle)" + ConsoleColors.RESET);
        String nom = scanner.nextLine("  Nouveau nom      (actuel: " + terrain.getNom() + ") : ");
        if (nom.isBlank()) nom = terrain.getNom();

        System.out.println("  Type actuel : " + terrain.getType().getLibelle());
        ConsoleView.afficherTypesTerrains();
        System.out.print("  Nouveau type (0 = conserver) : ");
        int typeIdx = scanner.nextInt();
        TypeTerrain type = (typeIdx >= 1 && typeIdx <= TypeTerrain.values().length)
                ? TypeTerrain.values()[typeIdx - 1] : terrain.getType();

        System.out.print("  Nouveau prix/h (actuel: " + terrain.getPrixParHeure() + " dt, 0 = conserver) : ");
        String prixStr = scanner.nextLine();
        BigDecimal prix;
        try {
            double prixVal = prixStr.isBlank() ? 0 : Double.parseDouble(prixStr.replace(",", "."));
            prix = prixVal > 0 ? BigDecimal.valueOf(prixVal) : terrain.getPrixParHeure();
        } catch (NumberFormatException e) {
            ConsoleView.erreur("Prix invalide.");
            return;
        }

        System.out.print("  Disponible ? (O/N, actuel: " + (terrain.isDisponibilite() ? "OUI" : "NON") + ") : ");
        String dispoStr = scanner.nextLine().trim();
        boolean dispo = dispoStr.isBlank()
                ? terrain.isDisponibilite()
                : dispoStr.equalsIgnoreCase("O");

        String desc = scanner.nextLine("  Description (actuel: " + terrain.getDescription() + ") : ");
        if (desc.isBlank()) desc = terrain.getDescription();

        try {
            Terrain t = terrainService.modifierTerrain(id, nom, type, prix, dispo, desc);
            ConsoleView.succes("Terrain #" + t.getIdTerrain() + " modifié avec succès !");
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void supprimerTerrain() {
        listerTerrains();
        int id = scanner.nextInt("  ID du terrain à supprimer (0 = annuler) : ");
        if (id == 0) return;

        System.out.print(ConsoleColors.YELLOW_BOLD
            + "  ⚠ Confirmer la suppression du terrain #" + id + " ? (O/N) : "
            + ConsoleColors.RESET);
        if (!scanner.nextLine().equalsIgnoreCase("O")) {
            ConsoleView.info("Suppression annulée.");
            return;
        }
        try {
            terrainService.supprimerTerrain(id);
            ConsoleView.succes("Terrain #" + id + " supprimé.");
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    private void toggleDisponibilite() {
        listerTerrains();
        int id = scanner.nextInt("  ID du terrain (0 = annuler) : ");
        if (id == 0) return;
        try {
            terrainService.toggleDisponibilite(id);
            Terrain t = terrainService.findById(id);
            String etat = t.isDisponibilite() ? "DISPONIBLE" : "INDISPONIBLE";
            ConsoleView.succes("Terrain #" + id + " est maintenant : " + etat);
        } catch (BusinessException e) {
            ConsoleView.erreur(e.getMessage());
        }
    }

    // ---- Helpers de saisie ----

    private TypeTerrain saisirTypeTerrain() {
        ConsoleView.afficherTypesTerrains();
        int idx = scanner.nextInt("  Choisir le type : ");
        if (idx < 1 || idx > TypeTerrain.values().length) {
            ConsoleView.erreur("Type invalide.");
            return null;
        }
        return TypeTerrain.values()[idx - 1];
    }

    private BigDecimal saisirPrix() {
        String prixStr = scanner.nextLine("  Prix par heure (dt)  : ");
        try {
            double prixVal = Double.parseDouble(prixStr.replace(",", "."));
            if (prixVal <= 0) throw new NumberFormatException();
            return BigDecimal.valueOf(prixVal);
        } catch (NumberFormatException e) {
            ConsoleView.erreur("Prix invalide. Doit être un nombre positif.");
            return null;
        }
    }
}
