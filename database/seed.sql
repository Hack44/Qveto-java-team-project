-- =====================================================================
-- Qveto E-Commerce System - Synthetic Seed Data
-- Member 1: Database Schema, Seed & Persistence Layer
-- Description: Synthetic demo accounts, product categories, catalog items,
--              and sample order history for testing and evaluation.
-- =====================================================================

USE ecommerce_db;

-- ---------------------------------------------------------------------
-- 1. Seed Categories
-- ---------------------------------------------------------------------
INSERT INTO categories (category_id, category_name) VALUES
    (1, 'Electronics'),
    (2, 'Clothing'),
    (3, 'Books'),
    (4, 'Home & Kitchen'),
    (5, 'Fitness & Sports')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- ---------------------------------------------------------------------
-- 2. Seed Users (Admin & Demo Customers)
-- ---------------------------------------------------------------------
-- Passwords:
-- admin@qveto.com -> 'admin123'
-- customer@qveto.com -> 'pass123'
-- priya.patel@example.com -> 'pass123'
-- amit.kumar@example.com -> 'pass123'
-- sneha.reddy@example.com -> 'pass123'
INSERT INTO users (user_id, full_name, email, password_hash, role, phone, address) VALUES
    (1, 'Rahul Sharma', 'customer@qveto.com', 'pass123', 'CUSTOMER', '+91 98765 43210', '124 Tech Park Road, Bengaluru, Karnataka, India'),
    (2, 'Priya Patel', 'priya.patel@example.com', 'pass123', 'CUSTOMER', '+91 98111 22334', '45 Marine Drive, Mumbai, Maharashtra, India'),
    (3, 'Amit Kumar', 'amit.kumar@example.com', 'pass123', 'CUSTOMER', '+91 97222 33445', '78 Connaught Place, New Delhi, Delhi, India'),
    (4, 'Sneha Reddy', 'sneha.reddy@example.com', 'pass123', 'CUSTOMER', '+91 99333 44556', '12 Jubilee Hills, Hyderabad, Telangana, India'),
    (99, 'System Administrator', 'admin@qveto.com', 'admin123', 'ADMIN', '+91 90000 00000', 'Qveto HQ, Tech Center, Sector 5, India')
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    role = VALUES(role),
    phone = VALUES(phone),
    address = VALUES(address);

-- ---------------------------------------------------------------------
-- 3. Seed Products
-- ---------------------------------------------------------------------
INSERT INTO products (product_id, category_id, product_name, description, price, stock_quantity, low_stock_threshold) VALUES
    (101, 1, 'Wireless Bluetooth Headphones', 'Noise-cancelling over-ear wireless headphones with deep bass and 30h battery life', 59.99, 25, 5),
    (102, 1, 'Mechanical Gaming Keyboard', 'RGB backlit blue switches with mechanical tactile feedback and detachable wrist rest', 79.99, 14, 5),
    (103, 2, 'Classic Cotton T-Shirt', '100% organic combed cotton everyday unisex casual wear', 19.99, 4, 5),
    (104, 3, 'Java Programming: OOP Essentials', 'Comprehensive guide to OOP, Collections, and Design Patterns in modern Java', 39.99, 30, 5),
    (105, 4, 'Stainless Steel Water Bottle', 'Vacuum insulated 750ml double-walled thermal flask, cold for 24h, hot for 12h', 14.99, 40, 10),
    (106, 1, 'USB-C Fast Charging Hub', '6-in-1 multi-port adapter with 4K HDMI, SD card reader, and 65W Power Delivery', 29.99, 3, 5),
    (107, 5, 'Adjustable Resistance Bands Set', '5 stackable exercise bands with door anchor, handles, and ankle straps', 24.99, 18, 5),
    (108, 4, 'Ceramic Pour-Over Coffee Dripper', 'Manual artisan coffee maker with spiral ribs for maximum brew flavor extraction', 21.50, 8, 4)
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    description = VALUES(description),
    price = VALUES(price),
    stock_quantity = VALUES(stock_quantity),
    low_stock_threshold = VALUES(low_stock_threshold);

-- ---------------------------------------------------------------------
-- 4. Seed Initial Orders
-- ---------------------------------------------------------------------
INSERT INTO orders (order_id, user_id, total_amount, order_status, payment_method, shipping_address, created_at) VALUES
    (1001, 1, 139.98, 'DELIVERED', 'CREDIT_CARD', '124 Tech Park Road, Bengaluru, Karnataka, India', '2026-10-24 11:30:00'),
    (1002, 2, 79.99, 'CONFIRMED', 'UPI', '45 Marine Drive, Mumbai, Maharashtra, India', '2026-10-26 14:15:00'),
    (1003, 3, 39.99, 'PENDING', 'COD', '78 Connaught Place, New Delhi, Delhi, India', '2026-10-28 16:45:00')
ON DUPLICATE KEY UPDATE
    total_amount = VALUES(total_amount),
    order_status = VALUES(order_status);

-- ---------------------------------------------------------------------
-- 5. Seed Order Items
-- ---------------------------------------------------------------------
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price) VALUES
    (2001, 1001, 101, 1, 59.99),
    (2002, 1001, 102, 1, 79.99),
    (2003, 1002, 102, 1, 79.99),
    (2004, 1003, 104, 1, 39.99)
ON DUPLICATE KEY UPDATE
    quantity = VALUES(quantity),
    unit_price = VALUES(unit_price);

-- ---------------------------------------------------------------------
-- 6. Seed Payments
-- ---------------------------------------------------------------------
INSERT INTO payments (payment_id, order_id, payment_method, payment_status, amount, created_at) VALUES
    (3001, 1001, 'CREDIT_CARD', 'COMPLETED', 139.98, '2026-10-24 11:32:00'),
    (3002, 1002, 'UPI', 'COMPLETED', 79.99, '2026-10-26 14:16:00'),
    (3003, 1003, 'COD', 'PENDING', 39.99, '2026-10-28 16:45:00')
ON DUPLICATE KEY UPDATE
    payment_status = VALUES(payment_status),
    amount = VALUES(amount);
