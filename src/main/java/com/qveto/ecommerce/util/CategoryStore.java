package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.CategoryDAO;
import com.qveto.ecommerce.model.Category;
import com.qveto.ecommerce.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Category Store coordinating between the JDBC CategoryDAO layer and
 * in-memory fallback state for standalone demo mode.
 * 
 * Supports Member 2 requirements:
 * - Dynamic category retrieval for customer search & filtering
 * - Category lookups for Add/Edit Product dialogs
 * - Admin Category Management (Add, Rename, Delete with product count safeguards)
 */
public class CategoryStore {

    private static final List<Category> inMemoryCategories = new CopyOnWriteArrayList<>();
    private static int nextMockCategoryId = 10;

    static {
        initializeSampleCategories();
    }

    private CategoryStore() {
        // Utility class
    }

    private static void initializeSampleCategories() {
        inMemoryCategories.clear();
        inMemoryCategories.add(new Category(1, "Electronics"));
        inMemoryCategories.add(new Category(2, "Clothing"));
        inMemoryCategories.add(new Category(3, "Books"));
        inMemoryCategories.add(new Category(4, "Home"));
        inMemoryCategories.add(new Category(5, "Sports"));
        inMemoryCategories.add(new Category(6, "Accessories"));
    }

    /**
     * Returns all categories with product counts populated.
     */
    public static List<Category> getAllCategoriesWithCounts() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<Category> dbList = new CategoryDAO().findAllWithProductCounts();
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Throwable t) {
                System.err.println("[CategoryStore] DB findAllWithProductCounts fallback: " + t.getMessage());
            }
        }

        // Calculate counts from ProductStore in demo mode
        List<Product> products = ProductStore.getAllProducts();
        List<Category> result = new ArrayList<>();
        for (Category cat : inMemoryCategories) {
            int count = 0;
            for (Product p : products) {
                if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(cat.getCategoryName())) {
                    count++;
                }
            }
            result.add(new Category(cat.getCategoryId(), cat.getCategoryName(), count));
        }
        return result;
    }

    /**
     * Returns a list of category names (e.g. for dropdowns).
     */
    public static List<String> getCategoryNames() {
        List<Category> categories = getAllCategoriesWithCounts();
        List<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getCategoryName());
        }
        return names;
    }

    /**
     * Checks if a category name already exists (case-insensitive).
     */
    public static boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String clean = name.trim();

        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                return new CategoryDAO().existsByName(clean);
            } catch (Throwable t) {
                System.err.println("[CategoryStore] DB existsByName fallback: " + t.getMessage());
            }
        }

        for (Category c : inMemoryCategories) {
            if (c.getCategoryName().equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new category to the store and DB.
     *
     * @param categoryName the name of the new category
     * @return the newly created {@link Category}, or null on failure
     */
    public static Category addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return null;
        String clean = categoryName.trim();

        int assignedId = nextMockCategoryId++;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int dbId = new CategoryDAO().addCategory(clean);
                if (dbId > 0) {
                    assignedId = dbId;
                }
            } catch (Throwable t) {
                System.err.println("[CategoryStore] DB addCategory fallback: " + t.getMessage());
            }
        }

        Category newCat = new Category(assignedId, clean, 0);
        inMemoryCategories.add(newCat);
        return newCat;
    }

    /**
     * Renames an existing category.
     */
    public static boolean updateCategory(int categoryId, String newName) {
        if (newName == null || newName.trim().isEmpty()) return false;
        String clean = newName.trim();

        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new CategoryDAO().updateCategory(categoryId, clean);
            } catch (Throwable t) {
                System.err.println("[CategoryStore] DB updateCategory fallback: " + t.getMessage());
            }
        }

        for (Category c : inMemoryCategories) {
            if (c.getCategoryId() == categoryId) {
                String oldName = c.getCategoryName();
                c.setCategoryName(clean);

                // Also update any products in memory referencing the old category name
                for (Product p : ProductStore.getAllProducts()) {
                    if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(oldName)) {
                        p.setCategory(clean);
                    }
                }
                return true;
            }
        }
        return dbSuccess;
    }

    /**
     * Deletes a category by its ID.
     * Ensures no active products belong to this category before deleting.
     *
     * @param categoryId ID of the category to delete
     * @return true if deleted successfully; false if products exist or deletion failed
     */
    public static boolean deleteCategory(int categoryId) {
        // First check product count
        for (Category c : getAllCategoriesWithCounts()) {
            if (c.getCategoryId() == categoryId) {
                if (c.getProductCount() > 0) {
                    return false; // active products exist
                }
                break;
            }
        }

        boolean dbSuccess = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbSuccess = new CategoryDAO().deleteById(categoryId);
            } catch (Throwable t) {
                System.err.println("[CategoryStore] DB deleteById fallback: " + t.getMessage());
            }
        }

        boolean memSuccess = inMemoryCategories.removeIf(c -> c.getCategoryId() == categoryId);
        return dbSuccess || memSuccess;
    }
}
