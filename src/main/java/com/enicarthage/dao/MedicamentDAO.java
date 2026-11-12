package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.Medicament;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicamentDAO {

    // ── Map a ResultSet row to a Medicament ──────────────────────────────────
    private Medicament map(ResultSet rs) throws SQLException {
        return new Medicament(
                rs.getInt("id_medicament"),
                rs.getString("nom"),
                rs.getString("dosage")
        );
    }

    // ── LIST ALL ─────────────────────────────────────────────────────────────
    public List<Medicament> findAll() throws SQLException {
        String sql = "SELECT id_medicament, nom, dosage FROM medicament ORDER BY nom";
        List<Medicament> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    // ── FIND BY ID ───────────────────────────────────────────────────────────
    public Optional<Medicament> findById(int id) throws SQLException {
        String sql = "SELECT id_medicament, nom, dosage FROM medicament WHERE id_medicament = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public Medicament create(Medicament m) throws SQLException {
        String sql = "INSERT INTO medicament (nom, dosage) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getDosage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setIdMedicament(keys.getInt(1));
                }
            }
        }
        return m;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public Optional<Medicament> update(int id, Medicament m) throws SQLException {
        String sql = "UPDATE medicament SET nom = ?, dosage = ? WHERE id_medicament = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getDosage());
            ps.setInt(3, id);
            int rows = ps.executeUpdate();
            if (rows == 0) return Optional.empty();
            m.setIdMedicament(id);
            return Optional.of(m);
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM medicament WHERE id_medicament = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
