package com.qveto.ecommerce.model;

import java.math.BigDecimal;

/**
 * Model entity representing an item inside a customer's shopping cart.
 * Maps to the 'cart_items' table in the database schema.
 *
 * Member 1 — DB/Model layer.
 */
public class CartItem {
    private int cartItemId;
    private int userId;
    private int productId;
    private String productName;
    private String categoryName;
    private int quantity;
    private BigDecimal unitPrice;
    private int stockAvailable;

    public CartItem() {
        this.unitPrice = BigDecimal.ZERO;
    }

    public CartItem(int cartItemId, int userId, int productId, String productName,
                    int quantity, BigDecimal unitPrice) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public CartItem(int cartItemId, int userId, int productId, String productName,
                    String categoryName, int quantity, BigDecimal unitPrice, int stockAvailable) {
        this(cartItemId, userId, productId, productName, quantity, unitPrice);
        this.categoryName = categoryName;
        this.stockAvailable = stockAvailable;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
        return categoryName;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public double getUnitPriceAsDouble() {
        return unitPrice != null ? unitPrice.doubleValue() : 0.0;
    }

    public int getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(int stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    /**
     * Calculates line subtotal = quantity * unitPrice with full BigDecimal precision.
     *
     * @return subtotal as {@link BigDecimal}
     */
    public BigDecimal getSubtotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public double getSubtotalAsDouble() {
        return getSubtotal().doubleValue();
    }

    @Override
    public String toString() {
        return productName + " x " + quantity + " (@ Rs. " + unitPrice + ")";
    }
}
