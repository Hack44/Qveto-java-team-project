# Qveto E-Commerce & Inventory Management System

**Internship Program:** Qveto Technology Java Internship  
**Project Track:** Project 1 (E-commerce & Inventory Management System)  
**Authors:** Chetan Reddy (`Chetanreddysai`) in collaboration with Harsh Verma (`Hack44`)  
**Submission Status:** Production-Ready Capstone Submission (November 09, 2026)  
**Verification Pass Rate:** 19 of 19 Tests Passed (100%)  

---

## Project Overview

A complete, enterprise-grade desktop business application built with **Java (JDK 17+/21)**, **Java Swing (`javax.swing`, `java.awt`)**, and **MySQL/JDBC**. It delivers an end-to-end e-commerce and retail operations platform connecting customer-facing shopping workflows with administrative inventory procurement, order fulfillment, compliance auditing, and executive analytics.

The system features a **Dual-Engine Architecture**:
* **Enterprise Live Mode:** Connects to MySQL via pre-leased connection pooling (`DBConnectionPool`) and exponential retry policies (`DatabaseRetryPolicy`).
* **Resilient Standalone Demo Mode:** Automatically engages in-memory store caching if MySQL is offline, ensuring 100% feature availability and crash-free reviewer demonstrations.

For the comprehensive project evaluation document, see [`PROJECT_REVIEW_DOSSIER.md`](PROJECT_REVIEW_DOSSIER.md).  
For the complete printable evaluation guide and viva presentation cheatsheet, see [**`PROJECT_REVIEW_GUIDE.pdf`**](PROJECT_REVIEW_GUIDE.pdf).

---

## Complete System Architecture & Enterprise Modules

The project is structured into modular enterprise tiers following Model-View-Controller (MVC) and Data Access Object (DAO) architectural patterns:

### 1. Presentation & Desktop UI Layer
* **Authentication Shell (`LoginFrame`):** Dual-persona login (Customer & Admin) with password visibility toggle and real-time database health pill (`● MySQL Active` vs `○ Standalone Demo`).
* **⚡ 1-Click Fast Login:** Instant demo login buttons for Customer (`customer@qveto.com` / `pass123`) and Admin (`admin` / `admin123`) for presentation convenience.
* **Modern Typography:** High-DPI subpixel font antialiasing enabled (`awt.useSystemAAFontSettings=on`, `swing.aatext=true`).
* **Application Command Center (`DashboardFrame`):** Tabbed interface with real-time badges, header notification drawer, and quick logout.
* **Project System Architecture Dossier (`ProjectOverviewDialog`):** Interactive modal presenting architecture tiers, live diagnostic health cards, and reviewer cheatsheet.

### 2. Product Catalog Discovery & Smart Shopping Cart
* **Catalog Discovery Table:** Live keyword search, dynamic category filtering, multi-criteria sorting (Price Low-to-High, Price High-to-Low, Name A-Z, Highest Stock), and in-stock availability toggle.
* **Product Details Modal (`ProductDetailsDialog`):** Displays high-res product metadata, stock availability, verified customer reviews, and interactive 5-star ratings.
* **Customer Wishlist & Saved Products (`WishlistStore`):** Dedicated **"Wishlist"** Tab allowing customers to curate saved items and monitor price changes, with 1-click `"♡ Save to Wishlist"` and instant `"🛒 Move to Cart"` transfer.
* **Shopping Cart with DB Sync (`CartDAO` & `CartStore`):** Quantity spinner controls, instant subtotal calculation, persistent database synchronization, and cart badge updates.
* **Promotional Coupon & Discount Engine (`CouponStore`):** Centralized rules engine supporting percentage and flat-rate vouchers (`QVETO10`, `SAVE20`, `FLAT15`, `WELCOME`, `MEGA50`) with minimum cart thresholds and dynamic discount calculation.

### 3. Logistics, Invoicing & Order Fulfillment
* **Saved Multi-Address Book (`Address.java`, `AddressBookStore`, `AddressBookDialog`):** Centralized shipping directory supporting `Home`, `Work`, and custom destinations with default address designation.
* **Smart Checkout Selector:** Pre-populates default destination, enables 1-click address switching, and provides a shortcut to add new delivery addresses during checkout.
* **Multi-Channel Payment Gateway (`PaymentStore`):** Supports Cash on Delivery (COD), UPI (Simulated), Credit/Debit Card, and Net Banking with automated settlement tracking (`TXN-...`).
* **Order Fulfillment Stepper (`OrderDetailsDialog`):** Visual tracking milestone bar (`[Placed] -> [Confirmed] -> [Shipped] -> [Delivered]`).
* **Pure Vector Adobe PDF 1.4 Invoice Generator (`PdfInvoiceGenerator`):** Pure Java vector PDF generator producing professional publication-grade invoices (`Invoice_ORD-XXXX.pdf`) without external library dependencies.
* **Live Consignment Shipment Tracking (`OrderTrackingDialog`):** Interactive courier tracking modal accessible from Customer Order History and Admin Orders via `"📍 Track Shipment"`. Features carrier details (BlueDart Express, FedEx Air Logistics), deterministic Airway Bill (AWB) consignment code, and time-stamped transit checkpoint scan cards.
* **Master Orders RFC 4180 CSV Export (`OrderStore.exportOrdersCsv`):** One-click master order register export.

### 4. Post-Delivery Operations & Customer Feedback
* **Customer Returns & Refund Automation (`OrderReturnDialog`, `OrderStore`):** Post-delivery return ticket logging with structured reason categories.
* **Admin 1-Click Return Approval:** Automated refund processing and physical inventory stock replenishment (`OrderStore.approveReturn`).
* **Verified Customer Reviews & 5-Star Ratings (`ProductReviewDialog`, `ReviewStore`):** Interactive rating selector (`★`) for verified purchasers with verified buyer badging (`✓ Verified Buyer`) and live average rating calculations.
* **Store Notification Broadcast Hub (`NotificationCenterDialog`, `NotificationStore`):** Real-time notification drawer with unread counter (`🔔 Alerts (N)`) categorizing deliveries, restocks, vouchers, and returns.

### 5. Inventory Procurement & Compliance Auditing
* **Admin Product Management:** Catalog CRUD, low-stock threshold queries, and inventory catalog CSV export (`ProductStore.exportCatalogCsv`).
* **Admin Low-Stock Alerts & Procurement Hub (`LowStockProcurementDialog`):** Real-time deficit metrics: Total Low-Stock Exposure, Out-of-Stock SKUs, and Total Unit Deficits. Supports 1-click single-SKU restock and ⚡ Batch Procurement replenishment.
* **Executive Analytics Command Center (`ReportStore`, `ReportDAO`):** 6-Metric KPI Deck (Gross Revenue, Settled Collections, Orders, AOV, Active Customers, Low-Stock Exposure) with dynamic date-range filtering (`All Time`, `Last 7 Days`, `Last 30 Days`, `This Month`), velocity leaderboard, category revenue breakdown, and Customer Lifetime Value (CLV) metrics.
* **Enterprise Audit Activity Journal (`AuditLog.java`, `AuditStore.java`, `AuditLogDialog`):** Immutable operational audit trail for compliance, with category filters, keyword search, and RFC 4180 CSV export.
* **Admin User Dossier Modal (`UserDetailsDialog`):** User profile inspection with CLV calculations and account deactivations with self-deletion protection.
* **Category Management (`CategoryManagementDialog`, `CategoryStore`, `CategoryDAO`):** Add, rename, delete categories with product foreign-key dependency validation.

### 6. Database, Persistence & Resilience Layer
* **Canonical MySQL Relational Schema (`database/schema.sql`):** 7 core normalized tables (`users`, `categories`, `products`, `orders`, `order_items`, `payments`, `cart_items`) with foreign keys and check constraints.
* **Database Migrations v2 & v3:** Performance B-tree query indexes, `updated_at` audit triggers, and 6 analytical reporting views (`view_sales_summary`, `view_top_selling_products`, `view_low_stock_alerts`, `view_category_sales_summary`, `view_customer_order_history`, `view_payment_method_distribution`).
* **Singleton JDBC Connection Factory (`DBConnection.java`):** Pre-leased connection pooling (`DBConnectionPool`), automated proxy recycling, and exponential retry backoff (`DatabaseRetryPolicy`).
* **Security & Sanitization:** SHA-256 cryptographic password hashing (`PasswordUtil`), XSS neutralization (`DataSanitizer`), and strict domain constraint validation (`InputBoundaryValidator`).
* **Complete DAO Persistence Suite:** `UserDAO`, `CategoryDAO`, `ProductDAO`, `OrderDAO`, `OrderItemDAO`, `PaymentDAO`, `CartDAO`, `ReportDAO`.

---

## Tech Stack

* **Language:** Java (JDK 17+ / JDK 21)
* **GUI Toolkit:** Java Swing (`javax.swing`, `java.awt`) with high-DPI font smoothing
* **Build System:** Apache Maven (`pom.xml`) and Windows Batch CLI scripts (`run.bat`, `test.bat`)
* **Database:** MySQL 8.x via JDBC (with standalone in-memory fallback)
* **Document Engine:** Pure Java Adobe PDF 1.4 Vector Invoice Generator (zero third-party bloat)

---

## How to Run

### 1. One-Click Batch Launchers (Windows)
* **Launch GUI Application:** Double-click `run.bat` in the project root.
* **Execute Verification Tests:** Double-click `test.bat` in the project root.

### 2. Standard Java CLI
```bash
# 1. Compile all source and test modules
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java \
             src/main/java/com/qveto/ecommerce/dao/*.java \
             src/main/java/com/qveto/ecommerce/util/*.java \
             src/main/java/com/qveto/ecommerce/ui/*.java \
             src/main/java/com/qveto/ecommerce/*.java \
             src/test/java/com/qveto/ecommerce/*.java

# 2. Run the Desktop Application
java -cp "bin;src/main/resources" com.qveto.ecommerce.Main

# 3. Run the Automated Verification Suites
java -cp "bin;src/main/resources" com.qveto.ecommerce.util.DatabaseIntegrityTest
java -cp "bin;src/main/resources" com.qveto.ecommerce.StoreWorkflowIntegrationTest
```

---

## Demo Accounts for Review & Testing

| Role | Username / Email | Password | Features Accessible |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@qveto.com` | `pass123` | Product catalog with live search/filter, product details modal with customer reviews, saved wishlist with 1-click cart transfer, promo coupons, checkout with live inventory deduction, order history, order tracking stepper, PDF & text invoice export, order review submission, return & refund requests, cancel orders with stock replenishment, notification hub, edit profile, change password |
| **Administrator** | `admin` | `admin123` | Inventory catalog management (add, edit, restock, delete products, low-stock filter, CSV export), low stock procurement hub, category management dialog, executive analytics dashboard with date-range filtering, customer order fulfillment management (update status, approve return requests, view invoices, master CSV export), user account management (view all users, search/filter, view dossier, deactivate accounts), store activity notifications, sales KPI overview |

*(Shortcut: Click the **⚡ 1-Click Fast Login** button on either login tab to enter immediately without typing credentials).*

---

## Quality Assurance & Automated Test Results

### **19 Run | 19 Passed | 0 Failed (100% Pass Rate)**

```
=====================================================================
   Qveto E-Commerce System - Database Integrity & Transaction Tests   
=====================================================================
Database Status: Demo / Standalone Mode
---------------------------------------------------------------------
[TEST 1] PasswordUtil SHA-256 Hashing & Verification .................... PASSED
[TEST 2] Model Financial Precision (BigDecimal Arithmetic) ............. PASSED
[TEST 3] Inventory Stock Consistency & Restoration ..................... PASSED
[TEST 4] Category Foreign-Key Protection & Safe Deletion ............... PASSED
[TEST 5] ReportDAO KPI Mathematical Consistency ........................ PASSED
[TEST 6] DatabaseRetryPolicy Transient Error & Exponential Backoff ..... PASSED
[TEST 7] DataSanitizer XSS Neutralization & Normalization .............. PASSED
[TEST 8] InputBoundaryValidator Domain Entity Constraint Rules ......... PASSED
=====================================================================
   TEST RESULTS: 8 Run | 8 Passed | 0 Failed (100%)
=====================================================================

========================================================================
   QVETO E-COMMERCE & INVENTORY MANAGEMENT SYSTEM - INTEGRATION TESTS   
========================================================================
[RUNNING] Coupon Engine Discount Thresholds ............................ PASSED
[RUNNING] Product Catalog & CSV Export ................................. PASSED
[RUNNING] Order Lifecycle & Orders Master CSV .......................... PASSED
[RUNNING] Executive Date-Range Sales Reporting ......................... PASSED
[RUNNING] PDF 1.4 Vector Invoice Binary Generation ..................... PASSED
[RUNNING] Customer Verified Reviews & Star Ratings ..................... PASSED
[RUNNING] Customer Wishlist & Saved Items .............................. PASSED
[RUNNING] Order Return / Refund & Stock Replenishment .................. PASSED
[RUNNING] Store Notifications & Broadcast Hub .......................... PASSED
[RUNNING] Customer Address Book & Multi-Destination Management ......... PASSED
[RUNNING] Store Audit Activity Journal & CSV Compliance Trail .......... PASSED
========================================================================
   TEST RESULTS: 11 Run | 11 Passed | 0 Failed (100%)
========================================================================
```
