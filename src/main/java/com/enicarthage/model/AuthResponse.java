
package com.enicarthage.model;

public class AuthResponse {

    private boolean success;
    private String message;
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String login;
    private String role;

    public AuthResponse(boolean success, String message,
                        int idUtilisateur, String nom,
                        String prenom, String login, String role) {

        this.success = success;
        this.message = message;
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
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

    public String getRole() {
        return role;
    }
}
