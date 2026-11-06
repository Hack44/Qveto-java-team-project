package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enterprise Input Boundary Validator for Database Entities.
 *
 * Enforces strict integrity rules, value range bounds, and database column constraints
 * prior to executing JDBC persistence operations:
 * - VARCHAR column length limits matching database schema definitions.
 * - Non-null and non-blank mandatory field constraints.
 * - Numerical boundary ranges (non-negative currency, valid quantity ranges, stock limits).
 * - Format validation (RFC-compliant email, E.164 phone, valid enum states).
 * - SQL injection pattern detection warnings.
 *
 * Member 1 — Persistence, Data Sanitization & Validation layer.
 */
public class InputBoundaryValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,24}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{7,18}$"
    );

    private static final Set<String> VALID_ORDER_STATUSES = new HashSet<>(Arrays.asList(
            "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"
    ));

    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList(
            "CUSTOMER", "ADMIN"
    ));

    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000.00");
    private static final int MAX_STOCK = 1000000;
    private static final int MAX_LOW_STOCK_THRESHOLD = 10000;
    private static final int MAX_ORDER_ITEM_QUANTITY = 10000;

    private InputBoundaryValidator() {
        // Utility class
    }

    // ---------------------------------------------------------------
    // User & Authentication Validation
    // ---------------------------------------------------------------

    /**
     * Validates user registration fields against database constraints.
     */
    public static ValidationResult validateUserRegistration(String fullName, String email, String password,
                                                            String phone, String address) {
        ValidationResult result = new ValidationResult();

        // 1. Full Name bounds: VARCHAR(100) NOT NULL
        if (fullName == null || fullName.trim().isEmpty()) {
            result.addError("fullName", "Full name is required.");
        } else {
            String cleanName = fullName.trim();
            if (cleanName.length() < 2) {
                result.addError("fullName", "Full name must be at least 2 characters.");
            } else if (cleanName.length() > 100) {
                result.addError("fullName", "Full name exceeds maximum length of 100 characters.");
            }
        }

        // 2. Email bounds: VARCHAR(150) NOT NULL UNIQUE
        if (email == null || email.trim().isEmpty()) {
            result.addError("email", "Email address is required.");
        } else {
            String cleanEmail = email.trim();
            if (cleanEmail.length() > 150) {
                result.addError("email", "Email address exceeds maximum length of 150 characters.");
            } else if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
                result.addError("email", "Email address format is invalid.");
            }
        }

        // 3. Password bounds: min 6, max 100 (prior to hashing)
        if (password == null || password.isEmpty()) {
            result.addError("password", "Password is required.");
        } else if (password.length() < 6) {
            result.addError("password", "Password must be at least 6 characters.");
        } else if (password.length() > 100) {
            result.addError("password", "Password exceeds maximum length of 100 characters.");
        }

        // 4. Phone bounds: VARCHAR(20) DEFAULT NULL (optional)
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = DataSanitizer.normalizePhone(phone);
            if (cleanPhone.length() < 7 || cleanPhone.length() > 20) {
                result.addError("phone", "Phone number must be between 7 and 20 digits.");
            } else if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                result.addError("phone", "Phone number contains invalid characters.");
            }
        }

        // 5. Address bounds: VARCHAR(255) DEFAULT NULL (optional)
        if (address != null && address.trim().length() > 255) {
            result.addError("address", "Address exceeds maximum length of 255 characters.");
        }

        return result;
    }

    // ---------------------------------------------------------------
    // Category Validation
    // ---------------------------------------------------------------

    /**
     * Validates category name against categories table constraints: VARCHAR(100) NOT NULL UNIQUE.
     */
    public static ValidationResult validateCategory(String categoryName) {
        ValidationResult result = new ValidationResult();

        if (categoryName == null || categoryName.trim().isEmpty()) {
            result.addError("categoryName", "Category name cannot be empty.");
            return result;
        }

        String clean = categoryName.trim();
        if (clean.length() < 2) {
            result.addError("categoryName", "Category name must be at least 2 characters.");
        } else if (clean.length() > 100) {
            result.addError("categoryName", "Category name exceeds maximum length of 100 characters.");
        }

        if (DataSanitizer.detectSqlInjectionRisk(clean)) {
            result.addError("categoryName", "Category name contains prohibited characters or patterns.");
        }

        return result;
    }

    // ---------------------------------------------------------------
    // Product Validation
    // ---------------------------------------------------------------

    /**
     * Validates product entity attributes against products table constraints and business bounds.
     */
    public static ValidationResult validateProduct(Product product) {
        ValidationResult result = new ValidationResult();

        if (product == null) {
            result.addError("product", "Product cannot be null.");
            return result;
        }

        // 1. Product Name: VARCHAR(150) NOT NULL
        String name = product.getProductName();
        if (name == null || name.trim().isEmpty()) {
            result.addError("productName", "Product name cannot be empty.");
        } else {
            String cleanName = name.trim();
            if (cleanName.length() < 2) {
                result.addError("productName", "Product name must be at least 2 characters.");
            } else if (cleanName.length() > 150) {
                result.addError("productName", "Product name exceeds maximum length of 150 characters.");
            }
        }

        // 2. Category ID: INT NOT NULL
        if (product.getCategoryId() <= 0) {
            result.addError("categoryId", "Valid category ID must be specified.");
        }

        // 3. Price: DECIMAL(10,2) NOT NULL CHECK (price >= 0)
        BigDecimal price = product.getPrice();
        if (price == null) {
            result.addError("price", "Price cannot be null.");
        } else if (price.compareTo(BigDecimal.ZERO) < 0) {
            result.addError("price", "Price cannot be negative.");
        } else if (price.compareTo(MAX_PRICE) > 0) {
            result.addError("price", "Price exceeds maximum allowed limit of $" + MAX_PRICE + ".");
        }

        // 4. Stock Quantity: INT NOT NULL CHECK (stock_quantity >= 0)
        int stock = product.getStockQuantity();
        if (stock < 0) {
            result.addError("stockQuantity", "Stock quantity cannot be negative.");
        } else if (stock > MAX_STOCK) {
            result.addError("stockQuantity", "Stock quantity exceeds maximum limit of " + MAX_STOCK + ".");
        }

        // 5. Low Stock Threshold: INT NOT NULL CHECK (low_stock_threshold >= 0)
        int threshold = product.getLowStockThreshold();
        if (threshold < 0) {
            result.addError("lowStockThreshold", "Low stock threshold cannot be negative.");
        } else if (threshold > MAX_LOW_STOCK_THRESHOLD) {
            result.addError("lowStockThreshold", "Threshold exceeds maximum limit of " + MAX_LOW_STOCK_THRESHOLD + ".");
        }

        // 6. Description: TEXT (max 2000 chars)
        String desc = product.getDescription();
        if (desc != null && desc.length() > 2000) {
            result.addError("description", "Product description exceeds maximum limit of 2,000 characters.");
        }

        return result;
    }

    // ---------------------------------------------------------------
    // Order & Line Item Validation
    // ---------------------------------------------------------------

    /**
     * Validates complete order structure and items prior to transactional checkout.
     */
    public static ValidationResult validateOrder(Order order) {
        ValidationResult result = new ValidationResult();

        if (order == null) {
            result.addError("order", "Order cannot be null.");
            return result;
        }

        // 1. User ID: INT NOT NULL
        if (order.getUserId() <= 0) {
            result.addError("userId", "Order must be linked to a valid customer user ID.");
        }

        // 2. Total Amount: DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0)
        if (order.getTotalAmount() < 0) {
            result.addError("totalAmount", "Total order amount cannot be negative.");
        }

        // 3. Shipping Address: VARCHAR(255)
        String address = order.getShippingAddress();
        if (address == null || address.trim().isEmpty()) {
            result.addError("shippingAddress", "Shipping address is required.");
        } else if (address.trim().length() > 255) {
            result.addError("shippingAddress", "Shipping address exceeds maximum length of 255 characters.");
        }

        // 4. Order Status: ENUM
        String status = order.getOrderStatus();
        if (status != null && !VALID_ORDER_STATUSES.contains(status.toUpperCase())) {
            result.addError("orderStatus", "Invalid order status '" + status + "'.");
        }

        // 5. Line items: Must contain at least 1 item
        if (order.getItems() == null || order.getItems().isEmpty()) {
            result.addError("items", "Order must contain at least one line item.");
        } else {
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                ValidationResult itemResult = validateOrderItem(item);
                if (!itemResult.isValid()) {
                    result.addError("items[" + i + "]", itemResult.getCombinedErrorMessage());
                }
            }
        }

        return result;
    }

    /**
     * Validates individual order item line attributes.
     */
    public static ValidationResult validateOrderItem(OrderItem item) {
        ValidationResult result = new ValidationResult();

        if (item == null) {
            result.addError("orderItem", "Order item cannot be null.");
            return result;
        }

        if (item.getProductId() <= 0) {
            result.addError("productId", "Order item must reference a valid product ID.");
        }

        if (item.getQuantity() <= 0) {
            result.addError("quantity", "Quantity must be greater than zero.");
        } else if (item.getQuantity() > MAX_ORDER_ITEM_QUANTITY) {
            result.addError("quantity", "Quantity exceeds maximum allowable order limit of " + MAX_ORDER_ITEM_QUANTITY + ".");
        }

        if (item.getUnitPriceAsBigDecimal() == null || item.getUnitPriceAsBigDecimal().compareTo(BigDecimal.ZERO) < 0) {
            result.addError("unitPrice", "Unit price cannot be negative.");
        }

        return result;
    }
}

