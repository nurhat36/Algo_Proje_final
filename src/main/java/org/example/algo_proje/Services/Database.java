package org.example.algo_proje.Services;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static final String URL =
            "jdbc:sqlserver://DESKTOP-T11FMIO;databaseName=SocialAppDB;integratedSecurity=True;encrypt=True;trustServerCertificate=True";



    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            throw new RuntimeException("Database Connection Error: " + e.getMessage());
        }
    }
}