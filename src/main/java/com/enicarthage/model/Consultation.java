package com.enicarthage.model;

public class Consultation {
    private int idConsultation;
    private String dateConsultation;
    private String diagnostic;
    private String observations;
    private int idRdv;

    // Joined info
    private int idPatient;
    private String patientNom;
    private String patientPrenom;
    private String patientEmail;
    private String patientDateNaissance;

    private int idDocteur;
    private String docteurNom;
    private String docteurPrenom;
    private String docteurSpecialite;

    public Consultation() {}

    public int getIdConsultation() { return idConsultation; }
    public void setIdConsultation(int idConsultation) { this.idConsultation = idConsultation; }

    public String getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(String dateConsultation) { this.dateConsultation = dateConsultation; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public int getIdRdv() { return idRdv; }
    public void setIdRdv(int idRdv) { this.idRdv = idRdv; }

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getPatientPrenom() { return patientPrenom; }
    public void setPatientPrenom(String patientPrenom) { this.patientPrenom = patientPrenom; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getPatientDateNaissance() { return patientDateNaissance; }
    public void setPatientDateNaissance(String patientDateNaissance) { this.patientDateNaissance = patientDateNaissance; }

    public int getIdDocteur() { return idDocteur; }
    public void setIdDocteur(int idDocteur) { this.idDocteur = idDocteur; }

    public String getDocteurNom() { return docteurNom; }
    public void setDocteurNom(String docteurNom) { this.docteurNom = docteurNom; }

    public String getDocteurPrenom() { return docteurPrenom; }
    public void setDocteurPrenom(String docteurPrenom) { this.docteurPrenom = docteurPrenom; }

    public String getDocteurSpecialite() { return docteurSpecialite; }
    public void setDocteurSpecialite(String docteurSpecialite) { this.docteurSpecialite = docteurSpecialite; }
}
