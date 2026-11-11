package com.enicarthage.service;

import com.enicarthage.dao.MedicamentDAO;
import com.enicarthage.exception.ResourceNotFoundException;
import com.enicarthage.exception.ValidationException;
import com.enicarthage.model.Medicament;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MedicamentService {

    private final MedicamentDAO medicamentDAO;

    public MedicamentService() {
        this.medicamentDAO = new MedicamentDAO();
    }

    public MedicamentService(MedicamentDAO medicamentDAO) {
        this.medicamentDAO = medicamentDAO;
    }

    public List<Medicament> getAllMedicaments() throws SQLException {
        return medicamentDAO.findAll();
    }

    public Medicament getMedicamentById(int id) throws SQLException {
        Optional<Medicament> m = medicamentDAO.findById(id);
        if (m.isEmpty()) {
            throw new ResourceNotFoundException("Médicament introuvable avec l'ID : " + id);
        }
        return m.get();
    }

    public Medicament createMedicament(String nom, String dosage) throws SQLException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom du médicament est obligatoire.");
        }
        if (dosage == null || dosage.trim().isEmpty()) {
            throw new ValidationException("Le dosage du médicament est obligatoire.");
        }

        Medicament m = new Medicament(0, nom.trim(), dosage.trim());
        return medicamentDAO.create(m);
    }

    public Medicament updateMedicament(int id, String nom, String dosage) throws SQLException {
        if (id <= 0) throw new ValidationException("Identifiant de médicament invalide.");
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom du médicament est obligatoire.");
        }
        if (dosage == null || dosage.trim().isEmpty()) {
            throw new ValidationException("Le dosage du médicament est obligatoire.");
        }

        Medicament m = new Medicament(id, nom.trim(), dosage.trim());
        Optional<Medicament> updated = medicamentDAO.update(id, m);
        if (updated.isEmpty()) {
            throw new ResourceNotFoundException("Médicament introuvable avec l'ID : " + id);
        }
        return updated.get();
    }

    public boolean deleteMedicament(int id) throws SQLException {
        if (id <= 0) throw new ValidationException("Identifiant de médicament invalide.");
        boolean deleted = medicamentDAO.delete(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Médicament introuvable avec l'ID : " + id);
        }
        return true;
    }
}
