package com.clubsportif.dao.interfaces;

import com.clubsportif.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface IUtilisateurDAO {

    Utilisateur save(Utilisateur utilisateur);

    Optional<Utilisateur> findById(int id);

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findAll();

    boolean update(Utilisateur utilisateur);

    boolean delete(int id);

    boolean existsByEmail(String email);
}
