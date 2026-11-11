package com.enicarthage.model;

import com.enicarthage.dto.UtilisateurAuthDTO;

public class UtilisateurAuth extends UtilisateurAuthDTO {

    public UtilisateurAuth(int idUtilisateur, String nom, String prenom, String login, String motDePasse, String role) {
        super(idUtilisateur, nom, prenom, login, motDePasse, role);
    }

    public UtilisateurAuth(int idUtilisateur, String nom, String prenom, String login, String motDePasse, String role, boolean actif) {
        super(idUtilisateur, nom, prenom, login, motDePasse, role, actif);
    }
}
