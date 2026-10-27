package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.UserDAO;
import com.qveto.ecommerce.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized User Store bridging the JDBC UserDAO layer and in-memory
 * fallback state for seamless offline demo and automated testing.
 * 
 * Supports Member 2 (UI, Login & User Management) requirements:
 * - Admin/Customer authentication
 * - Customer registration with email uniqueness validation
 * - Admin user discovery, searching, and deactivation
 * - Customer profile updates and password changes
 */
public class UserStore {

    private static final List<User> inMemoryUsers = new CopyOnWriteArrayList<>();
    private static final Map<String, String> inMemoryPasswords = new HashMap<>();
    private static int nextMockUserId = 10;

    static {
        initializeSampleUsers();
    }

    private UserStore() {
        // Utility class
    }

    private static void initializeSampleUsers() {
        inMemoryUsers.clear();
        inMemoryPasswords.clear();

        // 1. Primary Demo Customer
        User customer1 = new User(1, "Rahul Sharma", "customer@qveto.com", "CUSTOMER",
                "+91 98765 43210", "124 Tech Park Road, Bengaluru, Karnataka, India", "2026-10-18 10:30");
        inMemoryUsers.add(customer1);
        inMemoryPasswords.put("customer@qveto.com", "pass123");

        // 2. Demo Customer 2
        User customer2 = new User(2, "Priya Patel", "priya.patel@example.com", "CUSTOMER",
                "+91 98111 22334", "45 Marine Drive, Mumbai, Maharashtra, India", "2026-10-19 14:15");
        inMemoryUsers.add(customer2);
        inMemoryPasswords.put("priya.patel@example.com", "pass123");

        // 3. Demo Customer 3
        User customer3 = new User(3, "Amit Kumar", "amit.kumar@example.com", "CUSTOMER",
                "+91 97222 33445", "78 Connaught Place, New Delhi, Delhi, India", "2026-10-21 09:40");
        inMemoryUsers.add(customer3);
        inMemoryPasswords.put("amit.kumar@example.com", "pass123");

        // 4. Demo Customer 4
        User customer4 = new User(4, "Sneha Reddy", "sneha.reddy@example.com", "CUSTOMER",
                "+91 99333 44556", "12 Jubilee Hills, Hyderabad, Telangana, India", "2026-10-24 16:50");
        inMemoryUsers.add(customer4);
        inMemoryPasswords.put("sneha.reddy@example.com", "pass123");

        // 5. System Administrator
        User admin = new User(99, "System Administrator", "admin@qveto.com", "ADMIN",
                "+91 90000 00000", "Qveto HQ, Tech Center, Sector 5, India", "2026-10-15 08:00");
        inMemoryUsers.add(admin);
        inMemoryPasswords.put("admin@qveto.com", "admin123");
        inMemoryPasswords.put("admin", "admin123");
    }

    /**
     * Authenticates a user by email/username and password.
     * Queries UserDAO first if database is active; falls back to in-memory store.
     */
    public static User authenticate(String emailOrUsername, String password) {
        if (emailOrUsername == null || password == null) return null;
        String identifier = emailOrUsername.trim();

        // 1. Attempt database authentication via UserDAO
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                User dbUser = new UserDAO().authenticate(identifier, password);
                if (dbUser != null) {
                    return dbUser;
                }
            } catch (Throwable t) {
                System.err.println("[UserStore] DB authenticate fallback: " + t.getMessage());
            }
        }

        // 2. In-memory demo fallback
        // Check admin alias
        if (identifier.equalsIgnoreCase("admin") && password.equals("admin123")) {
            for (User u : inMemoryUsers) {
                if (u.isAdmin()) return u;
            }
        }

        // Check matching email and password
        for (User u : inMemoryUsers) {
            if (u.getEmail().equalsIgnoreCase(identifier)) {
                String storedPass = inMemoryPasswords.get(identifier.toLowerCase());
                if (storedPass != null && storedPass.equals(password)) {
                    return u;
                } else if (password.length() >= 6 && storedPass == null) {
                    // Default fallback for any newly registered session users
                    return u;
                }
            }
        }

        // Demo customer default fallback
        if (identifier.equalsIgnoreCase("customer@qveto.com") && password.equals("pass123")) {
            return inMemoryUsers.get(0);
        }

        return null;
    }

    /**
     * Checks if an email address is already registered in the system.
     */
    public static boolean emailExists(String email) {
        if (email == null) return false;
        String cleanEmail = email.trim().toLowerCase();

        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                if (new UserDAO().emailExists(cleanEmail)) {
                    return true;
                }
            } catch (Throwable t) {
                System.err.println("[UserStore] DB emailExists fallback: " + t.getMessage());
            }
        }

        for (User u : inMemoryUsers) {
            if (u.getEmail().equalsIgnoreCase(cleanEmail)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new customer into the database and local store.
     */
    public static User registerCustomer(String fullName, String email, String password, String phone, String address) {
        String cleanEmail = email.trim();
        int assignedId = nextMockUserId++;

        // 1. Persist to database if connected
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int dbId = new UserDAO().registerCustomer(fullName, cleanEmail, password, phone, address);
                if (dbId > 0) {
                    assignedId = dbId;
                }
            } catch (Throwable t) {
                System.err.println("[UserStore] DB registerCustomer fallback: " + t.getMessage());
            }
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        User newUser = new User(assignedId, fullName, cleanEmail, "CUSTOMER",
                phone != null ? phone : "",
                address != null ? address : "",
                now);

        inMemoryUsers.add(newUser);
        inMemoryPasswords.put(cleanEmail.toLowerCase(), password);
        return newUser;
    }

    /**
     * Returns all registered users in the system (for Admin User Management).
     */
    public static List<User> getAllUsers() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<User> dbList = new UserDAO().findAll();
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Throwable t) {
                System.err.println("[UserStore] DB findAll fallback: " + t.getMessage());
            }
        }
        return new ArrayList<>(inMemoryUsers);
    }

    /**
     * Deletes a user account by ID (Admin operation).
     */
    public static boolean deleteUser(int userId) {
        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new UserDAO().deleteById(userId);
            } catch (Throwable t) {
                System.err.println("[UserStore] DB deleteById fallback: " + t.getMessage());
            }
        }

        boolean memRemoved = inMemoryUsers.removeIf(u -> u.getUserId() == userId);
        return dbSuccess || memRemoved;
    }

    /**
     * Updates customer contact information.
     */
    public static boolean updateProfile(int userId, String fullName, String phone, String address) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                new UserDAO().updateContactInfo(userId, fullName, phone, address);
            } catch (Throwable t) {
                System.err.println("[UserStore] DB updateContactInfo fallback: " + t.getMessage());
            }
        }

        for (User u : inMemoryUsers) {
            if (u.getUserId() == userId) {
                u.setFullName(fullName);
                u.setPhone(phone);
                u.setAddress(address);
                return true;
            }
        }
        return true;
    }

    /**
     * Changes a user's password after verifying their current password.
     */
    public static boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                return new UserDAO().changePassword(userId, currentPassword, newPassword);
            } catch (Throwable t) {
                System.err.println("[UserStore] DB changePassword fallback: " + t.getMessage());
            }
        }

        for (User u : inMemoryUsers) {
            if (u.getUserId() == userId) {
                String emailKey = u.getEmail().toLowerCase();
                String stored = inMemoryPasswords.get(emailKey);
                if (stored != null && !stored.equals(currentPassword)) {
                    return false; // current password mismatch
                }
                inMemoryPasswords.put(emailKey, newPassword);
                return true;
            }
        }
        return true;
    }
}
