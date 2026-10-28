package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.OrderDAO;
import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Order Store coordinating between the JDBC OrderDAO layer and
 * in-memory fallback state for standalone demo mode.
 * 
 * Supports Member 2 requirements:
 * - Order placement with inventory reservation
 * - Customer order history tracking with status filtering
 * - Order cancellation with automatic inventory stock replenishment
 * - Admin order fulfillment updates and sales revenue KPI calculations
 */
public class OrderStore {

    private static final List<Order> inMemoryOrders = new CopyOnWriteArrayList<>();
    private static int nextMockOrderId = 10;

    static {
        initializeSampleOrders();
    }

    private OrderStore() {
        // Utility class
    }

    private static void initializeSampleOrders() {
        inMemoryOrders.clear();

        Order o1 = new Order(1, "ORD-2026-001", 1, 79.98, "DELIVERED", "COD", "124 Tech Park Road, Bengaluru", "2026-10-15");
        o1.addItem(new OrderItem(1, 1, 103, "Classic Cotton T-Shirt", 2, 19.99));
        o1.addItem(new OrderItem(2, 1, 104, "Java Programming: OOP Essentials", 1, 39.99));
        inMemoryOrders.add(o1);

        Order o2 = new Order(2, "ORD-2026-002", 1, 59.99, "SHIPPED", "PREPAID", "124 Tech Park Road, Bengaluru", "2026-10-18");
        o2.addItem(new OrderItem(3, 2, 101, "Wireless Bluetooth Headphones", 1, 59.99));
        inMemoryOrders.add(o2);

        Order o3 = new Order(3, "ORD-2026-003", 1, 94.98, "PENDING", "COD", "124 Tech Park Road, Bengaluru", "2026-10-20");
        o3.addItem(new OrderItem(4, 3, 102, "Mechanical Gaming Keyboard", 1, 79.99));
        o3.addItem(new OrderItem(5, 3, 105, "Stainless Steel Water Bottle", 1, 14.99));
        inMemoryOrders.add(o3);

        Order o4 = new Order(4, "ORD-2026-004", 1, 14.99, "CANCELLED", "COD", "124 Tech Park Road, Bengaluru", "2026-10-12");
        o4.addItem(new OrderItem(6, 4, 105, "Stainless Steel Water Bottle", 1, 14.99));
        inMemoryOrders.add(o4);
    }

    /**
     * Returns all orders in the system (for Admin view).
     */
    public static List<Order> getAllOrders() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<Order> dbList = new OrderDAO().findAll();
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Throwable t) {
                System.err.println("[OrderStore] DB findAll fallback: " + t.getMessage());
            }
        }
        return new ArrayList<>(inMemoryOrders);
    }

    /**
     * Returns all orders placed by a specific user.
     */
    public static List<Order> getOrdersByUser(int userId) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<Order> dbList = new OrderDAO().findByUserId(userId);
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Throwable t) {
                System.err.println("[OrderStore] DB findByUserId fallback: " + t.getMessage());
            }
        }

        List<Order> userOrders = new ArrayList<>();
        for (Order o : inMemoryOrders) {
            if (o.getUserId() == userId) {
                userOrders.add(o);
            }
        }
        return userOrders;
    }

    /**
     * Places a new customer order and reduces the corresponding inventory stock.
     */
    public static Order placeOrder(Order order) {
        int assignedId = nextMockOrderId++;

        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int dbId = new OrderDAO().placeOrder(order);
                if (dbId > 0) {
                    assignedId = dbId;
                }
            } catch (Throwable t) {
                System.err.println("[OrderStore] DB placeOrder fallback: " + t.getMessage());
            }
        }

        order.setOrderId(assignedId);
        inMemoryOrders.add(0, order); // Add to top of list

        // Decrement product inventory stock
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ProductStore.adjustStock(item.getProductId(), -item.getQuantity());
            }
        }

        return order;
    }

    /**
     * Updates an order's status (e.g. PENDING -> CONFIRMED -> SHIPPED -> DELIVERED).
     */
    public static boolean updateStatus(int orderId, String newStatus) {
        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new OrderDAO().updateStatus(orderId, newStatus);
            } catch (Throwable t) {
                System.err.println("[OrderStore] DB updateStatus fallback: " + t.getMessage());
            }
        }

        for (Order o : inMemoryOrders) {
            if (o.getOrderId() == orderId) {
                o.setOrderStatus(newStatus);
                return true;
            }
        }
        return dbSuccess;
    }

    /**
     * Cancels an order and automatically replenishes reserved product stock.
     */
    public static boolean cancelOrder(Order order) {
        if (order == null || !order.isCancellable()) return false;

        boolean success = updateStatus(order.getOrderId(), "CANCELLED");
        order.setOrderStatus("CANCELLED");

        // Replenish stock
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ProductStore.adjustStock(item.getProductId(), item.getQuantity());
            }
        }
        return success;
    }

    /**
     * Returns total order count across the platform.
     */
    public static int countAll() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int count = new OrderDAO().countAll();
                if (count > 0) return count;
            } catch (Throwable t) {
                System.err.println("[OrderStore] DB countAll fallback: " + t.getMessage());
            }
        }
        return inMemoryOrders.size();
    }

    /**
     * Calculates total sales revenue from confirmed/delivered orders.
     */
    public static double calculateTotalRevenue() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                double revenue = new OrderDAO().calculateTotalRevenue();
                if (revenue > 0) return revenue;
            } catch (Throwable t) {
                System.err.println("[OrderStore] DB calculateTotalRevenue fallback: " + t.getMessage());
            }
        }

        double revenue = 0.0;
        for (Order o : inMemoryOrders) {
            if ("DELIVERED".equalsIgnoreCase(o.getOrderStatus()) || "CONFIRMED".equalsIgnoreCase(o.getOrderStatus()) || "SHIPPED".equalsIgnoreCase(o.getOrderStatus())) {
                revenue += o.getTotalAmount();
            }
        }
        return revenue;
    }
}
