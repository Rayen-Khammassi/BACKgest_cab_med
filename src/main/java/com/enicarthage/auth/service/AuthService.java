
package com.enicarthage.auth.service;

import com.enicarthage.utilisateur.dao.UtilisateurDAO;
import com.enicarthage.utilisateur.model.UtilisateurAuth;

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

        // 5. Return the user if authentication succeeds
        if (passwordCorrect) {
            return utilisateur;
        }

        // Invalid password
        return null;
    }
}
