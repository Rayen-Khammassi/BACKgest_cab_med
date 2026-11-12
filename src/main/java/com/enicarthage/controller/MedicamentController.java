package com.enicarthage.controller;

import com.enicarthage.dao.MedicamentDAO;
import com.enicarthage.model.Medicament;
import com.enicarthage.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Handles all /api/medicaments requests on the secondary server (port 8082).
 *
 * Routes:
 *   GET    /api/medicaments        → list all
 *   GET    /api/medicaments/{id}   → get one
 *   POST   /api/medicaments        → create  (body: {nom, dosage})
 *   PUT    /api/medicaments/{id}   → update  (body: {nom, dosage})
 *   DELETE /api/medicaments/{id}   → delete  → 204 No Content
 */
public class MedicamentController implements HttpHandler {

    private static final int MAX_NOM    = 150;
    private static final int MAX_DOSAGE = 100;

    private final MedicamentDAO dao  = new MedicamentDAO();
    private final Gson          gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // CORS pre-flight
            if (HttpUtils.handleCorsAndPreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            String path   = exchange.getRequestURI().getPath(); // e.g. /api/medicaments or /api/medicaments/3

            // Extract optional trailing segment after /api/medicaments
            String prefix     = "/api/medicaments";
            String afterPrefix = path.substring(prefix.length()); // "" | "/3"

            if (afterPrefix.isEmpty() || afterPrefix.equals("/")) {
                // Collection endpoint: GET (list) or POST (create)
                switch (method) {
                    case "GET"  -> handleList(exchange);
                    case "POST" -> handleCreate(exchange);
                    default     -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Utilisez GET ou POST.");
                }
            } else {
                // Item endpoint: parse numeric id
                String segment = afterPrefix.replaceAll("^/|/$", "");
                int id;
                try {
                    id = Integer.parseInt(segment);
                } catch (NumberFormatException e) {
                    HttpUtils.sendError(exchange, 400, "Identifiant invalide");
                    return;
                }
                switch (method) {
                    case "GET"    -> handleGet(exchange, id);
                    case "PUT"    -> handleUpdate(exchange, id);
                    case "DELETE" -> handleDelete(exchange, id);
                    default       -> HttpUtils.sendError(exchange, 405, "Méthode non autorisée. Utilisez GET, PUT ou DELETE.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentController] SQL error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur base de données");
        } catch (Exception e) {
            System.err.println("[MedicamentController] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur serveur inattendue");
        } finally {
            exchange.close();
        }
    }

    // ── GET /api/medicaments ─────────────────────────────────────────────────
    private void handleList(HttpExchange exchange) throws IOException, SQLException {
        List<Medicament> list = dao.findAll();
        HttpUtils.sendJson(exchange, 200, list);
    }

    // ── GET /api/medicaments/{id} ────────────────────────────────────────────
    private void handleGet(HttpExchange exchange, int id) throws IOException, SQLException {
        Optional<Medicament> opt = dao.findById(id);
        if (opt.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Médicament introuvable");
            return;
        }
        HttpUtils.sendJson(exchange, 200, opt.get());
    }

    // ── POST /api/medicaments ────────────────────────────────────────────────
    private void handleCreate(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json;
        try {
            json = gson.fromJson(body, JsonObject.class);
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 400, "Corps de requête JSON invalide");
            return;
        }

        String nom    = getStringField(json, "nom");
        String dosage = getStringField(json, "dosage");

        String validationError = validate(nom, dosage);
        if (validationError != null) {
            HttpUtils.sendError(exchange, 400, validationError);
            return;
        }

        Medicament m = new Medicament(0, nom, dosage);
        Medicament created = dao.create(m);
        HttpUtils.sendJson(exchange, 201, created);
    }

    // ── PUT /api/medicaments/{id} ────────────────────────────────────────────
    private void handleUpdate(HttpExchange exchange, int id) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json;
        try {
            json = gson.fromJson(body, JsonObject.class);
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 400, "Corps de requête JSON invalide");
            return;
        }

        String nom    = getStringField(json, "nom");
        String dosage = getStringField(json, "dosage");

        String validationError = validate(nom, dosage);
        if (validationError != null) {
            HttpUtils.sendError(exchange, 400, validationError);
            return;
        }

        Optional<Medicament> updated = dao.update(id, new Medicament(0, nom, dosage));
        if (updated.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Médicament introuvable");
            return;
        }
        HttpUtils.sendJson(exchange, 200, updated.get());
    }

    // ── DELETE /api/medicaments/{id} ─────────────────────────────────────────
    private void handleDelete(HttpExchange exchange, int id) throws IOException, SQLException {
        boolean deleted = dao.delete(id);
        if (!deleted) {
            HttpUtils.sendError(exchange, 404, "Médicament introuvable");
            return;
        }
        // 204 No Content — no body, exchange will be closed in outer finally
        exchange.sendResponseHeaders(204, -1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String getStringField(JsonObject json, String field) {
        if (json == null || !json.has(field) || json.get(field).isJsonNull()) return "";
        return json.get(field).getAsString().trim();
    }

    private String validate(String nom, String dosage) {
        if (nom == null || nom.isBlank())    return "Le nom du médicament est obligatoire";
        if (dosage == null || dosage.isBlank()) return "Le dosage est obligatoire";
        if (nom.length() > MAX_NOM)          return "Le nom ne peut pas dépasser " + MAX_NOM + " caractères";
        if (dosage.length() > MAX_DOSAGE)    return "Le dosage ne peut pas dépasser " + MAX_DOSAGE + " caractères";
        return null;
    }
}
