package com.qveto.ecommerce.model;

import java.math.BigDecimal;

/**
 * Model entity representing an item within a customer order.
 * Maps to the 'order_items' table in the database schema.
 *
 * Member 1 — DB/Model layer.
 */
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private String productName;
    private String categoryName;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem() {
        this.unitPrice = BigDecimal.ZERO;
    }

    public OrderItem(int orderItemId, int orderId, int productId, String productName, int quantity, double unitPrice) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = BigDecimal.valueOf(unitPrice);
    }

    public OrderItem(int orderItemId, int orderId, int productId, String productName, int quantity, BigDecimal unitPrice) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public OrderItem(int orderItemId, int orderId, int productId, String productName, String categoryName,
                     int quantity, BigDecimal unitPrice) {
        this(orderItemId, orderId, productId, productName, quantity, unitPrice);
        this.categoryName = categoryName;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : "";
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceAsBigDecimal() {
        return unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public double getUnitPrice() {
        return unitPrice != null ? unitPrice.doubleValue() : 0.0;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = BigDecimal.valueOf(unitPrice);
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public BigDecimal getSubtotalAsBigDecimal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public double getSubtotal() {
        return getSubtotalAsBigDecimal().doubleValue();
    }

    @Override
    public String toString() {
        return productName + " x " + quantity + " (@ Rs. " + unitPrice + ")";
    }
}
