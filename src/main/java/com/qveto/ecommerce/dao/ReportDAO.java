package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for generating analytical reports and core KPI metrics
 * as specified in the Qveto Internship Project 1 evaluation requirements.
 *
 * Provides aggregated analytics on:
 * - Sales Revenue (excluding cancelled orders)
 * - Order Status Distribution (Pending, Confirmed, Shipped, Delivered, Cancelled)
 * - Best-Selling Products by units sold and revenue
 * - Low-Stock Exposure & Inventory alerts
 * - Category-wise Sales Revenue
 *
 * Member 1 — DB/JDBC layer.
 */
public class ReportDAO {

    /**
     * Data transfer object representing sales metrics for a single product.
     */
    public static class ProductSalesStat {
        private final int productId;
        private final String productName;
        private final String categoryName;
        private final int totalUnitsSold;
        private final BigDecimal totalRevenue;

        public ProductSalesStat(int productId, String productName, String categoryName,
                                int totalUnitsSold, BigDecimal totalRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.totalUnitsSold = totalUnitsSold;
            this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryName() { return categoryName; }
        public int getTotalUnitsSold() { return totalUnitsSold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public double getTotalRevenueAsDouble() { return totalRevenue.doubleValue(); }
    }

    // ---------------------------------------------------------------
    // Revenue & KPI Summary
    // ---------------------------------------------------------------

    /**
     * Calculates the gross revenue from all eligible non-cancelled orders.
     *
     * @return total revenue as {@link BigDecimal}
     */
    public BigDecimal calculateTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) AS total_revenue "
                   + "FROM orders "
                   + "WHERE order_status != 'CANCELLED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal rev = rs.getBigDecimal("total_revenue");
                return rev != null ? rev : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] calculateTotalRevenue() failed: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Counts the total number of customer orders across all statuses.
     *
     * @return order count
     */
    public int countTotalOrders() {
        String sql = "SELECT COUNT(*) FROM orders";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] countTotalOrders() failed: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts the total number of registered customer accounts (excluding admins).
     *
     * @return customer count
     */
    public int countTotalCustomers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] countTotalCustomers() failed: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // Order Status Distribution
    // ---------------------------------------------------------------

    /**
     * Returns the distribution of orders grouped by their lifecycle status:
     * PENDING, CONFIRMED, SHIPPED, DELIVERED, and CANCELLED.
     *
     * @return ordered map of status names to counts
     */
    public Map<String, Integer> getOrderStatusDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        // Initialize standard statuses with 0
        distribution.put("PENDING", 0);
        distribution.put("CONFIRMED", 0);
        distribution.put("SHIPPED", 0);
        distribution.put("DELIVERED", 0);
        distribution.put("CANCELLED", 0);

        String sql = "SELECT order_status, COUNT(*) AS status_count "
                   + "FROM orders "
                   + "GROUP BY order_status";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                distribution.put(rs.getString("order_status").toUpperCase(), rs.getInt("status_count"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getOrderStatusDistribution() failed: " + e.getMessage());
        }
        return distribution;
    }

    // ---------------------------------------------------------------
    // Inventory Alerts & Exposure
    // ---------------------------------------------------------------

    /**
     * Returns the number of products currently at or below their configured low-stock threshold.
     *
     * @return count of low-stock items
     */
    public int getLowStockExposureCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE stock_quantity <= low_stock_threshold";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getLowStockExposureCount() failed: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // Best-Selling Products
    // ---------------------------------------------------------------

    /**
     * Retrieves the top best-selling products ranked by units sold,
     * including their total revenue contribution.
     *
     * @param limit maximum number of products to return
     * @return list of {@link ProductSalesStat}
     */
    public List<ProductSalesStat> getTopSellingProducts(int limit) {
        List<ProductSalesStat> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, COALESCE(c.category_name, 'General') AS category_name, "
                   + "COALESCE(SUM(oi.quantity), 0) AS total_units_sold, "
                   + "COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS total_revenue "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "JOIN orders o ON oi.order_id = o.order_id "
                   + "WHERE o.order_status != 'CANCELLED' "
                   + "GROUP BY p.product_id, p.product_name, c.category_name "
                   + "ORDER BY total_units_sold DESC "
                   + "LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit > 0 ? limit : 5);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductSalesStat(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("total_units_sold"),
                        rs.getBigDecimal("total_revenue")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopSellingProducts() failed: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Category Revenue Breakdown
    // ---------------------------------------------------------------

    /**
     * Groups total sales revenue by product category.
     *
     * @return map of category names to total revenue
     */
    public Map<String, BigDecimal> getRevenueByCategory() {
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        String sql = "SELECT COALESCE(c.category_name, 'Uncategorized') AS cat_name, "
                   + "COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS cat_revenue "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "JOIN orders o ON oi.order_id = o.order_id "
                   + "WHERE o.order_status != 'CANCELLED' "
                   + "GROUP BY c.category_name "
                   + "ORDER BY cat_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                breakdown.put(rs.getString("cat_name"), rs.getBigDecimal("cat_revenue"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getRevenueByCategory() failed: " + e.getMessage());
        }
        return breakdown;
    }
}
