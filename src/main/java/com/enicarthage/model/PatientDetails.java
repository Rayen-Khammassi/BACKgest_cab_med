package com.enicarthage.model;

public class PatientDetails {
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String dateNaissance;
    private String adresse;
    private boolean actif;
    private int totalRdv;
    private int totalConsultations;

    public PatientDetails() {}

    public PatientDetails(int idUtilisateur, String nom, String prenom, String email,
                          String dateNaissance, String adresse, boolean actif,
                          int totalRdv, int totalConsultations) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.actif = actif;
        this.totalRdv = totalRdv;
        this.totalConsultations = totalConsultations;
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public int getTotalRdv() { return totalRdv; }
    public void setTotalRdv(int totalRdv) { this.totalRdv = totalRdv; }

    public int getTotalConsultations() { return totalConsultations; }
    public void setTotalConsultations(int totalConsultations) { this.totalConsultations = totalConsultations; }
}
