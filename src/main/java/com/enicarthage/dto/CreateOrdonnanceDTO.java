package com.enicarthage.dto;

import java.util.List;

public class CreateOrdonnanceDTO {
    private int idConsultation;
    private String dateCreation;
    private List<PrescriptionItemDTO> items;

    public CreateOrdonnanceDTO() {}

    public int getIdConsultation() { return idConsultation; }
    public void setIdConsultation(int idConsultation) { this.idConsultation = idConsultation; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public List<PrescriptionItemDTO> getItems() { return items; }
    public void setItems(List<PrescriptionItemDTO> items) { this.items = items; }
}
