package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.AuditLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized Store and Audit Activity Journal.
 * Captures all administrative and customer transactions, providing an immutable audit trail
 * for compliance, security tracking, and operational transparency.
 *
 * Supports Member 2 (UI, Presentation & Store Management) audit requirements.
 */
public class AuditStore {

    private static final List<AuditLog> auditTrail = new CopyOnWriteArrayList<>();
    private static final AtomicInteger nextId = new AtomicInteger(100);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        seedSampleAuditLogs();
    }

    private AuditStore() {
        // Utility class
    }

    private static void seedSampleAuditLogs() {
        auditTrail.clear();

        auditTrail.add(new AuditLog(nextId.incrementAndGet(),
                LocalDateTime.now().minusHours(24).format(FORMATTER),
                "SYSTEM", "SYSTEM", "STORE_SECURITY", "SYSTEM_STARTUP",
                "E-Commerce Gateway and Database Connection initialized successfully.", "127.0.0.1"));

        auditTrail.add(new AuditLog(nextId.incrementAndGet(),
                LocalDateTime.now().minusHours(18).format(FORMATTER),
                "admin", "ADMIN", "INVENTORY", "PRODUCT_RESTOCKED",
                "Restocked 'Wireless Noise-Cancelling Headphones' (+25 units). New Stock: 45.", "192.168.1.10"));

        auditTrail.add(new AuditLog(nextId.incrementAndGet(),
                LocalDateTime.now().minusHours(14).format(FORMATTER),
                "admin", "ADMIN", "ORDER_MANAGEMENT", "ORDER_STATUS_CHANGED",
                "Order ORD-2026-003 status updated from CONFIRMED to SHIPPED (Courier: BlueDart).", "192.168.1.10"));

        auditTrail.add(new AuditLog(nextId.incrementAndGet(),
                LocalDateTime.now().minusHours(10).format(FORMATTER),
                "alex.morgan", "CUSTOMER", "CHECKOUT", "ORDER_PLACED",
                "Placed order ORD-2026-001 ($149.99 via Credit Card). Shipping to Bengaluru.", "192.168.1.45"));

        auditTrail.add(new AuditLog(nextId.incrementAndGet(),
                LocalDateTime.now().minusHours(6).format(FORMATTER),
                "admin", "ADMIN", "RETURNS_REFUNDS", "RETURN_APPROVED",
                "Approved return request for ORD-2026-004. Reversed payment transaction ($49.99).", "192.168.1.10"));

        auditTrail.add(new AuditLog(nextId.incrementAndGet(),
                LocalDateTime.now().minusHours(2).format(FORMATTER),
                "alex.morgan", "CUSTOMER", "CHECKOUT", "PROMO_APPLIED",
                "Applied promotional coupon 'SAVE20' for 20% discount on cart checkout.", "192.168.1.45"));
    }

    public static AuditLog log(String username, String role, String category, String action, String details) {
        return log(username, role, category, action, details, "127.0.0.1");
    }

    public static AuditLog log(String username, String role, String category, String action, String details, String ip) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditLog entry = new AuditLog(nextId.incrementAndGet(), timestamp, username, role, category, action, details, ip);
        auditTrail.add(0, entry); // Keep newest first
        return entry;
    }

    public static List<AuditLog> getAllLogs() {
        return Collections.unmodifiableList(auditTrail);
    }

    public static List<AuditLog> getLogsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return getAllLogs();
        }
        List<AuditLog> filtered = new ArrayList<>();
        for (AuditLog log : auditTrail) {
            if (log.getCategory().equalsIgnoreCase(category)) {
                filtered.add(log);
            }
        }
        return filtered;
    }

    public static List<AuditLog> searchLogs(String query, String category) {
        String cleanQuery = (query != null) ? query.trim().toLowerCase() : "";
        boolean matchAllCategories = (category == null || category.equalsIgnoreCase("ALL"));

        List<AuditLog> results = new ArrayList<>();
        for (AuditLog log : auditTrail) {
            boolean categoryMatch = matchAllCategories || log.getCategory().equalsIgnoreCase(category);
            if (!categoryMatch) continue;

            if (cleanQuery.isEmpty()) {
                results.add(log);
            } else {
                boolean textMatch = log.getAction().toLowerCase().contains(cleanQuery)
                        || log.getDetails().toLowerCase().contains(cleanQuery)
                        || log.getUsername().toLowerCase().contains(cleanQuery)
                        || log.getRole().toLowerCase().contains(cleanQuery)
                        || log.getTimestamp().toLowerCase().contains(cleanQuery);
                if (textMatch) {
                    results.add(log);
                }
            }
        }
        return results;
    }

    public static int getLogCount() {
        return auditTrail.size();
    }

    public static void exportAuditCsv(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("LogID,Timestamp,Actor,Role,Category,Action,Details,IPAddress\n");
            for (AuditLog log : auditTrail) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        log.getLogId(),
                        log.getTimestamp(),
                        log.getUsername(),
                        log.getRole(),
                        log.getCategory(),
                        log.getAction(),
                        log.getDetails().replace("\"", "\"\""),
                        log.getIpAddress()));
            }
        }
    }
}
