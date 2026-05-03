package com.clubsportif.util;

import java.time.LocalTime;

/**
 * Utilitaire pour la détection de chevauchement d'intervalles horaires.
 *
 * Algorithme (Règle R1 du cahier des charges) :
 *   Deux intervalles [A, B] et [C, D] se chevauchent si et seulement si :
 *       A < D  ET  C < B
 *
 * Exemples :
 *   [10:00, 12:00] ∩ [11:00, 13:00] → chevauchement (10 < 13 ET 11 < 12)
 *   [10:00, 12:00] ∩ [12:00, 14:00] → PAS de chevauchement (10 < 14 ET 12 < 12 = FAUX)
 *   [10:00, 12:00] ∩ [08:00, 10:00] → PAS de chevauchement (10 < 10 = FAUX)
 */
public final class OverlapUtils {

    private OverlapUtils() {}

    /**
     * Vérifie si deux intervalles horaires se chevauchent.
     *
     * @param debutA  Début du premier intervalle
     * @param finA    Fin du premier intervalle (= debutA + durée)
     * @param debutB  Début du second intervalle
     * @param finB    Fin du second intervalle
     * @return true si les intervalles se chevauchent (conflit)
     */
    public static boolean chevauchement(LocalTime debutA, LocalTime finA,
                                         LocalTime debutB, LocalTime finB) {
        return debutA.isBefore(finB) && debutB.isBefore(finA);
    }
}
