package com.qveto.ecommerce.model;

import java.math.BigDecimal;

/**
 * Model entity representing a product in the catalog.
 * Maps to the 'products' table in the database schema.
 */
public class Product {
    private int productId;
    private int categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private int lowStockThreshold;
    private String createdAt;

    public Product() {
        this.lowStockThreshold = 5;
    }

    public Product(int productId, int categoryId, String productName,
                   String description, BigDecimal price, int stockQuantity,
                   int lowStockThreshold) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public Product(int productId, int categoryId, String categoryName, String productName,
                   String description, BigDecimal price, int stockQuantity, int lowStockThreshold) {
        this(productId, categoryId, productName, description, price, stockQuantity, lowStockThreshold);
        this.categoryName = categoryName;
    }

    public Product(int productId, int categoryId, String categoryName, String productName,
                   String description, double price, int stockQuantity) {
        this(productId, categoryId, categoryName, productName, description, BigDecimal.valueOf(price), stockQuantity, 5);
    }

    public Product(int productId, String productName, String categoryName,
                   String description, double price, int stockQuantity) {
        this(productId, 1, categoryName, productName, description, BigDecimal.valueOf(price), stockQuantity, 5);
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    // Convenience alias for UI integration
    public String getCategory() {
        return categoryName != null ? categoryName : "General";
    }
    public void setCategory(String category) {
        this.categoryName = category;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public double getPriceAsDouble() {
        return price != null ? price.doubleValue() : 0.0;
    }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * Returns true if current stock is at or below the low-stock threshold.
     */
    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public String getStockStatus() {
        if (isOutOfStock()) return "Out of Stock";
        if (isLowStock()) return "Low Stock (" + stockQuantity + ")";
        return "In Stock (" + stockQuantity + ")";
    }

    /**
     * Returns true if the requested quantity can be fulfilled from current stock.
     */
    public boolean hasSufficientStock(int requestedQty) {
        return stockQuantity >= requestedQty;
    }

    @Override
    public String toString() {
        return productName + " (ID: " + productId + ", Stock: " + stockQuantity + ")";
    }
}
