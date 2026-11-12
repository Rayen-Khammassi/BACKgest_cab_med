package com.enicarthage.router;

import com.enicarthage.controller.*;
import com.enicarthage.exception.AppException;
import com.enicarthage.util.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class Router implements HttpHandler {

    private final AuthController authController;
    private final RegisterController registerController;
    private final DocteurController docteurController;
    private final RendezVousController rendezVousController;
    private final PatientController patientController;
    private final ConsultationController consultationController;
    private final OrdonnanceController ordonnanceController;
    private final MedicamentController medicamentController;

    public Router() {
        this.authController = new AuthController();
        this.registerController = new RegisterController();
        this.docteurController = new DocteurController();
        this.rendezVousController = new RendezVousController();
        this.patientController = new PatientController();
        this.consultationController = new ConsultationController();
        this.ordonnanceController = new OrdonnanceController();
        this.medicamentController = new MedicamentController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // 1. Universal CORS Pre-flight Handling
            if (HttpUtils.handleCorsAndPreflight(exchange)) {
                return;
            }

            String path = exchange.getRequestURI().getPath();

            // Normalize trailing slashes
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            // 2. Route Dispatching
            if (path.equals("/api/auth/login")) {
                authController.handle(exchange);
            } else if (path.equals("/api/auth/register")) {
                registerController.handle(exchange);
            } else if (path.equals("/api/doctors") || path.startsWith("/api/doctors/")) {
                docteurController.handle(exchange);
            } else if (path.equals("/api/appointments") || path.startsWith("/api/appointments/")) {
                rendezVousController.handle(exchange);
            } else if (path.equals("/api/patients") || path.startsWith("/api/patients/")) {
                patientController.handle(exchange);
            } else if (path.equals("/api/consultations") || path.startsWith("/api/consultations/")) {
                consultationController.handle(exchange);
            } else if (path.equals("/api/prescriptions") || path.startsWith("/api/prescriptions/")) {
                ordonnanceController.handle(exchange);
            } else if (path.equals("/api/medicaments") || path.startsWith("/api/medicaments/")) {
                medicamentController.handle(exchange);
            } else {
                HttpUtils.sendError(exchange, 404, "Endpoint non trouvé : " + path);
            }

        } catch (AppException e) {
            System.err.println("[Router] AppException (" + e.getStatusCode() + "): " + e.getMessage());
            HttpUtils.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            System.err.println("[Router] Uncaught exception: " + e.getMessage());
            e.printStackTrace();
            HttpUtils.sendError(exchange, 500, "Erreur interne du serveur : " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    public AuthController getAuthController() { return authController; }
    public RegisterController getRegisterController() { return registerController; }
    public DocteurController getDocteurController() { return docteurController; }
    public RendezVousController getRendezVousController() { return rendezVousController; }
    public PatientController getPatientController() { return patientController; }
    public ConsultationController getConsultationController() { return consultationController; }
    public OrdonnanceController getOrdonnanceController() { return ordonnanceController; }
    public MedicamentController getMedicamentController() { return medicamentController; }
}
