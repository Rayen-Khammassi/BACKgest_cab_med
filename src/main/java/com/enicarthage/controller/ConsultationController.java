package com.enicarthage.controller;

import com.enicarthage.dao.ConsultationDAO;
import com.enicarthage.model.Consultation;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
                default     -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée.");
            }
        } catch (Exception e) {
            System.err.println("[ConsultationController] Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur.");
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HttpUtils.parseQueryParams(query);

        List<Consultation> list;
        if (params.containsKey("patient_id")) {
            int patientId = Integer.parseInt(params.get("patient_id"));
            list = dao.findByPatient(patientId);
        } else if (params.containsKey("doctor_id")) {
            int doctorId = Integer.parseInt(params.get("doctor_id"));
            list = dao.findByDocteur(doctorId);
        } else {
            list = dao.findAll();
        }

        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        if (json == null || !json.has("idRdv") || !json.has("diagnostic")) {
            HttpUtils.sendError(exchange, 400, "idRdv et diagnostic sont obligatoires.");
            return;
        }

        int idRdv = json.get("idRdv").getAsInt();
        String diagnostic = json.get("diagnostic").getAsString().trim();
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
    }

    private void handlePut(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        if (json == null || !json.has("idConsultation") || !json.has("diagnostic")) {
            HttpUtils.sendError(exchange, 400, "idConsultation et diagnostic sont obligatoires.");
            return;
        }

        int idConsultation = json.get("idConsultation").getAsInt();
        String diagnostic = json.get("diagnostic").getAsString().trim();
        String observations = json.has("observations") && !json.get("observations").isJsonNull()
                ? json.get("observations").getAsString().trim() : "";

        boolean ok = dao.update(idConsultation, diagnostic, observations);
        if (ok) {
            HttpUtils.sendSuccess(exchange, 200, "Consultation modifiée avec succès.");
        } else {
            HttpUtils.sendError(exchange, 404, "Consultation introuvable.");
        }
    }
}
