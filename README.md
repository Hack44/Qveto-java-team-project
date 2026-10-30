# Qveto-java-team-project
Collaborative Java development project for the Qveto Technology internship, focused on applying Java programming, Object-Oriented Programming (OOP), problem-solving, and software development best practices through teamwork.

## Project 1: E-commerce & Inventory Management System

A desktop business application built with **Java Swing** and **MySQL** that enables administrators to manage products, categories, stock, users, and orders while customers browse catalogs, manage their shopping carts, place orders, and track purchases.

---

### Team Roles & Modular Architecture

* **Member 1 (Database & Persistence Layer):**
  * Canonical MySQL Relational Schema (`database/schema.sql`) covering 7 core tables with foreign keys and check constraints
  * Database Migration v2 (`database/migration_v2.sql`) with performance indexes (`idx_products_category`, `idx_orders_status`, etc.) and `updated_at` audit triggers
  * Synthetic Seed Data (`database/seed.sql`) containing demo admin, customers, catalog items, and sample transaction orders
  * Singleton JDBC Connection Factory (`DBConnection.java`) with automated properties loading and resilient fallback
  * Model Entity Layer: `User`, `Product`, `Category`, `Order`, `OrderItem`, and `CartItem` with `BigDecimal` financial precision
  * Data Access Object (DAO) Persistence Suite:
    * `UserDAO`: Authentication with SHA-256 password hashing (`PasswordUtil`), registration, contact updates, and password changes
    * `CategoryDAO`: Dedicated category lifecycle management (full CRUD, product count aggregations, duplicate name checks, and FK safe-delete protection)
    * `ProductDAO`: Catalog CRUD, keyword search, category filtering, stock adjustments, and low-stock queries
    * `OrderDAO`: Atomic transactional checkout (order header + line items in a single commit), status transitions, and history
    * `CartDAO`: Full cart persistence (`cart_items` table) with upsert, quantity management, item removal, and total value calculation
    * `ReportDAO`: Core Qveto KPI reporting metrics (sales revenue, order status distribution, top-selling products, category breakdown)
  * Utility & Security: `PasswordUtil` providing SHA-256 hashing compatible with MySQL `SHA2()`
* **Member 2 (UI, Presentation & Store Management):**
  * Java Swing Authentication Frames (`LoginFrame`) with password visibility toggle (Show/Hide)
  * Customer & Admin Login workflows with input validation and JDBC `UserDAO` authentication
  * Customer Registration with form validation, email uniqueness checks, and database persistence
  * Application Dashboard Shell & Navigation (`DashboardFrame`)
  * Product Discovery: Live keyword search, category filtering, and Product Details Modal (`ProductDetailsDialog`)
  * Interactive Shopping Cart with dynamic badge, quantity controls, and checkout confirmation
  * Automated Inventory Stock Synchronization: Order checkout automatically reserves inventory (`ProductStore.adjustStock`), and order cancellations automatically replenish stock
  * Customer Profile Management (View, Edit Contact Info, Change Password Dialogs synced with MySQL)
  * Customer Order History Screen with Status Filtering & Order Cancellation
  * Order Details & Invoice Modal with Text Receipt Export (`OrderDetailsDialog`)
  * Admin Product Management: Add new product with validation, Edit & Restock inventory dialog (`EditProductDialog`) with quick restock actions, and product deletion
  * Admin Low Stock Alerts: Real-time low-stock filtering and colored inventory warning badges (`[LOW STOCK]`, `[OUT OF STOCK]`)
  * Admin Order Fulfillment Management: Inspect order details, filter by status, and advance order status (`PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED`)
  * Admin User Management (`createAdminUsersPanel`): Live user directory, real-time KPI metrics, search by name/email, role filtering, User Dossier Modal (`UserDetailsDialog`), and account deactivation with self-deletion protection
  * Real-Time Database Status Indicator (`DBStatusUtil`): Visual connectivity pill (`● MySQL Active` vs `○ Standalone Demo`) across login and dashboard headers
  * Centralized Store Architecture (`UserStore`, `ProductStore`, `OrderStore`) coordinating between JDBC DAOs (`UserDAO`, `ProductDAO`, `OrderDAO`) and resilient in-memory fallback state
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
| **Admin** | `admin` | `admin123` | Inventory catalog management (add, edit, restock, delete products, low-stock filter), customer order fulfillment management (update status, view invoices), user account management (view all users, search/filter, view dossier, deactivate accounts), sales KPI overview |

*(You can also use the **Register** tab to create a new customer account with full input validation and duplicate-email detection).*
