package com.enicarthage;

import com.enicarthage.router.Router;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer {

    private final int port;
    private final Integer medPort;
    private final Router router;
    private HttpServer mainServer;
    private HttpServer medServer;

    public ApiServer() {
        this(8081, null);
    }

    public ApiServer(int port) {
        this(port, null);
    }

    public ApiServer(int port, Integer medPort) {
        this.port = port;
        this.medPort = medPort;
        this.router = new Router();
    }

    public void start() throws IOException {
        // ── Serveur Principal Unifié (Auth, Patients, Docteurs, Rendez-vous, Consultations, Ordonnances, Médicaments) ──
        mainServer = HttpServer.create(new InetSocketAddress(port), 0);
        mainServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        mainServer.createContext("/api", router);
        mainServer.start();

        System.out.println("=================================================");
        System.out.println(" Backend Al-Zahrawi (GESTCABMED) : Serveur Démarré");
        System.out.println("-------------------------------------------------");
        System.out.println(" [" + port + "] API Unifiée : http://localhost:" + port + "/api");
        System.out.println("        - Auth, Patients, Docteurs, Rendez-vous");
        System.out.println("        - Consultations, Ordonnances, Médicaments");

        // ── Serveur Secondaire Optionnel (actif uniquement si configuré explicitement) ──
        if (medPort != null && medPort != port && medPort > 0) {
            medServer = HttpServer.create(new InetSocketAddress(medPort), 0);
            medServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            medServer.createContext("/api", router);
            medServer.start();
            System.out.println(" [" + medPort + "] API Médicaments (secondaire) : http://localhost:" + medPort + "/api/medicaments");
        }

        System.out.println("=================================================");
    }

    public void stop() {
        if (mainServer != null) {
            mainServer.stop(0);
            System.out.println("[ApiServer] Serveur principal arrêté.");
        }
        if (medServer != null) {
            medServer.stop(0);
            System.out.println("[ApiServer] Serveur médicaments arrêté.");
        }
    }

    public Router getRouter() {
        return router;
    }
}
