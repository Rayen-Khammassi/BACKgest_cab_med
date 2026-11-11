package com.enicarthage.dto;

public class UpdateAppointmentStatusDTO {
    private int idRdv;
    private String statut;

    public UpdateAppointmentStatusDTO() {}

    public int getIdRdv() { return idRdv; }
    public void setIdRdv(int idRdv) { this.idRdv = idRdv; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
