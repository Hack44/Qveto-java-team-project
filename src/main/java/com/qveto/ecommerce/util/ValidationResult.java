package com.qveto.ecommerce.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the outcome of domain boundary and persistence input validation.
 *
 * Collects field-specific validation errors and provides convenience methods
 * to inspect validity and retrieve formatted error summaries.
 *
 * Member 1 — Persistence, Data Sanitization & Validation layer.
 */
public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public ValidationResult() {
    }

    public static ValidationResult success() {
        return new ValidationResult();
    }

    public static ValidationResult failure(String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.addError(errorMessage);
        return result;
    }

    public static ValidationResult failure(String field, String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.addError(field, errorMessage);
        return result;
    }

    public void addError(String message) {
        if (message != null && !message.trim().isEmpty()) {
            errors.add(message.trim());
        }
    }

    public void addError(String field, String message) {
        if (message != null && !message.trim().isEmpty()) {
            String fieldPrefix = (field != null && !field.trim().isEmpty()) ? field.trim() + ": " : "";
            errors.add(fieldPrefix + message.trim());
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    public String getCombinedErrorMessage() {
        if (errors.isEmpty()) return "";
        return String.join("; ", errors);
    }

    @Override
    public String toString() {
        return isValid() ? "ValidationResult[VALID]" : "ValidationResult[INVALID: " + getCombinedErrorMessage() + "]";
    }
}

