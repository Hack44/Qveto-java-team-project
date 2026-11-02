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
 * - Database View integrations (view_sales_summary, view_low_stock_alerts, view_customer_order_history)
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

    /**
     * Data transfer object for executive KPI sales summary.
     */
    public static class SalesExecutiveSummary {
        private final int totalOrders;
        private final BigDecimal grossRevenue;
        private final BigDecimal averageOrderValue;
        private final int deliveredOrders;
        private final int confirmedOrders;
        private final int shippedOrders;
        private final int pendingOrders;
        private final int cancelledOrders;

        public SalesExecutiveSummary(int totalOrders, BigDecimal grossRevenue, BigDecimal averageOrderValue,
                                     int deliveredOrders, int confirmedOrders, int shippedOrders,
                                     int pendingOrders, int cancelledOrders) {
            this.totalOrders = totalOrders;
            this.grossRevenue = grossRevenue != null ? grossRevenue : BigDecimal.ZERO;
            this.averageOrderValue = averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO;
            this.deliveredOrders = deliveredOrders;
            this.confirmedOrders = confirmedOrders;
            this.shippedOrders = shippedOrders;
            this.pendingOrders = pendingOrders;
            this.cancelledOrders = cancelledOrders;
        }

        public int getTotalOrders() { return totalOrders; }
        public BigDecimal getGrossRevenue() { return grossRevenue; }
        public BigDecimal getAverageOrderValue() { return averageOrderValue; }
        public int getDeliveredOrders() { return deliveredOrders; }
        public int getConfirmedOrders() { return confirmedOrders; }
        public int getShippedOrders() { return shippedOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public int getCancelledOrders() { return cancelledOrders; }
    }

    /**
     * Data transfer object for inventory risk and reorder alerts.
     */
    public static class LowStockAlert {
        private final int productId;
        private final String productName;
        private final String categoryName;
        private final int stockQuantity;
        private final int threshold;
        private final String status;
        private final int unitsBelowThreshold;

        public LowStockAlert(int productId, String productName, String categoryName,
                             int stockQuantity, int threshold, String status, int unitsBelowThreshold) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.stockQuantity = stockQuantity;
            this.threshold = threshold;
            this.status = status;
            this.unitsBelowThreshold = unitsBelowThreshold;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryName() { return categoryName; }
        public int getStockQuantity() { return stockQuantity; }
        public int getThreshold() { return threshold; }
        public String getStatus() { return status; }
        public int getUnitsBelowThreshold() { return unitsBelowThreshold; }
    }

    /**
     * Data transfer object for customer lifetime value analysis.
     */
    public static class CustomerSpendStat {
        private final int userId;
        private final String fullName;
        private final String email;
        private final int lifetimeOrders;
        private final BigDecimal lifetimeSpent;
        private final String lastOrderTimestamp;

        public CustomerSpendStat(int userId, String fullName, String email,
                                 int lifetimeOrders, BigDecimal lifetimeSpent, String lastOrderTimestamp) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.lifetimeOrders = lifetimeOrders;
            this.lifetimeSpent = lifetimeSpent != null ? lifetimeSpent : BigDecimal.ZERO;
            this.lastOrderTimestamp = lastOrderTimestamp != null ? lastOrderTimestamp : "N/A";
        }

        public int getUserId() { return userId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public int getLifetimeOrders() { return lifetimeOrders; }
        public BigDecimal getLifetimeSpent() { return lifetimeSpent; }
        public String getLastOrderTimestamp() { return lastOrderTimestamp; }
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

    /**
     * Retrieves an executive KPI sales summary from view_sales_summary.
     * Falls back to computed query if migration view is not applied yet.
     */
    public SalesExecutiveSummary getSalesExecutiveSummary() {
        String viewSql = "SELECT * FROM view_sales_summary";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(viewSql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new SalesExecutiveSummary(
                    rs.getInt("total_orders"),
                    rs.getBigDecimal("gross_revenue"),
                    rs.getBigDecimal("average_order_value"),
                    rs.getInt("delivered_orders"),
                    rs.getInt("confirmed_orders"),
                    rs.getInt("shipped_orders"),
                    rs.getInt("pending_orders"),
                    rs.getInt("cancelled_orders")
                );
            }
        } catch (SQLException e) {
            // View might not be created yet, fallback to direct query
            return getFallbackSalesSummary();
        }
        return getFallbackSalesSummary();
    }

    private SalesExecutiveSummary getFallbackSalesSummary() {
        Map<String, Integer> dist = getOrderStatusDistribution();
        BigDecimal rev = calculateTotalRevenue();
        int total = countTotalOrders();
        BigDecimal aov = total > 0 ? rev.divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return new SalesExecutiveSummary(
            total,
            rev,
            aov,
            dist.getOrDefault("DELIVERED", 0),
            dist.getOrDefault("CONFIRMED", 0),
            dist.getOrDefault("SHIPPED", 0),
            dist.getOrDefault("PENDING", 0),
            dist.getOrDefault("CANCELLED", 0)
        );
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

    /**
     * Retrieves detailed low stock alert list utilizing view_low_stock_alerts.
     *
     * @return list of {@link LowStockAlert}
     */
    public List<LowStockAlert> getDetailedLowStockAlerts() {
        List<LowStockAlert> list = new ArrayList<>();
        String sql = "SELECT product_id, product_name, category_name, stock_quantity, "
                   + "low_stock_threshold, stock_status, units_below_threshold "
                   + "FROM view_low_stock_alerts";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new LowStockAlert(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getInt("stock_quantity"),
                    rs.getInt("low_stock_threshold"),
                    rs.getString("stock_status"),
                    rs.getInt("units_below_threshold")
                ));
            }
            return list;
        } catch (SQLException e) {
            // Fallback to products table direct query
            return getFallbackLowStockAlerts();
        }
    }

    private List<LowStockAlert> getFallbackLowStockAlerts() {
        List<LowStockAlert> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, COALESCE(c.category_name, 'General') AS cat_name, "
                   + "p.stock_quantity, p.low_stock_threshold "
                   + "FROM products p "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE p.stock_quantity <= p.low_stock_threshold "
                   + "ORDER BY p.stock_quantity ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int stock = rs.getInt("stock_quantity");
                int thresh = rs.getInt("low_stock_threshold");
                String status = stock == 0 ? "OUT_OF_STOCK" : "LOW_STOCK";
                int diff = Math.max(0, thresh - stock);

                list.add(new LowStockAlert(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("cat_name"),
                    stock,
                    thresh,
                    status,
                    diff
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getFallbackLowStockAlerts() failed: " + e.getMessage());
        }
        return list;
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

    // ---------------------------------------------------------------
    // Customer Spend Leaderboard
    // ---------------------------------------------------------------

    /**
     * Retrieves the top customer accounts by lifetime spend using view_customer_order_history.
     *
     * @param limit maximum customers to return
     * @return list of {@link CustomerSpendStat}
     */
    public List<CustomerSpendStat> getCustomerSpendLeaderboard(int limit) {
        List<CustomerSpendStat> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, lifetime_orders, lifetime_spent, last_order_timestamp "
                   + "FROM view_customer_order_history "
                   + "ORDER BY lifetime_spent DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit > 0 ? limit : 5);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new CustomerSpendStat(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getInt("lifetime_orders"),
                        rs.getBigDecimal("lifetime_spent"),
                        rs.getString("last_order_timestamp")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            // Fallback to direct query
            return getFallbackCustomerSpend(limit);
        }
    }

    private List<CustomerSpendStat> getFallbackCustomerSpend(int limit) {
        List<CustomerSpendStat> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, u.email, "
                   + "COUNT(o.order_id) AS orders_count, "
                   + "COALESCE(SUM(CASE WHEN o.order_status != 'CANCELLED' THEN o.total_amount ELSE 0.00 END), 0.00) AS spent, "
                   + "MAX(o.created_at) AS last_order "
                   + "FROM users u "
                   + "LEFT JOIN orders o ON u.user_id = o.user_id "
                   + "WHERE u.role = 'CUSTOMER' "
                   + "GROUP BY u.user_id, u.full_name, u.email "
                   + "ORDER BY spent DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit > 0 ? limit : 5);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new CustomerSpendStat(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getInt("orders_count"),
                        rs.getBigDecimal("spent"),
                        rs.getString("last_order")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getFallbackCustomerSpend() failed: " + e.getMessage());
        }
        return list;
    }
}
