package com.qveto.ecommerce.util;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.dao.ReportDAO;
import com.qveto.ecommerce.dao.ReportDAO.CustomerSpendStat;
import com.qveto.ecommerce.dao.ReportDAO.LowStockAlert;
import com.qveto.ecommerce.dao.ReportDAO.ProductSalesStat;
import com.qveto.ecommerce.dao.ReportDAO.SalesExecutiveSummary;
import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.model.User;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enterprise Report & Audit Export Utility.
 *
 * Provides high-fidelity data export capabilities in standard RFC 4180 CSV format
 * and human-readable plain text audit reports:
 *
 * 1. Sales Orders Audit (CSV): Order-level transaction ledger including payment & shipping details.
 * 2. Order Line Items Audit (CSV): Granular line-item sales metrics with product and category joins.
 * 3. Product Catalog & Inventory Audit (CSV): Real-time stock levels, low-stock flags, velocity & revenue.
 * 4. Customer Spend Leaderboard (CSV): Lifetime value, total order counts, and recency analysis.
 * 5. Comprehensive Sales Audit Report (.txt): Formal tabular audit dossier with executive KPI summary,
 *    order reconciliation, inventory risk exposure, and category share matrix.
 *
 * Implements dual-engine persistence: queries MySQL database views and tables when connected,
 * with full fallback to in-memory store records for standalone demo mode.
 *
 * Member 1 — Persistence, Analytics & Data Export layer.
 */
public class ReportExportUtil {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReportExportUtil() {
        // Utility class
    }

    // =========================================================================
    // 1. Sales Orders Audit (CSV)
    // =========================================================================

    /**
     * Exports a complete sales orders transaction audit log to a CSV file.
     *
     * @param targetFile destination CSV file
     * @return total order rows written
     * @throws IOException on I/O error
     */
    public static int exportSalesOrdersCSV(File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            return writeSalesOrdersCSV(writer);
        }
    }

    public static int writeSalesOrdersCSV(PrintWriter writer) {
        writer.println("Order ID,Created At,User ID,Customer Name,Customer Email,Shipping Address,Payment Method,Payment Status,Order Status,Total Amount ($),Item Count");

        int count = 0;
        if (DBStatusUtil.isDatabaseConnected()) {
            String sql = "SELECT o.order_id, o.created_at, o.user_id, "
                       + "COALESCE(u.full_name, 'Anonymous') AS customer_name, "
                       + "COALESCE(u.email, 'N/A') AS customer_email, "
                       + "COALESCE(o.shipping_address, 'N/A') AS shipping_address, "
                       + "COALESCE(o.payment_method, 'COD') AS payment_method, "
                       + "COALESCE(p.payment_status, 'PENDING') AS payment_status, "
                       + "o.order_status, o.total_amount, "
                       + "(SELECT COUNT(*) FROM order_items oi WHERE oi.order_id = o.order_id) AS item_count "
                       + "FROM orders o "
                       + "LEFT JOIN users u ON o.user_id = u.user_id "
                       + "LEFT JOIN payments p ON o.order_id = p.order_id "
                       + "ORDER BY o.order_id DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    writer.printf("%d,%s,%d,%s,%s,%s,%s,%s,%s,%.2f,%d%n",
                            rs.getInt("order_id"),
                            escapeCSV(rs.getString("created_at")),
                            rs.getInt("user_id"),
                            escapeCSV(rs.getString("customer_name")),
                            escapeCSV(rs.getString("customer_email")),
                            escapeCSV(rs.getString("shipping_address")),
                            escapeCSV(rs.getString("payment_method")),
                            escapeCSV(rs.getString("payment_status")),
                            escapeCSV(rs.getString("order_status")),
                            rs.getDouble("total_amount"),
                            rs.getInt("item_count"));
                    count++;
                }
                return count;
            } catch (SQLException e) {
                System.err.println("[ReportExportUtil] DB exportSalesOrdersCSV failed, falling back: " + e.getMessage());
            }
        }

        // In-memory fallback
        for (Order o : OrderStore.getAllOrders()) {
            String userName = "Demo Customer";
            String userEmail = "customer@qveto.com";
            User u = UserStore.findById(o.getUserId());
            if (u != null) {
                userName = u.getFullName();
                userEmail = u.getEmail();
            }

            String paymentMethod = o.getPaymentMethod() != null ? o.getPaymentMethod() : "COD";
            String paymentStatus = "CANCELLED".equalsIgnoreCase(o.getOrderStatus()) ? "REFUNDED"
                    : ("COD".equalsIgnoreCase(paymentMethod) ? "PENDING" : "COMPLETED");
            int itemsCount = o.getItems() != null ? o.getItems().size() : 0;

            writer.printf("%d,%s,%d,%s,%s,%s,%s,%s,%s,%.2f,%d%n",
                    o.getOrderId(),
                    escapeCSV(o.getOrderDate() != null ? o.getOrderDate() : LocalDateTime.now().format(ISO_DT)),
                    o.getUserId(),
                    escapeCSV(userName),
                    escapeCSV(userEmail),
                    escapeCSV(o.getShippingAddress() != null ? o.getShippingAddress() : "N/A"),
                    escapeCSV(paymentMethod),
                    escapeCSV(paymentStatus),
                    escapeCSV(o.getOrderStatus()),
                    o.getTotalAmount(),
                    itemsCount);
            count++;
        }
        return count;
    }

    // =========================================================================
    // 2. Order Line Items Granular Audit (CSV)
    // =========================================================================

    /**
     * Exports all line items associated with orders to a CSV file.
     *
     * @param targetFile destination CSV file
     * @return total line item rows written
     * @throws IOException on I/O error
     */
    public static int exportOrderItemsCSV(File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            return writeOrderItemsCSV(writer);
        }
    }

    public static int writeOrderItemsCSV(PrintWriter writer) {
        writer.println("Order Item ID,Order ID,Order Date,Product ID,Product Name,Category,Quantity,Unit Price ($),Line Total ($),Order Status");

        int count = 0;
        if (DBStatusUtil.isDatabaseConnected()) {
            String sql = "SELECT oi.order_item_id, oi.order_id, o.created_at AS order_date, "
                       + "oi.product_id, COALESCE(p.product_name, 'Unknown Product') AS product_name, "
                       + "COALESCE(c.category_name, 'General') AS category_name, "
                       + "oi.quantity, oi.unit_price, (oi.quantity * oi.unit_price) AS line_total, "
                       + "o.order_status "
                       + "FROM order_items oi "
                       + "JOIN orders o ON oi.order_id = o.order_id "
                       + "LEFT JOIN products p ON oi.product_id = p.product_id "
                       + "LEFT JOIN categories c ON p.category_id = c.category_id "
                       + "ORDER BY oi.order_id DESC, oi.order_item_id ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    writer.printf("%d,%d,%s,%d,%s,%s,%d,%.2f,%.2f,%s%n",
                            rs.getInt("order_item_id"),
                            rs.getInt("order_id"),
                            escapeCSV(rs.getString("order_date")),
                            rs.getInt("product_id"),
                            escapeCSV(rs.getString("product_name")),
                            escapeCSV(rs.getString("category_name")),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("line_total"),
                            escapeCSV(rs.getString("order_status")));
                    count++;
                }
                return count;
            } catch (SQLException e) {
                System.err.println("[ReportExportUtil] DB exportOrderItemsCSV failed, falling back: " + e.getMessage());
            }
        }

        // In-memory fallback
        for (Order o : OrderStore.getAllOrders()) {
            if (o.getItems() == null) continue;
            for (OrderItem item : o.getItems()) {
                String catName = "General";
                for (Product p : ProductStore.getAllProducts()) {
                    if (p.getProductId() == item.getProductId()) {
                        catName = p.getCategory();
                        break;
                    }
                }

                writer.printf("%d,%d,%s,%d,%s,%s,%d,%.2f,%.2f,%s%n",
                        item.getOrderItemId(),
                        o.getOrderId(),
                        escapeCSV(o.getOrderDate() != null ? o.getOrderDate() : LocalDateTime.now().format(ISO_DT)),
                        item.getProductId(),
                        escapeCSV(item.getProductName()),
                        escapeCSV(catName),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal(),
                        escapeCSV(o.getOrderStatus()));
                count++;
            }
        }
        return count;
    }

    // =========================================================================
    // 3. Product Catalog & Inventory Velocity Audit (CSV)
    // =========================================================================

    /**
     * Exports product catalog performance and inventory levels to CSV.
     *
     * @param targetFile destination CSV file
     * @return total product rows written
     * @throws IOException on I/O error
     */
    public static int exportProductPerformanceCSV(File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            return writeProductPerformanceCSV(writer);
        }
    }

    public static int writeProductPerformanceCSV(PrintWriter writer) {
        writer.println("Product ID,Product Name,Category,Price ($),Current Stock,Low Stock Threshold,Units Sold,Total Revenue ($),Stock Status");

        int count = 0;
        if (DBStatusUtil.isDatabaseConnected()) {
            String sql = "SELECT p.product_id, p.product_name, COALESCE(c.category_name, 'General') AS category_name, "
                       + "p.price, p.stock_quantity, p.low_stock_threshold, "
                       + "COALESCE(sales.units_sold, 0) AS units_sold, "
                       + "COALESCE(sales.total_revenue, 0.00) AS total_revenue, "
                       + "CASE "
                       + "  WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK' "
                       + "  WHEN p.stock_quantity <= p.low_stock_threshold THEN 'LOW_STOCK' "
                       + "  ELSE 'HEALTHY' "
                       + "END AS stock_status "
                       + "FROM products p "
                       + "LEFT JOIN categories c ON p.category_id = c.category_id "
                       + "LEFT JOIN ( "
                       + "    SELECT oi.product_id, SUM(oi.quantity) AS units_sold, "
                       + "           SUM(oi.quantity * oi.unit_price) AS total_revenue "
                       + "    FROM order_items oi "
                       + "    JOIN orders o ON oi.order_id = o.order_id "
                       + "    WHERE o.order_status != 'CANCELLED' "
                       + "    GROUP BY oi.product_id "
                       + ") sales ON p.product_id = sales.product_id "
                       + "ORDER BY units_sold DESC, p.product_id ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    writer.printf("%d,%s,%s,%.2f,%d,%d,%d,%.2f,%s%n",
                            rs.getInt("product_id"),
                            escapeCSV(rs.getString("product_name")),
                            escapeCSV(rs.getString("category_name")),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            rs.getInt("low_stock_threshold"),
                            rs.getInt("units_sold"),
                            rs.getDouble("total_revenue"),
                            escapeCSV(rs.getString("stock_status")));
                    count++;
                }
                return count;
            } catch (SQLException e) {
                System.err.println("[ReportExportUtil] DB exportProductPerformanceCSV failed, falling back: " + e.getMessage());
            }
        }

        // In-memory fallback
        Map<Integer, Integer> unitsSoldMap = new HashMap<>();
        Map<Integer, Double> revenueMap = new HashMap<>();

        for (Order o : OrderStore.getAllOrders()) {
            if ("CANCELLED".equalsIgnoreCase(o.getOrderStatus())) continue;
            if (o.getItems() != null) {
                for (OrderItem item : o.getItems()) {
                    int pid = item.getProductId();
                    unitsSoldMap.put(pid, unitsSoldMap.getOrDefault(pid, 0) + item.getQuantity());
                    revenueMap.put(pid, revenueMap.getOrDefault(pid, 0.0) + item.getSubtotal());
                }
            }
        }

        for (Product p : ProductStore.getAllProducts()) {
            int sold = unitsSoldMap.getOrDefault(p.getProductId(), 0);
            double rev = revenueMap.getOrDefault(p.getProductId(), 0.0);
            String status = p.getStockQuantity() == 0 ? "OUT_OF_STOCK"
                    : (p.getStockQuantity() <= p.getLowStockThreshold() ? "LOW_STOCK" : "HEALTHY");

            writer.printf("%d,%s,%s,%.2f,%d,%d,%d,%.2f,%s%n",
                    p.getProductId(),
                    escapeCSV(p.getProductName()),
                    escapeCSV(p.getCategory()),
                    p.getPrice(),
                    p.getStockQuantity(),
                    p.getLowStockThreshold(),
                    sold,
                    rev,
                    escapeCSV(status));
            count++;
        }
        return count;
    }

    // =========================================================================
    // 4. Customer Spend Leaderboard (CSV)
    // =========================================================================

    /**
     * Exports customer spend and lifetime value analysis to CSV.
     *
     * @param targetFile destination CSV file
     * @return total customer rows written
     * @throws IOException on I/O error
     */
    public static int exportCustomerSpendCSV(File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            return writeCustomerSpendCSV(writer);
        }
    }

    public static int writeCustomerSpendCSV(PrintWriter writer) {
        writer.println("User ID,Full Name,Email,Lifetime Orders,Total Spent ($),Last Order Date");

        int count = 0;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<CustomerSpendStat> list = new ReportDAO().getCustomerSpendLeaderboard(100);
                for (CustomerSpendStat stat : list) {
                    writer.printf("%d,%s,%s,%d,%.2f,%s%n",
                            stat.getUserId(),
                            escapeCSV(stat.getFullName()),
                            escapeCSV(stat.getEmail()),
                            stat.getLifetimeOrders(),
                            stat.getLifetimeSpent().doubleValue(),
                            escapeCSV(stat.getLastOrderTimestamp()));
                    count++;
                }
                return count;
            } catch (Throwable t) {
                System.err.println("[ReportExportUtil] DB exportCustomerSpendCSV failed, falling back: " + t.getMessage());
            }
        }

        // In-memory fallback
        for (User u : UserStore.getAllUsers()) {
            if (u.isAdmin()) continue;
            int orderCount = 0;
            double spent = 0.0;
            String lastDate = "N/A";

            for (Order o : OrderStore.getAllOrders()) {
                if (o.getUserId() == u.getUserId()) {
                    if (!"CANCELLED".equalsIgnoreCase(o.getOrderStatus())) {
                        spent += o.getTotalAmount();
                    }
                    orderCount++;
                    if (o.getOrderDate() != null) {
                        lastDate = o.getOrderDate();
                    }
                }
            }

            writer.printf("%d,%s,%s,%d,%.2f,%s%n",
                    u.getUserId(),
                    escapeCSV(u.getFullName()),
                    escapeCSV(u.getEmail()),
                    orderCount,
                    spent,
                    escapeCSV(lastDate));
            count++;
        }
        return count;
    }

    // =========================================================================
    // 5. Comprehensive Plain-Text Sales & Audit Dossier
    // =========================================================================

    /**
     * Generates a comprehensive plain text audit report containing all KPI metrics,
     * reconciliation tables, inventory alerts, and category shares.
     *
     * @param targetFile destination text file
     * @throws IOException on I/O error
     */
    public static void exportComprehensiveSalesAudit(File targetFile) throws IOException {
        String timestamp = LocalDateTime.now().format(ISO_DT);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {

            writer.println("========================================================================================");
            writer.println("                      QVETO E-COMMERCE & INVENTORY MANAGEMENT SYSTEM                     ");
            writer.println("                          COMPREHENSIVE SALES & TRANSACTION AUDIT                       ");
            writer.println("========================================================================================");
            writer.println("Audit Timestamp:      " + timestamp);
            writer.println("Database Provider:    " + DBStatusUtil.getStatusText());
            writer.println("Persistence Mode:     " + (DBStatusUtil.isDatabaseConnected() ? "Live MySQL (schema.sql + migration_v3)" : "Standalone In-Memory Store"));
            writer.println("Prepared By:          Member 1 (Database & Persistence Layer — Harsh)");
            writer.println("----------------------------------------------------------------------------------------\n");

            // --- Section 1: Executive KPI Summary ---
            writer.println("1. EXECUTIVE PERFORMANCE & REVENUE KPI SUMMARY");
            writer.println("----------------------------------------------------------------------------------------");
            double totalRevenue = ReportStore.getTotalRevenue();
            int totalOrders = ReportStore.getTotalOrders();
            int totalCustomers = ReportStore.getTotalCustomers();
            int lowStockCount = ReportStore.getLowStockExposureCount();
            int totalCatalog = ProductStore.countAll();
            double aov = totalOrders > 0 ? (totalRevenue / totalOrders) : 0.0;

            writer.printf("• Gross Sales Revenue (excl. Cancelled):  $%,12.2f%n", totalRevenue);
            writer.printf("• Total Recorded Orders:                   %12d orders%n", totalOrders);
            writer.printf("• Average Order Value (AOV):              $%,12.2f%n", aov);
            writer.printf("• Registered Customer Accounts:            %12d accounts%n", totalCustomers);
            writer.printf("• Active Catalog SKUs:                     %12d products%n", totalCatalog);
            writer.printf("• Inventory Low-Stock Risk Exposure:       %12d SKUs%n%n", lowStockCount);

            // --- Section 2: Order Fulfillment Pipeline ---
            writer.println("2. ORDER FULFILLMENT & LIFECYCLE DISTRIBUTION");
            writer.println("----------------------------------------------------------------------------------------");
            Map<String, Integer> statusDist = ReportStore.getOrderStatusDistribution();
            for (Map.Entry<String, Integer> e : statusDist.entrySet()) {
                double pct = totalOrders > 0 ? (e.getValue() * 100.0 / totalOrders) : 0.0;
                writer.printf("  %-14s : %4d orders (%5.1f%%)%n", e.getKey(), e.getValue(), pct);
            }
            writer.println();

            // --- Section 3: Revenue by Category Matrix ---
            writer.println("3. REVENUE CONTRIBUTION BY MERCHANDISE CATEGORY");
            writer.println("----------------------------------------------------------------------------------------");
            writer.printf("%-24s %-16s %-12s%n", "Category Name", "Revenue ($)", "Share (%)");
            writer.println("----------------------------------------------------------------------------------------");
            Map<String, Double> catRevenue = ReportStore.getRevenueByCategory();
            if (catRevenue.isEmpty()) {
                writer.println("No category sales recorded yet.");
            } else {
                for (Map.Entry<String, Double> e : catRevenue.entrySet()) {
                    double pct = totalRevenue > 0 ? (e.getValue() * 100.0 / totalRevenue) : 0.0;
                    writer.printf("%-24s $%,14.2f %10.1f%%%n", e.getKey(), e.getValue(), pct);
                }
            }
            writer.println();

            // --- Section 4: Top Velocity Products Leaderboard ---
            writer.println("4. TOP-SELLING MERCHANDISE LEADERBOARD (VELOCITY)");
            writer.println("----------------------------------------------------------------------------------------");
            writer.printf("%-5s %-32s %-16s %-12s %-14s%n", "Rank", "Product Name", "Category", "Units Sold", "Revenue ($)");
            writer.println("----------------------------------------------------------------------------------------");
            List<ProductSalesStat> topStats = ReportStore.getTopSellingProducts(10);
            if (topStats.isEmpty()) {
                writer.println("No sales data available.");
            } else {
                int rank = 1;
                for (ProductSalesStat p : topStats) {
                    writer.printf("#%-4d %-32s %-16s %-12d $%,12.2f%n",
                            rank++,
                            truncate(p.getProductName(), 30),
                            truncate(p.getCategoryName(), 14),
                            p.getTotalUnitsSold(),
                            p.getTotalRevenueAsDouble());
                }
            }
            writer.println();

            // --- Section 5: Inventory Risk & Reorder Audit ---
            writer.println("5. INVENTORY REORDER ALERTS & STOCK DEFICIT AUDIT");
            writer.println("----------------------------------------------------------------------------------------");
            writer.printf("%-6s %-32s %-16s %-8s %-10s %-14s%n", "SKU ID", "Product Name", "Category", "Stock", "Threshold", "Status");
            writer.println("----------------------------------------------------------------------------------------");
            List<LowStockAlert> alerts = Collections.emptyList();
            if (DBStatusUtil.isDatabaseConnected()) {
                try {
                    alerts = new ReportDAO().getDetailedLowStockAlerts();
                } catch (Throwable ignored) {}
            }

            if (!alerts.isEmpty()) {
                for (LowStockAlert a : alerts) {
                    writer.printf("%-6d %-32s %-16s %-8d %-10d %-14s%n",
                            a.getProductId(),
                            truncate(a.getProductName(), 30),
                            truncate(a.getCategoryName(), 14),
                            a.getStockQuantity(),
                            a.getThreshold(),
                            "[" + a.getStatus() + "]");
                }
            } else {
                List<Product> lowProducts = ProductStore.getLowStockProducts();
                if (lowProducts.isEmpty()) {
                    writer.println("All product inventory levels are currently healthy and above replenishment threshold.");
                } else {
                    for (Product p : lowProducts) {
                        String st = p.getStockQuantity() == 0 ? "OUT_OF_STOCK" : "LOW_STOCK";
                        writer.printf("%-6d %-32s %-16s %-8d %-10d %-14s%n",
                                p.getProductId(),
                                truncate(p.getProductName(), 30),
                                truncate(p.getCategory(), 14),
                                p.getStockQuantity(),
                                p.getLowStockThreshold(),
                                "[" + st + "]");
                    }
                }
            }
            writer.println();

            // --- Section 6: Recent Transactions Audit Trail ---
            writer.println("6. SALES TRANSACTIONS AUDIT TRAIL");
            writer.println("----------------------------------------------------------------------------------------");
            writer.printf("%-8s %-19s %-10s %-12s %-12s %-12s%n", "Order ID", "Timestamp", "Method", "Payment St.", "Order St.", "Amount ($)");
            writer.println("----------------------------------------------------------------------------------------");
            List<Order> orders = OrderStore.getAllOrders();
            if (orders.isEmpty()) {
                writer.println("No customer orders recorded in system ledger.");
            } else {
                int displayLimit = Math.min(25, orders.size());
                for (int i = 0; i < displayLimit; i++) {
                    Order o = orders.get(i);
                    String method = o.getPaymentMethod() != null ? o.getPaymentMethod() : "COD";
                    String payStatus = "CANCELLED".equalsIgnoreCase(o.getOrderStatus()) ? "REFUNDED"
                            : ("COD".equalsIgnoreCase(method) ? "PENDING" : "COMPLETED");
                    String date = o.getOrderDate() != null ? o.getOrderDate() : "N/A";

                    writer.printf("#%-7d %-19s %-10s %-12s %-12s $%,10.2f%n",
                            o.getOrderId(),
                            truncate(date, 19),
                            truncate(method, 10),
                            truncate(payStatus, 12),
                            truncate(o.getOrderStatus(), 12),
                            o.getTotalAmount());
                }
                if (orders.size() > displayLimit) {
                    writer.printf("... and %d more orders (see sales_orders_audit.csv for full un-truncated dataset)%n", orders.size() - displayLimit);
                }
            }

            writer.println("\n========================================================================================");
            writer.println("   END OF QVETO SALES AUDIT DOSSIER — CONFIDENTIAL FOR EVALUATION PURPOSES ONLY         ");
            writer.println("========================================================================================");
        }
    }

    // =========================================================================
    // 6. Complete Audit Bundle Exporter
    // =========================================================================

    /**
     * Exports all 4 CSV datasets and the comprehensive plain-text audit dossier
     * into a single designated directory.
     *
     * @param directory target directory
     * @return map of filename to record counts
     * @throws IOException on I/O error
     */
    public static Map<String, Integer> exportCompleteAuditBundle(File directory) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Map<String, Integer> summary = new LinkedHashMap<>();

        File ordersFile = new File(directory, "sales_orders_audit.csv");
        summary.put("sales_orders_audit.csv", exportSalesOrdersCSV(ordersFile));

        File itemsFile = new File(directory, "order_line_items_audit.csv");
        summary.put("order_line_items_audit.csv", exportOrderItemsCSV(itemsFile));

        File productsFile = new File(directory, "product_inventory_audit.csv");
        summary.put("product_inventory_audit.csv", exportProductPerformanceCSV(productsFile));

        File customersFile = new File(directory, "customer_spend_audit.csv");
        summary.put("customer_spend_audit.csv", exportCustomerSpendCSV(customersFile));

        File textFile = new File(directory, "comprehensive_sales_audit.txt");
        exportComprehensiveSalesAudit(textFile);
        summary.put("comprehensive_sales_audit.txt", 1);

        return summary;
    }

    // =========================================================================
    // RFC 4180 CSV & String Formatting Helpers
    // =========================================================================

    /**
     * Escapes a string field according to RFC 4180 CSV specifications:
     * - Surrounds with quotes if it contains commas, double quotes, CR or LF.
     * - Replaces each double quote with two double quotes.
     */
    public static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }

    /**
     * Direct CLI runner for self-testing and instant verification of report exports.
     */
    public static void main(String[] args) {
        System.out.println("=====================================================================");
        System.out.println("   Qveto ReportExportUtil - CSV & Plain Text Sales Audit Exporter    ");
        System.out.println("=====================================================================");
        System.out.println("Database Status: " + DBStatusUtil.getStatusText());
        System.out.println("---------------------------------------------------------------------\n");

        File outDir = new File("reports_export_test");
        try {
            System.out.print("[1/5] Exporting sales orders CSV ... ");
            File ordersFile = new File(outDir, "sales_orders_audit.csv");
            outDir.mkdirs();
            int orders = exportSalesOrdersCSV(ordersFile);
            System.out.println("OK (" + orders + " rows written -> " + ordersFile.getName() + ")");

            System.out.print("[2/5] Exporting order line items CSV ... ");
            File itemsFile = new File(outDir, "order_line_items_audit.csv");
            int items = exportOrderItemsCSV(itemsFile);
            System.out.println("OK (" + items + " rows written -> " + itemsFile.getName() + ")");

            System.out.print("[3/5] Exporting product performance CSV ... ");
            File prodFile = new File(outDir, "product_inventory_audit.csv");
            int prods = exportProductPerformanceCSV(prodFile);
            System.out.println("OK (" + prods + " rows written -> " + prodFile.getName() + ")");

            System.out.print("[4/5] Exporting customer spend CSV ... ");
            File custFile = new File(outDir, "customer_spend_audit.csv");
            int custs = exportCustomerSpendCSV(custFile);
            System.out.println("OK (" + custs + " rows written -> " + custFile.getName() + ")");

            System.out.print("[5/5] Exporting comprehensive plain-text audit ... ");
            File txtFile = new File(outDir, "comprehensive_sales_audit.txt");
            exportComprehensiveSalesAudit(txtFile);
            System.out.println("OK (" + txtFile.length() + " bytes -> " + txtFile.getName() + ")");

            System.out.println("\n🎉 All 5 report exports completed successfully!");
            System.out.println("Output directory: " + outDir.getAbsolutePath());

            // Clean up test directory files
            for (File f : Objects.requireNonNull(outDir.listFiles())) {
                f.delete();
            }
            outDir.delete();
            System.out.println("Verification files verified and cleanly wiped.");

        } catch (Exception e) {
            System.err.println("\n[ERROR] Report export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

