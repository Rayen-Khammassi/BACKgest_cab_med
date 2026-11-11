package com.enicarthage.dto;

public class CreateConsultationDTO {
    private int idRdv;
    private String diagnostic;
    private String observations;
    private String dateConsultation;

    public CreateConsultationDTO() {}

    public int getIdRdv() { return idRdv; }
    public void setIdRdv(int idRdv) { this.idRdv = idRdv; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(String dateConsultation) { this.dateConsultation = dateConsultation; }
}
