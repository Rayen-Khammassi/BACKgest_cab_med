package com.enicarthage.service;

import com.enicarthage.dao.ConsultationDAO;
import com.enicarthage.dto.CreateConsultationDTO;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.Consultation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ConsultationService {

    private final ConsultationDAO consultationDAO;

    public ConsultationService() {
        this.consultationDAO = new ConsultationDAO();
    }

    public ConsultationService(ConsultationDAO consultationDAO) {
        this.consultationDAO = consultationDAO;
    }

    public int createConsultation(CreateConsultationDTO dto) throws SQLException {
        if (dto == null) throw new ValidationException("Corps de requête manquant.");
        if (dto.getIdRdv() <= 0) throw new ValidationException("idRdv obligatoire et supérieur à 0.");
        if (dto.getDiagnostic() == null || dto.getDiagnostic().isBlank()) {
            throw new ValidationException("diagnostic obligatoire.");
        }

        String dateConsultation = (dto.getDateConsultation() != null && !dto.getDateConsultation().isBlank())
                ? dto.getDateConsultation()
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Consultation c = new Consultation();
        c.setIdRdv(dto.getIdRdv());
        c.setDateConsultation(dateConsultation);
        c.setDiagnostic(dto.getDiagnostic());
        c.setObservations(dto.getObservations());

        int id = consultationDAO.create(c);
        if (id <= 0) {
            throw new SQLException("Échec lors de la création de la consultation.");
        }
        return id;
    }

    public boolean updateConsultation(int id, String diagnostic, String observations) throws SQLException {
        if (id <= 0) throw new ValidationException("Identifiant de consultation invalide.");
        boolean updated = consultationDAO.update(id, diagnostic, observations);
        if (!updated) {
            throw new ResourceNotFoundException("Consultation introuvable avec l'ID : " + id);
        }
        return true;
    }

    public boolean deleteConsultation(int id) throws SQLException {
        if (id <= 0) throw new ValidationException("Identifiant de consultation invalide.");
        boolean deleted = consultationDAO.delete(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Consultation introuvable avec l'ID : " + id);
        }
        return true;
    }

    public List<Consultation> getConsultationsByDoctor(int doctorId) throws SQLException {
        return consultationDAO.findByDocteur(doctorId);
    }

    public List<Consultation> getConsultationsByPatient(int patientId) throws SQLException {
        return consultationDAO.findByPatient(patientId);
    }

    public List<Consultation> getAllConsultations() throws SQLException {
        return consultationDAO.findAll();
    }

    public Consultation getConsultationById(int id) throws SQLException {
        Optional<Consultation> c = consultationDAO.findById(id);
        if (c.isEmpty()) {
            throw new ResourceNotFoundException("Consultation introuvable avec l'ID : " + id);
        }
        return c.get();
    }
}
