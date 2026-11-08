package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.PatientDetails;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAO {

    private static final String SELECT_BASE = """
        SELECT
            u.id_utilisateur,
            u.nom,
            u.prenom,
            u.email,
            p.date_naissance,
            p.adresse,
            p.actif,
            (SELECT COUNT(*) FROM rendez_vous r WHERE r.id_patient = u.id_utilisateur) AS total_rdv,
            (SELECT COUNT(*) FROM consultation c JOIN rendez_vous r2 ON c.id_rdv = r2.id_rdv WHERE r2.id_patient = u.id_utilisateur) AS total_consultations
        FROM patient p
        JOIN utilisateur u ON p.id_utilisateur = u.id_utilisateur
        """;

    public List<PatientDetails> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY u.nom, u.prenom";
        List<PatientDetails> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<PatientDetails> findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean update(int id, String nom, String prenom, String email, String dateNaissance, String adresse) throws SQLException {
        String sqlUser = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ? WHERE id_utilisateur = ?";
        String sqlPatient = "UPDATE patient SET date_naissance = ?, adresse = ? WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {
                    stmtUser.setString(1, nom);
                    stmtUser.setString(2, prenom);
                    stmtUser.setString(3, email);
                    stmtUser.setInt(4, id);
                    stmtUser.executeUpdate();
                }

                try (PreparedStatement stmtPatient = conn.prepareStatement(sqlPatient)) {
                    if (dateNaissance != null && !dateNaissance.isBlank()) {
                        stmtPatient.setDate(1, Date.valueOf(dateNaissance));
                    } else {
                        stmtPatient.setNull(1, Types.DATE);
                    }
                    stmtPatient.setString(2, adresse);
                    stmtPatient.setInt(3, id);
                    stmtPatient.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean setActif(int id, boolean actif) throws SQLException {
        String sql = "UPDATE patient SET actif = ? WHERE id_utilisateur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, actif);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private PatientDetails map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date_naissance");
        String dateStr = (d != null) ? d.toString() : "";

        return new PatientDetails(
                rs.getInt("id_utilisateur"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                dateStr,
                rs.getString("adresse"),
                rs.getBoolean("actif"),
                rs.getInt("total_rdv"),
                rs.getInt("total_consultations")
        );
    }
}
