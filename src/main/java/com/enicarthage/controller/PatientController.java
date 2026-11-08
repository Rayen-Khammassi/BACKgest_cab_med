package com.enicarthage.controller;

import com.enicarthage.dao.PatientDAO;
import com.enicarthage.model.PatientDetails;
import com.enicarthage.service.PatientRegistrationService;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PatientController implements HttpHandler {

    private final PatientDAO dao = new PatientDAO();
    private final PatientRegistrationService registrationService = new PatientRegistrationService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpUtils.handleCorsAndPreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            switch (method) {
                case "GET"  -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "PUT"  -> handlePut(exchange);
                default     -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée.");
            }
        } catch (Exception e) {
            System.err.println("[PatientController] Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur patient: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/api/patients";
        String after = path.substring(prefix.length()).replaceAll("^/|/$", "");

        if (after.isEmpty()) {
            List<PatientDetails> patients = dao.findAll();
            HttpUtils.sendJson(exchange, 200, patients);
        } else {
            try {
                int id = Integer.parseInt(after);
                Optional<PatientDetails> p = dao.findById(id);
                if (p.isPresent()) {
                    HttpUtils.sendJson(exchange, 200, p.get());
                } else {
                    HttpUtils.sendError(exchange, 404, "Patient introuvable.");
                }
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "Identifiant invalide.");
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json == null || !json.has("nom") || !json.has("prenom") || !json.has("email")) {
                HttpUtils.sendError(exchange, 400, "Nom, prénom et email sont obligatoires.");
                return;
            }

            String nom = json.get("nom").getAsString().trim();
            String prenom = json.get("prenom").getAsString().trim();
            String email = json.get("email").getAsString().trim();
            String dateNaissance = json.has("dateNaissance") && !json.get("dateNaissance").isJsonNull()
                    ? json.get("dateNaissance").getAsString().trim() : "";
            String adresse = json.has("adresse") && !json.get("adresse").isJsonNull()
                    ? json.get("adresse").getAsString().trim() : "";

            String login = json.has("login") && !json.get("login").isJsonNull()
                    ? json.get("login").getAsString().trim()
                    : (prenom.toLowerCase().replaceAll("\\s+", "") + "." + nom.toLowerCase().replaceAll("\\s+", ""));

            String password = json.has("password") && !json.get("password").isJsonNull()
                    ? json.get("password").getAsString()
                    : "Patient123!"; // Default temporary password

            registrationService.registerPatient(nom, prenom, email, login, password, dateNaissance, adresse);
            HttpUtils.sendJson(exchange, 201, Map.of(
                    "success", true,
                    "message", "Patient enregistré avec succès.",
                    "login", login
            ));
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la création du patient.");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/api/patients";
        String after = path.substring(prefix.length()).replaceAll("^/|/$", "");

        if (after.isEmpty()) {
            HttpUtils.sendError(exchange, 400, "ID du patient manquant dans l'URL.");
            return;
        }

        try {
            int id = Integer.parseInt(after.split("/")[0]);
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json == null) {
                HttpUtils.sendError(exchange, 400, "Corps de requête invalide.");
                return;
            }

            // Check if this is a toggle status request
            if (json.has("actif")) {
                boolean actif = json.get("actif").getAsBoolean();
                boolean updated = dao.setActif(id, actif);
                if (updated) {
                    HttpUtils.sendSuccess(exchange, 200, "Statut du patient mis à jour avec succès.");
                } else {
                    HttpUtils.sendError(exchange, 404, "Patient introuvable.");
                }
                return;
            }

            // Otherwise, update patient information
            String nom = json.has("nom") ? json.get("nom").getAsString().trim() : "";
            String prenom = json.has("prenom") ? json.get("prenom").getAsString().trim() : "";
            String email = json.has("email") ? json.get("email").getAsString().trim() : "";
            String dateNaissance = json.has("dateNaissance") && !json.get("dateNaissance").isJsonNull()
                    ? json.get("dateNaissance").getAsString().trim() : "";
            String adresse = json.has("adresse") && !json.get("adresse").isJsonNull()
                    ? json.get("adresse").getAsString().trim() : "";

            if (nom.isBlank() || prenom.isBlank() || email.isBlank()) {
                HttpUtils.sendError(exchange, 400, "Nom, prénom et email sont requis.");
                return;
            }

            boolean updated = dao.update(id, nom, prenom, email, dateNaissance, adresse);
            if (updated) {
                HttpUtils.sendSuccess(exchange, 200, "Informations du patient mises à jour avec succès.");
            } else {
                HttpUtils.sendError(exchange, 404, "Patient introuvable.");
            }

        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Identifiant patient non numérique.");
        }
    }
}
