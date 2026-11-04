
package com.enicarthage.medicament.service;

import com.enicarthage.medicament.dao.MedicamentDAO;
import com.enicarthage.medicament.dto.MedicamentRequest;
import com.enicarthage.medicament.model.Medicament;

import java.sql.SQLException;
import java.util.List;

public class MedicamentService {

    private final MedicamentDAO dao = new MedicamentDAO();

    // LISTER
    public List<Medicament> list() throws SQLException {
        return dao.findAll();
    }

    // RECHERCHER
    public Medicament getById(int id) throws SQLException {
        return dao.findById(id);
    }

    // AJOUTER
    public Medicament create(MedicamentRequest request)
            throws SQLException {

        String nom = validate(
                request == null ? null : request.getNom(),
                "Nom",
                150
        );

        String dosage = validate(
                request == null ? null : request.getDosage(),
                "Dosage",
                100
        );

        return dao.create(nom, dosage);
    }

    // MODIFIER
    public Medicament update(int id, MedicamentRequest request)
            throws SQLException {

        String nom = validate(
                request == null ? null : request.getNom(),
                "Nom",
                150
        );

        String dosage = validate(
                request == null ? null : request.getDosage(),
                "Dosage",
                100
        );

        boolean updated = dao.update(id, nom, dosage);

        if (!updated) {
            return null;
        }

        return new Medicament(id, nom, dosage);
    }

    // SUPPRIMER
    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    // VALIDATION DES CHAMPS
    private String validate(String value, String champ, int max) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    champ + " obligatoire"
            );
        }

        String cleaned = value.trim();

        if (cleaned.length() > max) {
            throw new IllegalArgumentException(
                    champ + " dépasse " + max + " caractères"
            );
        }

        return cleaned;
    }
}
