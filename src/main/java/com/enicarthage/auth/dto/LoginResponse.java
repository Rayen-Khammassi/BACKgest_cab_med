package com.enicarthage.auth.dto;


import com.enicarthage.utilisateur.model.UtilisateurAuth;

public class LoginResponse {

    private boolean success;
    private String message;
    private Integer idUtilisateur;
    private String nom;
    private String prenom;
    private String login;
    private String role;

    public LoginResponse(
            boolean success,
            String message,
            Integer idUtilisateur,
            String nom,
            String prenom,
            String login,
            String role
    ) {
        this.success = success;
        this.message = message;
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.role = role;
    }

    // Réponse lorsque la connexion réussit
    public static LoginResponse success(UtilisateurAuth user) {
        return new LoginResponse(
                true,
                "Authentification réussie",
                user.getIdUtilisateur(),
                user.getNom(),
                user.getPrenom(),
                user.getLogin(),
                user.getRole()
        );
    }

    // Réponse lorsque la connexion échoue
    public static LoginResponse error(String message) {
        return new LoginResponse(
                false,
                message,
                null,
                null,
                null,
                null,
                null
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getIdUtilisateur() {
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

