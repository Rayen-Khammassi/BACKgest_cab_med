package com.enicarthage.service;

import com.enicarthage.dao.RendezVousDAO;
import com.enicarthage.dto.CreateAppointmentDTO;
import com.enicarthage.dto.UpdateAppointmentStatusDTO;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.RendezVous;

import java.sql.SQLException;
import java.util.List;
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
}
