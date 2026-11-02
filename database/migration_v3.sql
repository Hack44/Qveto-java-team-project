-- =====================================================================
-- Qveto E-Commerce System - Database Migration v3
-- Member 1: Database Schema & Migrations
-- Description: Analytics reporting views, customer order summaries,
--              inventory velocity metrics, and payment audit views.
-- =====================================================================

USE ecommerce_db;

-- ---------------------------------------------------------------------
-- 1. View: view_sales_summary
-- Executive KPI summary covering gross revenue, volume, and order health
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_sales_summary AS
SELECT 
    COUNT(order_id) AS total_orders,
    COALESCE(SUM(CASE WHEN order_status != 'CANCELLED' THEN total_amount ELSE 0 END), 0.00) AS gross_revenue,
    COALESCE(AVG(CASE WHEN order_status != 'CANCELLED' THEN total_amount ELSE NULL END), 0.00) AS average_order_value,
    SUM(CASE WHEN order_status = 'DELIVERED' THEN 1 ELSE 0 END) AS delivered_orders,
    SUM(CASE WHEN order_status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed_orders,
    SUM(CASE WHEN order_status = 'SHIPPED' THEN 1 ELSE 0 END) AS shipped_orders,
    SUM(CASE WHEN order_status = 'PENDING' THEN 1 ELSE 0 END) AS pending_orders,
    SUM(CASE WHEN order_status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_orders
FROM orders;

-- ---------------------------------------------------------------------
-- 2. View: view_top_selling_products
-- Ranks catalog items by sales volume and generated revenue
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_top_selling_products AS
SELECT 
    p.product_id,
    p.product_name,
    COALESCE(c.category_name, 'Uncategorized') AS category_name,
    p.price,
    p.stock_quantity AS current_stock,
    COALESCE(SUM(oi.quantity), 0) AS units_sold,
    COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS total_revenue
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id AND o.order_status != 'CANCELLED'
GROUP BY p.product_id, p.product_name, c.category_name, p.price, p.stock_quantity
ORDER BY units_sold DESC, total_revenue DESC;

-- ---------------------------------------------------------------------
-- 3. View: view_low_stock_alerts
-- Real-time inventory risk monitoring identifying reorder thresholds
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_low_stock_alerts AS
SELECT 
    p.product_id,
    p.product_name,
    COALESCE(c.category_name, 'Uncategorized') AS category_name,
    p.stock_quantity,
    p.low_stock_threshold,
    CASE 
        WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN p.stock_quantity <= p.low_stock_threshold THEN 'LOW_STOCK'
        ELSE 'OPTIMAL'
    END AS stock_status,
    GREATEST(p.low_stock_threshold - p.stock_quantity, 0) AS units_below_threshold
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.stock_quantity <= p.low_stock_threshold
ORDER BY p.stock_quantity ASC, p.product_name ASC;

-- ---------------------------------------------------------------------
-- 4. View: view_category_sales_summary
-- Category revenue distribution, catalog breadth, and volume contribution
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_category_sales_summary AS
SELECT 
    c.category_id,
    c.category_name,
    COUNT(DISTINCT p.product_id) AS total_products,
    COALESCE(SUM(CASE WHEN o.order_status != 'CANCELLED' THEN oi.quantity ELSE 0 END), 0) AS total_units_sold,
    COALESCE(SUM(CASE WHEN o.order_status != 'CANCELLED' THEN oi.quantity * oi.unit_price ELSE 0.00 END), 0.00) AS total_category_revenue
FROM categories c
LEFT JOIN products p ON c.category_id = p.category_id
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id
GROUP BY c.category_id, c.category_name
ORDER BY total_category_revenue DESC;

-- ---------------------------------------------------------------------
-- 5. View: view_customer_order_history
-- Lifetime value, order frequency, and account activity
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_customer_order_history AS
SELECT 
    u.user_id,
    u.full_name,
    u.email,
    u.role,
    COUNT(o.order_id) AS lifetime_orders,
    COALESCE(SUM(CASE WHEN o.order_status != 'CANCELLED' THEN o.total_amount ELSE 0.00 END), 0.00) AS lifetime_spent,
    MAX(o.created_at) AS last_order_timestamp
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
WHERE u.role = 'CUSTOMER'
GROUP BY u.user_id, u.full_name, u.email, u.role
ORDER BY lifetime_spent DESC;

-- ---------------------------------------------------------------------
-- 6. View: view_payment_method_distribution
-- Settlement channel volumes and completion rates
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_payment_method_distribution AS
SELECT 
    payment_method,
    payment_status,
    COUNT(payment_id) AS transaction_count,
    COALESCE(SUM(amount), 0.00) AS settled_amount
FROM payments
GROUP BY payment_method, payment_status
ORDER BY settled_amount DESC;
