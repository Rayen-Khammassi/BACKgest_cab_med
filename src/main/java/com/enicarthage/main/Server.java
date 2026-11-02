
package com.enicarthage.main;

import com.enicarthage.controller.AuthController;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import com.enicarthage.controller.RegisterController;

public class Server {

    public static void main(String[] args) {

        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(8081),
                    0);

            // Register the login endpoint
            server.createContext(
                    "/api/auth/login",
                    new AuthController());

            // Register the patient registration endpoint
            server.createContext(
                    "/api/auth/register",
                    new RegisterController());
            server.start();

            System.out.println("Backend server started!");
            System.out.println("Login API: http://localhost:8081/api/auth/login");
            System.out.println("Register API: http://localhost:8081/api/auth/register");

        } catch (Exception e) {
            System.out.println("Could not start the server.");
            e.printStackTrace();
        }
    }
}
