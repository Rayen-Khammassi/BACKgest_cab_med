
package com.enicarthage.medicament.controller;

import com.enicarthage.medicament.dto.MedicamentRequest;
import com.enicarthage.medicament.model.Medicament;
import com.enicarthage.medicament.service.MedicamentService;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

public class MedicamentController implements HttpHandler {

    private static final String BASE = "/api/medicaments";
    private static final int MAX_REQUEST_BYTES = 16 * 1024;

    private final MedicamentService service =
            new MedicamentService();

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // CONFIGURATION CORS
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "http://localhost:5173"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );

        exchange.getResponseHeaders().set(
                "Vary",
                "Origin"
        );

        try {

            String method = exchange.getRequestMethod()
                    .toUpperCase(Locale.ROOT);

            // PREFLIGHT CORS
            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();

            // ROUTE /api/medicaments
            if (path.equals(BASE)) {

                switch (method) {

                    case "GET" -> {
                        sendJson(exchange, 200, service.list());
                    }

                    case "POST" -> {
                        MedicamentRequest request =
                                readRequest(exchange);

                        Medicament created =
                                service.create(request);

                        sendJson(exchange, 201, created);
                    }

                    default -> methodNotAllowed(
                            exchange, "GET, POST, OPTIONS"
                    );
                }

                return;
            }

            // ROUTE /api/medicaments/{id}
            if (!path.startsWith(BASE + "/")) {
                sendError(exchange, 404, "Route introuvable");
                return;
            }

            int id = parseId(
                    path.substring((BASE + "/").length())
            );

            switch (method) {

                case "GET" -> {
                    Medicament found = service.getById(id);

                    if (found == null) {
                        sendError(
                                exchange, 404,
                                "Médicament introuvable"
                        );
                    } else {
                        sendJson(exchange, 200, found);
                    }
                }

                case "PUT" -> {
                    MedicamentRequest request =
                            readRequest(exchange);

                    Medicament updated =
                            service.update(id, request);

                    if (updated == null) {
                        sendError(
                                exchange, 404,
                                "Médicament introuvable"
                        );
                    } else {
                        sendJson(exchange, 200, updated);
                    }
                }

                case "DELETE" -> {
                    boolean deleted = service.delete(id);

                    if (deleted) {
                        exchange.sendResponseHeaders(204, -1);
                    } else {
                        sendError(
                                exchange, 404,
                                "Médicament introuvable"
                        );
                    }
                }

                default -> methodNotAllowed(
                        exchange, "GET, PUT, DELETE, OPTIONS"
                );
            }

        } catch (JsonParseException e) {

            sendError(exchange, 400, "Format JSON invalide");

        } catch (IllegalArgumentException e) {

            sendError(exchange, 400, e.getMessage());

        } catch (SQLException e) {

            // Clé étrangère : médicament utilisé
            if (e.getErrorCode() == 1451) {

                sendError(
                        exchange,
                        409,
                        "Suppression impossible : médicament utilisé dans une prescription"
                );

            } else {
                e.printStackTrace();
                sendError(
                        exchange, 500,
                        "Erreur de base de données"
                );
            }

        } catch (RuntimeException e) {

            e.printStackTrace();
            sendError(
                    exchange, 500,
                    "Erreur interne du serveur"
            );

        } finally {
            exchange.close();
        }
    }

    // LECTURE DU BODY JSON
    private MedicamentRequest readRequest(HttpExchange exchange)
            throws IOException {

        byte[] bytes = exchange.getRequestBody()
                .readNBytes(MAX_REQUEST_BYTES + 1);

        if (bytes.length > MAX_REQUEST_BYTES) {
            throw new IllegalArgumentException(
                    "Requête trop volumineuse"
            );
        }

        String json = new String(
                bytes, StandardCharsets.UTF_8
        );

        return gson.fromJson(
                json,
                MedicamentRequest.class
        );
    }

    // EXTRACTION DE L'IDENTIFIANT
    private int parseId(String segment) {

        if (!segment.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "Identifiant invalide"
            );
        }

        try {
            int id = Integer.parseInt(segment);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Identifiant invalide"
                );
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Identifiant invalide"
            );
        }
    }

    private void methodNotAllowed(
            HttpExchange exchange,
            String allowed
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Allow", allowed
        );

        sendError(
                exchange, 405,
                "Méthode non autorisée"
        );
    }

    private void sendError(
            HttpExchange exchange,
            int status,
            String message
    ) throws IOException {

        sendJson(
                exchange,
                status,
                Map.of(
                        "success", false,
                        "message", message
                )
        );
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object content
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        byte[] bytes = gson.toJson(content)
                .getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                status,
                bytes.length
        );

        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
