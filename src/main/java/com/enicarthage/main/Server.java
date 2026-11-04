
package com.enicarthage.main;

import com.enicarthage.auth.controller.AuthController;
import com.enicarthage.auth.controller.RegisterController;
import com.enicarthage.config.CorsFilter;
import com.enicarthage.medicament.controller.MedicamentController;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        try {

            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 8082),
                    0
            );

            // =================================
            // LOGIN
            // =================================

            HttpContext loginContext = server.createContext(
                    "/api/auth/login",
                    new AuthController()
            );

            loginContext.getFilters().add(
                    new CorsFilter()
            );

            // =================================
            // REGISTER
            // =================================

            HttpContext registerContext = server.createContext(
                    "/api/auth/register",
                    new RegisterController()
            );

            registerContext.getFilters().add(
                    new CorsFilter()
            );

            // =================================
            // CRUD MEDICAMENTS
            // =================================

            HttpContext medicamentContext = server.createContext(
                    "/api/medicaments",
                    new MedicamentController()
            );

            medicamentContext.getFilters().add(
                    new CorsFilter()
            );

            // =================================
            // DEMARRAGE DU SERVEUR
            // =================================

            server.setExecutor(
                    Executors.newFixedThreadPool(10)
            );

            server.start();

            System.out.println("=================================");
            System.out.println("GESTCABMED - Backend démarré");
            System.out.println("Port : 8082");
            System.out.println("=================================");

            System.out.println(
                    "LOGIN: http://localhost:8082/api/auth/login"
            );

            System.out.println(
                    "REGISTER: http://localhost:8082/api/auth/register"
            );

            System.out.println(
                    "MEDICAMENTS: http://localhost:8082/api/medicaments"
            );

        } catch (Exception e) {

            System.err.println(
                    "Erreur de démarrage du backend"
            );

            e.printStackTrace();
        }
    }
}
