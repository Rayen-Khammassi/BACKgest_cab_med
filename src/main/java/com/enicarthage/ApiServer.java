package com.enicarthage;

import com.enicarthage.router.Router;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer {

    private final int port;
    private final int medPort;
    private final Router router;
    private HttpServer mainServer;
    private HttpServer medServer;

    public ApiServer() {
        this(8081, 8082);
    }

    public ApiServer(int port) {
        this(port, 8082);
    }

    public ApiServer(int port, int medPort) {
        this.port = port;
        this.medPort = medPort;
        this.router = new Router();
    }

    public void start() throws IOException {
        // ── Serveur Principal (Auth, Patients, Rendez-vous, Consultations, Ordonnances) ──
        mainServer = HttpServer.create(new InetSocketAddress(port), 0);
        mainServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        mainServer.createContext("/api", router);
        mainServer.start();

        // ── Serveur Secondaire (Médicaments — contrat binôme port 8082) ──
        medServer = HttpServer.create(new InetSocketAddress(medPort), 0);
        medServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        medServer.createContext("/api", router);
        medServer.start();

        System.out.println("=================================================");
        System.out.println(" Backend Al-Zahraoui (GESTCABMED) : Serveurs Démarrés");
        System.out.println("-------------------------------------------------");
        System.out.println(" [8081] API Principale :  http://localhost:" + port + "/api");
        System.out.println(" [8082] API Médicaments : http://localhost:" + medPort + "/api/medicaments");
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
