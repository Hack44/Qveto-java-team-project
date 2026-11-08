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
 * 7. Customer Saved Items & Product Wishlist Management
 * 8. Post-Delivery Return / Refund Lifecycle & Automated Inventory Replenishment
 * 9. Real-Time Store Notifications & System Broadcast Hub
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
        runTest("Customer Wishlist & Saved Items", StoreWorkflowIntegrationTest::testWishlistStoreOperations);
        runTest("Order Return / Refund & Stock Replenishment", StoreWorkflowIntegrationTest::testReturnRequestAndApprovalWorkflow);
        runTest("Store Notifications & Broadcast Hub", StoreWorkflowIntegrationTest::testNotificationCenterStore);
        runTest("Customer Address Book & Multi-Destination Management", StoreWorkflowIntegrationTest::testAddressBookStoreOperations);
        runTest("Store Audit Activity Journal & CSV Compliance Trail", StoreWorkflowIntegrationTest::testAuditStoreLoggingAndCsvExport);

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

    // ========================================================================
    // 7. Customer Wishlist Store Operations Tests
    // ========================================================================
    private static void testWishlistStoreOperations() {
        int testUserId = 999;
        WishlistStore.clearWishlist(testUserId);
        assertEquals(0, WishlistStore.getWishlistCount(testUserId), "Cleared wishlist must be empty");

        Product p1 = ProductStore.findById(101);
        assertTrue(p1 != null, "Product 101 should exist");

        // Add to wishlist
        boolean added = WishlistStore.addToWishlist(testUserId, p1);
        assertTrue(added, "Initial add to wishlist should succeed");
        assertTrue(WishlistStore.isInWishlist(testUserId, 101), "Product 101 should be marked in wishlist");
        assertEquals(1, WishlistStore.getWishlistCount(testUserId), "Wishlist count should be 1");

        // Prevent duplicate add
        boolean duplicate = WishlistStore.addToWishlist(testUserId, p1);
        assertTrue(!duplicate, "Adding duplicate product should be rejected");
        assertEquals(1, WishlistStore.getWishlistCount(testUserId), "Wishlist count must remain 1");

        // Remove from wishlist
        boolean removed = WishlistStore.removeFromWishlist(testUserId, 101);
        assertTrue(removed, "Removing product from wishlist should succeed");
        assertTrue(!WishlistStore.isInWishlist(testUserId, 101), "Product 101 should no longer be in wishlist");
        assertEquals(0, WishlistStore.getWishlistCount(testUserId), "Wishlist count should be 0");
    }

    // ========================================================================
    // 8. Order Return / Refund Lifecycle & Stock Replenishment Tests
    // ========================================================================
    private static void testReturnRequestAndApprovalWorkflow() {
        Product p = ProductStore.findById(103);
        assertTrue(p != null, "Product 103 must exist");
        int stockBefore = p.getStockQuantity();

        // Place a test order for 2 units of p103
        Order testOrder = new Order(0, "ORD-TEST-RET", 1, 39.98, "DELIVERED", "PREPAID", "2026-11-06");
        testOrder.addItem(new OrderItem(0, 0, 103, p.getProductName(), 2, 19.99));
        testOrder = OrderStore.placeOrder(testOrder);

        // After order placement, stock was reduced by 2
        assertEquals(stockBefore - 2, ProductStore.findById(103).getStockQuantity(), "Stock should be reduced after placement");
        assertTrue(testOrder.isReturnable(), "DELIVERED order should be eligible for return");

        // Request return
        boolean requested = OrderStore.requestReturn(testOrder, "Item arrived damaged during transit");
        assertTrue(requested, "Return request should succeed");
        assertEquals("RETURN_REQUESTED", testOrder.getOrderStatus(), "Status should transition to RETURN_REQUESTED");

        // Admin approves return
        boolean approved = OrderStore.approveReturn(testOrder);
        assertTrue(approved, "Admin return approval should succeed");
        assertEquals("REFUNDED", testOrder.getOrderStatus(), "Status should transition to REFUNDED");

        // Product stock must be automatically replenished by 2 units
        assertEquals(stockBefore, ProductStore.findById(103).getStockQuantity(), "Inventory stock must be restored after refund approval");
        Payment pay = PaymentStore.getPaymentForOrder(testOrder.getOrderId());
        assertEquals("REFUNDED", pay.getPaymentStatus(), "Payment status must transition to REFUNDED");
    }

    // ========================================================================
    // 9. Real-Time Store Notifications & Broadcast Hub Tests
    // ========================================================================
    private static void testNotificationCenterStore() {
        int testUser = 888;
        NotificationStore.clearNotifications(testUser, "CUSTOMER");

        int initialUnread = NotificationStore.getUnreadCount(testUser, "CUSTOMER");
        NotificationStore.postOrderAlert(testUser, "Test Shipment Milestone", "Tracking ID: TRK-990812");

        int unreadAfter = NotificationStore.getUnreadCount(testUser, "CUSTOMER");
        assertTrue(unreadAfter > initialUnread, "Unread count should increment after posting alert");

        List<Notification> list = NotificationStore.getNotificationsForUser(testUser, "CUSTOMER");
        assertTrue(!list.isEmpty(), "User notifications list should contain posted alert");
        assertEquals("Test Shipment Milestone", list.get(0).getTitle(), "Latest notification should be top of list");

        NotificationStore.markAllAsRead(testUser, "CUSTOMER");
        assertEquals(0, NotificationStore.getUnreadCount(testUser, "CUSTOMER"), "All notifications should be marked read");
    }

    // ========================================================================
    // 10. Customer Address Book & Multi-Destination Management Tests
    // ========================================================================
    private static void testAddressBookStoreOperations() {
        int testUser = 901;
        List<Address> addresses = AddressBookStore.getAddresses(testUser);
        addresses.clear();

        // 1. Add primary address
        Address addr1 = new Address(0, testUser, "Home", "Alex Morgan",
                "124 Tech Park Road, Whitefield", "Bengaluru", "Karnataka", "560066",
                "+91 9876543210", true);
        Address saved1 = AddressBookStore.addAddress(addr1);
        assertTrue(saved1 != null && saved1.getAddressId() > 0, "Saved address should have positive ID");
        assertTrue(saved1.isDefault(), "First added address should be marked as default");

        // 2. Add second address
        Address addr2 = new Address(0, testUser, "Work", "Alex Morgan",
                "Tower B, Innovation Cyber City", "Bengaluru", "Karnataka", "560100",
                "+91 9876543211", false);
        Address saved2 = AddressBookStore.addAddress(addr2);
        assertEquals(2, AddressBookStore.count(testUser), "AddressBookStore count should be 2");

        // 3. Verify default address lookup
        Address defaultAddr = AddressBookStore.getDefaultAddress(testUser);
        assertEquals(saved1.getAddressId(), defaultAddr.getAddressId(), "Default address should match addr1");

        // 4. Switch default address
        boolean switched = AddressBookStore.setDefault(testUser, saved2.getAddressId());
        assertTrue(switched, "setDefault should return true");
        Address newDefault = AddressBookStore.getDefaultAddress(testUser);
        assertEquals(saved2.getAddressId(), newDefault.getAddressId(), "Default address should now match addr2");
        assertTrue(!saved1.isDefault(), "addr1 should no longer be default");

        // 5. Update address
        saved1.setStreetAddress("Suite 405, Prestige Tech Cloud");
        boolean updated = AddressBookStore.updateAddress(saved1);
        assertTrue(updated, "updateAddress should succeed");

        // 6. Delete address
        boolean deleted = AddressBookStore.deleteAddress(testUser, saved1.getAddressId());
        assertTrue(deleted, "deleteAddress should succeed");
        assertEquals(1, AddressBookStore.count(testUser), "AddressBookStore count should be 1 after deletion");
    }

    // ========================================================================
    // 11. Store Audit Activity Journal & CSV Compliance Trail Tests
    // ========================================================================
    private static void testAuditStoreLoggingAndCsvExport() {
        int initialCount = AuditStore.getLogCount();

        // 1. Log administrative activity
        AuditLog logged = AuditStore.log("admin_test", "ADMIN", "INVENTORY", "STOCK_ADJUSTMENT",
                "Automated test restock: +50 units of Test Item SKU-999");
        assertTrue(logged != null && logged.getLogId() > 0, "Logged audit entry must have positive ID");
        assertTrue(AuditStore.getLogCount() > initialCount, "Audit count must increase after log()");

        // 2. Retrieve by category
        List<AuditLog> invLogs = AuditStore.getLogsByCategory("INVENTORY");
        assertTrue(!invLogs.isEmpty(), "Inventory audit category should contain logged event");
        assertEquals("INVENTORY", invLogs.get(0).getCategory(), "First log should match requested category");

        // 3. Search logs
        List<AuditLog> searchResults = AuditStore.searchLogs("SKU-999", "ALL");
        assertTrue(!searchResults.isEmpty(), "Search for 'SKU-999' should return matching audit entry");
        assertEquals(logged.getLogId(), searchResults.get(0).getLogId(), "Search match must correspond to logged entry");

        // 4. Export CSV audit trail
        File exportFile = new File("test_store_audit_export.csv");
        try {
            AuditStore.exportAuditCsv(exportFile);
            assertTrue(exportFile.exists() && exportFile.length() > 0, "Exported audit CSV must exist and be non-empty");

            String content = new String(Files.readAllBytes(exportFile.toPath()), StandardCharsets.UTF_8);
            assertTrue(content.startsWith("LogID,Timestamp,Actor,Role,Category,Action,Details,IPAddress"),
                    "CSV must contain valid RFC-compliant audit header");
            assertTrue(content.contains("SKU-999"), "CSV must include logged audit payload");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to verify audit CSV export: " + ex.getMessage(), ex);
        } finally {
            if (exportFile.exists()) {
                exportFile.delete();
            }
        }
    }
}
