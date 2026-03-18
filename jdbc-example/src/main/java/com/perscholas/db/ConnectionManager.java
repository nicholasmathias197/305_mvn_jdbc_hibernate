package com.perscholas.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized database connection factory.
 * All DAO classes use this to obtain a connection — credentials are stored in one place.
 */
public class ConnectionManager {

    private static final String URL = "jdbc:mysql://localhost:3306/classicmodels";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    /**
     * Returns a new connection to the classicmodels database.
     * Callers are responsible for closing the connection (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
