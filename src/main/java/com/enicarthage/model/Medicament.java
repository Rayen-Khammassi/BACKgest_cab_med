package com.enicarthage.model;

public class Medicament {
    private int idMedicament;
    private String nom;
    private String dosage;

    public Medicament() {}

    public Medicament(int idMedicament, String nom, String dosage) {
        this.idMedicament = idMedicament;
        this.nom = nom;
        this.dosage = dosage;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
