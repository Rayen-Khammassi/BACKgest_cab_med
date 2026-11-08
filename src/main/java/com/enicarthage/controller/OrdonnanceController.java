package com.enicarthage.controller;

import com.enicarthage.dao.OrdonnanceDAO;
import com.enicarthage.model.Ordonnance;
import com.enicarthage.model.PrescriptionItem;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdonnanceController implements HttpHandler {

    private final OrdonnanceDAO dao = new OrdonnanceDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpUtils.handleCorsAndPreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            switch (method) {
                case "GET"  -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                default     -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Utilisez GET ou POST.");
            }
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

            if (json == null || !json.has("idConsultation")) {
                HttpUtils.sendError(exchange, 400, "idConsultation est obligatoire.");
                return;
            }

            int idConsultation;
            try {
                idConsultation = json.get("idConsultation").getAsInt();
            } catch (Exception e) {
                HttpUtils.sendError(exchange, 400, "idConsultation doit être un entier valide.");
                return;
            }

            String dateCreation = json.has("dateCreation") && !json.get("dateCreation").isJsonNull()
                    ? json.get("dateCreation").getAsString()
                    : LocalDate.now().toString();

            Ordonnance ord = new Ordonnance();
            ord.setIdConsultation(idConsultation);
            ord.setDateCreation(dateCreation);

            List<PrescriptionItem> items = new ArrayList<>();
            if (json.has("items") && json.get("items").isJsonArray()) {
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

            int idOrdonnance = dao.create(ord, items);
            if (idOrdonnance > 0) {
                HttpUtils.sendJson(exchange, 201, Map.of(
                        "success", true,
                        "idOrdonnance", idOrdonnance,
                        "message", "Ordonnance créée avec succès."
                ));
            } else {
                HttpUtils.sendError(exchange, 500, "Échec lors de la création de l'ordonnance.");
            }
        } catch (JsonSyntaxException e) {
            HttpUtils.sendError(exchange, 400, "Format JSON invalide.");
        }
    }
}
