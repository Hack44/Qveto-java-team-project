package com.qveto.ecommerce.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing and verifying user passwords using standard SHA-256.
 *
 * The hex output of {@link #hash(String)} directly matches the MySQL function
 * {@code SHA2(password, 256)}, ensuring seamless compatibility between application
 * registration/login and MySQL database seed records.
 *
 * Member 1 — Utility/Security layer.
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Computes a SHA-256 hexadecimal hash for the provided plain-text password.
     *
     * @param plainText the plain-text password string
     * @return 64-character lowercase hexadecimal hash string, or null if input is null
     */
    public static String hash(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in current JRE", e);
        }
    }

    /**
     * Verifies if a given plain-text password matches a stored SHA-256 hash
     * or a fallback legacy plain-text password.
     *
     * @param plainText  the user-entered password to verify
     * @param storedHash the hash stored in the database
     * @return {@code true} if password matches the hash or plain text
     */
    public static boolean verify(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) {
            return false;
        }

        // Compare against SHA-256 computed hash
        String computedHash = hash(plainText);
        if (computedHash.equalsIgnoreCase(storedHash)) {
            return true;
        }

        // Resilient fallback for plain-text entries in demo database
        return plainText.equals(storedHash);
    }
}
