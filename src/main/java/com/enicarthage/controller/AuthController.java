
package com.enicarthage.controller;

import com.enicarthage.model.AuthResponse;
import com.enicarthage.model.UtilisateurAuth;
import com.enicarthage.service.AuthService;
import com.enicarthage.util.HttpUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;

public class AuthController implements HttpHandler {

    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    private static class LoginRequest {
        String login;
        String password;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpUtils.handleCorsAndPreflight(exchange)) {
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Seul POST est supporté.");
                return;
            }

            String body = HttpUtils.readRequestBody(exchange);
            LoginRequest request = gson.fromJson(body, LoginRequest.class);

            if (request == null
                    || request.login == null
                    || request.password == null
                    || request.login.isBlank()
                    || request.password.isBlank()) {

                HttpUtils.sendError(exchange, 400, "Identifiant et mot de passe sont obligatoires.");
                return;
            }

            UtilisateurAuth user = authService.authenticate(
                    request.login.trim(),
                    request.password
            );

            if (user == null) {
                HttpUtils.sendError(exchange, 401, "Identifiants incorrects (login ou mot de passe invalide).");
                return;
            }

            AuthResponse response = new AuthResponse(
                    true,
                    "Authentification réussie.",
                    user.getIdUtilisateur(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getLogin(),
                    user.getRole()
            );

            HttpUtils.sendJson(exchange, 200, response);

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (SQLException e) {
            System.err.println("[AuthController] Database error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données lors de l'authentification.");
        } catch (Exception e) {
            System.err.println("[AuthController] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur inattendue.");
        } finally {
            exchange.close();
        }
    }
}
