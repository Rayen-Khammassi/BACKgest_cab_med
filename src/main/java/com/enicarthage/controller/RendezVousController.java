package com.enicarthage.controller;

import com.enicarthage.dao.RendezVousDAO;
import com.enicarthage.model.RendezVous;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RendezVousController implements HttpHandler {

    private final RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private final Gson gson = new Gson();

    private static final Set<String> STATUTS_VALIDES = Set.of("DEMANDE", "CONFIRME", "ANNULE", "TERMINE");

    private static class CreateAppointmentRequest {
        int idPatient;
        int idDocteur;
        String dateHeure;
        String motif;
    }

    private static class UpdateStatusRequest {
        int idRdv;
        String statut;
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
                default -> HttpUtils.sendError(exchange, 405, "Method not allowed. Use GET, POST, or PUT.");
            }

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
                list = rendezVousDAO.findByPatient(patientId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "patient_id doit être un entier valide.");
                return;
            }
        } else if (params.containsKey("doctor_id")) {
            try {
                int doctorId = Integer.parseInt(params.get("doctor_id"));
                list = rendezVousDAO.findByDocteur(doctorId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "doctor_id doit être un entier valide.");
                return;
            }
        } else {
            // Secretary view: all appointments
            list = rendezVousDAO.findAll();
        }

        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            CreateAppointmentRequest req = gson.fromJson(body, CreateAppointmentRequest.class);

            if (req == null || req.idPatient <= 0 || req.idDocteur <= 0
                    || req.dateHeure == null || req.dateHeure.isBlank()) {
                HttpUtils.sendError(exchange, 400, "Patient, Médecin et Date/Heure sont obligatoires.");
                return;
            }

            // Format date string for MySQL / MariaDB (e.g. "2026-11-10T14:30" -> "2026-11-10 14:30:00")
            String formattedDate = req.dateHeure.trim().replace("T", " ");
            if (formattedDate.length() == 16) {
                formattedDate += ":00";
            }

            RendezVous rdv = new RendezVous();
            rdv.setIdPatient(req.idPatient);
            rdv.setIdDocteur(req.idDocteur);
            rdv.setDateHeure(formattedDate);
            rdv.setMotif(req.motif != null ? req.motif.trim() : "");
            rdv.setStatut("DEMANDE");

            int idRdv = rendezVousDAO.create(rdv);
            if (idRdv > 0) {
                HttpUtils.sendJson(exchange, 201, Map.of(
                        "success", true,
                        "message", "Demande de rendez-vous enregistrée avec succès.",
                        "idRdv", idRdv
                ));
            } else {
                HttpUtils.sendError(exchange, 500, "Échec lors de l'enregistrement du rendez-vous.");
            }

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (SQLException e) {
            System.err.println("[RendezVousController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la création du rendez-vous.");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            UpdateStatusRequest req = gson.fromJson(body, UpdateStatusRequest.class);

            if (req == null || req.idRdv <= 0 || req.statut == null || req.statut.isBlank()) {
                HttpUtils.sendError(exchange, 400, "idRdv et statut sont obligatoires.");
                return;
            }

            String nouveauStatut = req.statut.trim().toUpperCase();
            if (!STATUTS_VALIDES.contains(nouveauStatut)) {
                HttpUtils.sendError(exchange, 400, "Statut invalide. Valeurs acceptées: " + STATUTS_VALIDES);
                return;
            }

            boolean updated = rendezVousDAO.updateStatut(req.idRdv, nouveauStatut);
            if (updated) {
                HttpUtils.sendSuccess(exchange, 200, "Statut du rendez-vous mis à jour avec succès (" + nouveauStatut + ").");
            } else {
                HttpUtils.sendError(exchange, 404, "Rendez-vous introuvable (ID: " + req.idRdv + ").");
            }

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (SQLException e) {
            System.err.println("[RendezVousController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données lors de la mise à jour du rendez-vous.");
        }
    }
}
