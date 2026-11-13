package com.enicarthage.controller;

import com.enicarthage.model.PatientDetails;
import com.enicarthage.service.PatientRegistrationService;
import com.enicarthage.service.PatientService;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PatientController implements HttpHandler {

    private final PatientService patientService;
    private final PatientRegistrationService registrationService;
    private final Gson gson;

    public PatientController() {
        this.patientService = new PatientService();
        this.registrationService = new PatientRegistrationService();
        this.gson = new Gson();
    }

    public PatientController(PatientService patientService, PatientRegistrationService registrationService) {
        this.patientService = patientService;
        this.registrationService = registrationService;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpUtils.handleCorsAndPreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            switch (method) {
                case "GET"    -> handleGet(exchange);
                case "POST"   -> handlePost(exchange);
                case "PUT"    -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange);
                default       -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Utilisez GET, POST, PUT ou DELETE.");
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
            List<PatientDetails> patients = patientService.getAllPatients();
            HttpUtils.sendJson(exchange, 200, patients);
        } else {
            try {
                int id = Integer.parseInt(after.split("/")[0]);
                PatientDetails p = patientService.getPatientById(id);
                HttpUtils.sendJson(exchange, 200, p);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "Identifiant non numérique.");
            } catch (com.enicarthage.exception.ResourceNotFoundException e) {
                HttpUtils.sendError(exchange, 404, e.getMessage());
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

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                HttpUtils.sendError(exchange, 400, "Format d'adresse email invalide (ex: exemple@domaine.com).");
                return;
            }
            String dateNaissance = json.has("dateNaissance") && !json.get("dateNaissance").isJsonNull()
                    ? json.get("dateNaissance").getAsString().trim() : "";
            String adresse = json.has("adresse") && !json.get("adresse").isJsonNull()
                    ? json.get("adresse").getAsString().trim() : "";

            String login = json.has("login") && !json.get("login").isJsonNull()
                    ? json.get("login").getAsString().trim()
                    : (prenom.toLowerCase().replaceAll("\\s+", "") + "." + nom.toLowerCase().replaceAll("\\s+", ""));

            String password = json.has("password") && !json.get("password").isJsonNull()
                    ? json.get("password").getAsString()
                    : "Patient123!"; // Mot de passe temporaire par défaut

            registrationService.registerPatient(nom, prenom, email, login, password, dateNaissance, adresse);
            HttpUtils.sendJson(exchange, 201, Map.of(
                    "success", true,
                    "message", "Patient enregistré avec succès.",
                    "login", login
            ));
        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 400, e.getMessage());
        } catch (SQLException e) {
            System.err.println("[PatientController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            if (e.getErrorCode() == 1062) {
                HttpUtils.sendError(exchange, 409, "Un patient avec cet email ou cet identifiant existe déjà.");
            } else {
                HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la création du patient.");
            }
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
            if (id <= 0) {
                HttpUtils.sendError(exchange, 400, "Identifiant patient non numérique ou non positif.");
                return;
            }
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json == null) {
                HttpUtils.sendError(exchange, 400, "Corps de requête invalide.");
                return;
            }

            // Statut actif / inactif
            if (json.has("actif")) {
                boolean actif = json.get("actif").getAsBoolean();
                patientService.setPatientActive(id, actif);
                HttpUtils.sendSuccess(exchange, 200, "Statut du patient mis à jour avec succès.");
                return;
            }

            // Mise à jour coordonnées
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

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                HttpUtils.sendError(exchange, 400, "Format d'adresse email invalide (ex: exemple@domaine.com).");
                return;
            }

            patientService.updatePatient(id, nom, prenom, email, dateNaissance, adresse);
            HttpUtils.sendSuccess(exchange, 200, "Informations du patient mises à jour avec succès.");

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Identifiant patient non numérique.");
        } catch (com.enicarthage.exception.ResourceNotFoundException e) {
            HttpUtils.sendError(exchange, 404, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/api/patients";
        String after = path.substring(prefix.length()).replaceAll("^/|/$", "");

        if (after.isEmpty()) {
            HttpUtils.sendError(exchange, 400, "ID du patient manquant dans l'URL.");
            return;
        }

        try {
            int id = Integer.parseInt(after.split("/")[0]);
            patientService.setPatientActive(id, false);
            HttpUtils.sendSuccess(exchange, 200, "Dossier patient désactivé avec succès.");
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Identifiant patient non numérique.");
        } catch (com.enicarthage.exception.ResourceNotFoundException e) {
            HttpUtils.sendError(exchange, 404, e.getMessage());
        }
    }
}
