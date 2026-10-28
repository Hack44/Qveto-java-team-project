package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.ProductDAO;
import com.qveto.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Product Store coordinating between the JDBC ProductDAO layer and
 * in-memory fallback state for standalone demo mode.
 * 
 * Supports Member 2 requirements:
 * - Product catalog discovery & search
 * - Live inventory tracking & low stock detection
 * - Admin product creation, updates, restocking, and deletion
 */
public class ProductStore {

    private static final List<Product> inMemoryProducts = new CopyOnWriteArrayList<>();
    private static int nextMockProductId = 110;

    static {
        initializeSampleProducts();
    }

    private ProductStore() {
        // Utility class
    }

    private static void initializeSampleProducts() {
        inMemoryProducts.clear();
        inMemoryProducts.add(new Product(101, 1, "Electronics", "Wireless Bluetooth Headphones",
                "Noise-cancelling over-ear wireless headphones with deep bass", BigDecimal.valueOf(59.99), 25, 5));
        inMemoryProducts.add(new Product(102, 1, "Electronics", "Mechanical Gaming Keyboard",
                "RGB backlit blue switches with mechanical tactile feedback", BigDecimal.valueOf(79.99), 14, 5));
        inMemoryProducts.add(new Product(103, 2, "Clothing", "Classic Cotton T-Shirt",
                "100% organic combed cotton everyday unisex wear", BigDecimal.valueOf(19.99), 4, 5));
        inMemoryProducts.add(new Product(104, 3, "Books", "Java Programming: OOP Essentials",
                "Comprehensive guide to OOP, Collections, and Design Patterns in Java", BigDecimal.valueOf(39.99), 30, 5));
        inMemoryProducts.add(new Product(105, 4, "Home", "Stainless Steel Water Bottle",
                "Vacuum insulated 750ml double-walled thermal flask", BigDecimal.valueOf(14.99), 40, 10));
        inMemoryProducts.add(new Product(106, 1, "Electronics", "USB-C Fast Charging Hub",
                "6-in-1 multi-port adapter with 4K HDMI and 65W Power Delivery", BigDecimal.valueOf(29.99), 3, 5));
    }

    /**
     * Returns all products in the catalog.
     */
    public static List<Product> getAllProducts() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<Product> dbList = new ProductDAO().findAll();
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB findAll fallback: " + t.getMessage());
            }
        }
        return new ArrayList<>(inMemoryProducts);
    }

    /**
     * Searches and filters products by keyword and category.
     */
    public static List<Product> searchProducts(String keyword, String category) {
        String cleanKeyword = (keyword != null) ? keyword.trim().toLowerCase() : "";
        String cleanCategory = (category != null) ? category.trim() : "All Categories";

        List<Product> all = getAllProducts();
        List<Product> results = new ArrayList<>();

        for (Product p : all) {
            boolean matchesCat = "All Categories".equalsIgnoreCase(cleanCategory)
                    || p.getCategory().equalsIgnoreCase(cleanCategory);

            boolean matchesKeyword = cleanKeyword.isEmpty()
                    || p.getProductName().toLowerCase().contains(cleanKeyword)
                    || p.getDescription().toLowerCase().contains(cleanKeyword)
                    || String.valueOf(p.getProductId()).contains(cleanKeyword);

            if (matchesCat && matchesKeyword) {
                results.add(p);
            }
        }
        return results;
    }

    /**
     * Returns products currently at or below their low stock threshold.
     */
    public static List<Product> getLowStockProducts() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<Product> dbList = new ProductDAO().findLowStockProducts();
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB findLowStockProducts fallback: " + t.getMessage());
            }
        }

        List<Product> lowStock = new ArrayList<>();
        for (Product p : inMemoryProducts) {
            if (p.isLowStock() || p.isOutOfStock()) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    /**
     * Adds a new product to the catalog.
     */
    public static Product addProduct(Product product) {
        int assignedId = nextMockProductId++;

        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int dbId = new ProductDAO().addProduct(product);
                if (dbId > 0) {
                    assignedId = dbId;
                }
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB addProduct fallback: " + t.getMessage());
            }
        }

        product.setProductId(assignedId);
        inMemoryProducts.add(product);
        return product;
    }

    /**
     * Updates an existing product's details.
     */
    public static boolean updateProduct(Product product) {
        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new ProductDAO().updateProduct(product);
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB updateProduct fallback: " + t.getMessage());
            }
        }

        for (int i = 0; i < inMemoryProducts.size(); i++) {
            if (inMemoryProducts.get(i).getProductId() == product.getProductId()) {
                inMemoryProducts.set(i, product);
                return true;
            }
        }
        return dbSuccess;
    }

    /**
     * Adjusts the inventory stock of a product by delta (+ for restock, - for order).
     */
    public static boolean adjustStock(int productId, int delta) {
        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new ProductDAO().adjustStock(productId, delta);
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB adjustStock fallback: " + t.getMessage());
            }
        }

        for (Product p : inMemoryProducts) {
            if (p.getProductId() == productId) {
                int newStock = Math.max(0, p.getStockQuantity() + delta);
                p.setStockQuantity(newStock);
                return true;
            }
        }
        return dbSuccess;
    }

    /**
     * Deletes a product from the catalog.
     */
    public static boolean deleteProduct(int productId) {
        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new ProductDAO().deleteById(productId);
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB deleteById fallback: " + t.getMessage());
            }
        }

        boolean memSuccess = inMemoryProducts.removeIf(p -> p.getProductId() == productId);
        return dbSuccess || memSuccess;
    }

    /**
     * Returns the total number of products in the catalog.
     */
    public static int countAll() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int count = new ProductDAO().countAll();
                if (count > 0) return count;
            } catch (Throwable t) {
                System.err.println("[ProductStore] DB countAll fallback: " + t.getMessage());
            }
        }
        return inMemoryProducts.size();
    }
}
