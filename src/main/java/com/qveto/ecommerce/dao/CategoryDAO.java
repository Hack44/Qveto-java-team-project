package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'categories' table.
 *
 * Provides dedicated CRUD operations, product count aggregations,
 * and category verification for the e-commerce catalog.
 *
 * All queries use PreparedStatements to prevent SQL injection.
 *
 * Member 1 — DB/JDBC layer.
 */
public class CategoryDAO {

    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    /**
     * Inserts a new category into the database.
     *
     * @param categoryName the name of the new category
     * @return the auto-generated category_id, or -1 on failure
     */
    public int addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return -1;
        }

        String sql = "INSERT INTO categories (category_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categoryName.trim());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] addCategory() failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Inserts a new Category entity.
     *
     * @param category the {@link Category} to insert
     * @return the auto-generated category_id, or -1 on failure
     */
    public int addCategory(Category category) {
        if (category == null) {
            return -1;
        }
        int id = addCategory(category.getCategoryName());
        if (id > 0) {
            category.setCategoryId(id);
        }
        return id;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves a category by its primary key.
     *
     * @param categoryId primary key of the category
     * @return the matching {@link Category}, or {@code null} if not found
     */
    public Category findById(int categoryId) {
        String sql = "SELECT category_id, category_name FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] findById() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a category by its unique name (case-insensitive).
     *
     * @param categoryName category name to search
     * @return the matching {@link Category}, or {@code null} if not found
     */
    public Category findByName(String categoryName) {
        if (categoryName == null) {
            return null;
        }

        String sql = "SELECT category_id, category_name FROM categories WHERE LOWER(category_name) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] findByName() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all categories ordered alphabetically by category name.
     *
     * @return list of {@link Category} records; empty list if none
     */
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] findAll() failed: " + e.getMessage());
        }
        return categories;
    }

    /**
     * Returns all categories including the count of active products in each category.
     * Useful for admin inventory and category overview screens.
     *
     * @return list of {@link Category} entities with productCount populated
     */
    public List<Category> findAllWithProductCounts() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT c.category_id, c.category_name, COUNT(p.product_id) AS product_count "
                   + "FROM categories c "
                   + "LEFT JOIN products p ON c.category_id = p.category_id "
                   + "GROUP BY c.category_id, c.category_name "
                   + "ORDER BY c.category_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getInt("product_count")
                );
                categories.add(cat);
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] findAllWithProductCounts() failed: " + e.getMessage());
        }
        return categories;
    }

    /**
     * Counts how many products currently belong to a specific category.
     *
     * @param categoryId the category to inspect
     * @return count of products in that category
     */
    public int countProductsInCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] countProductsInCategory() failed: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if a category with the specified name already exists.
     *
     * @param categoryName name to verify
     * @return {@code true} if category exists
     */
    public boolean existsByName(String categoryName) {
        if (categoryName == null) {
            return false;
        }

        String sql = "SELECT 1 FROM categories WHERE LOWER(category_name) = LOWER(?) LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] existsByName() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns total number of registered categories.
     *
     * @return total count
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM categories";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] countAll() failed: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates an existing category's name.
     *
     * @param categoryId      the category to update
     * @param newCategoryName the new name to assign
     * @return {@code true} if update succeeded
     */
    public boolean updateCategory(int categoryId, String newCategoryName) {
        if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
            return false;
        }

        String sql = "UPDATE categories SET category_name = ? WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCategoryName.trim());
            stmt.setInt(2, categoryId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] updateCategory() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    /**
     * Safely deletes a category by its ID.
     * Prevents deletion if products are still assigned to this category
     * to maintain foreign key integrity.
     *
     * @param categoryId the category to delete
     * @return {@code true} if deleted successfully, {@code false} if deletion failed or products exist
     */
    public boolean deleteById(int categoryId) {
        if (countProductsInCategory(categoryId) > 0) {
            System.err.println("[CategoryDAO] Cannot delete category ID " + categoryId + ": active products are associated.");
            return false;
        }

        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] deleteById() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Private Helpers
    // ---------------------------------------------------------------

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("category_id"),
            rs.getString("category_name")
        );
    }
}
