package com.qveto.ecommerce.util;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Utility class providing form input validation and user-friendly error alerts.
 * Supports Member 2 requirement: "Validate form inputs and display user-friendly errors."
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9\\s-]{7,15}$"
    );

    private ValidationUtil() {
        // Utility class
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // --- User-Friendly Dialog Helpers ---

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
                parent,
                "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>" + message + "</body></html>",
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showSuccess(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
                parent,
                "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>" + message + "</body></html>",
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(
                parent,
                "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>" + message + "</body></html>",
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>" + message + "</body></html>",
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
}
