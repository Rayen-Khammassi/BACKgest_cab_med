package com.enicarthage.dto;

public class CreateAppointmentDTO {
    private int idPatient;
    private int idDocteur;
    private String dateHeure;
    private String motif;
    private String statut;

    public CreateAppointmentDTO() {}

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }

    public int getIdDocteur() { return idDocteur; }
    public void setIdDocteur(int idDocteur) { this.idDocteur = idDocteur; }

    public String getDateHeure() { return dateHeure; }
    public void setDateHeure(String dateHeure) { this.dateHeure = dateHeure; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
