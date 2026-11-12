package com.enicarthage.model;

import com.enicarthage.dto.DocteurInfoDTO;

public class DocteurInfo extends DocteurInfoDTO {

    public DocteurInfo() {
        super();
    }

    public DocteurInfo(int idDocteur, String nom, String prenom, String specialite, String numeroOrdre) {
        super(idDocteur, nom, prenom, specialite, numeroOrdre);
    }
}
