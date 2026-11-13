package com.enicarthage.service;

import com.enicarthage.dao.PatientDAO;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.PatientDetails;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class PatientService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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
        if (id <= 0) {
            throw new ValidationException("Identifiant patient invalide (doit être un entier positif).");
        }
        Optional<PatientDetails> patient = patientDAO.findById(id);
        if (patient.isEmpty()) {
            throw new ResourceNotFoundException("Patient introuvable avec l'ID : " + id);
        }
        return patient.get();
    }

    public void updatePatient(int id, String nom, String prenom, String email, String dateNaissance, String adresse) throws SQLException {
        if (id <= 0) {
            throw new ValidationException("Identifiant patient invalide (doit être un entier positif).");
        }
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()) {
            throw new ValidationException("Le nom et le prénom sont obligatoires.");
        }
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Format d'adresse email invalide (ex: exemple@domaine.com).");
        }
        if (dateNaissance == null || dateNaissance.isBlank()) {
            throw new ValidationException("La date de naissance est obligatoire.");
        }
        try {
            LocalDate birthDate = LocalDate.parse(dateNaissance.trim());
            if (birthDate.isAfter(LocalDate.now())) {
                throw new ValidationException("La date de naissance ne peut pas être dans le futur.");
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Format de date de naissance invalide (AAAA-MM-JJ attendu).");
        }
        if (adresse == null || adresse.isBlank()) {
            throw new ValidationException("L'adresse est obligatoire.");
        }
        boolean updated = patientDAO.update(id, nom.trim(), prenom.trim(), email.trim(), dateNaissance.trim(), adresse.trim());
        if (!updated) {
            throw new ResourceNotFoundException("Impossible de mettre à jour : patient introuvable.");
        }
    }

    public void setPatientActive(int id, boolean actif) throws SQLException {
        if (id <= 0) {
            throw new ValidationException("Identifiant patient invalide (doit être un entier positif).");
        }
        boolean updated = patientDAO.setActif(id, actif);
        if (!updated) {
            throw new ResourceNotFoundException("Patient introuvable avec l'ID : " + id);
        }
    }
}
