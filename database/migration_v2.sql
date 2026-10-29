-- =====================================================================
-- Qveto E-Commerce System - Database Migration v2
-- Member 1: Database Schema & Migrations
-- Description: Adds query performance indexes, audit timestamps,
--              and schema optimizations for Order and Reporting queries.
-- =====================================================================

USE ecommerce_db;

-- 1. Index on Products category_id for fast category filtering & catalog browsing
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);

-- 2. Index on Products price for sorted price range filters
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- 3. Composite Index on Cart Items for fast user cart lookups
CREATE INDEX IF NOT EXISTS idx_cart_user_product ON cart_items(user_id, product_id);

-- 4. Index on Orders user_id for customer order history lookups
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);

-- 5. Index on Orders order_status for admin status filtering & KPI grouping
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);

-- 6. Index on Order Items order_id for order details dialog joining
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);

-- 7. Index on Order Items product_id for best-seller reporting aggregations
CREATE INDEX IF NOT EXISTS idx_order_items_product ON order_items(product_id);

-- 8. Add updated_at audit column to orders table if not present
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ecommerce_db'
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'updated_at'
);

SET @stmt = IF(@col_exists = 0,
    'ALTER TABLE orders ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;',
    'SELECT "Column updated_at already exists in orders table";'
);

PREPARE add_col_stmt FROM @stmt;
EXECUTE add_col_stmt;
DEALLOCATE PREPARE add_col_stmt;
