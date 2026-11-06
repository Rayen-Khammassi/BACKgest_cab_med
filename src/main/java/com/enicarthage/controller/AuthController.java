
package com.enicarthage.controller;

import com.enicarthage.model.AuthResponse;
import com.enicarthage.model.UtilisateurAuth;
import com.enicarthage.service.AuthService;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class AuthController implements HttpHandler {

    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    // Structure of the JSON received from React
    private static class LoginRequest {
        String login;
        String password;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Allow requests from the React/Vite frontend
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "http://localhost:5173"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "POST, OPTIONS"
        );
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type"
        );

        // Handle browser CORS preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        // Only POST is accepted
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        try {
            // Read the JSON request body
            String body;

            try (InputStream input = exchange.getRequestBody()) {
                body = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            LoginRequest request =
                    gson.fromJson(body, LoginRequest.class);

            // Validate input
            if (request == null
                    || request.login == null
                    || request.password == null
                    || request.login.isBlank()
                    || request.password.isBlank()) {

                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Login and password are required\"}");
                return;
            }

            // Authenticate the user
            UtilisateurAuth user = authService.authenticate(
                    request.login,
                    request.password
            );

            // Invalid credentials
            if (user == null) {
                sendJson(exchange, 401,
                        "{\"success\":false,\"message\":\"Invalid login or password\"}");
                return;
            }

            // Create a safe response (no password or hash)
            AuthResponse response = new AuthResponse(
                    true,
                    "Authentication successful",
                    user.getIdUtilisateur(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getLogin(),
                    user.getRole()
            );

            sendJson(exchange, 200, gson.toJson(response));

        } catch (JsonSyntaxException e) {

            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"Invalid JSON format\"}");

        } catch (SQLException e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"Database error\"}");

        } finally {
            exchange.close();
        }
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            String json
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );

        byte[] responseBytes =
                json.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}