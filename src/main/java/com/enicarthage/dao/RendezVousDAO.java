package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.RendezVous;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    private static final String SELECT_BASE = """
        SELECT
            r.id_rdv,
            r.date_heure,
            r.motif,
            r.statut,
            r.id_patient,
            up.nom AS patient_nom,
            up.prenom AS patient_prenom,
            up.email AS patient_email,
            r.id_docteur,
            ud.nom AS docteur_nom,
            ud.prenom AS docteur_prenom,
            d.specialite AS docteur_specialite
        FROM rendez_vous r
        JOIN utilisateur up ON r.id_patient = up.id_utilisateur
        JOIN docteur d ON r.id_docteur = d.id_utilisateur
        JOIN utilisateur ud ON d.id_utilisateur = ud.id_utilisateur
        """;

    public int create(RendezVous rdv) throws SQLException {
        String sql = """
            INSERT INTO rendez_vous (date_heure, motif, statut, id_patient, id_docteur)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, rdv.getDateHeure());
            stmt.setString(2, rdv.getMotif());
            stmt.setString(3, (rdv.getStatut() == null || rdv.getStatut().isBlank()) ? "DEMANDE" : rdv.getStatut());
            stmt.setInt(4, rdv.getIdPatient());
            stmt.setInt(5, rdv.getIdDocteur());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<RendezVous> findByPatient(int idPatient) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.id_patient = ? ORDER BY r.date_heure DESC";
        return executeQuery(sql, idPatient);
    }

    public List<RendezVous> findByDocteur(int idDocteur) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.id_docteur = ? ORDER BY r.date_heure DESC";
        return executeQuery(sql, idDocteur);
    }

    public List<RendezVous> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY r.date_heure DESC";
        return executeQuery(sql, null);
    }

    public boolean updateStatut(int idRdv, String nouveauStatut) throws SQLException {
        String sql = "UPDATE rendez_vous SET statut = ? WHERE id_rdv = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nouveauStatut);
            stmt.setInt(2, idRdv);
            return stmt.executeUpdate() > 0;
        }
    }

    private List<RendezVous> executeQuery(String sql, Integer param) throws SQLException {
        List<RendezVous> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param != null) {
                stmt.setInt(1, param);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new RendezVous(
                            rs.getInt("id_rdv"),
                            rs.getString("date_heure"),
                            rs.getString("motif"),
                            rs.getString("statut"),
                            rs.getInt("id_patient"),
                            rs.getString("patient_nom"),
                            rs.getString("patient_prenom"),
                            rs.getString("patient_email"),
                            rs.getInt("id_docteur"),
                            rs.getString("docteur_nom"),
                            rs.getString("docteur_prenom"),
                            rs.getString("docteur_specialite")
                    ));
                }
            }
        }
        return list;
    }
}
