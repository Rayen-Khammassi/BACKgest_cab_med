
package com.enicarthage.model;

public abstract class Utilisateur {

    protected int idUtilisateur;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String login;
    protected String motDePasse;

    public Utilisateur() {
    }

    public Utilisateur(int idUtilisateur, String nom,
                       String prenom, String email,
                       String login, String motDePasse) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.login = login;
        this.motDePasse = motDePasse;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public boolean seConnecter() {
        return false; // Authentication will be implemented later
    }
}