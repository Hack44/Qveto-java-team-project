package com.qveto.ecommerce.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model entity representing an in-app notification, order milestone alert,
 * or executive store announcement.
 *
 * Supports Member 2 (UI, Presentation & Store Management) real-time customer and admin alert hub.
 */
public class Notification {

    private int id;
    private int targetUserId;   // 0 for role-wide broadcast
    private String targetRole;  // "ALL", "CUSTOMER", "ADMIN"
    private String type;        // "ORDER", "STOCK", "PROMO", "SYSTEM", "RETURN"
    private String title;
    private String message;
    private String timestamp;
    private boolean read;

    public Notification() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.read = false;
    }

    public Notification(int id, int targetUserId, String targetRole, String type,
                        String title, String message, String timestamp, boolean read) {
        this.id = id;
        this.targetUserId = targetUserId;
        this.targetRole = targetRole != null ? targetRole.toUpperCase() : "ALL";
        this.type = type != null ? type.toUpperCase() : "SYSTEM";
        this.title = title;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.read = read;
    }

    public Notification(int targetUserId, String targetRole, String type, String title, String message) {
        this(0, targetUserId, targetRole, type, title, message, null, false);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTargetUserId() { return targetUserId; }
    public void setTargetUserId(int targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getTypeIcon() {
        if ("ORDER".equalsIgnoreCase(type)) return "📦";
        if ("STOCK".equalsIgnoreCase(type)) return "⚠️";
        if ("PROMO".equalsIgnoreCase(type)) return "🎟️";
        if ("RETURN".equalsIgnoreCase(type)) return "🔄";
        return "ℹ️";
    }

    @Override
    public String toString() {
        return "[" + type + "] " + title + " (" + timestamp + ") - Read: " + read;
    }
}
