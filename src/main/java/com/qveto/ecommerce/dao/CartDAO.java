package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.CartItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'cart_items' table.
 *
 * Provides persistence for customer shopping carts, bridging items
 * stored in MySQL to the GUI cart panel.
 *
 * All database operations use PreparedStatements to prevent SQL injection.
 *
 * Member 1 — DB/JDBC layer.
 */
public class CartDAO {

    // ---------------------------------------------------------------
    // Add / Upsert
    // ---------------------------------------------------------------

    /**
     * Adds an item to the user's cart. If the product already exists in the
     * user's cart, its quantity is incremented by {@code quantity}.
     *
     * @param userId    the customer's user_id
     * @param productId the product_id to add
     * @param quantity  the quantity to add (must be > 0)
     * @return {@code true} if inserted or updated successfully
     */
    public boolean addItem(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) "
                   + "VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CartDAO] addItem() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves all items currently in the specified user's shopping cart,
     * joined with product catalog details (name, category, price, current stock).
     *
     * @param userId the customer's user_id
     * @return list of {@link CartItem} records in the cart; empty list if none
     */
    public List<CartItem> getCartByUserId(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.cart_item_id, ci.user_id, ci.product_id, ci.quantity, "
                   + "p.product_name, c.category_name, p.price, p.stock_quantity "
                   + "FROM cart_items ci "
                   + "JOIN products p ON ci.product_id = p.product_id "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE ci.user_id = ? "
                   + "ORDER BY ci.cart_item_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setProductName(rs.getString("product_name"));
                    item.setCategoryName(rs.getString("category_name") != null ? rs.getString("category_name") : "");
                    item.setUnitPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO);
                    item.setStockAvailable(rs.getInt("stock_quantity"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CartDAO] getCartByUserId() failed: " + e.getMessage());
        }
        return items;
    }

    /**
     * Calculates the total cart monetary value for a user.
     *
     * @param userId the customer's user_id
     * @return total cart value as {@link BigDecimal}
     */
    public BigDecimal getCartTotal(int userId) {
        String sql = "SELECT COALESCE(SUM(ci.quantity * p.price), 0.00) AS total "
                   + "FROM cart_items ci "
                   + "JOIN products p ON ci.product_id = p.product_id "
                   + "WHERE ci.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CartDAO] getCartTotal() failed: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns the total item count (sum of quantities) in a user's cart.
     * Used for updating the shopping cart badge in the UI header.
     *
     * @param userId the customer's user_id
     * @return total count of units in cart
     */
    public int getCartItemCount(int userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CartDAO] getCartItemCount() failed: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates the exact quantity of a specific product in the user's cart.
     * If {@code newQuantity} <= 0, the item is removed from the cart.
     *
     * @param userId      the customer's user_id
     * @param productId   the product_id to update
     * @param newQuantity the updated quantity
     * @return {@code true} if updated or removed successfully
     */
    public boolean updateQuantity(int userId, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(userId, productId);
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CartDAO] updateQuantity() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    /**
     * Removes a single product line from the user's cart.
     *
     * @param userId    the customer's user_id
     * @param productId the product_id to remove
     * @return {@code true} if deleted successfully
     */
    public boolean removeItem(int userId, int productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CartDAO] removeItem() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Clears all items from the user's shopping cart.
     * Called automatically upon successful checkout / order placement.
     *
     * @param userId the customer's user_id
     * @return {@code true} if the cart was cleared
     */
    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[CartDAO] clearCart() failed: " + e.getMessage());
        }
        return false;
    }
}
