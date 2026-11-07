package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Store for managing customer saved items and product wishlists.
 * Coordinates wishlist persistence and cart transfers.
 *
 * Supports Member 2 (UI, Presentation & Store Management) customer discovery requirements.
 */
public class WishlistStore {

    private static final Map<Integer, List<Product>> userWishlists = new ConcurrentHashMap<>();

    static {
        seedSampleWishlist();
    }

    private WishlistStore() {
        // Utility class
    }

    private static void seedSampleWishlist() {
        userWishlists.clear();
        // Seed customer #1 with sample wishlist items
        List<Product> customerWishlist = new CopyOnWriteArrayList<>();
        Product p102 = ProductStore.findById(102);
        if (p102 != null) customerWishlist.add(p102);

        Product p104 = ProductStore.findById(104);
        if (p104 != null) customerWishlist.add(p104);

        userWishlists.put(1, customerWishlist);
    }

    public static List<Product> getWishlist(int userId) {
        return userWishlists.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
    }

    public static boolean addToWishlist(int userId, Product product) {
        if (product == null) return false;
        List<Product> list = getWishlist(userId);
        for (Product p : list) {
            if (p.getProductId() == product.getProductId()) {
                return false; // Already in wishlist
            }
        }
        list.add(0, product);
        return true;
    }

    public static boolean removeFromWishlist(int userId, int productId) {
        List<Product> list = getWishlist(userId);
        return list.removeIf(p -> p.getProductId() == productId);
    }

    public static boolean isInWishlist(int userId, int productId) {
        List<Product> list = getWishlist(userId);
        for (Product p : list) {
            if (p.getProductId() == productId) {
                return true;
            }
        }
        return false;
    }

    public static int getWishlistCount(int userId) {
        return getWishlist(userId).size();
    }

    public static void clearWishlist(int userId) {
        List<Product> list = userWishlists.get(userId);
        if (list != null) {
            list.clear();
        }
    }
}
