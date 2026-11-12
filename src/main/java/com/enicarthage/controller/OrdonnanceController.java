package com.enicarthage.controller;

import com.enicarthage.dto.CreateOrdonnanceDTO;
import com.enicarthage.exception.AppException;
import com.enicarthage.model.Ordonnance;
import com.enicarthage.model.PrescriptionItem;
import com.enicarthage.service.OrdonnanceService;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdonnanceController implements HttpHandler {

    private final OrdonnanceService ordonnanceService;
    private final Gson gson = new Gson();

    public OrdonnanceController() {
        this.ordonnanceService = new OrdonnanceService();
    }

    public OrdonnanceController(OrdonnanceService ordonnanceService) {
        this.ordonnanceService = ordonnanceService;
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
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            System.err.println("[OrdonnanceController] Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur lors du traitement des ordonnances.");
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HttpUtils.parseQueryParams(query);

        List<Ordonnance> list;
        if (params.containsKey("patient_id")) {
            try {
                int patientId = Integer.parseInt(params.get("patient_id"));
                list = ordonnanceService.getPrescriptionsByPatient(patientId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "patient_id doit être un entier valide.");
                return;
            }
        } else if (params.containsKey("doctor_id")) {
            try {
                int doctorId = Integer.parseInt(params.get("doctor_id"));
                list = ordonnanceService.getPrescriptionsByDoctor(doctorId);
            } catch (NumberFormatException e) {
                HttpUtils.sendError(exchange, 400, "doctor_id doit être un entier valide.");
                return;
            }
        } else {
            list = ordonnanceService.getAllPrescriptions();
        }

        HttpUtils.sendJson(exchange, 200, list);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = HttpUtils.readRequestBody(exchange);
            CreateOrdonnanceDTO dto = gson.fromJson(body, CreateOrdonnanceDTO.class);

            if (dto == null || dto.getIdConsultation() <= 0) {
                HttpUtils.sendError(exchange, 400, "idConsultation est obligatoire.");
                return;
            }

            int idOrdonnance = ordonnanceService.createOrdonnance(dto);
            HttpUtils.sendJson(exchange, 201, Map.of(
                    "success", true,
                    "idOrdonnance", idOrdonnance,
                    "message", "Ordonnance créée avec succès."
            ));

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[OrdonnanceController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Échec lors de la création de l'ordonnance.");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/api/prescriptions";
        String after = path.substring(prefix.length()).replaceAll("^/|/$", "");

        try {
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            int idOrdonnance = -1;
            if (!after.isEmpty()) {
                try {
                    idOrdonnance = Integer.parseInt(after.split("/")[0]);
                } catch (NumberFormatException ignored) {}
            }
            if (idOrdonnance <= 0 && json != null && json.has("idOrdonnance")) {
                try {
                    idOrdonnance = json.get("idOrdonnance").getAsInt();
                } catch (Exception ignored) {}
            }

            if (idOrdonnance <= 0) {
                HttpUtils.sendError(exchange, 400, "idOrdonnance obligatoire dans l'URL ou le corps de la requête.");
                return;
            }

            List<PrescriptionItem> items = parseItems(json);
            ordonnanceService.updatePrescriptionItems(idOrdonnance, items);
            HttpUtils.sendSuccess(exchange, 200, "Ordonnance modifiée avec succès.");

        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[OrdonnanceController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données.");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/api/prescriptions";
        String after = path.substring(prefix.length()).replaceAll("^/|/$", "");

        if (after.isEmpty()) {
            HttpUtils.sendError(exchange, 400, "ID de l'ordonnance manquant dans l'URL.");
            return;
        }

        try {
            int idOrdonnance = Integer.parseInt(after.split("/")[0]);
            ordonnanceService.deletePrescription(idOrdonnance);
            HttpUtils.sendSuccess(exchange, 200, "Ordonnance supprimée avec succès.");
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Identifiant non numérique.");
        } catch (AppException e) {
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (SQLException e) {
            System.err.println("[OrdonnanceController] SQL Error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur lors de la suppression.");
        }
    }

    private List<PrescriptionItem> parseItems(JsonObject json) {
        List<PrescriptionItem> items = new ArrayList<>();
        if (json != null && json.has("items") && json.get("items").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("items");
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject itemObj = el.getAsJsonObject();
                if (!itemObj.has("idMedicament")) continue;

                PrescriptionItem p = new PrescriptionItem();
                p.setIdMedicament(itemObj.get("idMedicament").getAsInt());
                p.setPosologie(itemObj.has("posologie") && !itemObj.get("posologie").isJsonNull()
                        ? itemObj.get("posologie").getAsString() : "1 cp");
                p.setFrequence(itemObj.has("frequence") && !itemObj.get("frequence").isJsonNull()
                        ? itemObj.get("frequence").getAsString() : "Par jour");
                p.setDuree(itemObj.has("duree") && !itemObj.get("duree").isJsonNull()
                        ? itemObj.get("duree").getAsString() : "7 jours");
                p.setQuantite(itemObj.has("quantite") && !itemObj.get("quantite").isJsonNull()
                        ? itemObj.get("quantite").getAsInt() : 1);
                items.add(p);
            }
        }
        return items;
    }
}
