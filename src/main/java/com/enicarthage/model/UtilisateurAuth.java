
package com.enicarthage.model;

public class UtilisateurAuth {

    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String login;
    private String motDePasse;
    private String role;

    public UtilisateurAuth(int idUtilisateur, String nom,
                           String prenom, String login,
                           String motDePasse, String role) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getLogin() {
        return login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getRole() {
        return role;
    }
}
