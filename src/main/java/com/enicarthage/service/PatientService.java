package com.enicarthage.service;

import com.enicarthage.dao.PatientDAO;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.PatientDetails;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PatientService {

    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAO();
    }

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public List<PatientDetails> getAllPatients() throws SQLException {
        return patientDAO.findAll();
    }

    public PatientDetails getPatientById(int id) throws SQLException {
        Optional<PatientDetails> patient = patientDAO.findById(id);
        if (patient.isEmpty()) {
            throw new ResourceNotFoundException("Patient introuvable avec l'ID : " + id);
        }
        return patient.get();
    }

    public void updatePatient(int id, String nom, String prenom, String email, String dateNaissance, String adresse) throws SQLException {
        if (id <= 0) {
            throw new ValidationException("Identifiant patient invalide.");
        }
        boolean updated = patientDAO.update(id, nom, prenom, email, dateNaissance, adresse);
        if (!updated) {
            throw new ResourceNotFoundException("Impossible de mettre à jour : patient introuvable.");
        }
    }

    public void setPatientActive(int id, boolean actif) throws SQLException {
        boolean updated = patientDAO.setActif(id, actif);
        if (!updated) {
            throw new ResourceNotFoundException("Patient introuvable avec l'ID : " + id);
        }
    }
}
