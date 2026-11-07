package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.Notification;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized Store for managing and dispatching real-time store alerts,
 * customer order tracking milestones, inventory restock warnings, and return requests.
 *
 * Supports Member 2 (UI, Presentation & Store Management) notification hub requirements.
 */
public class NotificationStore {

    private static final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private static final AtomicInteger nextId = new AtomicInteger(100);

    static {
        seedSampleNotifications();
    }

    private NotificationStore() {
        // Utility class
    }

    private static void seedSampleNotifications() {
        notifications.clear();

        // Customer Welcome & Promo Alerts
        notifications.add(new Notification(1, 0, "CUSTOMER", "PROMO",
                "Welcome to Qveto E-Commerce!",
                "Enjoy 10% instant discount on your initial store purchase using voucher code 'QVETO10'.",
                "2026-11-01 10:00", false));

        notifications.add(new Notification(2, 1, "CUSTOMER", "ORDER",
                "Order Dispatched: ORD-1001",
                "Your package has departed the fulfillment hub and is currently in transit with express carrier.",
                "2026-11-03 14:30", false));

        // Admin Operational Alerts
        notifications.add(new Notification(3, 0, "ADMIN", "STOCK",
                "Low Inventory Alert: Wireless Headphones",
                "Wireless Bluetooth Headphones (#101) has reached reorder threshold (4 units remaining). Procurement advised.",
                "2026-11-04 09:15", false));

        notifications.add(new Notification(4, 0, "ADMIN", "SYSTEM",
                "Database Connection Pool Active",
                "Connection pool initialized with 10 proxy recycling instances and resilient exponential backoff retry.",
                "2026-11-05 08:00", true));
    }

    public static void postNotification(Notification n) {
        if (n == null) return;
        n.setId(nextId.incrementAndGet());
        notifications.add(0, n);
    }

    public static void postOrderAlert(int userId, String title, String message) {
        postNotification(new Notification(userId, "CUSTOMER", "ORDER", title, message));
    }

    public static void postAdminAlert(String title, String message) {
        postNotification(new Notification(0, "ADMIN", "STOCK", title, message));
    }

    public static void postReturnAlert(int userId, String orderNumber, String reason) {
        // Post for customer
        postNotification(new Notification(userId, "CUSTOMER", "RETURN",
                "Return Request Submitted (" + orderNumber + ")",
                "Your return request for order " + orderNumber + " is currently under review by our fulfillment team. Reason: " + reason));

        // Post for admin
        postNotification(new Notification(0, "ADMIN", "RETURN",
                "Action Required: Return Request (" + orderNumber + ")",
                "Customer requested return for delivered order " + orderNumber + ". Reason: " + reason + ". Please inspect in Order Management."));
    }

    public static List<Notification> getNotificationsForUser(int userId, String role) {
        String cleanRole = role != null ? role.toUpperCase() : "CUSTOMER";
        List<Notification> result = new ArrayList<>();

        for (Notification n : notifications) {
            boolean matchesRole = "ALL".equalsIgnoreCase(n.getTargetRole()) || cleanRole.equalsIgnoreCase(n.getTargetRole());
            boolean matchesUser = n.getTargetUserId() == 0 || n.getTargetUserId() == userId;

            if (matchesRole && matchesUser) {
                result.add(n);
            }
        }
        return result;
    }

    public static int getUnreadCount(int userId, String role) {
        int count = 0;
        for (Notification n : getNotificationsForUser(userId, role)) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    public static void markAllAsRead(int userId, String role) {
        for (Notification n : getNotificationsForUser(userId, role)) {
            n.setRead(true);
        }
    }

    public static void clearNotifications(int userId, String role) {
        String cleanRole = role != null ? role.toUpperCase() : "CUSTOMER";
        notifications.removeIf(n -> {
            boolean matchesRole = "ALL".equalsIgnoreCase(n.getTargetRole()) || cleanRole.equalsIgnoreCase(n.getTargetRole());
            boolean matchesUser = n.getTargetUserId() == 0 || n.getTargetUserId() == userId;
            return matchesRole && matchesUser;
        });
    }
}
