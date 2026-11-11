package com.enicarthage.dto;

public class DocteurInfoDTO {
    private int idDocteur;
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String specialite;
    private String numeroOrdre;

    public DocteurInfoDTO() {}

    public DocteurInfoDTO(int idDocteur, String nom, String prenom, String specialite, String numeroOrdre) {
        this.idDocteur = idDocteur;
        this.idUtilisateur = idDocteur;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.numeroOrdre = numeroOrdre;
    }

    public int getIdDocteur() { return idDocteur; }
    public void setIdDocteur(int idDocteur) { this.idDocteur = idDocteur; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getNumeroOrdre() { return numeroOrdre; }
    public void setNumeroOrdre(String numeroOrdre) { this.numeroOrdre = numeroOrdre; }
}
