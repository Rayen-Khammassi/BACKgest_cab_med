package com.enicarthage.controller;

import com.enicarthage.service.PatientRegistrationService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class RegisterController implements HttpHandler {

    private final PatientRegistrationService registrationService =
            new PatientRegistrationService();

    private final Gson gson = new Gson();

    private static class RegisterRequest {
        String nom;
        String prenom;
        String email;
        String login;
        String password;
        String dateNaissance;
        String adresse;
    }

    private static class ApiResponse {
        boolean success;
        String message;

        ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // CORS configuration
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "http://localhost:5173"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "POST, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, false, "Method not allowed.");
            return;
        }

        try {
            String body;

            try (InputStream input = exchange.getRequestBody()) {
                body = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            RegisterRequest request =
                    gson.fromJson(body, RegisterRequest.class);

            if (request == null) {
                sendJson(exchange, 400, false, "Invalid request.");
                return;
            }

            registrationService.registerPatient(
                    request.nom,
                    request.prenom,
                    request.email,
                    request.login,
                    request.password,
                    request.dateNaissance,
                    request.adresse
            );

            sendJson(
                    exchange,
                    201,
                    true,
                    "Patient account created successfully."
            );

        } catch (JsonSyntaxException e) {

            sendJson(exchange, 400, false, "Invalid JSON format.");

        } catch (IllegalArgumentException e) {

            sendJson(exchange, 400, false, e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();

            String message = "Database error.";

            // MariaDB duplicate entry error
            if (e.getErrorCode() == 1062) {
                message = "This email or login is already in use.";
                sendJson(exchange, 409, false, message);
            } else {
                sendJson(exchange, 500, false, message);
            }

        } finally {
            exchange.close();
        }
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            boolean success,
            String message
    ) throws IOException {

        ApiResponse response = new ApiResponse(success, message);
        String json = gson.toJson(response);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );

        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}