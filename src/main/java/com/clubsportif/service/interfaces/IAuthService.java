package com.clubsportif.service.interfaces;

import com.clubsportif.model.Utilisateur;

public interface IAuthService {

    /** Inscrit un nouveau client. */
    Utilisateur register(String nom, String email, String motDePasse);

    /** Connecte un utilisateur et démarre une session. */
    Utilisateur login(String email, String motDePasse);

    /** Déconnecte l'utilisateur courant. */
    void logout();

    /** Retourne l'utilisateur actuellement connecté, ou null. */
    Utilisateur getCurrentUser();

    boolean isLoggedIn();

    boolean isAdmin();

    /** Met à jour le profil de l'utilisateur connecté. */
    void updateProfil(String nom, String email, String nouveauMotDePasse);

    /** Supprime un compte utilisateur (admin uniquement). */
    void deleteAccount(int idUtilisateur);

    /** Retourne la liste de tous les utilisateurs (admin uniquement). */
    java.util.List<com.clubsportif.model.Utilisateur> findAllUtilisateurs();
}
