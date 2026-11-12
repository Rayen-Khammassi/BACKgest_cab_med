package com.enicarthage.controller;

import com.enicarthage.dao.ConsultationDAO;
import com.enicarthage.model.Consultation;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ConsultationController implements HttpHandler {

    private final ConsultationDAO dao = new ConsultationDAO();
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
                default     -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Utilisez GET, POST ou PUT.");
            }
        } catch (Exception e) {
            System.err.println("[ConsultationController] Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur lors du traitement de la consultation.");
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HttpUtils.parseQueryParams(query);

        List<Consultation> list;
        if (params.containsKey("patient_id")) {
            try {
                int patientId = Integer.parseInt(params.get("patient_id"));
                list = dao.findByPatient(patientId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "patient_id doit être un entier valide.");
                return;
            }
        } else if (params.containsKey("doctor_id")) {
            try {
                int doctorId = Integer.parseInt(params.get("doctor_id"));
                list = dao.findByDocteur(doctorId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "doctor_id doit être un entier valide.");
                return;
            }
        } else {
            list = dao.findAll();
        }

        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json == null || !json.has("idRdv") || !json.has("diagnostic")) {
                HttpUtils.sendError(exchange, 400, "idRdv et diagnostic sont obligatoires.");
                return;
            }

            int idRdv;
            try {
                idRdv = json.get("idRdv").getAsInt();
            } catch (Exception e) {
                HttpUtils.sendError(exchange, 400, "idRdv doit être un identifiant numérique.");
                return;
            }

            String diagnostic = json.get("diagnostic").getAsString().trim();
            if (diagnostic.isBlank()) {
                HttpUtils.sendError(exchange, 400, "Le diagnostic ne peut pas être vide.");
                return;
            }

            String observations = json.has("observations") && !json.get("observations").isJsonNull()
                    ? json.get("observations").getAsString().trim() : "";

            String dateConsultation = json.has("dateConsultation") && !json.get("dateConsultation").isJsonNull()
                    ? json.get("dateConsultation").getAsString()
                    : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Consultation c = new Consultation();
            c.setIdRdv(idRdv);
            c.setDiagnostic(diagnostic);
            c.setObservations(observations);
            c.setDateConsultation(dateConsultation);

            int idConsultation = dao.create(c);
            if (idConsultation > 0) {
                HttpUtils.sendJson(exchange, 201, Map.of(
                        "success", true,
                        "idConsultation", idConsultation,
                        "message", "Consultation enregistrée avec succès."
                ));
            } else {
                HttpUtils.sendError(exchange, 500, "Impossible d'enregistrer la consultation.");
            }
        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException, SQLException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json == null || !json.has("idConsultation") || !json.has("diagnostic")) {
                HttpUtils.sendError(exchange, 400, "idConsultation et diagnostic sont obligatoires.");
                return;
            }

            int idConsultation;
            try {
                idConsultation = json.get("idConsultation").getAsInt();
            } catch (Exception e) {
                HttpUtils.sendError(exchange, 400, "idConsultation doit être un identifiant numérique.");
                return;
            }

            String diagnostic = json.get("diagnostic").getAsString().trim();
            if (diagnostic.isBlank()) {
                HttpUtils.sendError(exchange, 400, "Le diagnostic ne peut pas être vide.");
                return;
            }

            String observations = json.has("observations") && !json.get("observations").isJsonNull()
                    ? json.get("observations").getAsString().trim() : "";

            boolean ok = dao.update(idConsultation, diagnostic, observations);
            if (ok) {
                HttpUtils.sendSuccess(exchange, 200, "Consultation modifiée avec succès.");
            } else {
                HttpUtils.sendError(exchange, 404, "Consultation introuvable.");
            }
        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        }
    }
}
