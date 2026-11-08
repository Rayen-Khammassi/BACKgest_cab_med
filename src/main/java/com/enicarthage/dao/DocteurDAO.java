package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.DocteurInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocteurDAO {

    public List<DocteurInfo> findAll() throws SQLException {
        String sql = """
            SELECT
                d.id_utilisateur,
                u.nom,
                u.prenom,
                d.specialite,
                d.numero_ordre
            FROM docteur d
            JOIN utilisateur u ON d.id_utilisateur = u.id_utilisateur
            ORDER BY u.nom, u.prenom
            """;

        List<DocteurInfo> docteurs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                docteurs.add(new DocteurInfo(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("specialite"),
                        rs.getString("numero_ordre")
                ));
            }
        }

        return docteurs;
    }
}
