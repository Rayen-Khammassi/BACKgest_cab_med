package com.enicarthage.controller;

import com.enicarthage.dto.AuthResponseDTO;
import com.enicarthage.dto.LoginRequestDTO;
import com.enicarthage.dto.UtilisateurAuthDTO;
import com.enicarthage.service.AuthService;
import com.enicarthage.util.HttpUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;

public class AuthController implements HttpHandler {

    private final AuthService authService;
    private final Gson gson;

    public AuthController() {
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
        this.gson = new Gson();
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
            LoginRequestDTO request = gson.fromJson(body, LoginRequestDTO.class);

            if (request == null
                    || request.getLogin() == null
                    || request.getPassword() == null
                    || request.getLogin().isBlank()
                    || request.getPassword().isBlank()) {

                HttpUtils.sendError(exchange, 400, "Identifiant et mot de passe sont obligatoires.");
                return;
            }

            UtilisateurAuthDTO user = authService.authenticate(
                    request.getLogin().trim(),
                    request.getPassword()
            );

            if (user == null) {
                HttpUtils.sendError(exchange, 401, "Identifiants incorrects (login ou mot de passe invalide).");
                return;
            }

            String token = com.enicarthage.util.TokenUtils.generateToken(user.getIdUtilisateur(), user.getRole());

            AuthResponseDTO response = new AuthResponseDTO(
                    true,
                    "Authentification réussie.",
                    user.getIdUtilisateur(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getLogin(),
                    user.getRole(),
                    token
            );

            HttpUtils.sendJson(exchange, 200, response);

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (com.enicarthage.exception.AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
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
