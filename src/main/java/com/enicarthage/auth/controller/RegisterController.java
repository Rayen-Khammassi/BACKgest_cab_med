
package com.enicarthage.auth.controller;

import com.enicarthage.auth.dto.RegisterRequest;
import com.enicarthage.auth.dto.RegisterResponse;
import com.enicarthage.auth.service.RegisterService;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class RegisterController implements HttpHandler {

    private static final int MAX_REQUEST_BYTES = 16 * 1024;

    private final RegisterService registerService =
            new RegisterService();

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // ========================================
        // 1. CONFIGURATION CORS POUR REACT
        // ========================================

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

            // ========================================
            // 2. GESTION PREFLIGHT CORS (OPTIONS)
            // ========================================

            if ("OPTIONS".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                System.out.println(
                        "CORS OPTIONS reçu : /api/auth/register"
                );

                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // ========================================
            // 3. VERIFICATION DE LA METHODE HTTP
            // ========================================

            if (!"POST".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                exchange.getResponseHeaders().set(
                        "Allow",
                        "POST, OPTIONS"
                );

                sendJson(
                        exchange,
                        405,
                        new RegisterResponse(
                                false,
                                "Méthode non autorisée",
                                null
                        )
                );

                return;
            }

            System.out.println(
                    "Nouvelle demande d'inscription patient"
            );

            // ========================================
            // 4. LECTURE DU JSON ENVOYE PAR REACT
            // ========================================

            byte[] bytes = exchange.getRequestBody()
                    .readNBytes(MAX_REQUEST_BYTES + 1);

            if (bytes.length > MAX_REQUEST_BYTES) {

                sendJson(
                        exchange,
                        413,
                        new RegisterResponse(
                                false,
                                "Requête trop volumineuse",
                                null
                        )
                );

                return;
            }

            String json = new String(
                    bytes,
                    StandardCharsets.UTF_8
            );

            RegisterRequest request = gson.fromJson(
                    json,
                    RegisterRequest.class
            );

            // ========================================
            // 5. VALIDATION DE LA REQUETE
            // ========================================

            if (request == null) {

                sendJson(
                        exchange,
                        400,
                        new RegisterResponse(
                                false,
                                "Données d'inscription manquantes",
                                null
                        )
                );

                return;
            }

            // ========================================
            // 6. INSCRIPTION DU PATIENT
            // ========================================

            int idUtilisateur =
                    registerService.registerPatient(request);

            System.out.println(
                    "Patient inscrit avec succès. ID : "
                            + idUtilisateur
            );

            // ========================================
            // 7. REPONSE EN CAS DE SUCCES
            // ========================================

            sendJson(
                    exchange,
                    201,
                    new RegisterResponse(
                            true,
                            "Compte patient créé avec succès",
                            idUtilisateur
                    )
            );

        } catch (JsonParseException e) {

            // ========================================
            // 8. ERREUR FORMAT JSON
            // ========================================

            sendJson(
                    exchange,
                    400,
                    new RegisterResponse(
                            false,
                            "Format JSON invalide",
                            null
                    )
            );

        } catch (IllegalArgumentException e) {

            // ========================================
            // 9. ERREUR DE VALIDATION
            // ========================================

            sendJson(
                    exchange,
                    400,
                    new RegisterResponse(
                            false,
                            e.getMessage(),
                            null
                    )
            );

        } catch (SQLException e) {

            // ========================================
            // 10. ERREURS MYSQL
            // ========================================

            if (e.getErrorCode() == 1062) {

                // Email ou login déjà existant
                sendJson(
                        exchange,
                        409,
                        new RegisterResponse(
                                false,
                                "Cet email ou ce login est déjà utilisé",
                                null
                        )
                );

            } else {

                e.printStackTrace();

                sendJson(
                        exchange,
                        500,
                        new RegisterResponse(
                                false,
                                "Erreur lors de l'enregistrement dans la base de données",
                                null
                        )
                );
            }

        } catch (Exception e) {

            // ========================================
            // 11. ERREUR INATTENDUE
            // ========================================

            e.printStackTrace();

            sendJson(
                    exchange,
                    500,
                    new RegisterResponse(
                            false,
                            "Erreur interne du serveur",
                            null
                    )
            );

        } finally {

            // ========================================
            // 12. FERMETURE DE LA CONNEXION HTTP
            // ========================================

            exchange.close();
        }
    }

    // ========================================
    // 13. ENVOYER UNE REPONSE JSON A REACT
    // ========================================

    private void sendJson(
            HttpExchange exchange,
            int status,
            RegisterResponse response
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        byte[] data = gson.toJson(response)
                .getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                status,
                data.length
        );

        try (var out = exchange.getResponseBody()) {
            out.write(data);
        }
    }
}
