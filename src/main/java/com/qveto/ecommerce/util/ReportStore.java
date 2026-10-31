package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.ReportDAO;
import com.qveto.ecommerce.dao.ReportDAO.ProductSalesStat;
import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Centralized Report and Analytics Store coordinating between the JDBC ReportDAO layer and
 * in-memory transactional records for standalone demo mode.
 * 
 * Supports Member 2 (UI, Login & User Management) Admin Reports & KPIs requirements.
 */
public class ReportStore {

    private ReportStore() {
        // Utility class
    }

    /**
     * Calculates total gross sales revenue.
     */
    public static double getTotalRevenue() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                BigDecimal rev = new ReportDAO().calculateTotalRevenue();
                if (rev != null && rev.doubleValue() > 0) {
                    return rev.doubleValue();
                }
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB calculateTotalRevenue fallback: " + t.getMessage());
            }
        }
        return OrderStore.calculateTotalRevenue();
    }

    /**
     * Returns total order count across all statuses.
     */
    public static int getTotalOrders() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int count = new ReportDAO().countTotalOrders();
                if (count > 0) return count;
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB countTotalOrders fallback: " + t.getMessage());
            }
        }
        return OrderStore.countAll();
    }

    /**
     * Returns total registered customer count.
     */
    public static int getTotalCustomers() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int count = new ReportDAO().countTotalCustomers();
                if (count > 0) return count;
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB countTotalCustomers fallback: " + t.getMessage());
            }
        }
        return (int) UserStore.getAllUsers().stream().filter(u -> !u.isAdmin()).count();
    }

    /**
     * Returns count of products currently at or below their low-stock threshold.
     */
    public static int getLowStockExposureCount() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int count = new ReportDAO().getLowStockExposureCount();
                if (count > 0) return count;
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB getLowStockExposureCount fallback: " + t.getMessage());
            }
        }
        return ProductStore.getLowStockProducts().size();
    }

    /**
     * Retrieves top-selling products ranked by units sold and revenue.
     */
    public static List<ProductSalesStat> getTopSellingProducts(int limit) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<ProductSalesStat> dbStats = new ReportDAO().getTopSellingProducts(limit);
                if (dbStats != null && !dbStats.isEmpty()) {
                    return dbStats;
                }
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB getTopSellingProducts fallback: " + t.getMessage());
            }
        }

        // In-memory fallback calculation
        Map<Integer, Integer> productUnits = new HashMap<>();
        Map<Integer, Double> productRevenue = new HashMap<>();
        Map<Integer, String> productNames = new HashMap<>();

        for (Order o : OrderStore.getAllOrders()) {
            if ("CANCELLED".equalsIgnoreCase(o.getOrderStatus())) continue;
            if (o.getItems() != null) {
                for (OrderItem item : o.getItems()) {
                    int pId = item.getProductId();
                    productUnits.put(pId, productUnits.getOrDefault(pId, 0) + item.getQuantity());
                    productRevenue.put(pId, productRevenue.getOrDefault(pId, 0.0) + item.getSubtotal());
                    productNames.put(pId, item.getProductName());
                }
            }
        }

        List<ProductSalesStat> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : productUnits.entrySet()) {
            int pId = entry.getKey();
            int units = entry.getValue();
            double rev = productRevenue.getOrDefault(pId, 0.0);
            String name = productNames.getOrDefault(pId, "Product #" + pId);

            // Lookup category
            String cat = "General";
            for (Product p : ProductStore.getAllProducts()) {
                if (p.getProductId() == pId) {
                    cat = p.getCategory();
                    break;
                }
            }

            list.add(new ProductSalesStat(pId, name, cat, units, BigDecimal.valueOf(rev)));
        }

        list.sort((a, b) -> Integer.compare(b.getTotalUnitsSold(), a.getTotalUnitsSold()));
        if (limit > 0 && list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    /**
     * Returns distribution of orders grouped by lifecycle status.
     */
    public static Map<String, Integer> getOrderStatusDistribution() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                Map<String, Integer> dist = new ReportDAO().getOrderStatusDistribution();
                if (dist != null && !dist.isEmpty()) {
                    return dist;
                }
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB getOrderStatusDistribution fallback: " + t.getMessage());
            }
        }

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("PENDING", 0);
        distribution.put("CONFIRMED", 0);
        distribution.put("SHIPPED", 0);
        distribution.put("DELIVERED", 0);
        distribution.put("CANCELLED", 0);

        for (Order o : OrderStore.getAllOrders()) {
            String status = o.getOrderStatus().toUpperCase();
            distribution.put(status, distribution.getOrDefault(status, 0) + 1);
        }
        return distribution;
    }

    /**
     * Returns revenue breakdown by product category.
     */
    public static Map<String, Double> getRevenueByCategory() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                Map<String, BigDecimal> dbMap = new ReportDAO().getRevenueByCategory();
                if (dbMap != null && !dbMap.isEmpty()) {
                    Map<String, Double> converted = new LinkedHashMap<>();
                    for (Map.Entry<String, BigDecimal> e : dbMap.entrySet()) {
                        converted.put(e.getKey(), e.getValue().doubleValue());
                    }
                    return converted;
                }
            } catch (Throwable t) {
                System.err.println("[ReportStore] DB getRevenueByCategory fallback: " + t.getMessage());
            }
        }

        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (Order o : OrderStore.getAllOrders()) {
            if ("CANCELLED".equalsIgnoreCase(o.getOrderStatus())) continue;
            if (o.getItems() != null) {
                for (OrderItem item : o.getItems()) {
                    String cat = "General";
                    for (Product p : ProductStore.getAllProducts()) {
                        if (p.getProductId() == item.getProductId()) {
                            cat = p.getCategory();
                            break;
                        }
                    }
                    breakdown.put(cat, breakdown.getOrDefault(cat, 0.0) + item.getSubtotal());
                }
            }
        }
        return breakdown;
    }

    /**
     * Exports an official executive business summary report to a text file.
     */
    public static void exportExecutiveReport(File file) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("========================================================================\n");
            writer.write("                   QVETO E-COMMERCE & INVENTORY SYSTEM\n");
            writer.write("                     EXECUTIVE SALES & KPI REPORT\n");
            writer.write("========================================================================\n");
            writer.write("Generated At:     " + timestamp + "\n");
            writer.write("Data Source Mode: " + DBStatusUtil.getStatusText() + "\n");
            writer.write("------------------------------------------------------------------------\n\n");

            writer.write("1. EXECUTIVE PERFORMANCE METRICS\n");
            writer.write("------------------------------------------------------------------------\n");
            writer.write(String.format("• Gross Sales Revenue:      $%.2f\n", getTotalRevenue()));
            writer.write(String.format("• Total Platform Orders:    %d\n", getTotalOrders()));
            writer.write(String.format("• Registered Customers:     %d\n", getTotalCustomers()));
            writer.write(String.format("• Total Active Catalog SKUs:%d\n", ProductStore.countAll()));
            writer.write(String.format("• Low-Stock Alerts / Risks: %d products\n\n", getLowStockExposureCount()));

            writer.write("2. ORDER LIFECYCLE DISTRIBUTION\n");
            writer.write("------------------------------------------------------------------------\n");
            Map<String, Integer> statusDist = getOrderStatusDistribution();
            int totalOrders = getTotalOrders();
            for (Map.Entry<String, Integer> entry : statusDist.entrySet()) {
                double pct = totalOrders > 0 ? (entry.getValue() * 100.0 / totalOrders) : 0.0;
                writer.write(String.format("• %-12s : %3d orders (%5.1f%%)\n", entry.getKey(), entry.getValue(), pct));
            }
            writer.write("\n");

            writer.write("3. TOP-SELLING PRODUCTS LEADERBOARD\n");
            writer.write("------------------------------------------------------------------------\n");
            writer.write(String.format("%-4s %-32s %-15s %-10s %-12s\n", "Rank", "Product Name", "Category", "Units Sold", "Revenue ($)"));
            writer.write("------------------------------------------------------------------------\n");
            List<ProductSalesStat> topSelling = getTopSellingProducts(5);
            if (topSelling.isEmpty()) {
                writer.write("No product sales data recorded yet.\n");
            } else {
                int rank = 1;
                for (ProductSalesStat stat : topSelling) {
                    writer.write(String.format("#%-3d %-32s %-15s %-10d $%-11.2f\n",
                            rank++, stat.getProductName(), stat.getCategoryName(),
                            stat.getTotalUnitsSold(), stat.getTotalRevenueAsDouble()));
                }
            }
            writer.write("\n");

            writer.write("4. REVENUE CONTRIBUTION BY CATEGORY\n");
            writer.write("------------------------------------------------------------------------\n");
            Map<String, Double> catRevenue = getRevenueByCategory();
            double totalRev = getTotalRevenue();
            if (catRevenue.isEmpty()) {
                writer.write("No category revenue data recorded.\n");
            } else {
                for (Map.Entry<String, Double> e : catRevenue.entrySet()) {
                    double pct = totalRev > 0 ? (e.getValue() * 100.0 / totalRev) : 0.0;
                    writer.write(String.format("• %-18s : $%-10.2f (%5.1f%%)\n", e.getKey(), e.getValue(), pct));
                }
            }
            writer.write("========================================================================\n");
            writer.write("CONFIDENTIAL — FOR INTERNAL MANAGEMENT & INTERNSHIP EVALUATION ONLY\n");
        }
    }
}
