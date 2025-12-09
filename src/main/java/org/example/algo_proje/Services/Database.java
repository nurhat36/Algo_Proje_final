package org.example.algo_proje.Services;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static final String URL =
            "jdbc:sqlserver://DESKTOP-T11FMIO;"
                    + "databaseName=SocialAppDB;"
                    + "integratedSecurity=true;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Database connected successfully!");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database Connection Error: " + e.getMessage());
        }
    }


}