
package com.enicarthage.medicament.model;

public class Medicament {

    private final int idMedicament;
    private final String nom;
    private final String dosage;

    public Medicament(int idMedicament, String nom, String dosage) {
        this.idMedicament = idMedicament;
        this.nom = nom;
        this.dosage = dosage;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public String getNom() {
        return nom;
    }

    public String getDosage() {
        return dosage;
    }
}
