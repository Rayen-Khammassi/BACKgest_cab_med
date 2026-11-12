package com.enicarthage.model;

public class DocteurInfo {
    private int idDocteur;
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String specialite;
    private String numeroOrdre;

    public DocteurInfo(int idDocteur, String nom, String prenom, String specialite, String numeroOrdre) {
        this.idDocteur = idDocteur;
        this.idUtilisateur = idDocteur;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.numeroOrdre = numeroOrdre;
    }

    public int getIdDocteur() {
        return idDocteur;
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

    public String getSpecialite() {
        return specialite;
    }

    public String getNumeroOrdre() {
        return numeroOrdre;
    }
}
