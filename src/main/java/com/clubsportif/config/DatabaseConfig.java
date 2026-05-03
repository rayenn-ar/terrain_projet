package com.clubsportif.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de connexion JDBC.
 * Fournit une connexion unique et réutilisable à la base de données MySQL.
 */
public class DatabaseConfig {

    private static DatabaseConfig instance;
    private Connection connection;

    private DatabaseConfig() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = createConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Driver MySQL introuvable. Vérifiez que mysql-connector-j est dans le classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Impossible de se connecter à MySQL [" + AppConfig.DB_URL + "]: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }
        return connection;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.DB_URL,
            AppConfig.DB_USER,
            AppConfig.DB_PASSWORD
        );
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture connexion: " + e.getMessage());
        }
    }
}
