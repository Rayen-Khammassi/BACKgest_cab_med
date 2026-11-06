package com.enicarthage.service;

import com.enicarthage.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

public class PatientRegistrationService {

    public void registerPatient(
            String nom,
            String prenom,
            String email,
            String login,
            String password,
            String dateNaissance,
            String adresse
    ) throws SQLException {

        // Validate required fields
        if (isBlank(nom) || isBlank(prenom)
                || isBlank(email) || isBlank(login)
                || isBlank(password)) {
            throw new IllegalArgumentException(
                    "Please fill in all required fields."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        // Convert the date received from React
        Date sqlDate = null;

        if (!isBlank(dateNaissance)) {
            try {
                sqlDate = Date.valueOf(LocalDate.parse(dateNaissance));
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid date of birth."
                );
            }
        }

        // Hash the password before storing it
        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        String userSql = """
            INSERT INTO utilisateur
                (nom, prenom, email, login, mot_de_passe)
            VALUES (?, ?, ?, ?, ?)
            """;

        String patientSql = """
            INSERT INTO patient
                (id_utilisateur, date_naissance, adresse)
            VALUES (?, ?, ?)
            """;

        // Both inserts must succeed together
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int userId;

                // Insert the general user information
                try (PreparedStatement userStatement =
                             connection.prepareStatement(
                                     userSql,
                                     PreparedStatement.RETURN_GENERATED_KEYS
                             )) {

                    userStatement.setString(1, nom.trim());
                    userStatement.setString(2, prenom.trim());
                    userStatement.setString(3, email.trim());
                    userStatement.setString(4, login.trim());
                    userStatement.setString(5, hashedPassword);

                    userStatement.executeUpdate();

                    try (ResultSet keys =
                                 userStatement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not retrieve the new user ID."
                            );
                        }

                        userId = keys.getInt(1);
                    }
                }

                // Insert the patient-specific information
                try (PreparedStatement patientStatement =
                             connection.prepareStatement(patientSql)) {

                    patientStatement.setInt(1, userId);

                    if (sqlDate == null) {
                        patientStatement.setNull(
                                2,
                                java.sql.Types.DATE
                        );
                    } else {
                        patientStatement.setDate(2, sqlDate);
                    }

                    patientStatement.setString(3, adresse);

                    patientStatement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}