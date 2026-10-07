package com.caeliusconsulting.jobqueuesim.database;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides raw JDBC connections sourced from environment variables.
 *
 * Required environment variables:
 *   DB_URL      e.g. jdbc:mysql://localhost:3306/jobqueue?useSSL=false&allowPublicKeyRetrieval=true
 *   DB_USER     e.g. root
 *   DB_PASSWORD e.g. secret
 *
 * No connection pooling — each call to getConnection() opens a new connection.
 * Callers are responsible for closing it (use try-with-resources).
 *
 * Credentials are never hardcoded — always read from the environment.
 *
 * Syllabus: JDBC, environment variables, try-with-resources (caller's responsibility)
 */
public final class DatabaseConnection {

    private static final String DB_URL      = System.getenv("DB_URL");
    private static final String DB_USER     = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private DatabaseConnection() { }

    /**
     * Opens and returns a new JDBC Connection.
     *
     * @throws DatabaseException if env vars are missing or the connection fails
     */
    public static Connection getConnection() {
        validateConfig();
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Failed to connect to database at: " + DB_URL, e);
        }
    }

    private static void validateConfig() {
        if (DB_URL == null || DB_URL.isBlank()) {
            throw new DatabaseException(
                    "Missing required environment variable: DB_URL\n" +
                    "Example: export DB_URL=jdbc:mysql://localhost:3306/jobqueue" +
                    "?useSSL=false&allowPublicKeyRetrieval=true");
        }
        if (DB_USER == null || DB_USER.isBlank()) {
            throw new DatabaseException(
                    "Missing required environment variable: DB_USER");
        }
        if (DB_PASSWORD == null) {
            throw new DatabaseException(
                    "Missing required environment variable: DB_PASSWORD");
        }
    }
}
