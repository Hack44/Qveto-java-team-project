package com.qveto.ecommerce.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model entity representing a user in the system (Customer or Admin).
 * Maps to the 'users' table in the database schema.
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
        this.role = role.toUpperCase();
        this.phone = "+91 98765 43210";
        this.address = "124 Tech Park Road, Bengaluru, Karnataka, India";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public User(int userId, String fullName, String email, String role, String phone, String address, String createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role.toUpperCase();
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
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
        this.role = role.toUpperCase();
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
