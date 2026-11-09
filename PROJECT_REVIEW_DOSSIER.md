# Project 1: E-Commerce & Inventory Management System
## Final Capstone Review & Submission Dossier
**Internship Program:** Qveto Technology Java Internship  
**Project Track:** Project 1 (E-commerce & Inventory Management System)  
**Assigned Role:** Member 2 — UI, Presentation & Store Management  
**Team Leader / Member 1:** Harsh Verma (`Hack44`)  
**Submission Date:** November 09, 2026 (Final Submission & Evaluation Day)  

---

## 1. Executive Summary

This repository hosts a production-grade, enterprise-architected desktop business application built using pure **Java (JDK 17+/21)**, **Java Swing (`javax.swing`, `java.awt`)**, and **MySQL/JDBC**. It delivers an end-to-end e-commerce and store operations platform connecting customer-facing shopping workflows with administrative inventory procurement, order fulfillment, compliance auditing, and executive analytics.

The application operates with a **dual-engine architecture**:
1. **Live Enterprise Mode:** Connects to MySQL via pre-leased connection pooling (`DBConnectionPool`) and exponential retry policies (`DatabaseRetryPolicy`).
2. **Resilient Standalone Demo Mode:** Automatically engages in-memory store caching if MySQL is offline or during offline presentations, ensuring 100% feature availability and crash-free reviewer demonstrations.

---

## 2. System Architecture & Modular Design

```
+-----------------------------------------------------------------------------------+
|                            PRESENTATION LAYER (Java Swing)                         |
|  LoginFrame (Auth & Fast Demo)  |  DashboardFrame (Main Navigation & KPI Deck)    |
|  ProductDetailsDialog            |  AddressBookDialog (Multi-Address Book)         |
|  OrderDetailsDialog (Stepper)   |  OrderTrackingDialog (AWB & Checkpoints)        |
|  OrderReturnDialog (Refunds)    |  ProductReviewDialog (5-Star Reviews)           |
|  NotificationCenterDialog       |  LowStockProcurementDialog (Batch Restock)      |
|  CategoryManagementDialog       |  AuditLogDialog (Compliance Journal)            |
|  ProjectOverviewDialog          |  UserDetailsDialog (CLV Dossier)                |
+-----------------------------------------------------------------------------------+
                                        |
+-----------------------------------------------------------------------------------+
|                        BUSINESS & STORE SERVICE LAYER                             |
|  UserStore        | ProductStore      | OrderStore        | CategoryStore         |
|  CartStore        | PaymentStore      | CouponStore       | ReviewStore           |
|  WishlistStore    | NotificationStore | AddressBookStore  | AuditStore            |
|  PdfInvoiceGenerator (Adobe 1.4)      | ValidationUtil    | DBStatusUtil          |
+-----------------------------------------------------------------------------------+
                                        |
+-----------------------------------------------------------------------------------+
|                      DATA ACCESS OBJECT (DAO) LAYER                               |
|  UserDAO          | CategoryDAO       | ProductDAO        | OrderDAO              |
|  OrderItemDAO     | PaymentDAO        | CartDAO           | ReportDAO             |
|  DataSanitizer    | InputBoundaryValidator | ReportExportUtil                     |
+-----------------------------------------------------------------------------------+
                                        |
+-----------------------------------------------------------------------------------+
|                    JDBC PERSISTENCE & RELATIONAL DATABASE                         |
|  DBConnection (Singleton) | DBConnectionPool | DatabaseRetryPolicy                |
|  database/schema.sql (7 Tables) | migration_v2.sql (Indexes/Triggers)             |
|  migration_v3.sql (6 Views: view_sales_summary, view_low_stock_alerts, etc.)      |
+-----------------------------------------------------------------------------------+
```

---

## 3. Team Member Division of Responsibilities

### **Member 1 (Database, Schema & Persistence Layer)**
* **Relational Schema:** Canonical schema (`database/schema.sql`) covering 7 core normalized tables (`users`, `categories`, `products`, `orders`, `order_items`, `payments`, `cart_items`).
* **Migrations v2 & v3:** Performance B-tree indexes, `updated_at` audit triggers, and 6 SQL reporting views (`view_sales_summary`, `view_top_selling_products`, `view_low_stock_alerts`, `view_category_sales_summary`, `view_customer_order_history`, `view_payment_method_distribution`).
* **Connection Pooling & Resilience:** Thread-safe `DBConnectionPool` with automated proxy connection recycling, and `DatabaseRetryPolicy` with jittered exponential backoff.
* **Security & Sanitization:** SHA-256 password hashing with salt compatibility (`PasswordUtil`), XSS neutralization (`DataSanitizer`), and strict domain constraint validation (`InputBoundaryValidator`).
* **Reporting DAO:** Executive summary aggregations and dual-engine CSV reporting (`ReportExportUtil`).

---

### **Member 2 (UI, Presentation & Store Management — Authored Deliverables)**

1. **Authentication & Quick-Demo Access:**
   * Modern authentication shell (`LoginFrame`) with password visibility toggle, real-time database health pill (`● MySQL Active` vs `○ Standalone Demo`), and registration validation.
   * **⚡ 1-Click Fast Login:** Instant demo login buttons for both Customer (`customer@qveto.com` / `pass123`) and Admin (`admin` / `admin123`) for presentation convenience.

2. **Product Catalog & Interactive Discovery:**
   * Full catalog discovery table with real-time keyword search, category filtering, multi-sort (Price Low-to-High, Price High-to-Low, Name A-Z, Highest Stock), and in-stock toggles.
   * **Product Details Modal (`ProductDetailsDialog`):** Displays high-res product metadata, stock availability, verified customer reviews, and interactive 5-star ratings.

3. **Customer Saved Wishlist & Favorites (`WishlistStore`):**
   * Dedicated **"Wishlist"** Tab allowing customers to curate saved items and monitor price changes.
   * 1-Click `"♡ Save to Wishlist"` button with dynamic `"♥ In Wishlist"` visual toggling.
   * Instant `"🛒 Move to Cart"` action with automatic wishlist removal and cart badge count updates.

4. **Promotional Coupon & Discount Engine (`CouponStore`):**
   * Centralized promotional rules engine (`QVETO10`, `SAVE20`, `FLAT15`, `WELCOME`, `MEGA50`) with minimum cart thresholds and discount caps.
   * Real-time coupon validation bar in shopping cart with instant savings calculation.

5. **Saved Multi-Address Book & Smart Checkout Selector (`Address.java`, `AddressBookStore`, `AddressBookDialog`):**
   * Centralized shipping directory supporting `Home`, `Work`, and custom destinations with default address designation.
   * Direct address management from **Customer Profile** (`"Manage Address Book"`).
   * **Smart Checkout Dropdown:** Pre-populates default destination, enables 1-click address switching, and provides a shortcut to add new delivery addresses during checkout.

6. **Simulated Payment Gateway & Automated Settlement (`PaymentStore`):**
   * Multi-channel checkout: Cash on Delivery (COD), UPI (Simulated), Credit/Debit Card, and Net Banking.
   * Automated settlement: Instant `COMPLETED` for prepaid channels, `PENDING` for COD, auto-settlement on delivery, and automatic refund marking (`REFUNDED`) on cancellation.
   * Unique financial audit tracking references (`TXN-...`).

7. **Invoicing & Vector PDF 1.4 Generator (`PdfInvoiceGenerator`, `OrderDetailsDialog`):**
   * Visual **Order Fulfillment Stepper**: Interactive tracking milestone bar (`[Placed] -> [Confirmed] -> [Shipped] -> [Delivered]`).
   * **Adobe PDF 1.4 Vector Invoice Generator:** Pure Java vector PDF generator producing professional publication-grade invoices (`Invoice_ORD-XXXX.pdf`) without external library dependencies.
   * 1-Click formatted plain-text invoice export (`.txt`).

8. **Live Consignment Shipment Tracking (`OrderTrackingDialog`):**
   * Interactive courier tracking modal accessible from Customer Order History and Admin Orders via `"📍 Track Shipment"`.
   * Carrier assignment (BlueDart Express Priority, FedEx Air Logistics), deterministic Airway Bill (AWB) consignment code, and estimated delivery dates.
   * Time-stamped transit checkpoint scan cards (Order Picked, Hub Arrival, Out for Delivery, Handed Over) and 1-click clipboard copy.

9. **Customer Returns, Refunds & Inventory Replenishment (`OrderReturnDialog`):**
   * 1-Click `"🔄 Request Return / Refund"` button on delivered orders with structured reason categories.
   * Real-time status transitions to `RETURN_REQUESTED` with automated customer and administrator broadcast alerts.
   * Admin 1-Click `"⚡ Approve Return & Refund"` control in Order Fulfillment that automatically issues a refund and replenishes inventory stock.

10. **Store Notification Broadcast Hub (`NotificationCenterDialog`, `NotificationStore`):**
    * Prominent header notification bell with live unread counter (`🔔 Alerts (N)`).
    * Modal drawer categorizing order delivery updates (📦), inventory restocks (⚠️), vouchers (🎟️), and returns (🔄).

11. **Verified Customer Reviews & 5-Star Ratings (`ProductReviewDialog`, `ReviewStore`):**
    * Interactive rating selector (`★`) for verified purchasers with headline and feedback.
    * Live recalculation of average product star ratings and verified buyer badging (`✓ Verified Buyer`).

12. **Admin Low-Stock Alerts & Procurement Hub (`LowStockProcurementDialog`):**
    * Real-time deficit metrics: Total Low-Stock Exposure, Out-of-Stock SKUs, and Total Unit Deficits.
    * 1-Click single-SKU restock and ⚡ Batch Procurement replenishment.

13. **Executive Analytics Command Center (`DashboardFrame`):**
    * 6-Metric KPI Deck: Gross Sales Revenue, Settled Collections, Total Orders, Average Order Value (AOV), Active Customers, Low-Stock Exposure.
    * **Dynamic Date-Range Filtering:** Presets (`All Time`, `Last 7 Days`, `Last 30 Days`, `This Month`) and custom calendar date filtering.
    * Top-Selling Products Velocity Leaderboard, Category Revenue Breakdown, and Customer Lifetime Value (CLV) Leaderboard.

14. **Enterprise Audit Activity Journal (`AuditLog.java`, `AuditStore.java`, `AuditLogDialog`):**
    * Centralized immutable audit journal capturing administrative and customer store events (order status updates, inventory restocks, price changes, refund approvals, account deactivations).
    * Real-time category filtering (`ORDER_MANAGEMENT`, `INVENTORY`, `STORE_SECURITY`, `RETURNS_REFUNDS`, `USER_MANAGEMENT`, `CHECKOUT`), keyword search, and detailed record inspector.
    * 1-Click RFC 4180 compliant CSV export for operational compliance.

15. **System Architecture & Review Dossier Dialog (`ProjectOverviewDialog`):**
    * Integrated interactive modal accessible from login and dashboard headers (`"ℹ️ Project Dossier"`) presenting architecture tiers, team member responsibilities, live diagnostic health cards, and a reviewer demonstration cheatsheet.

---

## 4. Quality Assurance & Verification Report

### Test Execution Summary: **19 Run | 19 Passed | 0 Failed (100% Pass Rate)**

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

---

## 5. Quick-Start Instructions for Evaluators

### **Option 1: 1-Click Automated Batch Launchers**
* **To launch the full graphical application:** Double-click `run.bat` (or run `./run.bat` in Command Prompt / PowerShell).
* **To execute the automated verification test harness:** Double-click `test.bat` (or run `./test.bat`).

### **Option 2: Command Line (JDK 17+/21)**
```bash
# 1. Compile all source and test modules
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java \
             src/main/java/com/qveto/ecommerce/dao/*.java \
             src/main/java/com/qveto/ecommerce/util/*.java \
             src/main/java/com/qveto/ecommerce/ui/*.java \
             src/main/java/com/qveto/ecommerce/*.java \
             src/test/java/com/qveto/ecommerce/*.java

# 2. Run the application GUI
java -cp "bin;src/main/resources" com.qveto.ecommerce.Main

# 3. Run all verification tests
java -cp "bin;src/main/resources" com.qveto.ecommerce.util.DatabaseIntegrityTest
java -cp "bin;src/main/resources" com.qveto.ecommerce.StoreWorkflowIntegrationTest
```

### **Demo Credentials**
| User Role | Email / Username | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@qveto.com` | `pass123` | Catalog discovery, cart, wishlist, promo coupons, checkout, address book, tracking, returns, reviews |
| **Administrator** | `admin` | `admin123` | Inventory catalog CRUD, batch restock procurement, order fulfillment, return approvals, audit journal, reports |

*(Shortcut: Click the **⚡ 1-Click Fast Login** button on either login tab to enter immediately without typing credentials).*
