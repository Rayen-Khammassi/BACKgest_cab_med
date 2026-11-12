package com.enicarthage;

public class Main {

    public static void main(String[] args) {
        try {
            int port = 8081;
            String envPort = System.getenv("PORT");
            if (envPort != null && !envPort.isBlank()) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException ignored) {}
            }

            Integer medPort = null;
            String envMedPort = System.getenv("MED_PORT");
            if (envMedPort != null && !envMedPort.isBlank()) {
                try {
                    medPort = Integer.parseInt(envMedPort);
                } catch (NumberFormatException ignored) {}
            }

            ApiServer server = new ApiServer(port, medPort);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Main] Arrêt de l'application en cours...");
                server.stop();
            }));

            server.start();

        } catch (Exception e) {
            System.err.println("[Main] Impossible de démarrer le serveur.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
