
package com.enicarthage.config;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsFilter extends Filter {

    @Override
    public void doFilter(
            HttpExchange exchange,
            Chain chain
    ) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        System.out.println(
                "[CORS] " + method + " " + path
        );

        Headers headers = exchange.getResponseHeaders();

        headers.set(
                "Access-Control-Allow-Origin",
                "http://localhost:5173"
        );

        headers.set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        headers.set(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
        );

        headers.set("Vary", "Origin");

        if ("OPTIONS".equalsIgnoreCase(method)) {

            System.out.println(
                    "[CORS] Preflight accepté"
            );

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Filtre CORS GESTCABMED";
    }
}
