
package com.enicarthage.medicament.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.medicament.model.Medicament;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentDAO {

    // 1. LISTER TOUS LES MEDICAMENTS
    public List<Medicament> findAll() throws SQLException {

        String sql = """
            SELECT id_medicament, nom, dosage
            FROM medicament
            ORDER BY id_medicament DESC
            """;

        List<Medicament> medicaments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medicaments.add(mapRow(rs));
            }
        }

        return medicaments;
    }

    // 2. RECHERCHER PAR IDENTIFIANT
    public Medicament findById(int id) throws SQLException {

        String sql = """
            SELECT id_medicament, nom, dosage
            FROM medicament
            WHERE id_medicament = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    // 3. AJOUTER UN MEDICAMENT
    public Medicament create(String nom, String dosage)
            throws SQLException {

        String sql = """
            INSERT INTO medicament (nom, dosage)
            VALUES (?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nom);
            stmt.setString(2, dosage);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {

                if (keys.next()) {
                    int id = keys.getInt(1);

                    return new Medicament(id, nom, dosage);
                }

                throw new SQLException(
                        "Impossible de récupérer l'identifiant"
                );
            }
        }
    }

    // 4. MODIFIER UN MEDICAMENT
    public boolean update(int id, String nom, String dosage)
            throws SQLException {

        String sql = """
            UPDATE medicament
            SET nom = ?, dosage = ?
            WHERE id_medicament = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            stmt.setString(2, dosage);
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. SUPPRIMER UN MEDICAMENT
    public boolean delete(int id) throws SQLException {

        String sql = """
            DELETE FROM medicament
            WHERE id_medicament = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        }
    }

    // CONVERTIR RESULTSET EN OBJET MEDICAMENT
    private Medicament mapRow(ResultSet rs)
            throws SQLException {

        return new Medicament(
                rs.getInt("id_medicament"),
                rs.getString("nom"),
                rs.getString("dosage")
        );
    }
}
