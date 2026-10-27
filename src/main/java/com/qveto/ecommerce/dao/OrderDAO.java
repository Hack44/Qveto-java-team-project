package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'orders' and 'order_items' tables.
 *
 * Handles order placement, retrieval, status updates, and loading
 * of associated line items. Uses transactions to ensure that an
 * order header and all its items are committed atomically.
 *
 * Member 1 — DB/JDBC layer.
 */
public class OrderDAO {

    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    /**
     * Persists a complete order (header + all line items) as a single atomic transaction.
     *
     * The method:
     * 1. Inserts the order header into 'orders' and retrieves the generated order_id.
     * 2. Inserts each {@link OrderItem} into 'order_items' with the new order_id.
     * 3. Rolls back the entire transaction if any step fails.
     *
     * @param order the {@link Order} to save; its {@code items} list must not be empty
     * @return the auto-generated order_id, or -1 if the transaction failed
     */
    public int placeOrder(Order order) {
        String orderSql  = "INSERT INTO orders (user_id, total_amount, order_status, "
                         + "payment_method, shipping_address) VALUES (?, ?, ?, ?, ?)";
        String itemSql   = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                         + "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // begin transaction

            // 1. Insert order header
            int newOrderId = -1;
            try (PreparedStatement orderStmt =
                         conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {

                orderStmt.setInt(1, order.getUserId());
                orderStmt.setDouble(2, order.getTotalAmount());
                orderStmt.setString(3, order.getOrderStatus());
                orderStmt.setString(4, order.getPaymentMethod());
                orderStmt.setString(5, order.getShippingAddress());

                orderStmt.executeUpdate();

                try (ResultSet keys = orderStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newOrderId = keys.getInt(1);
                    }
                }
            }

            if (newOrderId == -1) {
                conn.rollback();
                return -1;
            }

            // 2. Insert all line items
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    itemStmt.setInt(1, newOrderId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getUnitPrice());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit(); // everything succeeded
            return newOrderId;

        } catch (SQLException e) {
            System.err.println("[OrderDAO] placeOrder() failed: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore rollback error */ }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignore */ }
            }
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves a single order by its primary key, including all line items.
     *
     * @param orderId the order_id to look up
     * @return the {@link Order} with its items populated, or {@code null} if not found
     */
    public Order findById(int orderId) {
        String sql = "SELECT order_id, user_id, total_amount, order_status, "
                   + "payment_method, shipping_address, created_at "
                   + "FROM orders WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] findById() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all orders placed by a specific customer, newest first.
     * Line items are loaded for each order.
     *
     * @param userId the customer's user_id
     * @return list of the customer's {@link Order} objects (may be empty)
     */
    public List<Order> findByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, total_amount, order_status, "
                   + "payment_method, shipping_address, created_at "
                   + "FROM orders WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(order.getOrderId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] findByUserId() failed: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Returns all orders in the system (admin view), newest first.
     * Line items are NOT eagerly loaded to keep the admin list query fast;
     * call {@link #findById(int)} on demand to get items for a specific order.
     *
     * @return list of all {@link Order} objects (header only)
     */
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, total_amount, order_status, "
                   + "payment_method, shipping_address, created_at "
                   + "FROM orders ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapOrderRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] findAll() failed: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Returns all orders matching a specific status (e.g. "PENDING", "SHIPPED").
     *
     * @param status the order_status value to filter by (case-insensitive)
     * @return matching {@link Order} list (header only, no items loaded)
     */
    public List<Order> findByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, total_amount, order_status, "
                   + "payment_method, shipping_address, created_at "
                   + "FROM orders WHERE UPPER(order_status) = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.toUpperCase());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] findByStatus() failed: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Returns the total count of all orders in the system.
     * Used for the Admin KPI dashboard card.
     *
     * @return total order count, or 0 on error
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM orders";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] countAll() failed: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calculates the sum of total_amount across all DELIVERED and CONFIRMED orders.
     * Used for the Admin "Total Revenue" KPI card.
     *
     * @return total revenue as a double, or 0.0 on error
     */
    public double calculateTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders "
                   + "WHERE order_status IN ('DELIVERED', 'CONFIRMED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] calculateTotalRevenue() failed: " + e.getMessage());
        }
        return 0.0;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates the status of an existing order.
     * Valid transitions are enforced at the service/UI layer, not here.
     *
     * @param orderId   the order to update
     * @param newStatus the new order_status string (must match the DB ENUM values)
     * @return {@code true} if the update was applied
     */
    public boolean updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.toUpperCase());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[OrderDAO] updateStatus() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Loads all order_items rows for a given order_id, joined with the product name.
     *
     * @param orderId the parent order's ID
     * @return list of {@link OrderItem} objects for that order
     */
    private List<OrderItem> findItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, "
                   + "p.product_name, oi.quantity, oi.unit_price "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "WHERE oi.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderItem(
                            rs.getInt("order_item_id"),
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderDAO] findItemsByOrderId() failed: " + e.getMessage());
        }
        return items;
    }

    /**
     * Maps the current row of a {@link ResultSet} to an {@link Order} header object.
     * Does NOT load items — call {@link #findItemsByOrderId(int)} separately.
     * Does NOT call rs.next() — the caller must position the cursor.
     */
    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        // Generate a display order number like "ORD-2026-001" from the PK
        order.setOrderNumber(String.format("ORD-%04d", rs.getInt("order_id")));
        order.setUserId(rs.getInt("user_id"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setOrderStatus(rs.getString("order_status"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setShippingAddress(rs.getString("shipping_address") != null
                ? rs.getString("shipping_address") : "");
        order.setOrderDate(rs.getString("created_at") != null
                ? rs.getString("created_at").substring(0, 10) : "");
        return order;
    }
}
