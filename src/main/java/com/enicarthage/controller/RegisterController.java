package com.enicarthage.controller;

import com.enicarthage.service.PatientRegistrationService;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class RegisterController implements HttpHandler {

    private final PatientRegistrationService registrationService =
            new PatientRegistrationService();

    private final Gson gson = new Gson();

    private static class RegisterRequest {
        String nom;
        String prenom;
        String email;
        String login;
        String password;
        String dateNaissance;
        String adresse;
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
            RegisterRequest request = gson.fromJson(body, RegisterRequest.class);

            if (request == null) {
                HttpUtils.sendError(exchange, 400, "Corps de requête invalide.");
                return;
            }

            registrationService.registerPatient(
                    request.nom,
                    request.prenom,
                    request.email,
                    request.login,
                    request.password,
                    request.dateNaissance,
                    request.adresse
            );

            HttpUtils.sendJson(
                    exchange,
                    201,
                    Map.of(
                            "success", true,
                            "message", "Compte patient créé avec succès."
                    )
            );

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            System.err.println("[RegisterController] Database error: " + e.getMessage());
            e.printStackTrace();

            if (e.getErrorCode() == 1062) {
                HttpUtils.sendError(exchange, 409, "Cet email ou cet identifiant est déjà utilisé.");
            } else {
                HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la création du compte.");
            }
        } catch (Exception e) {
            System.err.println("[RegisterController] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur inattendue.");
        } finally {
            exchange.close();
        }
    }
}
