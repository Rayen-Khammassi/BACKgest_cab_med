
package com.enicarthage.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.model.UtilisateurAuth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilisateurDAO {

    public UtilisateurAuth findByLogin(String login)
            throws SQLException {

        String sql = """
            SELECT
                u.id_utilisateur,
                u.nom,
                u.prenom,
                u.login,
                u.mot_de_passe,
                CASE
                    WHEN p.id_utilisateur IS NOT NULL THEN 'PATIENT'
                    WHEN d.id_utilisateur IS NOT NULL THEN 'DOCTEUR'
                    WHEN s.id_utilisateur IS NOT NULL THEN 'SECRETAIRE'
                    ELSE NULL
                END AS role,
                p.actif AS patient_actif
            FROM utilisateur u
            LEFT JOIN patient p
                ON u.id_utilisateur = p.id_utilisateur
            LEFT JOIN docteur d
                ON u.id_utilisateur = d.id_utilisateur
            LEFT JOIN secretaire s
                ON u.id_utilisateur = s.id_utilisateur
            WHERE u.login = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    String role = rs.getString("role");

                    if (role == null) {
                        return null;
                    }

                    boolean actif = true;
                    if ("PATIENT".equalsIgnoreCase(role)) {
                        Object actifObj = rs.getObject("patient_actif");
                        if (actifObj != null) {
                            actif = rs.getBoolean("patient_actif");
                        }
                    }

                    return new UtilisateurAuth(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        role,
                        actif
                    );
                }
            }
        }

        return null;
    }
}
