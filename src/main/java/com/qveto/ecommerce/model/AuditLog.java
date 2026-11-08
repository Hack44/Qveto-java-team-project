package com.qveto.ecommerce.model;

/**
 * Model entity representing an administrative or operational audit journal event.
 * Tracks critical changes in inventory, order statuses, return settlements, and store security.
 *
 * Supports Member 2 (UI, Presentation & Store Management) audit & compliance requirements.
 */
public class AuditLog {

    private int logId;
    private String timestamp;
    private String username;
    private String role; // ADMIN, CUSTOMER, SYSTEM
    private String category; // ORDER_MANAGEMENT, INVENTORY, STORE_SECURITY, USER_MANAGEMENT, RETURNS_REFUNDS, CHECKOUT
    private String action;
    private String details;
    private String ipAddress;

    public AuditLog() {
        this.ipAddress = "127.0.0.1";
    }

    public AuditLog(int logId, String timestamp, String username, String role,
                    String category, String action, String details, String ipAddress) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.username = username;
        this.role = role != null ? role.toUpperCase() : "SYSTEM";
        this.category = category != null ? category.toUpperCase() : "GENERAL";
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress != null ? ipAddress : "127.0.0.1";
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    @Override
    public String toString() {
        return "[" + timestamp + "] (" + role + "/" + username + ") " + action + ": " + details;
    }
}
