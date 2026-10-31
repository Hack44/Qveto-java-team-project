package com.qveto.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model entity representing a payment transaction for an order.
 * Maps to the 'payments' table in the database schema.
 *
 * Member 1 — DB/Model layer.
 */
public class Payment {
    private int paymentId;
    private int orderId;
    private String paymentMethod; // "COD", "CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING"
    private String paymentStatus; // "PENDING", "COMPLETED", "FAILED", "REFUNDED"
    private BigDecimal amount;
    private String transactionRef; // Simulated gateway reference ID
    private String createdAt;

    public Payment() {
        this.amount = BigDecimal.ZERO;
        this.paymentStatus = "PENDING";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Payment(int paymentId, int orderId, String paymentMethod, String paymentStatus,
                   BigDecimal amount, String createdAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod != null ? paymentMethod.toUpperCase() : "COD";
        this.paymentStatus = paymentStatus != null ? paymentStatus.toUpperCase() : "PENDING";
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.createdAt = createdAt != null ? createdAt
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.transactionRef = generateTransactionRef(paymentId, orderId);
    }

    public Payment(int orderId, String paymentMethod, String paymentStatus, BigDecimal amount) {
        this(0, orderId, paymentMethod, paymentStatus, amount, null);
    }

    public Payment(int orderId, String paymentMethod, String paymentStatus, double amount) {
        this(0, orderId, paymentMethod, paymentStatus, BigDecimal.valueOf(amount), null);
    }

    private String generateTransactionRef(int paymentId, int orderId) {
        return "TXN-" + (orderId > 0 ? orderId : "SIM") + "-" + (System.currentTimeMillis() % 1000000);
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
        if (this.transactionRef == null || this.transactionRef.isEmpty()) {
            this.transactionRef = generateTransactionRef(paymentId, this.orderId);
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : "COD";
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod.toUpperCase() : "COD";
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : "PENDING";
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus != null ? paymentStatus.toUpperCase() : "PENDING";
    }

    public BigDecimal getAmount() {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public double getAmountAsDouble() {
        return amount != null ? amount.doubleValue() : 0.0;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(paymentStatus);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(paymentStatus);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(paymentStatus);
    }

    public boolean isRefunded() {
        return "REFUNDED".equalsIgnoreCase(paymentStatus);
    }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " [Order #" + orderId + ", " + paymentMethod
                + ", " + paymentStatus + ", Rs. " + amount + "]";
    }
}
