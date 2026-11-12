
package com.enicarthage.main;

import com.enicarthage.controller.AuthController;
import com.enicarthage.controller.ConsultationController;
import com.enicarthage.controller.DocteurController;
import com.enicarthage.controller.MedicamentController;
import com.enicarthage.controller.OrdonnanceController;
import com.enicarthage.controller.PatientController;
import com.enicarthage.controller.RegisterController;
import com.enicarthage.controller.RendezVousController;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        try {
            // ==========================================
            // 1. Configuration du port
            // ==========================================

            int port = 8082;

            String envPort = System.getenv("PORT");

            if (envPort != null && !envPort.isBlank()) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException e) {
                    System.err.println(
                            "PORT invalide, utilisation de 8082."
                    );
                }
            }

            // ==========================================
            // 2. Création du serveur HTTP unique
            // ==========================================

            HttpServer server = HttpServer.create(
                    new InetSocketAddress(port),
                    0
            );

            // Java 21 : Virtual Threads
            server.setExecutor(
                    Executors.newVirtualThreadPerTaskExecutor()
            );

            // ==========================================
            // 3. Authentification
            // ==========================================

            server.createContext(
                    "/api/auth/login",
                    new AuthController()
            );

            server.createContext(
                    "/api/auth/register",
                    new RegisterController()
            );

            // ==========================================
            // 4. Gestion des médecins
            // ==========================================

            server.createContext(
                    "/api/doctors",
                    new DocteurController()
            );

            // ==========================================
            // 5. Gestion des rendez-vous
            // ==========================================

            server.createContext(
                    "/api/appointments",
                    new RendezVousController()
            );

            // ==========================================
            // 6. Gestion des patients
            // ==========================================

            server.createContext(
                    "/api/patients",
                    new PatientController()
            );

            // ==========================================
            // 7. Gestion des consultations
            // ==========================================

            server.createContext(
                    "/api/consultations",
                    new ConsultationController()
            );

            // ==========================================
            // 8. Gestion des ordonnances
            // ==========================================

            server.createContext(
                    "/api/prescriptions",
                    new OrdonnanceController()
            );

            // ==========================================
            // 9. Gestion des médicaments
            // ==========================================

            server.createContext(
                    "/api/medicaments",
                    new MedicamentController()
            );

            // ==========================================
            // 10. Démarrage du serveur
            // ==========================================

            server.start();

            // ==========================================
            // 11. Affichage des API disponibles
            // ==========================================

            String baseUrl = "http://localhost:" + port;

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    " Backend Al-Zahraoui - Serveur demarre"
            );

            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "Port : " + port
            );

            System.out.println(
                    "----------------------------------------------"
            );

            System.out.println(
                    "Login API:         "
                            + baseUrl + "/api/auth/login"
            );

            System.out.println(
                    "Register API:      "
                            + baseUrl + "/api/auth/register"
            );

            System.out.println(
                    "Doctors API:       "
                            + baseUrl + "/api/doctors"
            );

            System.out.println(
                    "Appointments API:  "
                            + baseUrl + "/api/appointments"
            );

            System.out.println(
                    "Patients API:      "
                            + baseUrl + "/api/patients"
            );

            System.out.println(
                    "Consultations API: "
                            + baseUrl + "/api/consultations"
            );

            System.out.println(
                    "Prescriptions API: "
                            + baseUrl + "/api/prescriptions"
            );

            System.out.println(
                    "Medicaments API:   "
                            + baseUrl + "/api/medicaments"
            );

            System.out.println(
                    "=============================================="
            );

        } catch (java.net.BindException e) {

            System.err.println(
                    "ERREUR : Le port est deja utilise."
            );

            System.err.println(
                    "Arretez l'ancien serveur ou changez le port."
            );

            e.printStackTrace();

        } catch (Exception e) {

            System.err.println(
                    "Could not start the server."
            );

            e.printStackTrace();
        }
    }
}
