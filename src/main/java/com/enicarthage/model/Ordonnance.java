package com.enicarthage.model;

import java.util.ArrayList;
import java.util.List;

public class Ordonnance {
    private int idOrdonnance;
    private String dateCreation;
    private int idConsultation;

    // Consultation & Rendez-vous info
    private String diagnostic;
    private int idPatient;
    private String patientNom;
    private String patientPrenom;
    private String patientEmail;

    private int idDocteur;
    private String docteurNom;
    private String docteurPrenom;
    private String docteurSpecialite;

    // Prescriptions lines
    private List<PrescriptionItem> items = new ArrayList<>();

    public Ordonnance() {}

    public int getIdOrdonnance() { return idOrdonnance; }
    public void setIdOrdonnance(int idOrdonnance) { this.idOrdonnance = idOrdonnance; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public int getIdConsultation() { return idConsultation; }
    public void setIdConsultation(int idConsultation) { this.idConsultation = idConsultation; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getPatientPrenom() { return patientPrenom; }
    public void setPatientPrenom(String patientPrenom) { this.patientPrenom = patientPrenom; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public int getIdDocteur() { return idDocteur; }
    public void setIdDocteur(int idDocteur) { this.idDocteur = idDocteur; }

    public String getDocteurNom() { return docteurNom; }
    public void setDocteurNom(String docteurNom) { this.docteurNom = docteurNom; }

    public String getDocteurPrenom() { return docteurPrenom; }
    public void setDocteurPrenom(String docteurPrenom) { this.docteurPrenom = docteurPrenom; }

    public String getDocteurSpecialite() { return docteurSpecialite; }
    public void setDocteurSpecialite(String docteurSpecialite) { this.docteurSpecialite = docteurSpecialite; }

    public List<PrescriptionItem> getItems() { return items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items; }
}
