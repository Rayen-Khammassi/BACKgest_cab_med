package com.enicarthage.controller;

import com.enicarthage.dto.PatientRegistrationDTO;
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

    private final PatientRegistrationService registrationService;
    private final Gson gson;

    public RegisterController() {
        this.registrationService = new PatientRegistrationService();
        this.gson = new Gson();
    }

    public RegisterController(PatientRegistrationService registrationService) {
        this.registrationService = registrationService;
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
            PatientRegistrationDTO request = gson.fromJson(body, PatientRegistrationDTO.class);

            if (request == null) {
                HttpUtils.sendError(exchange, 400, "Corps de requête invalide.");
                return;
            }

            registrationService.registerPatient(
                    request.getNom(),
                    request.getPrenom(),
                    request.getEmail(),
                    request.getLogin(),
                    request.getPassword(),
                    request.getDateNaissance(),
                    request.getAdresse()
            );

            HttpUtils.sendJson(exchange, 201, Map.of(
                    "success", true,
                    "message", "Patient enregistré avec succès. Vous pouvez maintenant vous connecter."
            ));

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            System.err.println("[RegisterController] Database error: " + e.getMessage());
            e.printStackTrace();
            if (e.getErrorCode() == 1062) {
                HttpUtils.sendError(exchange, 409, "Un compte existe déjà avec cet email ou cet identifiant.");
            } else {
                HttpUtils.sendError(exchange, 500, "Erreur lors de l'enregistrement en base de données.");
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
