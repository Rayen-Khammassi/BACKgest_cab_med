
package com.enicarthage.main;

import com.enicarthage.model.UtilisateurAuth;
import com.enicarthage.service.AuthService;

import java.sql.SQLException;

public class AuthTest {

    public static void main(String[] args) {

        AuthService authService = new AuthService();

        try {
            UtilisateurAuth user = authService.authenticate(
                    "patient1",
                    "Test123!"
            );

            if (user != null) {
                System.out.println("Authentication successful!");
                System.out.println("Welcome " + user.getPrenom());
                System.out.println("Role: " + user.getRole());
            } else {
                System.out.println("Invalid login or password.");
            }

        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
    }
}