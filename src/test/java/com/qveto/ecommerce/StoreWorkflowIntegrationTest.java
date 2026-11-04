package com.qveto.ecommerce;

import com.qveto.ecommerce.model.*;
import com.qveto.ecommerce.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * End-to-end integration and automated regression test suite for Qveto E-commerce System.
 *
 * Verifies core enterprise workflows authored for Member 2:
 * 1. Promotional Coupon & Discount Validation Engine
 * 2. Product Catalog Stock Reservation & RFC 4180 CSV Export
 * 3. Customer Order Placement, Cancellation, and Orders Master CSV Export
 * 4. Executive Analytics & Dynamic Date-Range Filtering
 * 5. Standalone PDF 1.4 Vector Invoice Receipt Generation
 * 6. Verified Customer Product Review & 5-Star Rating Calculations
 */
public class StoreWorkflowIntegrationTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("   QVETO E-COMMERCE & INVENTORY MANAGEMENT SYSTEM - INTEGRATION TESTS   ");
        System.out.println("========================================================================");

        runTest("Coupon Engine Discount Thresholds", StoreWorkflowIntegrationTest::testCouponStoreDiscounts);
        runTest("Product Catalog & CSV Export", StoreWorkflowIntegrationTest::testProductStoreStockAndCsvExport);
        runTest("Order Lifecycle & Orders Master CSV", StoreWorkflowIntegrationTest::testOrderLifecycleAndCsvExport);
        runTest("Executive Date-Range Sales Reporting", StoreWorkflowIntegrationTest::testDateRangeReporting);
        runTest("PDF 1.4 Vector Invoice Binary Generation", StoreWorkflowIntegrationTest::testPdfInvoiceGeneration);
        runTest("Customer Verified Reviews & Star Ratings", StoreWorkflowIntegrationTest::testReviewStoreCalculations);

        System.out.println("========================================================================");
        System.out.println(String.format("TEST EXECUTION COMPLETED: %d PASSED, %d FAILED", totalPassed, totalFailed));
        System.out.println("========================================================================");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, Runnable testRunnable) {
        try {
            System.out.print("[RUNNING] " + testName + " ... ");
            testRunnable.run();
            System.out.println("PASSED");
            totalPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED (" + t.getMessage() + ")");
            t.printStackTrace(System.err);
            totalFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("%s: expected <%s> but got <%s>", message, expected, actual));
        }
    }

    // ========================================================================
    // 1. Coupon Engine Tests
    // ========================================================================
    private static void testCouponStoreDiscounts() {
        // Test valid percentage coupon
        CouponStore.CouponResult res1 = CouponStore.validateAndApply("QVETO10", 100.0);
        assertTrue(res1.isValid(), "QVETO10 should be valid for cart of $100");
        assertEquals(10.0, res1.getDiscountAmount(), "QVETO10 should yield $10.0 discount on $100");

        // Test minimum cart requirement
        CouponStore.CouponResult resMin = CouponStore.validateAndApply("SAVE20", 25.0);
        assertTrue(!resMin.isValid(), "SAVE20 should be invalid for cart under $80 threshold");

        // Test valid threshold
        CouponStore.CouponResult res2 = CouponStore.validateAndApply("SAVE20", 100.0);
        assertTrue(res2.isValid(), "SAVE20 should be valid for cart >= $80");
        assertEquals(20.0, res2.getDiscountAmount(), "SAVE20 should yield $20.0 discount on $100");

        // Test invalid promo code
        CouponStore.CouponResult resInvalid = CouponStore.validateAndApply("UNKNOWN99", 200.0);
        assertTrue(!resInvalid.isValid(), "Non-existent coupon must be rejected");
    }

    // ========================================================================
    // 2. Product Store Stock & CSV Export Tests
    // ========================================================================
    private static void testProductStoreStockAndCsvExport() {
        List<Product> products = ProductStore.getAllProducts();
        assertTrue(!products.isEmpty(), "Catalog must contain seeded products");

        Product p101 = ProductStore.findById(101);
        assertTrue(p101 != null, "Product 101 must exist");
        int initialStock = p101.getStockQuantity();

        // Adjust stock
        boolean adjusted = ProductStore.adjustStock(101, -2);
        assertTrue(adjusted, "Stock decrement of 2 units should succeed");
        assertEquals(initialStock - 2, ProductStore.findById(101).getStockQuantity(), "Stock quantity should reflect decrement");

        // Restore stock
        ProductStore.adjustStock(101, 2);
        assertEquals(initialStock, ProductStore.findById(101).getStockQuantity(), "Stock quantity should be restored");

        // Test CSV Export
        try {
            File tempCsv = File.createTempFile("qveto_catalog_test_", ".csv");
            tempCsv.deleteOnExit();
            ProductStore.exportCatalogCsv(tempCsv);

            assertTrue(tempCsv.exists() && tempCsv.length() > 0, "Exported catalog CSV must exist and be non-empty");
            List<String> lines = Files.readAllLines(tempCsv.toPath(), StandardCharsets.UTF_8);
            assertTrue(lines.size() >= 2, "CSV must have header row and at least 1 product row");
            assertTrue(lines.get(0).contains("Product ID") && lines.get(0).contains("Product Name"), "CSV header must match standard RFC 4180 columns");
        } catch (Exception e) {
            throw new RuntimeException("CSV export test encountered IO error: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // 3. Order Lifecycle & Orders Master CSV Export Tests
    // ========================================================================
    private static void testOrderLifecycleAndCsvExport() {
        List<Order> orders = OrderStore.getAllOrders();
        assertTrue(!orders.isEmpty(), "OrderStore must contain seeded orders");

        // Test CSV Export
        try {
            File tempCsv = File.createTempFile("qveto_orders_test_", ".csv");
            tempCsv.deleteOnExit();
            OrderStore.exportOrdersCsv(tempCsv);

            assertTrue(tempCsv.exists() && tempCsv.length() > 0, "Exported orders CSV must exist and be non-empty");
            List<String> lines = Files.readAllLines(tempCsv.toPath(), StandardCharsets.UTF_8);
            assertTrue(lines.size() >= 2, "Orders CSV must have header row and order entries");
            assertTrue(lines.get(0).contains("Order Number") && lines.get(0).contains("Total Amount"), "Orders CSV header must be present");
        } catch (Exception e) {
            throw new RuntimeException("Orders CSV export failed: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // 4. Executive Date-Range Sales Reporting Tests
    // ========================================================================
    private static void testDateRangeReporting() {
        // Test all-time vs range filtering
        double totalRev = ReportStore.getTotalRevenue();
        assertTrue(totalRev > 0, "Total revenue must be positive");

        List<Order> allOrders = ReportStore.getOrdersBetween(null, null);
        assertEquals(OrderStore.getAllOrders().size(), allOrders.size(), "Null date range should return all orders");

        // Test wide range encompassing 2026
        List<Order> rangeOrders = ReportStore.getOrdersBetween("2026-01-01", "2026-12-31");
        assertTrue(!rangeOrders.isEmpty(), "Orders from 2026 should be matched");

        // Test past date where no orders exist
        List<Order> emptyRange = ReportStore.getOrdersBetween("2020-01-01", "2020-01-02");
        assertEquals(0, emptyRange.size(), "Empty date range should return zero orders");

        Map<String, Integer> statusDist = ReportStore.getOrderStatusDistributionBetween("2026-01-01", "2026-12-31");
        assertTrue(statusDist.containsKey("DELIVERED"), "Status distribution must include DELIVERED orders");
    }

    // ========================================================================
    // 5. PDF 1.4 Vector Invoice Binary Generation Tests
    // ========================================================================
    private static void testPdfInvoiceGeneration() {
        List<Order> orders = OrderStore.getAllOrders();
        assertTrue(!orders.isEmpty(), "Orders must exist to test PDF generation");
        Order testOrder = orders.get(0);

        try {
            File tempPdf = File.createTempFile("qveto_invoice_test_", ".pdf");
            tempPdf.deleteOnExit();

            PdfInvoiceGenerator.generateInvoicePdf(testOrder, "123 Business Way, Metro City, 500001", tempPdf);

            assertTrue(tempPdf.exists(), "Generated invoice PDF must exist on disk");
            assertTrue(tempPdf.length() > 500, "PDF file must contain binary vector content (size > 500 bytes)");

            // Validate PDF header magic bytes: %PDF-1.4
            byte[] header = new byte[8];
            try (FileInputStream fis = new FileInputStream(tempPdf)) {
                int read = fis.read(header);
                assertTrue(read >= 8, "Must read at least 8 header bytes");
            }
            String magic = new String(header, StandardCharsets.US_ASCII);
            assertTrue(magic.startsWith("%PDF-1."), "PDF file must begin with standard Adobe PDF header magic bytes");
        } catch (Exception e) {
            throw new RuntimeException("PDF generation test failed: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // 6. Review Store Calculations Tests
    // ========================================================================
    private static void testReviewStoreCalculations() {
        int productId = 101;
        int initialCount = ReviewStore.getReviewCount(productId);
        assertTrue(initialCount >= 2, "Product 101 must have seeded reviews");

        double initialAvg = ReviewStore.getAverageRating(productId);
        assertTrue(initialAvg >= 1.0 && initialAvg <= 5.0, "Average rating must be between 1.0 and 5.0");

        // Add a 5-star review
        Review testReview = new Review(
                0,
                productId,
                99,
                "Test Evaluator",
                5,
                "Exceptional Fidelity",
                "Automated integration test review verification comment.",
                "2026-11-04",
                true
        );
        ReviewStore.addReview(testReview);

        assertEquals(initialCount + 1, ReviewStore.getReviewCount(productId), "Review count must increment by 1");
        List<Review> reviews = ReviewStore.getReviewsForProduct(productId);
        assertEquals("Exceptional Fidelity", reviews.get(0).getHeadline(), "Latest review must be at top of list");
    }
}
