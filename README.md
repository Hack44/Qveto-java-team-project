# Qveto-java-team-project
Collaborative Java development project for the Qveto Technology internship, focused on applying Java programming, Object-Oriented Programming (OOP), problem-solving, and software development best practices through teamwork.

## Project 1: E-commerce & Inventory Management System

A desktop business application built with **Java Swing** and **MySQL** that enables administrators to manage products, categories, stock, and orders while customers browse catalogs, manage their shopping carts, place orders, and track purchases.

---

### Team Roles & Modular Architecture

* **Member 1:** Database Schema, JDBC Connectivity, Model/Entities & Migrations
* **Member 2 (Current Module):** UI, Login & User Management
  * Java Swing Authentication Frames (`LoginFrame`)
  * Customer & Admin Login workflows with input validation
  * Customer Registration with form validation
  * Application Dashboard Shell & Navigation (`DashboardFrame`)
  * Customer Profile and Order History screens
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

#### Using Maven (Recommended)
```bash
# 1. Compile the application
mvn clean compile

# 2. Run the Swing GUI
mvn exec:java
```

#### Using Standard Java CLI
```bash
# Compile to a bin directory
javac -d bin src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java

# Run the application
java -cp bin com.qveto.ecommerce.Main
```

---

### Demo Accounts for Review & Testing

| Role | Username / Email | Password |
| :--- | :--- | :--- |
| **Customer** | `customer@qveto.com` | `pass123` |
| **Admin** | `admin` | `admin123` |

*(You can also use the **Register** tab to create a new customer account with full input validation).*
