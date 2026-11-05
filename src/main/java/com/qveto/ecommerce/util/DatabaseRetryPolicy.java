package com.qveto.ecommerce.util;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resilient Retry Policy with Exponential Backoff and Jitter for Database Operations.
 *
 * Classifies SQLExceptions into transient (recoverable) vs fatal (non-recoverable):
 * - Transient (Retried):
 *     - Deadlock detected (MySQL error 1213 / SQLState 40001)
 *     - Lock wait timeout exceeded (MySQL error 1205)
 *     - Communications link failure (SQLState 08S01)
 *     - Connection lost / unable to establish (SQLState 08001, 08003, 08006, 08007)
 *     - Server shutdown / connection reset
 * - Fatal (Never Retried):
 *     - Constraint violations (Duplicate entry 1062, Foreign key 1451/1452)
 *     - SQL syntax errors (1064, 42000)
 *     - Access denied / authentication failures (1045, 28000)
 *
 * Member 1 — Persistence Resilience & Reliability layer.
 */
public class DatabaseRetryPolicy {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_INITIAL_BACKOFF_MS = 150L;
    private static final long DEFAULT_MAX_BACKOFF_MS = 2000L;
    private static final double JITTER_FACTOR = 0.25;

    // MySQL Vendor error codes for transient concurrency / network errors
    private static final Set<Integer> TRANSIENT_VENDOR_CODES = new HashSet<>(Arrays.asList(
            1205, // Lock wait timeout exceeded
            1213, // Deadlock found when trying to get lock
            1158, // Got error reading communication packets
            1159, // Got timeout reading communication packets
            1160, // Got error writing communication packets
            1161, // Got timeout writing communication packets
            2002, // Can't connect to local MySQL server
            2003, // Can't connect to MySQL server
            2006, // MySQL server has gone away
            2013  // Lost connection to MySQL server during query
    ));

    // SQLStates indicating connection or transaction state issues
    private static final Set<String> TRANSIENT_SQL_STATES = new HashSet<>(Arrays.asList(
            "08001", // Unable to connect to database
            "08003", // Connection does not exist
            "08004", // Server rejected connection
            "08006", // Connection failure
            "08007", // Connection failure during transaction
            "08S01", // Communications link failure
            "40001", // Serialization failure / deadlock
            "41000"  // Deadlock detected
    ));

    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    public DatabaseRetryPolicy() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_BACKOFF_MS, DEFAULT_MAX_BACKOFF_MS);
    }

    public DatabaseRetryPolicy(int maxAttempts, long initialBackoffMs, long maxBackoffMs) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialBackoffMs = Math.max(10L, initialBackoffMs);
        this.maxBackoffMs = Math.max(this.initialBackoffMs, maxBackoffMs);
    }

    @FunctionalInterface
    public interface SQLSupplier<T> {
        T get() throws SQLException;
    }

    @FunctionalInterface
    public interface SQLRunnable {
        void run() throws SQLException;
    }

    /**
     * Executes a database action returning a value, retrying on transient errors.
     *
     * @param operationName human-readable operation label for logging
     * @param action        the supplier to execute
     * @param <T>           the return type
     * @return the result of the action
     * @throws SQLException if attempts are exhausted or a fatal error occurs
     */
    public <T> T execute(String operationName, SQLSupplier<T> action) throws SQLException {
        int attempt = 0;
        SQLException lastException = null;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                return action.get();
            } catch (SQLException e) {
                lastException = e;
                if (!isTransient(e) || attempt >= maxAttempts) {
                    throw e;
                }

                long backoff = calculateBackoff(attempt);
                System.err.println(String.format(
                        "[DatabaseRetryPolicy] %s: transient error (Code: %d, SQLState: %s) on attempt %d/%d. Retrying in %d ms...",
                        operationName, e.getErrorCode(), e.getSQLState(), attempt, maxAttempts, backoff
                ));

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Database retry interrupted during backoff", ie);
                }
            }
        }

        throw lastException != null ? lastException : new SQLException("Operation failed after " + maxAttempts + " attempts");
    }

    /**
     * Executes a void database action, retrying on transient errors.
     */
    public void executeVoid(String operationName, SQLRunnable action) throws SQLException {
        execute(operationName, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Determines whether an exception represents a transient, recoverable failure.
     */
    public static boolean isTransient(SQLException ex) {
        if (ex == null) return false;

        // Check vendor error code
        if (TRANSIENT_VENDOR_CODES.contains(ex.getErrorCode())) {
            return true;
        }

        // Check SQLState
        String sqlState = ex.getSQLState();
        if (sqlState != null) {
            if (TRANSIENT_SQL_STATES.contains(sqlState)) {
                return true;
            }
            if (sqlState.startsWith("08")) { // All 08xxx connection class errors
                return true;
            }
        }

        // Check message keywords
        String msg = ex.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("deadlock") || lower.contains("lock wait timeout")
                    || lower.contains("communications link failure")
                    || lower.contains("connection reset")
                    || lower.contains("broken pipe")) {
                return true;
            }
        }

        // Check nested cause
        Throwable cause = ex.getCause();
        if (cause instanceof SQLException) {
            return isTransient((SQLException) cause);
        }

        return false;
    }

    private long calculateBackoff(int attempt) {
        // Exponential backoff: initialBackoffMs * 2^(attempt-1)
        long exp = (long) (initialBackoffMs * Math.pow(2, attempt - 1));
        long bounded = Math.min(exp, maxBackoffMs);

        // Add randomized jitter to avoid thundering herd problem
        double jitterRange = bounded * JITTER_FACTOR;
        double jitter = ThreadLocalRandom.current().nextDouble(-jitterRange, jitterRange);
        return Math.max(10L, (long) (bounded + jitter));
    }

    public int getMaxAttempts() { return maxAttempts; }
    public long getInitialBackoffMs() { return initialBackoffMs; }
    public long getMaxBackoffMs() { return maxBackoffMs; }
}
