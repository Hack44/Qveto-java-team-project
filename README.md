# Qveto-java-team-project
Collaborative Java development project for the Qveto Technology internship, focused on applying Java programming, Object-Oriented Programming (OOP), problem-solving, and software development best practices through teamwork.

## Project 1: E-commerce & Inventory Management System

A desktop business application built with **Java Swing** and **MySQL** that enables administrators to manage products, categories, stock, users, and orders while customers browse catalogs, manage their shopping carts, place orders, and track purchases.

---

### Team Roles & Modular Architecture

* **Member 1:** Database Schema, JDBC Connectivity, Model/Entities, DAO Layer & Migrations (`database/schema.sql`, `DBConnection.java`, `UserDAO.java`, `ProductDAO.java`, `OrderDAO.java`, `Category.java`)
* **Member 2 (Current Module):** UI, Login & User Management
  * Java Swing Authentication Frames (`LoginFrame`) with password visibility toggle (Show/Hide)
  * Customer & Admin Login workflows with input validation and JDBC `UserDAO` authentication
  * Customer Registration with form validation, email uniqueness checks, and database persistence
  * Application Dashboard Shell & Navigation (`DashboardFrame`)
  * Product Discovery: Live keyword search, category filtering, and Product Details Modal (`ProductDetailsDialog`)
  * Interactive Shopping Cart with dynamic badge, quantity controls, and checkout confirmation
  * Customer Profile Management (View, Edit Contact Info, Change Password Dialogs synced with MySQL)
  * Customer Order History Screen with Status Filtering & Order Cancellation
  * Order Details & Invoice Modal (`OrderDetailsDialog`)
  * Admin Product Management (Add new product with validation, view details, delete product)
  * Admin Order Fulfillment Management (Inspect order details, filter by status, and advance order status: `PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED`)
  * Admin User Management (`createAdminUsersPanel`): Live user directory, real-time KPI metrics, search by name/email, role filtering, User Dossier Modal (`UserDetailsDialog`), and account deactivation with self-deletion protection
  * Real-Time Database Status Indicator (`DBStatusUtil`): Visual connectivity pill (`● MySQL Active` vs `○ Standalone Demo`) across login and dashboard headers
  * Centralized User Store coordinating between `UserDAO` and resilient in-memory fallback state (`UserStore`)
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
| **Customer** | `customer@qveto.com` | `pass123` | Product catalog with live search/filter, product details modal, cart, checkout, order history, cancel orders, edit profile, change password (synced with MySQL if connected) |
| **Admin** | `admin` | `admin123` | Inventory catalog management (add/delete products), customer order fulfillment management (update status, view invoices), user account management (view all users, search/filter, view dossier, deactivate accounts), sales KPI overview |

*(You can also use the **Register** tab to create a new customer account with full input validation and duplicate-email detection).*
