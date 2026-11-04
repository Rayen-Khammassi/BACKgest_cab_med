package com.enicarthage.auth.dto;

public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String login;
    private String password;
    private String dateNaissance;
    private String adresse;

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getDateNaissance() { return dateNaissance; }
    public String getAdresse() { return adresse; }
}