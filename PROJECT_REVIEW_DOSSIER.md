# Project 1: E-Commerce & Inventory Management System
## Final Capstone Review & Submission Dossier
**Internship Program:** Qveto Technology Java Internship  
**Project Track:** Project 1 (E-commerce & Inventory Management System)  
**Authors / Developers:** Chetan Reddy (`Chetanreddysai`) in collaboration with Harsh Verma (`Hack44`)  
**Submission Date:** November 09, 2026 (Final Submission & Evaluation Day)  
**Status:** **100% Complete & Production Ready | 19 of 19 Tests Passed**  

---

## 1. Executive Summary

This repository hosts a production-grade, enterprise-architected desktop business application built using pure **Java (JDK 17+/21)**, **Java Swing (`javax.swing`, `java.awt`)**, and **MySQL/JDBC**. It delivers an end-to-end e-commerce and retail operations platform connecting customer-facing shopping workflows with administrative inventory procurement, order fulfillment, compliance auditing, and executive analytics.

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

## 3. End-to-End Functional Capabilities & Delivered Modules

### **A. User Authentication & Session Management**
1. **Secure Registration & Login (`LoginFrame.java`):** Dual persona login (Customer & Admin) with input boundary validation, email uniqueness checks, and SHA-256 cryptographic hashing (`PasswordUtil.java`).
2. **⚡ 1-Click Fast Login:** Instant demo login buttons for both Customer (`customer@qveto.com` / `pass123`) and Admin (`admin` / `admin123`) for presentation convenience.
3. **Database Health Indicator:** Real-time visual pill (`● MySQL Active` vs `○ Standalone Demo`) across all headers.
4. **Customer Profile Management:** Update contact, address, password change dialogs synced with persistence layer, and lifetime spend metrics.

### **B. Product Catalog & Smart Shopping Cart**
1. **Catalog Discovery Table:** Live keyword search, dynamic category filtering, multi-sort (Price Low-High, Price High-Low, Name A-Z, Stock), and in-stock toggle.
2. **Product Details Modal (`ProductDetailsDialog.java`):** High-res product metadata, stock availability, customer reviews, and interactive 5-star ratings.
3. **Customer Saved Wishlist (`WishlistStore.java`):** Dedicated tab for curating saved items, 1-click `"♡ Save to Wishlist"`, and instant `"🛒 Move to Cart"` transfer.
4. **Shopping Cart with DB Sync (`CartDAO.java`, `CartStore.java`):** Dynamic cart badge, quantity spinner controls, subtotal recalculations, and database sync.
5. **Promotional Coupon & Discount Engine (`CouponStore.java`):** Centralized rules engine supporting percentage and flat-rate vouchers (`QVETO10`, `SAVE20`, `FLAT15`, `WELCOME`, `MEGA50`) with minimum cart thresholds and dynamic discount calculation.

### **C. Logistics, Invoicing & Order Fulfillment**
1. **Saved Multi-Address Book (`Address.java`, `AddressBookStore.java`, `AddressBookDialog.java`):** Centralized shipping directory supporting `Home`, `Work`, and custom destinations with default address designation and direct profile management.
2. **Smart Checkout Selector:** Pre-populates default destination, enables 1-click address switching, and provides a shortcut to add new delivery addresses during checkout.
3. **Multi-Channel Payment Gateway (`PaymentStore.java`):** Supports Cash on Delivery (COD), UPI (Simulated), Credit/Debit Card, and Net Banking with automated settlement tracking (`TXN-...`).
4. **Order Fulfillment Stepper (`OrderDetailsDialog.java`):** Visual tracking milestone bar (`[Placed] -> [Confirmed] -> [Shipped] -> [Delivered]`).
5. **Pure Vector Adobe PDF 1.4 Invoice Generator (`PdfInvoiceGenerator.java`):** Pure Java vector PDF generator producing professional publication-grade invoices (`Invoice_ORD-XXXX.pdf`) without external library dependencies.
6. **Live Consignment Shipment Tracking (`OrderTrackingDialog.java`):** Interactive courier tracking modal accessible from Customer Order History and Admin Orders via `"📍 Track Shipment"`. Features carrier details (BlueDart Express, FedEx Air Logistics), deterministic Airway Bill (AWB) consignment code, and time-stamped transit checkpoint scan cards.
7. **Master Orders RFC 4180 CSV Export (`OrderStore.exportOrdersCsv`):** One-click master order register export.

### **D. Post-Delivery Operations & Customer Feedback**
1. **Customer Returns & Refund Automation (`OrderReturnDialog.java`, `OrderStore.java`):** Post-delivery return ticket logging with structured reason categories.
2. **Admin 1-Click Return Approval:** Automated refund processing and physical inventory stock replenishment (`OrderStore.approveReturn`).
3. **Verified Customer Reviews & 5-Star Ratings (`ProductReviewDialog.java`, `ReviewStore.java`):** Interactive rating selector (`★`) for verified purchasers with verified buyer badging (`✓ Verified Buyer`) and live average rating calculations.
4. **Store Notification Broadcast Hub (`NotificationCenterDialog.java`, `NotificationStore.java`):** Real-time notification drawer with unread counter (`🔔 Alerts (N)`) categorizing deliveries, restocks, vouchers, and returns.

### **E. Inventory Procurement & Compliance Auditing**
1. **Admin Product Management:** Catalog CRUD, low-stock threshold queries, and inventory catalog CSV export (`ProductStore.exportCatalogCsv`).
2. **Admin Low-Stock Alerts & Procurement Hub (`LowStockProcurementDialog.java`):** Real-time deficit metrics: Total Low-Stock Exposure, Out-of-Stock SKUs, and Total Unit Deficits. Supports 1-click single-SKU restock and ⚡ Batch Procurement replenishment.
3. **Executive Analytics Command Center (`ReportStore.java`, `ReportDAO.java`):** 6-Metric KPI Deck (Gross Revenue, Settled Collections, Orders, AOV, Active Customers, Low-Stock Exposure) with dynamic date-range filtering (`All Time`, `Last 7 Days`, `Last 30 Days`, `This Month`), velocity leaderboard, category revenue breakdown, and Customer Lifetime Value (CLV) metrics.
4. **Enterprise Audit Activity Journal (`AuditLog.java`, `AuditStore.java`, `AuditLogDialog.java`):** Immutable operational audit trail for compliance, with category filters, keyword search, and RFC 4180 CSV export.
5. **Admin User Dossier Modal (`UserDetailsDialog.java`):** User profile inspection with CLV calculations and account deactivations with self-deletion protection.
6. **Category Management (`CategoryManagementDialog.java`, `CategoryStore.java`, `CategoryDAO.java`):** Add, rename, delete categories with product foreign-key dependency validation.

### **F. Database, Persistence & Resilience Layer**
1. **Canonical MySQL Relational Schema (`database/schema.sql`):** 7 core normalized tables (`users`, `categories`, `products`, `orders`, `order_items`, `payments`, `cart_items`) with foreign keys and check constraints.
2. **Database Migrations v2 & v3:** Performance B-tree query indexes, `updated_at` audit triggers, and 6 analytical reporting views (`view_sales_summary`, `view_top_selling_products`, `view_low_stock_alerts`, `view_category_sales_summary`, `view_customer_order_history`, `view_payment_method_distribution`).
3. **Singleton JDBC Connection Factory (`DBConnection.java`):** Pre-leased connection pooling (`DBConnectionPool`), automated proxy recycling, and exponential retry backoff (`DatabaseRetryPolicy`).
4. **Security & Sanitization:** SHA-256 cryptographic password hashing (`PasswordUtil`), XSS neutralization (`DataSanitizer`), and strict domain constraint validation (`InputBoundaryValidator`).
5. **Complete DAO Persistence Suite:** `UserDAO`, `CategoryDAO`, `ProductDAO`, `OrderDAO`, `OrderItemDAO`, `PaymentDAO`, `CartDAO`, `ReportDAO`.

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
