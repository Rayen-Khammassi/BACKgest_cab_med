package com.enicarthage.controller;

import com.enicarthage.dto.CreateAppointmentDTO;
import com.enicarthage.dto.UpdateAppointmentStatusDTO;
import com.enicarthage.exception.AppException;
import com.enicarthage.model.RendezVous;
import com.enicarthage.service.RendezVousService;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RendezVousController implements HttpHandler {

    private final RendezVousService rendezVousService;
    private final Gson gson = new Gson();

    public RendezVousController() {
        this.rendezVousService = new RendezVousService();
    }

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpUtils.handleCorsAndPreflight(exchange)) {
                return;
            }

            String method = exchange.getRequestMethod().toUpperCase();

            switch (method) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange);
                default -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Utilisez GET, POST ou PUT.");
            }

        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            System.err.println("[RendezVousController] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Une erreur inattendue est survenue.");
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HttpUtils.parseQueryParams(query);

        List<RendezVous> list;

        if (params.containsKey("patient_id")) {
            try {
                int patientId = Integer.parseInt(params.get("patient_id"));
                list = rendezVousService.getAppointmentsByPatient(patientId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "patient_id doit être un entier valide.");
                return;
            }
        } else if (params.containsKey("doctor_id")) {
            try {
                int doctorId = Integer.parseInt(params.get("doctor_id"));
                list = rendezVousService.getAppointmentsByDoctor(doctorId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "doctor_id doit être un entier valide.");
                return;
            }
        } else {
            // Secretary view: all appointments
            list = rendezVousService.getAllAppointments();
        }

        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            CreateAppointmentDTO req = gson.fromJson(body, CreateAppointmentDTO.class);

            if (req == null || req.getIdPatient() <= 0 || req.getIdDocteur() <= 0
                    || req.getDateHeure() == null || req.getDateHeure().isBlank()) {
                HttpUtils.sendError(exchange, 400, "Patient, Médecin et Date/Heure sont obligatoires.");
                return;
            }

            int idRdv = rendezVousService.createAppointment(req);
            String statutInitial = (req.getStatut() != null && !req.getStatut().isBlank())
                    ? req.getStatut().trim().toUpperCase() : "DEMANDE";

            HttpUtils.sendJson(exchange, 201, Map.of(
                    "success", true,
                    "message", "Rendez-vous enregistré avec succès.",
                    "idRdv", idRdv,
                    "statut", statutInitial
            ));

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[RendezVousController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la création du rendez-vous.");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            UpdateAppointmentStatusDTO req = gson.fromJson(body, UpdateAppointmentStatusDTO.class);

            if (req == null || req.getIdRdv() <= 0 || req.getStatut() == null || req.getStatut().isBlank()) {
                HttpUtils.sendError(exchange, 400, "idRdv et statut sont obligatoires.");
                return;
            }

            rendezVousService.updateAppointmentStatus(req);
            HttpUtils.sendSuccess(exchange, 200, "Statut du rendez-vous mis à jour avec succès (" + req.getStatut().trim().toUpperCase() + ").");

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[RendezVousController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la mise à jour du rendez-vous.");
        }
    }
}
