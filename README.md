# Qveto-java-team-project
Collaborative Java development project for the Qveto Technology internship, focused on applying Java programming, Object-Oriented Programming (OOP), problem-solving, and software development best practices through teamwork.

## Project 1: E-commerce & Inventory Management System

A desktop business application built with **Java Swing** and **MySQL** that enables administrators to manage products, categories, stock, users, and orders while customers browse catalogs, manage their shopping carts, place orders, and track purchases.

---

### Team Roles & Modular Architecture

* **Member 1 (Database & Persistence Layer):**
  * Canonical MySQL Relational Schema (`database/schema.sql`) covering 7 core tables with foreign keys and check constraints
  * Database Migration v2 (`database/migration_v2.sql`) with query performance indexes (`idx_products_category`, `idx_orders_status`, etc.) and `updated_at` audit triggers
  * Database Migration v3 (`database/migration_v3.sql`) with 6 reporting and analytics views (`view_sales_summary`, `view_top_selling_products`, `view_low_stock_alerts`, `view_category_sales_summary`, `view_customer_order_history`, `view_payment_method_distribution`)
  * Synthetic Seed Data (`database/seed.sql`) containing demo admin, customers, catalog items, and sample transaction orders
  * Singleton JDBC Connection Factory (`DBConnection.java`) with automated properties loading and resilient fallback
  * Model Entity Layer: `User`, `Product`, `Category`, `Order`, `OrderItem`, `CartItem`, and `Payment` with `BigDecimal` financial precision
  * Data Access Object (DAO) Persistence Suite:
    * `UserDAO`: Authentication with SHA-256 password hashing (`PasswordUtil`), registration, contact updates, and password changes
    * `CategoryDAO`: Dedicated category lifecycle management (full CRUD, product count aggregations, duplicate name checks, and FK safe-delete protection)
    * `ProductDAO`: Catalog CRUD, keyword search, category filtering, stock adjustments, and low-stock queries
    * `OrderDAO`: Atomic transactional checkout (order header + line items + payment record in a single commit), status transitions, and history
    * `OrderItemDAO`: Dedicated line item persistence, transactional batch inserts, line-level sales aggregations (units sold, item revenue), and category joined lookups
    * `PaymentDAO`: Order payment persistence, test-mode simulated payment gateway integration, transaction status tracking (`PENDING`, `COMPLETED`, `REFUNDED`), and revenue audit
    * `CartDAO`: Full cart persistence (`cart_items` table) with upsert, quantity management, item removal, and total value calculation
    * `ReportDAO`: Core Qveto KPI reporting metrics, executive sales summary, low-stock reorder alerts, customer lifetime spend leaderboard, and view-backed queries
  * Utility & Security: `PasswordUtil` providing SHA-256 hashing compatible with MySQL `SHA2()`
* **Member 2 (UI, Presentation & Store Management):**
  * Java Swing Authentication Frames (`LoginFrame`) with password visibility toggle (Show/Hide)
  * Customer & Admin Login workflows with input validation and JDBC `UserDAO` authentication
  * Customer Registration with form validation, email uniqueness checks, and database persistence
  * Application Dashboard Shell & Navigation (`DashboardFrame`)
  * Product Discovery: Live keyword search, dynamic category filtering, multi-criteria sorting (Price Low-to-High, Price High-to-Low, Name A-Z, Highest Stock), quick in-stock availability toggle, and Product Details Modal (`ProductDetailsDialog`) with verified buyer 5-star ratings, review counts, and customer feedback tabs
  * Customer Verified Reviews & Rating System (`Review.java`, `ReviewStore.java`, `ProductReviewDialog.java`):
    * Interactive Star Rating Selector (`★`) with headline and comments for customer feedback on delivered purchases
    * Live recalculation of average product star ratings and review counts
    * Verified Buyer Badging (`✓ Verified Buyer`) on customer reviews
  * Interactive Shopping Cart with dynamic badge, quantity controls, cart persistence (`CartStore` & `CartDAO`), and checkout confirmation
  * E-Commerce Promotional Coupon & Discount Engine (`CouponStore.java`):
    * Centralized store managing promotional discount vouchers with percentage/flat-rate rules, minimum cart thresholds, and discount caps (e.g., `QVETO10`, `SAVE20`, `FLAT15`, `WELCOME`, `MEGA50`)
    * Dynamic Cart Promo Bar: Instant code validation, live discount feedback, and cart net total recalculation
    * Order & Checkout Integration: Applies promo savings to checkout total and records promotional discounts on customer invoices and official receipts
  * Simulated Payment Gateway & Settlement Integration (`PaymentStore` & `PaymentDAO`):
    * Multi-channel checkout supporting Cash on Delivery (COD), UPI (Simulated), Credit/Debit Card, and Net Banking
    * Automated settlement lifecycle management: immediate `COMPLETED` for online channels, `PENDING` for COD, auto-settlement on `DELIVERED`, and automatic refund marking (`REFUNDED`) on order cancellation
    * Unique transaction reference tracking (`TXN-...`) with financial audit persistence
  * Automated Inventory Stock Synchronization: Order checkout automatically reserves inventory (`ProductStore.adjustStock`), and order cancellations automatically replenish stock
  * Customer Profile Management (View, Edit Contact Info, Change Password Dialogs synced with MySQL, and Lifetime Purchase Summary)
  * Customer Order History Screen with Status Filtering, Order Cancellation, and 1-Click `"★ Rate & Review"` for delivered items
  * Order Details & Branded PDF / Text Invoice Export Suite (`OrderDetailsDialog` & `PdfInvoiceGenerator.java`):
    * Interactive **Order Fulfillment Tracking Stepper**: Visual milestone progress bar (`[1. Placed] -> [2. Confirmed] -> [3. Shipped] -> [4. Delivered]`) indicating real-time order lifecycle stage, completed step checkmarks, active tracking status, and voided cancellation alert styling
    * **Branded Adobe PDF 1.4 Vector Invoice Generator (`PdfInvoiceGenerator.java`)**: Pure Java vector PDF generator producing professional, publication-quality receipts (`Invoice_ORD-XXXX.pdf`) with corporate palette, customer billing block, tabular line items, tax breakdowns, and payment audit stamps without native dependency bloat
    * 1-Click Formatted Text Receipt Export (`.txt`) and PDF Download (`.pdf`)
  * Admin Product Management: Add new product with validation, Edit & Restock inventory dialog (`EditProductDialog`) with quick restock actions, and product deletion
  * Admin Low Stock Alerts & Inventory Procurement Hub (`LowStockProcurementDialog` & `ReportStore`):
    * Dedicated modal dialog for inspecting SKUs at or below low-stock threshold utilizing `view_low_stock_alerts`
    * Real-time deficit metrics: Total Low-Stock Exposure, Out-of-Stock SKUs, and Total Unit Deficits
    * 1-Click single-SKU quick restock with configurable quantity spinner
    * ⚡ Batch Procurement replenishment: Rapidly restores all deficit SKUs to safe inventory levels with a single confirmation
    * Integrated launch points from both Admin Inventory Catalog toolbar (`📦 Restock Hub`) and Executive Reports KPI deck (`📦 Restock Procurement`)
  * Admin Order Fulfillment Management: Inspect order details, live keyword search (by Order #, Customer ID, or Address), filter by status, and advance order status (`PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED`) with automated payment settlement
  * Corporate CSV Audit & Reporting Export Suite (`ProductStore.exportCatalogCsv` & `OrderStore.exportOrdersCsv`):
    * 1-Click RFC 4180 compliant CSV export for complete inventory catalog (`qveto_inventory_catalog.csv`)
    * 1-Click CSV export for master orders audit register (`qveto_orders_master.csv`) with payment modes and transaction references
  * Admin User Management (`createAdminUsersPanel`): Live user directory, real-time KPI metrics, search by name/email, role filtering, User Dossier Modal (`UserDetailsDialog` with Customer Lifetime Value calculation), and account deactivation with self-deletion protection
  * Admin Category Management (`CategoryManagementDialog` & `CategoryStore`): Interactive category manager dialog allowing adding categories, renaming, deleting with product safety check, and reviewing product counts
  * Executive Analytics Command Center with Date-Range Filtering (`ReportStore` & `ReportDAO`):
    * 6-Metric KPI Deck: Gross Sales Revenue, Settled Collections, Total Orders, Average Order Value (AOV), Active Customers, Low-Stock Exposure
    * **Dynamic Date-Range Analytics Filtering**: Preset time windows (`All Time`, `Last 7 Days`, `Last 30 Days`, `This Month`) and custom `From`/`To` calendar date filtering dynamically updating revenue KPIs and order distribution
    * Top-Selling Products Velocity Leaderboard (`JTable` with ranking, volume, and revenue)
    * Multi-Tab Analytics Breakdown: Order Lifecycle Distribution, Category Revenue Breakdown, Customer Lifetime Value (CLV) Leaderboard, and Payment Channel Distribution
    * One-click Executive Performance Report text export (`qveto_executive_report.txt`)
  * Automated Regression & Integration Test Suite (`StoreWorkflowIntegrationTest.java`): End-to-end integration tests verifying promotional coupon discounts, stock adjustments, order cancellations, CSV exports, date filtering, PDF generation, and customer reviews
  * Real-Time Database Status Indicator (`DBStatusUtil`): Visual connectivity pill (`● MySQL Active` vs `○ Standalone Demo`) across login and dashboard headers
  * Centralized Store Architecture (`UserStore`, `ProductStore`, `OrderStore`, `CategoryStore`, `CartStore`, `ReportStore`, `PaymentStore`, `CouponStore`, `ReviewStore`) coordinating between JDBC DAOs and resilient in-memory fallback state
  * Centralized Form Validation Utility (`ValidationUtil`)
* **Member 3:** Catalog, Cart & Order Placement Services
* **Member 4:** Inventory Consistency, Invoices & Sales Reporting

---

### Tech Stack

* **Language:** Java (JDK 17+)
* **GUI Toolkit:** Java Swing (`javax.swing`, `java.awt`)
* **Build System:** Apache Maven
* **Database:** MySQL via JDBC
* **Document Engine:** Pure Java Adobe PDF 1.4 Vector Generator

---

### How to Run

#### Using the One-Click Script (Windows)
Double-click `run.bat` in the project root to compile and launch the application.

#### Using Maven (Recommended)
```bash
# 1. Compile the application
mvn clean compile

# 2. Run the Swing GUI
mvn exec:java
```

#### Using Standard Java CLI
```bash
# Compile all modules to a bin directory
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/dao/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java src/test/java/com/qveto/ecommerce/*.java

# Run the GUI Application
java -cp bin com.qveto.ecommerce.Main

# Run the Automated Integration Test Suite
java -cp bin com.qveto.ecommerce.StoreWorkflowIntegrationTest
```

---

### Demo Accounts for Review & Testing

| Role | Username / Email | Password | Features Accessible |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@qveto.com` | `pass123` | Product catalog with live search/filter, product details modal with customer reviews, cart, promo coupons, checkout with live inventory deduction, order history, order tracking stepper, PDF & text invoice export, order review submission, cancel orders with stock replenishment, edit profile, change password |
| **Admin** | `admin` | `admin123` | Inventory catalog management (add, edit, restock, delete products, low-stock filter, CSV export), low stock procurement hub, category management dialog, executive analytics dashboard with date-range filtering, customer order fulfillment management (update status, view invoices, master CSV export), user account management (view all users, search/filter, view dossier, deactivate accounts), sales KPI overview |

*(You can also use the **Register** tab to create a new customer account with full input validation and duplicate-email detection).*
