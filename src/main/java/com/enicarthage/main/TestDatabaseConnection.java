
package com.enicarthage.main;

import com.enicarthage.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnection {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("Connection successful!");
            System.out.println("Database: " + connection.getCatalog());

        } catch (SQLException e) {

            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    }
}
