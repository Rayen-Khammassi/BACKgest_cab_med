package com.enicarthage.model;

public class RendezVous {
    private int idRdv;
    private String dateHeure;
    private String motif;
    private String statut;
    private int idPatient;
    private String patientNom;
    private String patientPrenom;
    private String patientEmail;
    private int idDocteur;
    private String docteurNom;
    private String docteurPrenom;
    private String docteurSpecialite;

    public RendezVous() {}

    public RendezVous(int idRdv, String dateHeure, String motif, String statut,
                      int idPatient, String patientNom, String patientPrenom, String patientEmail,
                      int idDocteur, String docteurNom, String docteurPrenom, String docteurSpecialite) {
        this.idRdv = idRdv;
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.statut = statut;
        this.idPatient = idPatient;
        this.patientNom = patientNom;
        this.patientPrenom = patientPrenom;
        this.patientEmail = patientEmail;
        this.idDocteur = idDocteur;
        this.docteurNom = docteurNom;
        this.docteurPrenom = docteurPrenom;
        this.docteurSpecialite = docteurSpecialite;
    }

    public int getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(int idRdv) {
        this.idRdv = idRdv;
    }

    public String getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(String dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    public String getPatientNom() {
        return patientNom;
    }

    public String getPatientPrenom() {
        return patientPrenom;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public int getIdDocteur() {
        return idDocteur;
    }

    public void setIdDocteur(int idDocteur) {
        this.idDocteur = idDocteur;
    }

    public String getDocteurNom() {
        return docteurNom;
    }

    public String getDocteurPrenom() {
        return docteurPrenom;
    }

    public String getDocteurSpecialite() {
        return docteurSpecialite;
    }
}
