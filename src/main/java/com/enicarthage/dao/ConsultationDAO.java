package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.Consultation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsultationDAO {

    private static final String SELECT_BASE = """
        SELECT
            c.id_consultation,
            c.date_consultation,
            c.diagnostic,
            c.observations,
            c.id_rdv,
            r.id_patient,
            up.nom AS patient_nom,
            up.prenom AS patient_prenom,
            up.email AS patient_email,
            p.date_naissance AS patient_date_naissance,
            r.id_docteur,
            ud.nom AS docteur_nom,
            ud.prenom AS docteur_prenom,
            d.specialite AS docteur_specialite
        FROM consultation c
        JOIN rendez_vous r ON c.id_rdv = r.id_rdv
        JOIN utilisateur up ON r.id_patient = up.id_utilisateur
        JOIN patient p ON r.id_patient = p.id_utilisateur
        JOIN docteur d ON r.id_docteur = d.id_utilisateur
        JOIN utilisateur ud ON d.id_utilisateur = ud.id_utilisateur
        """;

    public int create(Consultation c) throws SQLException {
        String sql = """
            INSERT INTO consultation (date_consultation, diagnostic, observations, id_rdv)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idConsultation = -1;
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, c.getDateConsultation());
                    stmt.setString(2, c.getDiagnostic());
                    stmt.setString(3, c.getObservations());
                    stmt.setInt(4, c.getIdRdv());
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            idConsultation = keys.getInt(1);
                        }
                    }
                }

                // Update rendez-vous status to 'TERMINE'
                String updateRdv = "UPDATE rendez_vous SET statut = 'TERMINE' WHERE id_rdv = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateRdv)) {
                    stmt.setInt(1, c.getIdRdv());
                    stmt.executeUpdate();
                }

                conn.commit();
                return idConsultation;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean update(int idConsultation, String diagnostic, String observations) throws SQLException {
        String sql = "UPDATE consultation SET diagnostic = ?, observations = ? WHERE id_consultation = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, diagnostic);
            stmt.setString(2, observations);
            stmt.setInt(3, idConsultation);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int idConsultation) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Get associated id_rdv
                int idRdv = -1;
                String selectRdv = "SELECT id_rdv FROM consultation WHERE id_consultation = ?";
                try (PreparedStatement stmt = conn.prepareStatement(selectRdv)) {
                    stmt.setInt(1, idConsultation);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            idRdv = rs.getInt("id_rdv");
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // 2. Delete linked ordonnance(s) (prescriptions cascade automatically)
                String deleteOrdonnances = "DELETE FROM ordonnance WHERE id_consultation = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteOrdonnances)) {
                    stmt.setInt(1, idConsultation);
                    stmt.executeUpdate();
                }

                // 3. Delete consultation
                String deleteConsultation = "DELETE FROM consultation WHERE id_consultation = ?";
                int affectedRows;
                try (PreparedStatement stmt = conn.prepareStatement(deleteConsultation)) {
                    stmt.setInt(1, idConsultation);
                    affectedRows = stmt.executeUpdate();
                }

                // 4. Revert rendez_vous status back to 'CONFIRME' if rdv exists
                if (idRdv > 0) {
                    String revertRdv = "UPDATE rendez_vous SET statut = 'CONFIRME' WHERE id_rdv = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(revertRdv)) {
                        stmt.setInt(1, idRdv);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Consultation> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY c.date_consultation DESC";
        return executeQuery(sql, null);
    }

    public List<Consultation> findByDocteur(int idDocteur) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.id_docteur = ? ORDER BY c.date_consultation DESC";
        return executeQuery(sql, idDocteur);
    }

    public List<Consultation> findByPatient(int idPatient) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.id_patient = ? ORDER BY c.date_consultation DESC";
        return executeQuery(sql, idPatient);
    }

    public Optional<Consultation> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.id_consultation = ?";
        List<Consultation> list = executeQuery(sql, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<Consultation> executeQuery(String sql, Integer param) throws SQLException {
        List<Consultation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param != null) {
                stmt.setInt(1, param);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Consultation c = new Consultation();
                    c.setIdConsultation(rs.getInt("id_consultation"));
                    c.setDateConsultation(rs.getString("date_consultation"));
                    c.setDiagnostic(rs.getString("diagnostic"));
                    c.setObservations(rs.getString("observations"));
                    c.setIdRdv(rs.getInt("id_rdv"));
                    c.setIdPatient(rs.getInt("id_patient"));
                    c.setPatientNom(rs.getString("patient_nom"));
                    c.setPatientPrenom(rs.getString("patient_prenom"));
                    c.setPatientEmail(rs.getString("patient_email"));
                    Date d = rs.getDate("patient_date_naissance");
                    c.setPatientDateNaissance(d != null ? d.toString() : "");
                    c.setIdDocteur(rs.getInt("id_docteur"));
                    c.setDocteurNom(rs.getString("docteur_nom"));
                    c.setDocteurPrenom(rs.getString("docteur_prenom"));
                    c.setDocteurSpecialite(rs.getString("docteur_specialite"));
                    list.add(c);
                }
            }
        }
        return list;
    }
}
