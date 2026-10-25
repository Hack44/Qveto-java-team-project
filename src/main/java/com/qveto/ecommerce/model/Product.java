package com.qveto.ecommerce.model;

/**
 * Model entity representing a catalog product.
 * Maps to the 'products' table in the database schema.
 */
public class Product {
    private int productId;
    private String productName;
    private String category;
    private String description;
    private double price;
    private int stockQuantity;
    private int lowStockThreshold;

    public Product() {
        this.lowStockThreshold = 5;
    }

    public Product(int productId, String productName, String category, String description, double price, int stockQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = 5;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public String getStockStatus() {
        if (isOutOfStock()) return "Out of Stock";
        if (isLowStock()) return "Low Stock";
        return "In Stock";
    }
}
