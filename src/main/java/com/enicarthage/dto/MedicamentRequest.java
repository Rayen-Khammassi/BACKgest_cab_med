
package com.enicarthage.dto;

public class MedicamentRequest {

    private String nom;
    private String dosage;

    public MedicamentRequest() {
    }

    public String getNom() {
        return nom;
    }

    public String getDosage() {
        return dosage;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
