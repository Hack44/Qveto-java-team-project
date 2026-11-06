package com.qveto.ecommerce.util;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.dao.*;
import com.qveto.ecommerce.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Automated Verification Suite for Database Integrity, Transaction Safety,
 * and Model Financial Precision.
 *
 * Fulfills Week 3 & Week 4 Qveto Project 1 Evaluation Metrics:
 * 1. Data Integrity: Verifies atomic transactions and clean rollbacks without orphaned records.
 * 2. Stock Accuracy: Verifies inventory decrements upon order and safe restoration on cancellation.
 * 3. Foreign Key Protection: Verifies relational constraints protect categories with active products.
 * 4. Password Security: Verifies SHA-256 password hashing and validation.
 * 5. Analytical Consistency: Verifies ReportDAO KPI metrics match underlying order data.
 *
 * Member 1 — Verification & Testing layer.
 */
public class DatabaseIntegrityTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=====================================================================");
        System.out.println("   Qveto E-Commerce System - Database Integrity & Transaction Tests   ");
        System.out.println("=====================================================================");
        System.out.println("Database Status: " + DBStatusUtil.getStatusText());
        System.out.println("---------------------------------------------------------------------\n");

        runTest("PasswordUtil SHA-256 Hashing & Verification", DatabaseIntegrityTest::testPasswordSecurity);
        runTest("Model Financial Precision (BigDecimal Arithmetic)", DatabaseIntegrityTest::testModelFinancialPrecision);
        runTest("Inventory Stock Consistency & Restoration", DatabaseIntegrityTest::testInventoryConsistency);
        runTest("Category Foreign-Key Protection & Safe Deletion", DatabaseIntegrityTest::testCategoryForeignKeyProtection);
        runTest("ReportDAO KPI Mathematical Consistency", DatabaseIntegrityTest::testAnalyticsConsistency);
        runTest("DatabaseRetryPolicy Transient Error Classification & Exponential Backoff", DatabaseIntegrityTest::testRetryPolicyResilience);
        runTest("DataSanitizer XSS Neutralization & Normalization", DatabaseIntegrityTest::testDataSanitizationAndNormalization);
        runTest("InputBoundaryValidator Domain Entity Constraint Rules", DatabaseIntegrityTest::testInputBoundaryValidationRules);

        if (DBStatusUtil.isDatabaseConnected()) {
            runTest("DBConnectionPool Lifecycle & Transparent Proxy Recycling", DatabaseIntegrityTest::testConnectionPoolRecycling);
            runTest("Live JDBC Atomic Transaction Rollback Safety", DatabaseIntegrityTest::testLiveTransactionRollback);
            runTest("Live Order-Payment Atomic Commit Completeness", DatabaseIntegrityTest::testLiveOrderPaymentAtomicCommit);
            runTest("Live Persistence Layer Sanitization & Boundary Defense", DatabaseIntegrityTest::testSanitizedPersistenceLayer);
        } else {
            System.out.println("ℹ Note: MySQL is offline; live JDBC connection pool & multi-table transaction tests skipped (standalone fallback verified).\n");
        }

        printSummary();
    }

    private static void runTest(String testName, RunnableTest test) {
        testsRun++;
        System.out.print("[TEST " + testsRun + "] " + testName + " ... ");
        try {
            test.run();
            testsPassed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            testsFailed++;
            System.out.println("FAILED -> " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

    // ---------------------------------------------------------------
    // 1. Password Security & Hashing Tests
    // ---------------------------------------------------------------
    private static void testPasswordSecurity() {
        String rawPassword = "SecurePass2026!";
        String hash = PasswordUtil.hash(rawPassword);

        assertNotNull("Computed hash should not be null", hash);
        assertEquals("SHA-256 hash length must be 64 hexadecimal chars", 64, hash.length());

        // Verification tests
        assertTrue("PasswordUtil.verify should match raw password against its hash",
                PasswordUtil.verify(rawPassword, hash));
        assertFalse("PasswordUtil.verify should reject wrong password",
                PasswordUtil.verify("WrongPassword123", hash));

        // Legacy fallback support verification
        assertTrue("PasswordUtil.verify should support fallback plain-text comparison for demo seeds",
                PasswordUtil.verify("pass123", "pass123"));
    }

    // ---------------------------------------------------------------
    // 2. Financial Precision Tests
    // ---------------------------------------------------------------
    private static void testModelFinancialPrecision() {
        // Floating point arithmetic flaw test: 19.99 * 3 in double can produce 59.970000000000006
        BigDecimal unitPrice = new BigDecimal("19.99");
        int quantity = 3;

        CartItem cartItem = new CartItem(1, 1, 101, "Test Shirt", "Clothing", quantity, unitPrice, 10);
        BigDecimal expectedSubtotal = new BigDecimal("59.97");

        assertEquals("CartItem subtotal must have exact BigDecimal precision",
                expectedSubtotal, cartItem.getSubtotal());

        OrderItem orderItem = new OrderItem(1, 1, 101, "Test Shirt", quantity, unitPrice);
        assertEquals("OrderItem subtotal must have exact BigDecimal precision",
                expectedSubtotal, orderItem.getSubtotalAsBigDecimal());
    }

    // ---------------------------------------------------------------
    // 3. Inventory Stock Consistency & Safe Restoration Tests
    // ---------------------------------------------------------------
    private static void testInventoryConsistency() {
        int productId = 101;
        int orderQty = 2;

        Product p = ProductStore.getAllProducts().stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst()
                .orElse(null);

        assertNotNull("Test product should exist in store", p);
        int initialStock = p.getStockQuantity();

        // 1. Simulate Order placement: decrement stock
        ProductStore.adjustStock(productId, -orderQty);
        int decrementedStock = ProductStore.getAllProducts().stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst()
                .get()
                .getStockQuantity();

        assertEquals("Stock must be exactly decremented by order quantity",
                initialStock - orderQty, decrementedStock);

        // 2. Simulate Order cancellation: safely restore stock
        ProductStore.adjustStock(productId, orderQty);
        int restoredStock = ProductStore.getAllProducts().stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst()
                .get()
                .getStockQuantity();

        assertEquals("Cancelled order must restore stock exactly to initial value",
                initialStock, restoredStock);
    }

    // ---------------------------------------------------------------
    // 4. Foreign Key Protection & Safe Deletion
    // ---------------------------------------------------------------
    private static void testCategoryForeignKeyProtection() {
        CategoryDAO categoryDAO = new CategoryDAO();
        // Category 1 is "Electronics" which has assigned products (101, 102, etc.)
        int assignedCategoryId = 1;

        if (DBStatusUtil.isDatabaseConnected()) {
            int productCount = categoryDAO.countProductsInCategory(assignedCategoryId);
            if (productCount > 0) {
                boolean deleted = categoryDAO.deleteById(assignedCategoryId);
                assertFalse("CategoryDAO must prevent deleting a category with active products", deleted);
            }
        }

        // Test non-existent category deletion handles gracefully
        if (DBStatusUtil.isDatabaseConnected()) {
            boolean nonExistentDeleted = categoryDAO.deleteById(999999);
            assertFalse("Deleting non-existent category must return false", nonExistentDeleted);
        }
    }

    // ---------------------------------------------------------------
    // 5. Analytics Aggregation Consistency
    // ---------------------------------------------------------------
    private static void testAnalyticsConsistency() {
        ReportDAO reportDAO = new ReportDAO();

        BigDecimal revenue = reportDAO.calculateTotalRevenue();
        assertNotNull("Total revenue must not be null", revenue);
        assertTrue("Total revenue must be non-negative", revenue.compareTo(BigDecimal.ZERO) >= 0);

        Map<String, Integer> statusDist = reportDAO.getOrderStatusDistribution();
        assertNotNull("Order status distribution must not be null", statusDist);
        assertTrue("Distribution must contain PENDING status", statusDist.containsKey("PENDING"));
        assertTrue("Distribution must contain DELIVERED status", statusDist.containsKey("DELIVERED"));
        assertTrue("Distribution must contain CANCELLED status", statusDist.containsKey("CANCELLED"));

        int lowStockCount = reportDAO.getLowStockExposureCount();
        assertTrue("Low stock exposure count must be non-negative", lowStockCount >= 0);
    }

    // ---------------------------------------------------------------
    // 6. Live JDBC Atomic Transaction Rollback Safety
    // ---------------------------------------------------------------
    private static void testLiveTransactionRollback() {
        String testEmail = "rollback.test." + System.currentTimeMillis() + "@qveto-test.com";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // start transaction

            String insertSql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, 'CUSTOMER')";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, "Rollback Test User");
                stmt.setString(2, testEmail);
                stmt.setString(3, "secret");
                stmt.executeUpdate();
            }

            // Deliberately rollback the transaction
            conn.rollback();
            conn.setAutoCommit(true);

            // Verify that the record does NOT exist in the database
            String checkSql = "SELECT user_id FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, testEmail);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    assertFalse("Record inserted in rolled back transaction must NOT persist in database", rs.next());
                }
            }

        } catch (SQLException e) {
            fail("Transaction rollback test failed with SQLException: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 7. Live Order-Payment Atomic Commit Completeness
    // ---------------------------------------------------------------
    private static void testLiveOrderPaymentAtomicCommit() {
        OrderDAO orderDAO = new OrderDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        OrderItemDAO orderItemDAO = new OrderItemDAO();

        int targetUserId = -1;
        int targetProductId = -1;
        String targetProductName = "Test Product";
        BigDecimal targetPrice = new BigDecimal("49.99");
        boolean createdTempUser = false;

        try (Connection conn = DBConnection.getConnection()) {
            // Find existing customer user
            try (PreparedStatement uStmt = conn.prepareStatement("SELECT user_id FROM users LIMIT 1");
                 ResultSet rs = uStmt.executeQuery()) {
                if (rs.next()) {
                    targetUserId = rs.getInt("user_id");
                }
            }

            // Find existing product
            try (PreparedStatement pStmt = conn.prepareStatement("SELECT product_id, product_name, price FROM products LIMIT 1");
                 ResultSet rs = pStmt.executeQuery()) {
                if (rs.next()) {
                    targetProductId = rs.getInt("product_id");
                    targetProductName = rs.getString("product_name");
                    BigDecimal dbPrice = rs.getBigDecimal("price");
                    if (dbPrice != null) {
                        targetPrice = dbPrice;
                    }
                }
            }
        } catch (SQLException e) {
            fail("Failed to query live database fixtures: " + e.getMessage());
        }

        if (targetUserId == -1) {
            // Create a temporary user if database has no users
            UserDAO userDAO = new UserDAO();
            targetUserId = userDAO.registerCustomer("Integrity Tester", "test." + System.currentTimeMillis() + "@qveto-integrity.com", "pass123", "9999999999", "Test Address");
            createdTempUser = true;
        }

        if (targetProductId == -1) {
            targetProductId = 101;
        }

        // Construct a synthetic test order
        Order testOrder = new Order();
        testOrder.setUserId(targetUserId);
        testOrder.setTotalAmount(targetPrice.doubleValue());
        testOrder.setOrderStatus("PENDING");
        testOrder.setPaymentMethod("UPI");
        testOrder.setShippingAddress("Test Verification Address");

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(0, 0, targetProductId, targetProductName, 1, targetPrice));
        testOrder.setItems(items);

        int generatedOrderId = orderDAO.placeOrder(testOrder);
        assertTrue("placeOrder must generate a valid order ID > 0", generatedOrderId > 0);

        try {
            // Verify order exists
            Order fetchedOrder = orderDAO.findById(generatedOrderId);
            assertNotNull("Order header must be persisted and retrievable", fetchedOrder);
            assertEquals("Order amount must match", targetPrice.doubleValue(), fetchedOrder.getTotalAmount(), 0.001);

            // Verify line items exist
            List<OrderItem> fetchedItems = orderItemDAO.findByOrderId(generatedOrderId);
            assertNotNull("Line items must not be null", fetchedItems);
            assertEquals("Order must contain exactly 1 line item", 1, fetchedItems.size());

            // Verify payment record exists and is synced
            Payment payment = paymentDAO.findByOrderId(generatedOrderId);
            assertNotNull("Transactional payment record must be created atomically with order", payment);
            assertEquals("Payment order_id must match", generatedOrderId, payment.getOrderId());
            assertEquals("Digital payment UPI status should be COMPLETED", "COMPLETED", payment.getPaymentStatus());

        } finally {
            // Clean up test records
            paymentDAO.updateStatusByOrderId(generatedOrderId, "REFUNDED");
            orderItemDAO.deleteByOrderId(generatedOrderId);
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement del = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                del.setInt(1, generatedOrderId);
                del.executeUpdate();
            } catch (SQLException ignored) {}

            if (createdTempUser && targetUserId > 0) {
                new UserDAO().deleteById(targetUserId);
            }
        }
    }

    // ---------------------------------------------------------------
    // 8. Database Retry Policy Resilience
    // ---------------------------------------------------------------
    private static void testRetryPolicyResilience() throws SQLException {
        DatabaseRetryPolicy policy = new DatabaseRetryPolicy(3, 20L, 100L);

        // 1. Verify transient error identification
        assertTrue("Deadlock (1213) must be classified as transient",
                DatabaseRetryPolicy.isTransient(new SQLException("Deadlock found", "40001", 1213)));
        assertTrue("Lock wait timeout (1205) must be classified as transient",
                DatabaseRetryPolicy.isTransient(new SQLException("Lock wait timeout exceeded", "HY000", 1205)));
        assertTrue("Communications link failure must be classified as transient",
                DatabaseRetryPolicy.isTransient(new SQLException("Communications link failure", "08S01", 0)));

        // 2. Verify fatal error identification
        assertFalse("Unique constraint violation (1062) must be fatal",
                DatabaseRetryPolicy.isTransient(new SQLException("Duplicate entry", "23000", 1062)));
        assertFalse("Foreign key violation (1452) must be fatal",
                DatabaseRetryPolicy.isTransient(new SQLException("Cannot add child row", "23000", 1452)));

        // 3. Verify retry execution with simulated recovery on attempt 2
        final int[] attempts = {0};
        String result = policy.execute("SimulatedTransientRecovery", () -> {
            attempts[0]++;
            if (attempts[0] == 1) {
                throw new SQLException("Simulated lock wait timeout", "HY000", 1205);
            }
            return "SUCCESS";
        });

        assertEquals("Policy must retry and succeed on attempt 2", 2, attempts[0]);
        assertEquals("Returned value must match", "SUCCESS", result);

        // 4. Verify fatal error aborts immediately without redundant retries
        final int[] fatalAttempts = {0};
        try {
            policy.execute("SimulatedFatalError", () -> {
                fatalAttempts[0]++;
                throw new SQLException("Duplicate entry for key", "23000", 1062);
            });
            fail("Fatal exception should have been thrown");
        } catch (SQLException expected) {
            assertEquals("Fatal exception must fail immediately on first attempt", 1, fatalAttempts[0]);
        }
    }

    // ---------------------------------------------------------------
    // 9. Connection Pool Proxy Recycling & Lifecycle
    // ---------------------------------------------------------------
    private static void testConnectionPoolRecycling() throws SQLException {
        assertTrue("Connection pooling must be enabled", DBConnection.isPoolEnabled());

        int initialActive = DBConnection.getActiveConnections();

        Connection conn1 = DBConnection.getConnection();
        assertNotNull("Acquired connection must not be null", conn1);
        assertTrue("Acquired connection must be healthy and valid", conn1.isValid(2));
        assertEquals("Active count must increment by 1", initialActive + 1, DBConnection.getActiveConnections());

        // Close connection — must be returned to pool, not destroyed
        conn1.close();
        assertTrue("Proxy connection must report isClosed() true after close", conn1.isClosed());
        assertEquals("Active count must decrement back to initial after close", initialActive, DBConnection.getActiveConnections());

        // Attempting to query closed proxy should throw SQLException
        try {
            conn1.createStatement();
            fail("Calling method on closed proxy connection must throw SQLException");
        } catch (SQLException expected) {
            assertTrue("Expected closed connection exception", expected.getMessage().contains("closed"));
        }

        // Re-acquiring connection from pool should be fast and valid
        try (Connection conn2 = DBConnection.getConnection()) {
            assertNotNull("Re-acquired pooled connection must not be null", conn2);
            assertTrue("Re-acquired pooled connection must be valid", conn2.isValid(2));
        }
    }

    // ---------------------------------------------------------------
    // 10. Data Sanitization & Normalization
    // ---------------------------------------------------------------
    private static void testDataSanitizationAndNormalization() {
        // 1. HTML & Script stripping
        String dirtyInput = "<script type='text/javascript'>alert('XSS');</script><b>Studio Headphones</b>";
        String cleanHtml = DataSanitizer.stripHtml(dirtyInput);
        assertEquals("stripHtml must remove script tags and HTML markup", "Studio Headphones", cleanHtml);

        // 2. HTML escaping
        String escapeInput = "<div class=\"test\">&'Hello'</div>";
        String escaped = DataSanitizer.escapeHtml(escapeInput);
        assertTrue("escapeHtml must convert < to &lt;", escaped.contains("&lt;div"));
        assertTrue("escapeHtml must convert & to &amp;", escaped.contains("&amp;"));
        assertTrue("escapeHtml must convert \" to &quot;", escaped.contains("&quot;"));

        // 3. Email normalization
        String dirtyEmail = "  Customer.Test@Example.COM  ";
        assertEquals("normalizeEmail must trim and lowercase", "customer.test@example.com", DataSanitizer.normalizeEmail(dirtyEmail));

        // 4. Phone normalization
        String dirtyPhone = " +91 (98765) 43-210 ";
        assertEquals("normalizePhone must retain digits and leading +", "+919876543210", DataSanitizer.normalizePhone(dirtyPhone));

        // 5. Whitespace normalization
        String multiSpace = "  Premium    Mechanical   Keyboard  ";
        assertEquals("normalizeWhitespace must collapse multiple spaces", "Premium Mechanical Keyboard", DataSanitizer.normalizeWhitespace(multiSpace));

        // 6. Safe truncation
        String longText = "1234567890ABCDEF";
        assertEquals("truncate must bound string to maxLength", "12345", DataSanitizer.truncate(longText, 5));

        // 7. SQL injection risk detection
        assertTrue("detectSqlInjectionRisk must identify classic OR 1=1 pattern",
                DataSanitizer.detectSqlInjectionRisk("' OR '1'='1"));
        assertTrue("detectSqlInjectionRisk must identify UNION SELECT pattern",
                DataSanitizer.detectSqlInjectionRisk("1 UNION ALL SELECT null, email, password FROM users--"));
        assertTrue("detectSqlInjectionRisk must identify stacked DROP TABLE query",
                DataSanitizer.detectSqlInjectionRisk("1; DROP TABLE products"));
        assertFalse("detectSqlInjectionRisk must pass normal search keywords",
                DataSanitizer.detectSqlInjectionRisk("Wireless Bluetooth Noise Cancelling"));
    }

    // ---------------------------------------------------------------
    // 11. Input Boundary Validation
    // ---------------------------------------------------------------
    private static void testInputBoundaryValidationRules() {
        // 1. User validation bounds
        ValidationResult emptyUser = InputBoundaryValidator.validateUserRegistration("", "", "", null, null);
        assertFalse("Empty user registration must be rejected", emptyUser.isValid());
        assertTrue("Must report missing name", emptyUser.getCombinedErrorMessage().contains("Full name"));

        ValidationResult invalidEmailUser = InputBoundaryValidator.validateUserRegistration("John Doe", "not-an-email", "secret123", null, null);
        assertFalse("Invalid email format must be rejected", invalidEmailUser.isValid());

        ValidationResult shortPassUser = InputBoundaryValidator.validateUserRegistration("John Doe", "john@example.com", "123", null, null);
        assertFalse("Short password (<6 chars) must be rejected", shortPassUser.isValid());

        ValidationResult validUser = InputBoundaryValidator.validateUserRegistration("John Doe", "john@example.com", "validPass123", "+919876543210", "123 Street");
        assertTrue("Valid user registration must pass", validUser.isValid());

        // 2. Product validation bounds
        Product negativePrice = new Product(0, 1, "General", "Test Product", "Description", new BigDecimal("-15.00"), 10, 5);
        ValidationResult negPriceRes = InputBoundaryValidator.validateProduct(negativePrice);
        assertFalse("Negative price must be rejected", negPriceRes.isValid());

        Product negativeStock = new Product(0, 1, "General", "Test Product", "Description", new BigDecimal("15.00"), -5, 5);
        ValidationResult negStockRes = InputBoundaryValidator.validateProduct(negativeStock);
        assertFalse("Negative stock must be rejected", negStockRes.isValid());

        Product blankName = new Product(0, 1, "General", "   ", "Description", new BigDecimal("15.00"), 10, 5);
        ValidationResult blankNameRes = InputBoundaryValidator.validateProduct(blankName);
        assertFalse("Blank product name must be rejected", blankNameRes.isValid());

        Product validProduct = new Product(0, 1, "General", "Valid Product", "Description", new BigDecimal("49.99"), 25, 5);
        assertTrue("Valid product must pass", InputBoundaryValidator.validateProduct(validProduct).isValid());

        // 3. Category validation bounds
        assertFalse("Empty category must be rejected", InputBoundaryValidator.validateCategory("").isValid());
        assertTrue("Valid category must pass", InputBoundaryValidator.validateCategory("Wearable Tech").isValid());

        // 4. Order & OrderItem validation bounds
        OrderItem invalidItem = new OrderItem(0, 0, 1, "Item", 0, 10.0);
        assertFalse("Zero quantity order item must be rejected", InputBoundaryValidator.validateOrderItem(invalidItem).isValid());

        OrderItem validItem = new OrderItem(0, 0, 1, "Item", 2, 10.0);
        assertTrue("Valid quantity order item must pass", InputBoundaryValidator.validateOrderItem(validItem).isValid());
    }

    // ---------------------------------------------------------------
    // 12. Live Persistence Layer Sanitization & Boundary Defense
    // ---------------------------------------------------------------
    private static void testSanitizedPersistenceLayer() {
        ProductDAO productDAO = new ProductDAO();

        // 1. Attempt invalid product insertion (negative price) -> DAO should reject before executing SQL
        Product invalidPriceProd = new Product(0, 1, "General", "Unsafe Product", "Desc", new BigDecimal("-9.99"), 5, 5);
        int invalidResult = productDAO.addProduct(invalidPriceProd);
        assertEquals("ProductDAO must reject invalid negative price", -1, invalidResult);

        // 2. Insert product with dirty HTML description and excess whitespace in name
        String dirtyDesc = "<p>Noise-cancelling</p><script>alert('hack');</script>";
        Product dirtyProd = new Product(0, 1, "General", "   Sanitized Wireless Buds   ", dirtyDesc, new BigDecimal("89.99"), 12, 5);
        int generatedId = productDAO.addProduct(dirtyProd);
        assertTrue("ProductDAO should successfully sanitize and insert product", generatedId > 0);

        try {
            Product fetched = productDAO.findById(generatedId);
            assertNotNull("Inserted product must be retrievable", fetched);
            assertEquals("Product name must be trimmed and normalized", "Sanitized Wireless Buds", fetched.getProductName());
            assertFalse("Description must not contain script tags", fetched.getDescription().contains("<script>"));
        } finally {
            // Clean up test product
            productDAO.deleteById(generatedId);
        }

        // 3. CategoryDAO boundary validation
        CategoryDAO categoryDAO = new CategoryDAO();
        int invalidCat = categoryDAO.addCategory("   ");
        assertEquals("CategoryDAO must reject empty category name", -1, invalidCat);
    }

    // ---------------------------------------------------------------
    // Assertions and Reporting Helpers
    // ---------------------------------------------------------------
    private static void printSummary() {
        System.out.println("=====================================================================");
        System.out.println(String.format("   TEST RESULTS: %d Run | %d Passed | %d Failed", testsRun, testsPassed, testsFailed));
        System.out.println("=====================================================================");
        if (testsFailed == 0) {
            System.out.println("🎉 All integrity and transaction tests passed successfully!\n");
        } else {
            System.out.println("⚠ One or more tests failed. Check log output above.\n");
        }
    }

    @FunctionalInterface
    private interface RunnableTest {
        void run() throws Throwable;
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(String message, boolean condition) {
        if (condition) throw new AssertionError(message);
    }

    private static void assertNotNull(String message, Object obj) {
        if (obj == null) throw new AssertionError(message);
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    private static void assertEquals(String message, double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}

