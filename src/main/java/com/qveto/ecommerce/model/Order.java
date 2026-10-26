package com.qveto.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model entity representing a customer order.
 * Maps to the 'orders' table in the database schema.
 */
public class Order {
    private int orderId;
    private String orderNumber;
    private int userId;
    private double totalAmount;
    private String orderStatus; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String paymentMethod; // COD, PREPAID
    private String shippingAddress;
    private String orderDate;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
    }

    public Order(int orderId, String orderNumber, int userId, double totalAmount, String orderStatus, String paymentMethod, String orderDate) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus.toUpperCase();
        this.paymentMethod = paymentMethod.toUpperCase();
        this.orderDate = orderDate;
        this.items = new ArrayList<>();
    }

    public Order(int orderId, String orderNumber, int userId, double totalAmount, String orderStatus, String paymentMethod, String shippingAddress, String orderDate) {
        this(orderId, orderNumber, userId, totalAmount, orderStatus, paymentMethod, orderDate);
        this.shippingAddress = shippingAddress;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus.toUpperCase();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        int sum = 0;
        for (OrderItem item : items) {
            sum += item.getQuantity();
        }
        return sum;
    }

    /**
     * Determines whether this order can be cancelled by the customer.
     * Orders that are PENDING or CONFIRMED are eligible for cancellation.
     */
    public boolean isCancellable() {
        return "PENDING".equalsIgnoreCase(orderStatus) || "CONFIRMED".equalsIgnoreCase(orderStatus);
    }
}
