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
  * Product Discovery: Live keyword search, dynamic category filtering, and Product Details Modal (`ProductDetailsDialog`)
  * Interactive Shopping Cart with dynamic badge, quantity controls, cart persistence (`CartStore` & `CartDAO`), and checkout confirmation
  * Simulated Payment Gateway & Settlement Integration (`PaymentStore` & `PaymentDAO`):
    * Multi-channel checkout supporting Cash on Delivery (COD), UPI (Simulated), Credit/Debit Card, and Net Banking
    * Automated settlement lifecycle management: immediate `COMPLETED` for online channels, `PENDING` for COD, auto-settlement on `DELIVERED`, and automatic refund marking (`REFUNDED`) on order cancellation
    * Unique transaction reference tracking (`TXN-...`) with financial audit persistence
  * Automated Inventory Stock Synchronization: Order checkout automatically reserves inventory (`ProductStore.adjustStock`), and order cancellations automatically replenish stock
  * Customer Profile Management (View, Edit Contact Info, Change Password Dialogs synced with MySQL, and Lifetime Purchase Summary)
  * Customer Order History Screen with Status Filtering & Order Cancellation
  * Order Details & Invoice Modal with Official Receipt Export (`OrderDetailsDialog`): Real-time payment settlement badge, transaction reference ID, and formatted text invoice export
  * Admin Product Management: Add new product with validation, Edit & Restock inventory dialog (`EditProductDialog`) with quick restock actions, and product deletion
  * Admin Low Stock Alerts: Real-time low-stock filtering and colored inventory warning badges (`[LOW STOCK]`, `[OUT OF STOCK]`)
  * Admin Order Fulfillment Management: Inspect order details, filter by status, and advance order status (`PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED`) with automated payment settlement
  * Admin User Management (`createAdminUsersPanel`): Live user directory, real-time KPI metrics, search by name/email, role filtering, User Dossier Modal (`UserDetailsDialog` with Customer Lifetime Value calculation), and account deactivation with self-deletion protection
  * Admin Category Management (`CategoryManagementDialog` & `CategoryStore`): Interactive category manager dialog allowing adding categories, renaming, deleting with product safety check, and reviewing product counts
  * Executive Analytics Command Center (`ReportStore` & `ReportDAO`):
    * 6-Metric KPI Deck: Gross Sales Revenue, Settled Collections, Total Orders, Average Order Value (AOV), Active Customers, Low-Stock Exposure
    * Top-Selling Products Velocity Leaderboard (`JTable` with ranking, volume, and revenue)
    * Multi-Tab Analytics Breakdown: Order Lifecycle Distribution, Category Revenue Breakdown, Customer Lifetime Value (CLV) Leaderboard, and Payment Channel Distribution
    * One-click Executive Performance Report text export (`qveto_executive_report.txt`)
  * Real-Time Database Status Indicator (`DBStatusUtil`): Visual connectivity pill (`● MySQL Active` vs `○ Standalone Demo`) across login and dashboard headers
  * Centralized Store Architecture (`UserStore`, `ProductStore`, `OrderStore`, `CategoryStore`, `CartStore`, `ReportStore`, `PaymentStore`) coordinating between JDBC DAOs and resilient in-memory fallback state
  * Centralized Form Validation Utility (`ValidationUtil`)
* **Member 3:** Catalog, Cart & Order Placement Services
* **Member 4:** Inventory Consistency, Invoices & Sales Reporting

---

### Tech Stack

* **Language:** Java (JDK 17+)
* **GUI Toolkit:** Java Swing (`javax.swing`, `java.awt`)
* **Build System:** Apache Maven
* **Database:** MySQL via JDBC

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
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/dao/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java

# Run the application
java -cp bin com.qveto.ecommerce.Main
```

---

### Demo Accounts for Review & Testing

| Role | Username / Email | Password | Features Accessible |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@qveto.com` | `pass123` | Product catalog with live search/filter, product details modal, cart, checkout with live inventory deduction, order history, invoice text export, cancel orders with stock replenishment, edit profile, change password |
| **Admin** | `admin` | `admin123` | Inventory catalog management (add, edit, restock, delete products, low-stock filter), category management dialog, executive analytics dashboard, customer order fulfillment management (update status, view invoices), user account management (view all users, search/filter, view dossier, deactivate accounts), sales KPI overview |

*(You can also use the **Register** tab to create a new customer account with full input validation and duplicate-email detection).*
