package com.enicarthage.controller;

import com.enicarthage.model.DocteurInfo;
import com.enicarthage.service.DocteurService;
import com.enicarthage.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DocteurController implements HttpHandler {

    private final DocteurService docteurService;

    public DocteurController() {
        this.docteurService = new DocteurService();
    }

    public DocteurController(DocteurService docteurService) {
        this.docteurService = docteurService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpUtils.handleCorsAndPreflight(exchange)) {
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtils.sendError(exchange, 405, "Method not allowed. Use GET.");
                return;
            }

            List<DocteurInfo> docteurs = docteurService.getAllDoctors();
            HttpUtils.sendJson(exchange, 200, docteurs);

        } catch (SQLException e) {
            System.err.println("[DocteurController] Database error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur lors de la récupération des médecins.");
        } catch (Exception e) {
            System.err.println("[DocteurController] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Une erreur inattendue est survenue.");
        } finally {
            exchange.close();
        }
    }
}
