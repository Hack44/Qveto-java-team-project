package com.qveto.ecommerce;

import com.qveto.ecommerce.util.DBConnectionPool;
import com.qveto.ecommerce.util.DatabaseRetryPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Enterprise Database Connection Provider & Pooling Gateway.
 *
 * Provides thread-safe, pooled JDBC connections via {@link DBConnectionPool}
 * with automated recycling, pre-lease validation, and transparent fallback.
 *
 * Also provides centralized transactional retry execution via {@link DatabaseRetryPolicy}.
 *
 * Member 1 — DB Schema, JDBC Connectivity & Persistence layer.
 */
public class DBConnection {

    private static final Properties properties = new Properties();
    private static final DatabaseRetryPolicy defaultRetryPolicy = new DatabaseRetryPolicy();
    private static volatile boolean usePool = true;

    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                    "db.properties not found in src/main/resources");
            }

            properties.load(input);

            String poolEnabledProp = properties.getProperty("db.pool.enabled", "true");
            usePool = Boolean.parseBoolean(poolEnabledProp.trim());

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Retrieves a database connection.
     * Uses {@link DBConnectionPool} for connection pooling by default,
     * which transparently recycles physical connections on {@code conn.close()}.
     *
     * @return active {@link Connection}
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        validateCredentials();

        if (usePool) {
            try {
                return DBConnectionPool.getInstance().getConnection();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Connection pool lease failed, falling back to direct connection: " + e.getMessage());
                return getConnectionDirect();
            }
        }
        return getConnectionDirect();
    }

    /**
     * Bypasses the connection pool to acquire a raw physical JDBC connection.
     * Useful for administrative maintenance, schema migrations, and diagnostics.
     *
     * @return raw physical {@link Connection}
     * @throws SQLException on database error
     */
    public static Connection getConnectionDirect() throws SQLException {
        validateCredentials();
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private static void validateCredentials() {
        String password = properties.getProperty("db.password");
        if (password == null || password.isBlank()
                || password.equals("REPLACE_WITH_YOUR_MYSQL_PASSWORD")) {
            throw new IllegalStateException(
                "Set your MySQL password in your local db.properties file.");
        }
    }

    // ---------------------------------------------------------------
    // Resilience & Retry Execution Wrappers
    // ---------------------------------------------------------------

    /**
     * Executes a database operation with exponential backoff retries for transient errors.
     */
    public static <T> T executeWithRetry(String operationName, DatabaseRetryPolicy.SQLSupplier<T> action) throws SQLException {
        return defaultRetryPolicy.execute(operationName, action);
    }

    /**
     * Executes a void database operation with exponential backoff retries for transient errors.
     */
    public static void executeVoidWithRetry(String operationName, DatabaseRetryPolicy.SQLRunnable action) throws SQLException {
        defaultRetryPolicy.executeVoid(operationName, action);
    }

    // ---------------------------------------------------------------
    // Pool Metrics & Diagnostics
    // ---------------------------------------------------------------

    public static boolean isPoolEnabled() {
        return usePool;
    }

    public static void setPoolEnabled(boolean enabled) {
        usePool = enabled;
    }

    public static String getPoolStats() {
        return usePool ? DBConnectionPool.getInstance().getPoolStats() : "Pool Disabled (Direct JDBC Mode)";
    }

    public static int getActiveConnections() {
        return usePool ? DBConnectionPool.getInstance().getActiveCount() : 0;
    }

    public static int getIdleConnections() {
        return usePool ? DBConnectionPool.getInstance().getIdleCount() : 0;
    }

    public static void shutdownPool() {
        if (usePool) {
            DBConnectionPool.getInstance().shutdown();
        }
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("   Qveto DBConnection & Pool Diagnostic Test        ");
        System.out.println("=====================================================");
        try {
            System.out.println("Initial Pool State: " + getPoolStats());
            try (Connection conn = getConnection()) {
                System.out.println("Acquired Connection: " + conn.getClass().getName());
                System.out.println("Active State: " + getPoolStats());
            }
            System.out.println("After close() Pool State: " + getPoolStats());
            System.out.println("MySQL database connection and pool verified successfully!");
        } catch (Exception e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
