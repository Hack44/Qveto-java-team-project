package com.qveto.ecommerce.model;

/**
 * Model entity representing a product category.
 * Maps to the 'categories' table in the database schema.
 *
 * Member 1 — DB/Model layer.
 */
public class Category {
    private int categoryId;
    private String categoryName;
    private int productCount;

    public Category() {
    }

    public Category(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productCount = 0;
    }

    public Category(int categoryId, String categoryName, int productCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productCount = productCount;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }

    @Override
    public String toString() {
        return categoryName;
    }
}
