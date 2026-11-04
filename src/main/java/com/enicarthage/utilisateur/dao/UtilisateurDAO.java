
package com.enicarthage.utilisateur.dao;

import com.enicarthage.config.DatabaseConnection;
import com.enicarthage.utilisateur.model.UtilisateurAuth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;

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
                END AS role
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

                    return new UtilisateurAuth(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        role
                    );
                }
            }
        }

        return null;
    }

    public int createPatient(
            String nom,
            String prenom,
            String email,
            String login,
            String passwordHash,
            LocalDate dateNaissance,
            String adresse
    ) throws SQLException {

        String sqlUtilisateur = """
        INSERT INTO utilisateur
        (nom, prenom, email, login, mot_de_passe)
        VALUES (?, ?, ?, ?, ?)
        """;

        String sqlPatient = """
        INSERT INTO patient
        (id_utilisateur, date_naissance, adresse, actif)
        VALUES (?, ?, ?, TRUE)
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int idUtilisateur;

                // 1. Enregistrer l'utilisateur
                try (PreparedStatement stmt = conn.prepareStatement(
                        sqlUtilisateur,
                        Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, nom);
                    stmt.setString(2, prenom);
                    stmt.setString(3, email);
                    stmt.setString(4, login);
                    stmt.setString(5, passwordHash);

                    stmt.executeUpdate();

                    // 2. Récupérer son identifiant
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Identifiant utilisateur non généré"
                            );
                        }
                        idUtilisateur = keys.getInt(1);
                    }
                }

                // 3. Enregistrer le patient
                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlPatient)) {

                    stmt.setInt(1, idUtilisateur);
                    stmt.setDate(2, Date.valueOf(dateNaissance));
                    stmt.setString(3, adresse);

                    stmt.executeUpdate();
                }

                // 4. Valider les deux insertions
                conn.commit();

                return idUtilisateur;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
