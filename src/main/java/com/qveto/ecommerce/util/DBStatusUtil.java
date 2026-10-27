package com.qveto.ecommerce.util;

import com.qveto.ecommerce.DBConnection;

import java.sql.Connection;

/**
 * Utility class to safely check MySQL database connectivity status without
 * throwing initialization or linkage errors when running in offline or standalone demo mode.
 * 
 * Supports Member 2 UI integration with Member 1's JDBC DAO layer.
 */
public class DBStatusUtil {

    private static Boolean cachedStatus = null;
    private static long lastCheckTime = 0;
    private static final long CACHE_TTL_MS = 10000; // recheck every 10 seconds

    private DBStatusUtil() {
        // Utility class
    }

    /**
     * Checks if the MySQL database is available and responding.
     * Uses a short cache to avoid excessive connection attempts on every UI repaint.
     */
    public static boolean isDatabaseConnected() {
        long now = System.currentTimeMillis();
        if (cachedStatus != null && (now - lastCheckTime) < CACHE_TTL_MS) {
            return cachedStatus;
        }

        try {
            try (Connection conn = DBConnection.getConnection()) {
                cachedStatus = (conn != null && !conn.isClosed());
            }
        } catch (Throwable t) {
            // Catches SQLException, IllegalStateException, ExceptionInInitializerError, NoClassDefFoundError
            cachedStatus = false;
        }
        lastCheckTime = now;
        return cachedStatus;
    }

    /**
     * Forces an immediate re-check of database connectivity.
     */
    public static boolean refreshDatabaseStatus() {
        cachedStatus = null;
        return isDatabaseConnected();
    }

    /**
     * Returns an HTML formatted pill badge representing the database state for UI headers.
     */
    public static String getStatusBadgeHtml() {
        if (isDatabaseConnected()) {
            return "<html><span style='background-color: #dcfce7; color: #15803d; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: bold;'>&#9679; MySQL Active</span></html>";
        } else {
            return "<html><span style='background-color: #fef3c7; color: #b45309; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: bold;'>&#9675; Standalone Demo</span></html>";
        }
    }

    /**
     * Returns a plain text label for the database status.
     */
    public static String getStatusText() {
        return isDatabaseConnected() ? "MySQL Active" : "Demo / Standalone Mode";
    }
}
