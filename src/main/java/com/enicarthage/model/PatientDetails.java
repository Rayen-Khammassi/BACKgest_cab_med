package com.enicarthage.model;

import com.enicarthage.dto.PatientDetailsDTO;

public class PatientDetails extends PatientDetailsDTO {

    public PatientDetails() {
        super();
    }

    public PatientDetails(int idUtilisateur, String nom, String prenom, String email,
                          String dateNaissance, String adresse, boolean actif,
                          int totalRdv, int totalConsultations) {
        super(idUtilisateur, nom, prenom, email, dateNaissance, adresse, actif, totalRdv, totalConsultations);
    }
}
