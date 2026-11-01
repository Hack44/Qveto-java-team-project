package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'order_items' table.
 *
 * Provides dedicated transactional insertion, query operations by order and product,
 * and line-item level aggregations (units sold, line revenues).
 *
 * All database operations use PreparedStatements to prevent SQL injection.
 *
 * Member 1 — DB/JDBC layer.
 */
public class OrderItemDAO {

    // ---------------------------------------------------------------
    // Create / Batch Insert
    // ---------------------------------------------------------------

    /**
     * Inserts a single order item into the database.
     *
     * @param item the {@link OrderItem} to insert
     * @return the auto-generated order_item_id, or -1 on failure
     */
    public int addOrderItem(OrderItem item) {
        if (item == null) {
            return -1;
        }

        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getUnitPriceAsBigDecimal());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        item.setOrderItemId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] addOrderItem() failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Inserts a list of line items in a batch within an existing database transaction.
     *
     * @param conn    active transactional database connection
     * @param orderId the parent order ID
     * @param items   list of {@link OrderItem} objects
     * @return true if batch execution completed successfully
     * @throws SQLException if a database access error occurs
     */
    public boolean insertBatch(Connection conn, int orderId, List<OrderItem> items) throws SQLException {
        if (conn == null || items == null || items.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPriceAsBigDecimal());
                stmt.addBatch();
            }
            stmt.executeBatch();
            return true;
        }
    }

    /**
     * Standalone batch insertion for line items under an order ID.
     * Creates its own connection and commits atomically.
     *
     * @param orderId parent order ID
     * @param items   list of line items
     * @return true if all items inserted successfully
     */
    public boolean insertBatch(int orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            boolean success = insertBatch(conn, orderId, items);
            if (success) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] standalone insertBatch() failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    /* ignore rollback error */
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    /* ignore */
                }
            }
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves all order line items for a specific order ID, joined with products
     * and categories to retrieve product name and category details.
     *
     * @param orderId parent order ID
     * @return list of {@link OrderItem} records; empty list if none
     */
    public List<OrderItem> findByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, "
                   + "p.product_name, COALESCE(c.category_name, '') AS category_name, "
                   + "oi.quantity, oi.unit_price "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE oi.order_id = ? "
                   + "ORDER BY oi.order_item_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] findByOrderId() failed: " + e.getMessage());
        }
        return items;
    }

    /**
     * Retrieves a single order item by its primary key.
     *
     * @param orderItemId the order_item_id
     * @return the {@link OrderItem}, or null if not found
     */
    public OrderItem findById(int orderItemId) {
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, "
                   + "p.product_name, COALESCE(c.category_name, '') AS category_name, "
                   + "oi.quantity, oi.unit_price "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE oi.order_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderItemId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] findById() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all order item instances associated with a specific product ID.
     * Useful for product audit history and sales tracking.
     *
     * @param productId product ID
     * @return list of matching {@link OrderItem} records
     */
    public List<OrderItem> findByProductId(int productId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, "
                   + "p.product_name, COALESCE(c.category_name, '') AS category_name, "
                   + "oi.quantity, oi.unit_price "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE oi.product_id = ? "
                   + "ORDER BY oi.order_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] findByProductId() failed: " + e.getMessage());
        }
        return items;
    }

    // ---------------------------------------------------------------
    // Aggregations & Metrics
    // ---------------------------------------------------------------

    /**
     * Calculates the total units sold for a product across all eligible orders (excluding cancelled).
     *
     * @param productId the product ID
     * @return total units sold
     */
    public int countTotalUnitsSold(int productId) {
        String sql = "SELECT COALESCE(SUM(oi.quantity), 0) AS total_units "
                   + "FROM order_items oi "
                   + "JOIN orders o ON oi.order_id = o.order_id "
                   + "WHERE oi.product_id = ? AND o.order_status != 'CANCELLED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_units");
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] countTotalUnitsSold() failed: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calculates gross sales revenue generated by a specific product across valid orders.
     *
     * @param productId product ID
     * @return total product revenue as BigDecimal
     */
    public BigDecimal calculateProductRevenue(int productId) {
        String sql = "SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS revenue "
                   + "FROM order_items oi "
                   + "JOIN orders o ON oi.order_id = o.order_id "
                   + "WHERE oi.product_id = ? AND o.order_status != 'CANCELLED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal rev = rs.getBigDecimal("revenue");
                    return rev != null ? rev : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] calculateProductRevenue() failed: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns total distinct line items in a given order.
     *
     * @param orderId order ID
     * @return count of items
     */
    public int countItemsInOrder(int orderId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] countItemsInOrder() failed: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns total unit quantity summed across all line items in an order.
     *
     * @param orderId order ID
     * @return total quantity of units
     */
    public int sumQuantityInOrder(int orderId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM order_items WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] sumQuantityInOrder() failed: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    /**
     * Deletes all line items associated with an order ID.
     *
     * @param orderId order ID
     * @return true if deletion succeeded
     */
    public boolean deleteByOrderId(int orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] deleteByOrderId() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a specific line item by primary key.
     *
     * @param orderItemId order item ID
     * @return true if deleted
     */
    public boolean deleteById(int orderItemId) {
        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[OrderItemDAO] deleteById() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Private Helpers
    // ---------------------------------------------------------------

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem(
            rs.getInt("order_item_id"),
            rs.getInt("order_id"),
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getInt("quantity"),
            rs.getBigDecimal("unit_price")
        );
        item.setCategoryName(rs.getString("category_name"));
        return item;
    }
}
