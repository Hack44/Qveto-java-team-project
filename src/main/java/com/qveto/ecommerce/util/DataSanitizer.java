package com.qveto.ecommerce.util;

import java.util.regex.Pattern;

/**
 * Enterprise Data Sanitization & Normalization Utility.
 *
 * Implements defense-in-depth sanitization for data entering the persistence layer:
 * 1. HTML / XSS neutralization: Strips executable markup and dangerous tags.
 * 2. Canonical normalization: Uniformly formats emails, phone numbers, and addresses.
 * 3. Whitespace & control char cleanup: Eliminates non-printable chars, null bytes, and excess spaces.
 * 4. Safe truncation: Enforces column-width bounds preventing MySQL {@code DataTruncation} errors.
 * 5. SQL injection pattern detection: Audits input for malicious exploitation signatures.
 *
 * Member 1 — Persistence, Data Sanitization & Validation layer.
 */
public class DataSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    // SQL Injection heuristic signatures for defensive auditing
    private static final Pattern SQLI_PATTERN = Pattern.compile(
            "('|\")\\s*(OR|AND)\\s*('|\")?(\\d+|\\w+)('|\")?\\s*=\\s*('|\")?(\\d+|\\w+)|" +
            "UNION(\\s+ALL)?\\s+SELECT|" +
            "--|/\\*|\\*/|;\\s*(DROP|DELETE|TRUNCATE|ALTER|EXEC|EXECUTE)\\s+",
            Pattern.CASE_INSENSITIVE
    );

    private DataSanitizer() {
        // Utility class
    }

    /**
     * Strips dangerous script blocks and HTML markup from untrusted text inputs.
     *
     * @param input raw input string
     * @return clean plain-text string
     */
    public static String stripHtml(String input) {
        if (input == null) return "";
        String clean = SCRIPT_PATTERN.matcher(input).replaceAll("");
        clean = HTML_TAG_PATTERN.matcher(clean).replaceAll("");
        return clean.trim();
    }

    /**
     * Escapes standard HTML special characters (&, <, >, ", ') to prevent XSS injection.
     *
     * @param input plain text string
     * @return HTML-escaped string
     */
    public static String escapeHtml(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;"); break;
                case '<':  sb.append("&lt;"); break;
                case '>':  sb.append("&gt;"); break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#x27;"); break;
                default:   sb.append(c); break;
            }
        }
        return sb.toString();
    }

    /**
     * Trims leading and trailing whitespace and collapses consecutive spaces into one.
     */
    public static String normalizeWhitespace(String input) {
        if (input == null) return "";
        String stripped = CONTROL_CHARS_PATTERN.matcher(input).replaceAll("");
        return MULTI_SPACE_PATTERN.matcher(stripped.trim()).replaceAll(" ");
    }

    /**
     * Normalizes an email address by stripping whitespace and converting to lowercase.
     */
    public static String normalizeEmail(String email) {
        if (email == null) return "";
        String clean = email.trim().toLowerCase();
        // Remove control characters and internal whitespace
        clean = CONTROL_CHARS_PATTERN.matcher(clean).replaceAll("");
        return clean.replaceAll("\\s+", "");
    }

    /**
     * Normalizes a phone number by keeping only digits, spaces, hyphens, and an optional leading '+'.
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        String trimmed = phone.trim();
        boolean hasPlus = trimmed.startsWith("+");
        // Keep only digits
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) return "";
        return hasPlus ? "+" + digitsOnly : digitsOnly;
    }

    /**
     * Normalizes a shipping address: strips control characters, collapses whitespace,
     * and trims leading/trailing spaces.
     */
    public static String normalizeAddress(String address) {
        if (address == null) return "";
        String clean = CONTROL_CHARS_PATTERN.matcher(address).replaceAll(" ");
        clean = stripHtml(clean);
        return MULTI_SPACE_PATTERN.matcher(clean.trim()).replaceAll(" ");
    }

    /**
     * Safely truncates a string to fit within database column length limits.
     *
     * @param text      string to truncate
     * @param maxLength maximum allowed characters
     * @return truncated string, or original if within limits
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) return null;
        if (maxLength <= 0) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength);
    }

    /**
     * Heuristic scanner for detecting potential SQL injection signatures in user input.
     * Note: PreparedStatements are always the primary defense; this provides defense-in-depth auditing.
     *
     * @param input user string to inspect
     * @return {@code true} if high-risk SQL injection signature is detected
     */
    public static boolean detectSqlInjectionRisk(String input) {
        if (input == null || input.isBlank()) return false;
        return SQLI_PATTERN.matcher(input).find();
    }

    /**
     * General string sanitizer for standard database text columns.
     * Strips HTML, removes control characters, collapses whitespace, and enforces max length.
     */
    public static String sanitizeText(String input, int maxLength) {
        if (input == null) return "";
        String clean = stripHtml(input);
        clean = normalizeWhitespace(clean);
        return truncate(clean, maxLength);
    }
}

