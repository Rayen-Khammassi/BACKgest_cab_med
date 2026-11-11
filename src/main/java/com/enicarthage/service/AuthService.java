
package com.enicarthage.service;

import com.enicarthage.dao.UtilisateurDAO;
import com.enicarthage.model.UtilisateurAuth;
import com.enicarthage.exception.ForbiddenException;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public UtilisateurAuth authenticate(String login, String password)
            throws SQLException {

        // 1. Check that fields are not empty
        if (login == null || login.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        // 2. Search for the user in the database
        UtilisateurAuth utilisateur =
                utilisateurDAO.findByLogin(login);

        // 3. Check whether the user exists
        if (utilisateur == null) {
            return null;
        }

        // 4. Verify the password using BCrypt
        boolean passwordCorrect = BCrypt.checkpw(
                password,
                utilisateur.getMotDePasse()
        );

        if (!passwordCorrect) {
            return null;
        }

        // 5. If user is a PATIENT, verify that the account is active
        if ("PATIENT".equalsIgnoreCase(utilisateur.getRole()) && !utilisateur.isActif()) {
            throw new ForbiddenException("Votre compte patient a été désactivé par le cabinet. Veuillez contacter l'accueil.");
        }

        return utilisateur;
    }
}
