package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'users' table.
 *
 * Provides CRUD operations and authentication support for User entities.
 * All queries use PreparedStatements to prevent SQL injection.
 *
 * Member 1 — DB/JDBC layer.
 */
public class UserDAO {

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    /**
     * Authenticates a user by email and plain-text password.
     * NOTE: In production this should compare a salted hash (e.g. BCrypt).
     *       For this internship project we store the raw password in the
     *       password_hash column and compare directly.
     *
     * @param email    the user's email address
     * @param password the plain-text password entered at login
     * @return the matching {@link User}, or {@code null} if not found / wrong password
     */
    public User authenticate(String email, String password) {
        String sql = "SELECT user_id, full_name, email, role, phone, address, created_at "
                   + "FROM users WHERE email = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate() failed: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    /**
     * Inserts a new user (customer registration) into the database.
     *
     * @param fullName the customer's full name
     * @param email    unique email address used as login credential
     * @param password plain-text password (stored as-is for this project)
     * @param phone    contact phone number (may be null)
     * @param address  shipping address (may be null)
     * @return the auto-generated user_id, or -1 if insertion failed
     */
    public int registerCustomer(String fullName, String email, String password,
                                 String phone, String address) {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, phone, address) "
                   + "VALUES (?, ?, ?, 'CUSTOMER', ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, phone);
            stmt.setString(5, address);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] registerCustomer() failed: " + e.getMessage());
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves a single user by their primary key.
     *
     * @param userId the user_id to look up
     * @return the {@link User}, or {@code null} if no record found
     */
    public User findById(int userId) {
        String sql = "SELECT user_id, full_name, email, role, phone, address, created_at "
                   + "FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findById() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a single user by their email address.
     *
     * @param email the email to search for
     * @return the {@link User}, or {@code null} if no record found
     */
    public User findByEmail(String email) {
        String sql = "SELECT user_id, full_name, email, role, phone, address, created_at "
                   + "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findByEmail() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all users in the system (admin utility).
     *
     * @return list of all {@link User} records; empty list if none
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, role, phone, address, created_at "
                   + "FROM users ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findAll() failed: " + e.getMessage());
        }
        return users;
    }

    /**
     * Checks whether an email address is already registered.
     *
     * @param email the email to check
     * @return {@code true} if a user with that email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] emailExists() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates a customer's contact information (full name, phone, address).
     *
     * @param userId  the user to update
     * @param fullName new full name
     * @param phone   new phone number
     * @param address new shipping address
     * @return {@code true} if the update was successful
     */
    public boolean updateContactInfo(int userId, String fullName, String phone, String address) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, address = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setInt(4, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateContactInfo() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Changes a user's password. Verifies the current password before updating.
     *
     * @param userId          the user whose password to change
     * @param currentPassword the current plain-text password (for verification)
     * @param newPassword     the new plain-text password to set
     * @return {@code true} if password was successfully changed;
     *         {@code false} if current password didn't match or update failed
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        // First verify the existing password
        String verifySql = "SELECT 1 FROM users WHERE user_id = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {

            verifyStmt.setInt(1, userId);
            verifyStmt.setString(2, currentPassword);

            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (!rs.next()) {
                    return false; // current password is wrong
                }
            }

            // Current password matched — update to new password
            String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, userId);
                return updateStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] changePassword() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    /**
     * Deletes a user account by user_id (admin operation).
     * Associated orders and cart items must be removed first due to FK constraints,
     * or cascade delete must be configured in the schema.
     *
     * @param userId the user to delete
     * @return {@code true} if deletion was successful
     */
    public boolean deleteById(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteById() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Maps the current row of a {@link ResultSet} to a {@link User} object.
     * Does NOT call rs.next() — the caller must position the cursor.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getString("phone")   != null ? rs.getString("phone")   : "",
                rs.getString("address") != null ? rs.getString("address") : "",
                rs.getString("created_at") != null ? rs.getString("created_at") : ""
        );
    }
}
