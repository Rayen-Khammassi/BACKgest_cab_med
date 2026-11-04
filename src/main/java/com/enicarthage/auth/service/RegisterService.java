package com.enicarthage.auth.service;

import com.enicarthage.utilisateur.dao.UtilisateurDAO;
import com.enicarthage.auth.dto.RegisterRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class RegisterService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UtilisateurDAO utilisateurDAO =
            new UtilisateurDAO();

    public int registerPatient(RegisterRequest request)
            throws SQLException {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Données d'inscription manquantes"
            );
        }

        String nom = required(request.getNom(), "Nom", 100);
        String prenom = required(request.getPrenom(), "Prénom", 100);
        String email = required(request.getEmail(), "Email", 150);
        String login = required(request.getLogin(), "Login", 100);
        String adresse = required(request.getAdresse(), "Adresse", 255);
        String password = request.getPassword();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Adresse e-mail invalide"
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Mot de passe trop long (72 octets maximum)"
            );
        }

        LocalDate dateNaissance;

        try {
            dateNaissance = LocalDate.parse(
                    request.getDateNaissance()
            );
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Date de naissance invalide (AAAA-MM-JJ)"
            );
        }

        if (dateNaissance.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La date de naissance doit être dans le passé"
            );
        }

        // Hachage du mot de passe
        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        // Enregistrement utilisateur + patient
        return utilisateurDAO.createPatient(
                nom,
                prenom,
                email,
                login,
                passwordHash,
                dateNaissance,
                adresse
        );
    }

    private static String required(
            String value,
            String field,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " obligatoire"
            );
        }

        String clean = value.trim();

        if (clean.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " dépasse " + maxLength + " caractères"
            );
        }

        return clean;
    }
}