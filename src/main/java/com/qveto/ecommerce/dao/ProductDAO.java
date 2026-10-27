package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.Category;
import com.qveto.ecommerce.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'products' and 'categories' tables.
 *
 * Handles full CRUD for products, category lookups, and inventory
 * stock operations needed by the catalog and admin modules.
 *
 * Member 1 — DB/JDBC layer.
 */
public class ProductDAO {

    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    /**
     * Inserts a new product into the database.
     *
     * @param product a {@link Product} object with all fields populated
     *                (productId is ignored — DB auto-increments)
     * @return the auto-generated product_id, or -1 on failure
     */
    public int addProduct(Product product) {
        String sql = "INSERT INTO products (category_id, product_name, description, price, "
                   + "stock_quantity, low_stock_threshold) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.setInt(6, product.getLowStockThreshold());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] addProduct() failed: " + e.getMessage());
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves a single product by its primary key, including its category name.
     *
     * @param productId the product_id to look up
     * @return the {@link Product}, or {@code null} if not found
     */
    public Product findById(int productId) {
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.product_name, "
                   + "p.description, p.price, p.stock_quantity, p.low_stock_threshold, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE p.product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] findById() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all products joined with their category names, ordered by name.
     *
     * @return list of all {@link Product} records; empty list if none
     */
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.product_name, "
                   + "p.description, p.price, p.stock_quantity, p.low_stock_threshold, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "ORDER BY p.product_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] findAll() failed: " + e.getMessage());
        }
        return products;
    }

    /**
     * Returns all products belonging to a specific category.
     *
     * @param categoryId the category_id to filter by
     * @return filtered list of {@link Product} records
     */
    public List<Product> findByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.product_name, "
                   + "p.description, p.price, p.stock_quantity, p.low_stock_threshold, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE p.category_id = ? "
                   + "ORDER BY p.product_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] findByCategory() failed: " + e.getMessage());
        }
        return products;
    }

    /**
     * Full-text keyword search across product_name and description columns.
     *
     * @param keyword the search term (partial match, case-insensitive)
     * @return matching {@link Product} list; empty if no results
     */
    public List<Product> searchByKeyword(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.product_name, "
                   + "p.description, p.price, p.stock_quantity, p.low_stock_threshold, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE LOWER(p.product_name) LIKE ? OR LOWER(p.description) LIKE ? "
                   + "ORDER BY p.product_name ASC";

        String pattern = "%" + keyword.toLowerCase() + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] searchByKeyword() failed: " + e.getMessage());
        }
        return products;
    }

    /**
     * Returns all products whose stock_quantity is at or below their low_stock_threshold.
     * Used by the Admin Reports/KPIs panel.
     *
     * @return list of low-stock {@link Product} records
     */
    public List<Product> findLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.category_id, c.category_name, p.product_name, "
                   + "p.description, p.price, p.stock_quantity, p.low_stock_threshold, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN categories c ON p.category_id = c.category_id "
                   + "WHERE p.stock_quantity <= p.low_stock_threshold "
                   + "ORDER BY p.stock_quantity ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] findLowStockProducts() failed: " + e.getMessage());
        }
        return products;
    }

    /**
     * Returns the total number of products currently in the catalog.
     *
     * @return count of all products, or 0 on error
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] countAll() failed: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates all editable fields of an existing product.
     *
     * @param product the {@link Product} with updated values (product_id must be set)
     * @return {@code true} if the update was applied
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET category_id = ?, product_name = ?, description = ?, "
                   + "price = ?, stock_quantity = ?, low_stock_threshold = ? "
                   + "WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.setInt(6, product.getLowStockThreshold());
            stmt.setInt(7, product.getProductId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductDAO] updateProduct() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Adjusts the stock_quantity of a product by a delta amount.
     * Use a positive delta to increase stock (restock), negative to decrease (sale/reservation).
     * The DB CHECK constraint (stock_quantity >= 0) prevents going below zero.
     *
     * @param productId the product to update
     * @param delta     the quantity change (positive = add, negative = subtract)
     * @return {@code true} if the stock was updated successfully
     */
    public boolean adjustStock(int productId, int delta) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, delta);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductDAO] adjustStock() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    /**
     * Deletes a product from the catalog by its primary key.
     * Note: will fail if existing order_items or cart_items reference this product.
     *
     * @param productId the product_id to delete
     * @return {@code true} if deletion succeeded
     */
    public boolean deleteById(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductDAO] deleteById() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Category helpers
    // ---------------------------------------------------------------

    /**
     * Returns all categories ordered by name.
     *
     * @return list of {@link Category} records; empty if none
     */
    public List<Category> findAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(rs.getInt("category_id"), rs.getString("category_name")));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] findAllCategories() failed: " + e.getMessage());
        }
        return categories;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Product} object.
     * Does NOT call rs.next() — the caller must position the cursor.
     */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name") != null ? rs.getString("category_name") : "");
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description") != null ? rs.getString("description") : "");
        p.setPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO);
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        p.setCreatedAt(rs.getString("created_at") != null ? rs.getString("created_at") : "");
        return p;
    }
}
