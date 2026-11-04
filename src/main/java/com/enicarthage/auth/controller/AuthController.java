
package com.enicarthage.auth.controller;

import com.enicarthage.auth.dto.LoginRequest;
import com.enicarthage.auth.dto.LoginResponse;
import com.enicarthage.utilisateur.model.UtilisateurAuth;
import com.enicarthage.auth.service.AuthService;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;



import com.google.gson.JsonParseException;


public class AuthController implements HttpHandler {

    private static final int MAX_REQUEST_BYTES = 16 * 1024;

    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Configuration CORS
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "http://localhost:5173"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "POST, OPTIONS"
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

            // 1. Gestion de la requête OPTIONS
            if ("OPTIONS".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // 2. Vérifier la méthode HTTP
            if (!"POST".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                exchange.getResponseHeaders().set(
                        "Allow",
                        "POST, OPTIONS"
                );

                sendJson(
                        exchange,
                        405,
                        LoginResponse.error(
                                "Méthode non autorisée"
                        )
                );

                return;
            }

            // 3. Lire les données envoyées par React
            byte[] bytes = exchange.getRequestBody()
                    .readNBytes(MAX_REQUEST_BYTES + 1);

            if (bytes.length > MAX_REQUEST_BYTES) {

                sendJson(
                        exchange,
                        413,
                        LoginResponse.error(
                                "Requête trop volumineuse"
                        )
                );

                return;
            }

            String body = new String(
                    bytes,
                    StandardCharsets.UTF_8
            );

            // 4. JSON -> LoginRequest
            LoginRequest request = gson.fromJson(
                    body,
                    LoginRequest.class
            );

            // 5. Vérifier les champs
            if (request == null ||
                    request.getLogin() == null ||
                    request.getPassword() == null ||
                    request.getLogin().isBlank() ||
                    request.getPassword().isBlank()) {

                sendJson(
                        exchange,
                        400,
                        LoginResponse.error(
                                "Login et mot de passe obligatoires"
                        )
                );

                return;
            }

            // 6. Appeler AuthService
            UtilisateurAuth user = authService.authenticate(
                    request.getLogin().trim(),
                    request.getPassword()
            );

            // 7. Identifiants incorrects
            if (user == null) {

                sendJson(
                        exchange,
                        401,
                        LoginResponse.error(
                                "Login ou mot de passe incorrect"
                        )
                );

                return;
            }

            // 8. Construire la réponse avec LoginResponse
            LoginResponse response = LoginResponse.success(user);

            // 9. Envoyer la réponse à React
            sendJson(exchange, 200, response);

        } catch (JsonParseException e) {

            sendJson(
                    exchange,
                    400,
                    LoginResponse.error(
                            "Format JSON invalide"
                    )
            );

        } catch (SQLException e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    LoginResponse.error(
                            "Erreur de connexion à la base de données"
                    )
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    LoginResponse.error(
                            "Erreur interne du serveur"
                    )
            );

        } finally {
            exchange.close();
        }
    }

    // Méthode pour envoyer une réponse JSON
    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            LoginResponse response
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        byte[] responseBytes = gson.toJson(response)
                .getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (var output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}
