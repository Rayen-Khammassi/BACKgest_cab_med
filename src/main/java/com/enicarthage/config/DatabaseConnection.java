package com.enicarthage.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/gest_cab_med";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Driver registration: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
