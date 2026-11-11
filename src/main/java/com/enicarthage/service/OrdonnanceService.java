package com.enicarthage.service;

import com.enicarthage.dao.OrdonnanceDAO;
import com.enicarthage.dto.CreateOrdonnanceDTO;
import com.enicarthage.dto.PrescriptionItemDTO;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.Ordonnance;
import com.enicarthage.model.PrescriptionItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrdonnanceService {

    private final OrdonnanceDAO ordonnanceDAO;

    public OrdonnanceService() {
        this.ordonnanceDAO = new OrdonnanceDAO();
    }

    public OrdonnanceService(OrdonnanceDAO ordonnanceDAO) {
        this.ordonnanceDAO = ordonnanceDAO;
    }

    public int createOrdonnance(CreateOrdonnanceDTO dto) throws SQLException {
        if (dto == null) throw new ValidationException("Corps de requête manquant.");
        if (dto.getIdConsultation() <= 0) throw new ValidationException("idConsultation obligatoire et valide.");

        Ordonnance ord = new Ordonnance();
        ord.setIdConsultation(dto.getIdConsultation());
        String dateCreation = (dto.getDateCreation() != null && !dto.getDateCreation().isBlank())
                ? dto.getDateCreation()
                : LocalDate.now().toString();
        ord.setDateCreation(dateCreation);

        List<PrescriptionItem> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (PrescriptionItemDTO itemDto : dto.getItems()) {
                if (itemDto.getIdMedicament() > 0) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setIdMedicament(itemDto.getIdMedicament());
                    item.setPosologie(itemDto.getPosologie());
                    item.setFrequence(itemDto.getFrequence());
                    item.setDuree(itemDto.getDuree());
                    item.setQuantite(itemDto.getQuantite());
                    items.add(item);
                }
            }
        }

        int id = ordonnanceDAO.create(ord, items);
        if (id <= 0) {
            throw new SQLException("Échec lors de la création de l'ordonnance.");
        }
        return id;
    }

    public List<Ordonnance> getPrescriptionsByPatient(int idPatient) throws SQLException {
        return ordonnanceDAO.findByPatient(idPatient);
    }

    public List<Ordonnance> getPrescriptionsByDoctor(int idDocteur) throws SQLException {
        return ordonnanceDAO.findByDocteur(idDocteur);
    }

    public List<Ordonnance> getAllPrescriptions() throws SQLException {
        return ordonnanceDAO.findAll();
    }

    public Ordonnance getPrescriptionById(int idOrdonnance) throws SQLException {
        Optional<Ordonnance> ord = ordonnanceDAO.findById(idOrdonnance);
        if (ord.isEmpty()) {
            throw new ResourceNotFoundException("Ordonnance introuvable avec l'ID : " + idOrdonnance);
        }
        return ord.get();
    }

    public boolean updatePrescriptionItems(int idOrdonnance, List<PrescriptionItem> items) throws SQLException {
        if (idOrdonnance <= 0) throw new ValidationException("idOrdonnance invalide.");
        boolean updated = ordonnanceDAO.update(idOrdonnance, items);
        if (!updated) {
            throw new ResourceNotFoundException("Ordonnance introuvable avec l'ID : " + idOrdonnance);
        }
        return true;
    }

    public boolean deletePrescription(int idOrdonnance) throws SQLException {
        if (idOrdonnance <= 0) throw new ValidationException("idOrdonnance invalide.");
        boolean deleted = ordonnanceDAO.delete(idOrdonnance);
        if (!deleted) {
            throw new ResourceNotFoundException("Ordonnance introuvable avec l'ID : " + idOrdonnance);
        }
        return true;
    }
}
