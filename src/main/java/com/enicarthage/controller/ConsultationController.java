package com.enicarthage.controller;

import com.enicarthage.dto.CreateConsultationDTO;
import com.enicarthage.exception.AppException;
import com.enicarthage.model.Consultation;
import com.enicarthage.service.ConsultationService;
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

public class ConsultationController implements HttpHandler {

    private final ConsultationService consultationService;
    private final Gson gson = new Gson();

    public ConsultationController() {
        this.consultationService = new ConsultationService();
    }

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

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
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
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
                list = consultationService.getConsultationsByPatient(patientId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "patient_id doit être un entier valide.");
                return;
            }
        } else if (params.containsKey("doctor_id")) {
            try {
                int doctorId = Integer.parseInt(params.get("doctor_id"));
                list = consultationService.getConsultationsByDoctor(doctorId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "doctor_id doit être un entier valide.");
                return;
            }
        } else {
            list = consultationService.getAllConsultations();
        }

        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            CreateConsultationDTO dto = gson.fromJson(body, CreateConsultationDTO.class);

            if (dto == null || dto.getIdRdv() <= 0 || dto.getDiagnostic() == null || dto.getDiagnostic().isBlank()) {
                HttpUtils.sendError(exchange, 400, "idRdv et diagnostic sont obligatoires.");
                return;
            }

            int idConsultation = consultationService.createConsultation(dto);
            HttpUtils.sendJson(exchange, 201, Map.of(
                    "success", true,
                    "idConsultation", idConsultation,
                    "message", "Consultation enregistrée avec succès."
            ));

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ConsultationController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Impossible d'enregistrer la consultation.");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
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

            consultationService.updateConsultation(idConsultation, diagnostic, observations);
            HttpUtils.sendSuccess(exchange, 200, "Consultation modifiée avec succès.");

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ConsultationController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur lors de la modification de la consultation.");
        }
    }
}
