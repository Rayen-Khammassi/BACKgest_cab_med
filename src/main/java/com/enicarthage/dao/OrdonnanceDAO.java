package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.Ordonnance;
import com.enicarthage.model.PrescriptionItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceDAO {

    public int create(Ordonnance ord, List<PrescriptionItem> items) throws SQLException {
        String sqlOrd = "INSERT INTO ordonnance (date_creation, id_consultation) VALUES (?, ?)";
        String sqlPresc = """
            INSERT INTO prescription (posologie, frequence, duree, quantite, id_ordonnance, id_medicament)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idOrdonnance = -1;
                try (PreparedStatement stmt = conn.prepareStatement(sqlOrd, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, ord.getDateCreation());
                    stmt.setInt(2, ord.getIdConsultation());
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            idOrdonnance = keys.getInt(1);
                        }
                    }
                }

                if (idOrdonnance > 0 && items != null && !items.isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlPresc)) {
                        for (PrescriptionItem item : items) {
                            stmt.setString(1, item.getPosologie());
                            stmt.setString(2, item.getFrequence());
                            stmt.setString(3, item.getDuree());
                            stmt.setInt(4, item.getQuantite() <= 0 ? 1 : item.getQuantite());
                            stmt.setInt(5, idOrdonnance);
                            stmt.setInt(6, item.getIdMedicament());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                conn.commit();
                return idOrdonnance;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Ordonnance> findByPatient(int idPatient) throws SQLException {
        String sql = """
            SELECT
                o.id_ordonnance,
                o.date_creation,
                o.id_consultation,
                c.diagnostic,
                r.id_patient,
                up.nom AS patient_nom,
                up.prenom AS patient_prenom,
                up.email AS patient_email,
                r.id_docteur,
                ud.nom AS docteur_nom,
                ud.prenom AS docteur_prenom,
                d.specialite AS docteur_specialite
            FROM ordonnance o
            JOIN consultation c ON o.id_consultation = c.id_consultation
            JOIN rendez_vous r ON c.id_rdv = r.id_rdv
            JOIN utilisateur up ON r.id_patient = up.id_utilisateur
            JOIN docteur d ON r.id_docteur = d.id_utilisateur
            JOIN utilisateur ud ON d.id_utilisateur = ud.id_utilisateur
            WHERE r.id_patient = ?
            ORDER BY o.date_creation DESC, o.id_ordonnance DESC
            """;

        return fetchOrdonnancesWithItems(sql, idPatient);
    }

    public List<Ordonnance> findByDocteur(int idDocteur) throws SQLException {
        String sql = """
            SELECT
                o.id_ordonnance,
                o.date_creation,
                o.id_consultation,
                c.diagnostic,
                r.id_patient,
                up.nom AS patient_nom,
                up.prenom AS patient_prenom,
                up.email AS patient_email,
                r.id_docteur,
                ud.nom AS docteur_nom,
                ud.prenom AS docteur_prenom,
                d.specialite AS docteur_specialite
            FROM ordonnance o
            JOIN consultation c ON o.id_consultation = c.id_consultation
            JOIN rendez_vous r ON c.id_rdv = r.id_rdv
            JOIN utilisateur up ON r.id_patient = up.id_utilisateur
            JOIN docteur d ON r.id_docteur = d.id_utilisateur
            JOIN utilisateur ud ON d.id_utilisateur = ud.id_utilisateur
            WHERE r.id_docteur = ?
            ORDER BY o.date_creation DESC, o.id_ordonnance DESC
            """;

        return fetchOrdonnancesWithItems(sql, idDocteur);
    }

    public List<Ordonnance> findAll() throws SQLException {
        String sql = """
            SELECT
                o.id_ordonnance,
                o.date_creation,
                o.id_consultation,
                c.diagnostic,
                r.id_patient,
                up.nom AS patient_nom,
                up.prenom AS patient_prenom,
                up.email AS patient_email,
                r.id_docteur,
                ud.nom AS docteur_nom,
                ud.prenom AS docteur_prenom,
                d.specialite AS docteur_specialite
            FROM ordonnance o
            JOIN consultation c ON o.id_consultation = c.id_consultation
            JOIN rendez_vous r ON c.id_rdv = r.id_rdv
            JOIN utilisateur up ON r.id_patient = up.id_utilisateur
            JOIN docteur d ON r.id_docteur = d.id_utilisateur
            JOIN utilisateur ud ON d.id_utilisateur = ud.id_utilisateur
            ORDER BY o.date_creation DESC, o.id_ordonnance DESC
            """;

        return fetchOrdonnancesWithItems(sql, null);
    }

    private List<Ordonnance> fetchOrdonnancesWithItems(String sql, Integer param) throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param != null) {
                stmt.setInt(1, param);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ordonnance ord = new Ordonnance();
                    ord.setIdOrdonnance(rs.getInt("id_ordonnance"));
                    Date d = rs.getDate("date_creation");
                    ord.setDateCreation(d != null ? d.toString() : "");
                    ord.setIdConsultation(rs.getInt("id_consultation"));
                    ord.setDiagnostic(rs.getString("diagnostic"));
                    ord.setIdPatient(rs.getInt("id_patient"));
                    ord.setPatientNom(rs.getString("patient_nom"));
                    ord.setPatientPrenom(rs.getString("patient_prenom"));
                    ord.setPatientEmail(rs.getString("patient_email"));
                    ord.setIdDocteur(rs.getInt("id_docteur"));
                    ord.setDocteurNom(rs.getString("docteur_nom"));
                    ord.setDocteurPrenom(rs.getString("docteur_prenom"));
                    ord.setDocteurSpecialite(rs.getString("docteur_specialite"));

                    // Fetch prescription items for this ordonnance
                    ord.setItems(fetchItems(conn, ord.getIdOrdonnance()));
                    list.add(ord);
                }
            }
        }
        return list;
    }

    private List<PrescriptionItem> fetchItems(Connection conn, int idOrdonnance) throws SQLException {
        String sql = """
            SELECT
                p.id_prescription,
                p.id_ordonnance,
                p.id_medicament,
                m.nom AS nom_medicament,
                m.dosage AS dosage_medicament,
                p.posologie,
                p.frequence,
                p.duree,
                p.quantite
            FROM prescription p
            JOIN medicament m ON p.id_medicament = m.id_medicament
            WHERE p.id_ordonnance = ?
            ORDER BY p.id_prescription
            """;

        List<PrescriptionItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idOrdonnance);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setIdPrescription(rs.getInt("id_prescription"));
                    item.setIdOrdonnance(rs.getInt("id_ordonnance"));
                    item.setIdMedicament(rs.getInt("id_medicament"));
                    item.setNomMedicament(rs.getString("nom_medicament"));
                    item.setDosageMedicament(rs.getString("dosage_medicament"));
                    item.setPosologie(rs.getString("posologie"));
                    item.setFrequence(rs.getString("frequence"));
                    item.setDuree(rs.getString("duree"));
                    item.setQuantite(rs.getInt("quantite"));
                    items.add(item);
                }
            }
        }
        return items;
    }
}
