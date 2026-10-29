package com.qveto.ecommerce.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model entity representing a user in the system (Customer or Admin).
 * Maps to the 'users' table in the database schema.
 *
 * Member 1 — DB/Model layer.
 */
public class User {
    private int userId;
    private String fullName;
    private String email;
    private String role; // "ADMIN" or "CUSTOMER"
    private String phone;
    private String address;
    private String createdAt;

    public User() {
    }

    public User(int userId, String fullName, String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "CUSTOMER";
        this.phone = "";
        this.address = "";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public User(int userId, String fullName, String email, String role, String phone, String address, String createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "CUSTOMER";
        this.phone = phone != null ? phone : "";
        this.address = address != null ? address : "";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.toUpperCase() : "CUSTOMER";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
