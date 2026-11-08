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
            // ── Primary server: port 8081 (auth, doctors, appointments) ───────
            int port = 8081;
            String envPort = System.getenv("PORT");
            if (envPort != null && !envPort.isBlank()) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException ignored) {}
            }

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            server.createContext("/api/auth/login",    new AuthController());
            server.createContext("/api/auth/register", new RegisterController());
            server.createContext("/api/doctors",       new DocteurController());
            server.createContext("/api/appointments",  new RendezVousController());
            server.createContext("/api/patients",      new PatientController());
            server.createContext("/api/consultations", new ConsultationController());
            server.createContext("/api/prescriptions", new OrdonnanceController());

            server.start();

            // ── Secondary server: port 8082 (médicaments — binôme contract) ───
            int medPort = 8082;
            HttpServer medServer = HttpServer.create(new InetSocketAddress(medPort), 0);
            medServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            medServer.createContext("/api/medicaments", new MedicamentController());
            medServer.start();

            System.out.println("=================================================");
            System.out.println(" Backend Al-Zahraoui — Serveurs démarrés");
            System.out.println("-------------------------------------------------");
            System.out.println(" [8081] Login API:         http://localhost:" + port + "/api/auth/login");
            System.out.println(" [8081] Register API:      http://localhost:" + port + "/api/auth/register");
            System.out.println(" [8081] Doctors API:       http://localhost:" + port + "/api/doctors");
            System.out.println(" [8081] Appointments API:  http://localhost:" + port + "/api/appointments");
            System.out.println(" [8081] Patients API:      http://localhost:" + port + "/api/patients");
            System.out.println(" [8081] Consultations API: http://localhost:" + port + "/api/consultations");
            System.out.println(" [8081] Prescriptions API: http://localhost:" + port + "/api/prescriptions");
            System.out.println("-------------------------------------------------");
            System.out.println(" [8082] Medicaments API:   http://localhost:" + medPort + "/api/medicaments");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("Could not start the server.");
            e.printStackTrace();
        }
    }
}
