package com.enicarthage.dto;

public class PrescriptionItemDTO {
    private int idPrescription;
    private int idOrdonnance;
    private int idMedicament;
    private String nomMedicament;
    private String dosageMedicament;
    private String posologie;
    private String frequence;
    private String duree;
    private int quantite;

    public PrescriptionItemDTO() {}

    public int getIdPrescription() { return idPrescription; }
    public void setIdPrescription(int idPrescription) { this.idPrescription = idPrescription; }

    public int getIdOrdonnance() { return idOrdonnance; }
    public void setIdOrdonnance(int idOrdonnance) { this.idOrdonnance = idOrdonnance; }

    public int getIdMedicament() { return idMedicament; }
    public void setIdMedicament(int idMedicament) { this.idMedicament = idMedicament; }

    public String getNomMedicament() { return nomMedicament; }
    public void setNomMedicament(String nomMedicament) { this.nomMedicament = nomMedicament; }

    public String getDosageMedicament() { return dosageMedicament; }
    public void setDosageMedicament(String dosageMedicament) { this.dosageMedicament = dosageMedicament; }

    public String getPosologie() { return posologie; }
    public void setPosologie(String posologie) { this.posologie = posologie; }

    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
}
