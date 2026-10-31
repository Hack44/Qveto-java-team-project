package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.CartDAO;
import com.qveto.ecommerce.model.CartItem;
import com.qveto.ecommerce.model.OrderItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Shopping Cart Store coordinating between the JDBC CartDAO layer and
 * in-memory session carts for seamless standalone demo mode.
 * 
 * Supports Member 2 (UI, Login & User Management) Cart requirements:
 * - Session cart loading per customer
 * - Adding, updating, removing, and clearing cart items
 * - Synchronizing cart badges and totals
 */
public class CartStore {

    // In-memory fallback: Map<userId, List<OrderItem>>
    private static final Map<Integer, List<OrderItem>> sessionCarts = new ConcurrentHashMap<>();

    private CartStore() {
        // Utility class
    }

    /**
     * Loads the active cart for a specific customer.
     */
    public static List<OrderItem> getCart(int userId) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<CartItem> dbItems = new CartDAO().getCartByUserId(userId);
                if (dbItems != null && !dbItems.isEmpty()) {
                    List<OrderItem> orderItems = new ArrayList<>();
                    int seq = 1;
                    for (CartItem ci : dbItems) {
                        orderItems.add(new OrderItem(
                                seq++,
                                0,
                                ci.getProductId(),
                                ci.getProductName(),
                                ci.getQuantity(),
                                ci.getUnitPriceAsDouble()
                        ));
                    }
                    sessionCarts.put(userId, new CopyOnWriteArrayList<>(orderItems));
                    return orderItems;
                }
            } catch (Throwable t) {
                System.err.println("[CartStore] DB getCartByUserId fallback: " + t.getMessage());
            }
        }

        return sessionCarts.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
    }

    /**
     * Adds an item to the customer's cart.
     */
    public static void addItem(int userId, int productId, String productName, double price, int qty) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                new CartDAO().addItem(userId, productId, qty);
            } catch (Throwable t) {
                System.err.println("[CartStore] DB addItem fallback: " + t.getMessage());
            }
        }

        List<OrderItem> cart = sessionCarts.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        for (OrderItem item : cart) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + qty);
                return;
            }
        }
        cart.add(new OrderItem(cart.size() + 1, 0, productId, productName, qty, price));
    }

    /**
     * Updates item quantity in the cart. If newQty <= 0, item is removed.
     */
    public static void updateQuantity(int userId, int productId, int newQty) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                new CartDAO().updateQuantity(userId, productId, newQty);
            } catch (Throwable t) {
                System.err.println("[CartStore] DB updateQuantity fallback: " + t.getMessage());
            }
        }

        List<OrderItem> cart = sessionCarts.get(userId);
        if (cart != null) {
            if (newQty <= 0) {
                cart.removeIf(item -> item.getProductId() == productId);
            } else {
                for (OrderItem item : cart) {
                    if (item.getProductId() == productId) {
                        item.setQuantity(newQty);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes an item completely from the cart.
     */
    public static void removeItem(int userId, int productId) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                new CartDAO().removeItem(userId, productId);
            } catch (Throwable t) {
                System.err.println("[CartStore] DB removeItem fallback: " + t.getMessage());
            }
        }

        List<OrderItem> cart = sessionCarts.get(userId);
        if (cart != null) {
            cart.removeIf(item -> item.getProductId() == productId);
        }
    }

    /**
     * Clears all items from the customer's cart (e.g. upon checkout).
     */
    public static void clearCart(int userId) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                new CartDAO().clearCart(userId);
            } catch (Throwable t) {
                System.err.println("[CartStore] DB clearCart fallback: " + t.getMessage());
            }
        }

        List<OrderItem> cart = sessionCarts.get(userId);
        if (cart != null) {
            cart.clear();
        }
    }
}
