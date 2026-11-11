package com.enicarthage.service;

import com.enicarthage.dao.RendezVousDAO;
import com.enicarthage.dto.CreateAppointmentDTO;
import com.enicarthage.dto.UpdateAppointmentStatusDTO;
import com.enicarthage.exception.ConflictException;
import com.enicarthage.exception.ForbiddenException;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.RendezVous;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RendezVousService {

    private final RendezVousDAO rendezVousDAO;
    private static final Set<String> STATUTS_VALIDES = Set.of("DEMANDE", "CONFIRME", "ANNULE", "TERMINE");

    public RendezVousService() {
        this.rendezVousDAO = new RendezVousDAO();
    }

    public RendezVousService(RendezVousDAO rendezVousDAO) {
        this.rendezVousDAO = rendezVousDAO;
    }

    public int createAppointment(CreateAppointmentDTO req) throws SQLException {
        if (req == null) throw new ValidationException("Corps de requête manquant.");
        if (req.getIdPatient() <= 0) throw new ValidationException("idPatient obligatoire et valide.");
        if (req.getIdDocteur() <= 0) throw new ValidationException("idDocteur obligatoire et valide.");
        if (req.getDateHeure() == null || req.getDateHeure().isBlank()) {
            throw new ValidationException("dateHeure obligatoire.");
        }

        String formattedDate = req.getDateHeure().trim().replace("T", " ");
        if (formattedDate.length() == 16) {
            formattedDate += ":00";
        }

        String statut = req.getStatut();
        if (statut == null || statut.isBlank()) {
            statut = "DEMANDE";
        } else if (!STATUTS_VALIDES.contains(statut.toUpperCase())) {
            throw new ValidationException("Statut invalide. Valeurs acceptées : " + STATUTS_VALIDES);
        }

        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(req.getIdPatient());
        rdv.setIdDocteur(req.getIdDocteur());
        rdv.setDateHeure(formattedDate);
        rdv.setMotif(req.getMotif());
        rdv.setStatut(statut.toUpperCase());

        int generatedId = rendezVousDAO.create(rdv);
        if (generatedId <= 0) {
            throw new SQLException("Échec lors de l'insertion du rendez-vous.");
        }
        return generatedId;
    }

    public List<RendezVous> getAppointmentsByPatient(int idPatient) throws SQLException {
        return rendezVousDAO.findByPatient(idPatient);
    }

    public List<RendezVous> getAppointmentsByDoctor(int idDocteur) throws SQLException {
        return rendezVousDAO.findByDocteur(idDocteur);
    }

    public List<RendezVous> getAllAppointments() throws SQLException {
        return rendezVousDAO.findAll();
    }

    public void updateAppointmentStatus(UpdateAppointmentStatusDTO req) throws SQLException {
        if (req == null || req.getIdRdv() <= 0) {
            throw new ValidationException("idRdv obligatoire et valide.");
        }
        if (req.getStatut() == null || req.getStatut().isBlank()) {
            throw new ValidationException("statut obligatoire.");
        }

        String nouveauStatut = req.getStatut().trim().toUpperCase();
        if (!STATUTS_VALIDES.contains(nouveauStatut)) {
            throw new ValidationException("Statut invalide. Valeurs acceptées : " + STATUTS_VALIDES);
        }

        boolean updated = rendezVousDAO.updateStatut(req.getIdRdv(), nouveauStatut);
        if (!updated) {
            throw new ResourceNotFoundException("Rendez-vous introuvable avec l'ID : " + req.getIdRdv());
        }
    }

    public void updateAppointmentStatusByDoctor(UpdateAppointmentStatusDTO req, int idDocteur) throws SQLException {
        if (req == null || req.getIdRdv() <= 0) {
            throw new ValidationException("idRdv obligatoire et valide.");
        }
        Optional<RendezVous> optRdv = rendezVousDAO.findById(req.getIdRdv());
        if (optRdv.isEmpty()) {
            throw new ResourceNotFoundException("Rendez-vous introuvable avec l'ID : " + req.getIdRdv());
        }
        RendezVous rdv = optRdv.get();
        if (rdv.getIdDocteur() != idDocteur) {
            throw new ForbiddenException("Accès refusé : vous n'êtes pas le médecin assigné à ce rendez-vous.");
        }
        updateAppointmentStatus(req);
    }

    public void cancelAppointmentByPatient(int idRdv, int idPatient) throws SQLException {
        if (idRdv <= 0) {
            throw new ValidationException("idRdv obligatoire et valide.");
        }
        if (idPatient <= 0) {
            throw new ValidationException("idPatient obligatoire et valide.");
        }

        // 1. Atomic conditional update: only succeeds if statut is DEMANDE and id_patient matches
        int updatedRows = rendezVousDAO.cancelByPatientAtomic(idRdv, idPatient);
        if (updatedRows > 0) {
            return;
        }

        // 2. If 0 rows updated, inspect exact state to return precise HTTP status code
        Optional<RendezVous> optRdv = rendezVousDAO.findById(idRdv);
        if (optRdv.isEmpty()) {
            throw new ResourceNotFoundException("Rendez-vous introuvable avec l'ID : " + idRdv);
        }

        RendezVous rdv = optRdv.get();
        if (rdv.getIdPatient() != idPatient) {
            throw new ForbiddenException("Accès refusé : vous ne pouvez pas annuler le rendez-vous d'un autre patient.");
        }

        if (!"DEMANDE".equalsIgnoreCase(rdv.getStatut())) {
            throw new ConflictException("Impossible d'annuler ce rendez-vous : son statut actuel est déjà " + rdv.getStatut() + ".");
        }

        // Race condition / concurrency conflict
        throw new ConflictException("Le statut du rendez-vous a été modifié entre-temps.");
    }
}
