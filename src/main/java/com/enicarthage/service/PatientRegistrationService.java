package com.enicarthage.service;

import com.enicarthage.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class PatientRegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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
                || isBlank(password) || isBlank(dateNaissance)
                || isBlank(adresse)) {
            throw new IllegalArgumentException(
                    "Tous les champs sont obligatoires (nom, prénom, email, date de naissance, adresse, identifiant, mot de passe)."
            );
        }

        // Email format validation
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Format d'adresse email invalide (ex: exemple@domaine.com)."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit comporter au moins 8 caractères."
            );
        }

        // Convert and validate the date received from React
        Date sqlDate;
        try {
            LocalDate parsedDate = LocalDate.parse(dateNaissance.trim());
            if (parsedDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "La date de naissance ne peut pas être dans le futur."
                );
            }
            sqlDate = Date.valueOf(parsedDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Format de date de naissance invalide (AAAA-MM-JJ attendu)."
            );
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

                    patientStatement.setDate(2, sqlDate);
                    patientStatement.setString(3, adresse.trim());

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