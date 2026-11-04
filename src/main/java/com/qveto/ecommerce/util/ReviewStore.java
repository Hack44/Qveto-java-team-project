package com.qveto.ecommerce.util;

import com.qveto.ecommerce.model.Review;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Store for managing verified customer ratings and product reviews.
 * Coordinates in-memory review state and calculations.
 *
 * Supports Member 2 (UI, Presentation & Store Management) customer discovery & feedback requirements.
 */
public class ReviewStore {

    private static final Map<Integer, List<Review>> productReviews = new ConcurrentHashMap<>();
    private static int nextReviewId = 100;

    static {
        seedSampleReviews();
    }

    private ReviewStore() {
        // Utility class
    }

    private static void seedSampleReviews() {
        productReviews.clear();

        // 101: Wireless Bluetooth Headphones
        List<Review> r101 = new CopyOnWriteArrayList<>();
        r101.add(new Review(1, 101, 1, "Alex Morgan", 5, "Phenomenal Soundstage & ANC", "The active noise cancellation is top notch. Deep rich bass and comfortable memory foam pads.", "2026-10-18", true));
        r101.add(new Review(2, 101, 2, "Sarah Jenkins", 4, "Great Daily Drivers", "Battery easily lasts 35+ hours. Very happy with the USB-C fast charging.", "2026-10-22", true));
        productReviews.put(101, r101);

        // 102: Mechanical Gaming Keyboard
        List<Review> r102 = new CopyOnWriteArrayList<>();
        r102.add(new Review(3, 102, 1, "David Chen", 5, "Crisp Tactile Feedback", "Blue switches feel amazing for typing code. RGB backlighting modes are vibrant.", "2026-10-19", true));
        r102.add(new Review(4, 102, 3, "Priya Sharma", 5, "Built Like a Tank", "Heavy duty aluminum chassis. Solid keystroke sound with zero wobble.", "2026-10-25", true));
        productReviews.put(102, r102);

        // 103: Classic Cotton T-Shirt
        List<Review> r103 = new CopyOnWriteArrayList<>();
        r103.add(new Review(5, 103, 2, "Michael Scott", 4, "Super Soft & Breathable", "100% combed cotton fits true to size. Doesn't shrink in normal wash.", "2026-10-20", true));
        productReviews.put(103, r103);

        // 104: Java Programming: OOP Essentials
        List<Review> r104 = new CopyOnWriteArrayList<>();
        r104.add(new Review(6, 104, 1, "Rohan Verma", 5, "Must-Read for Software Engineers", "Covers Design Patterns, Collections, and Multi-threading with crystal clear visual diagrams.", "2026-10-28", true));
        productReviews.put(104, r104);
    }

    public static List<Review> getReviewsForProduct(int productId) {
        return productReviews.getOrDefault(productId, Collections.emptyList());
    }

    public static double getAverageRating(int productId) {
        List<Review> list = productReviews.get(productId);
        if (list == null || list.isEmpty()) return 4.5; // default fallback
        double sum = 0;
        for (Review r : list) sum += r.getRating();
        return Math.round((sum / list.size()) * 10.0) / 10.0;
    }

    public static int getReviewCount(int productId) {
        List<Review> list = productReviews.get(productId);
        return list != null ? list.size() : 0;
    }

    public static void addReview(Review review) {
        int id = nextReviewId++;
        Review toAdd = new Review(
                id,
                review.getProductId(),
                review.getUserId(),
                review.getCustomerName(),
                review.getRating(),
                review.getHeadline(),
                review.getComment(),
                review.getReviewDate(),
                review.isVerifiedPurchase()
        );

        productReviews.computeIfAbsent(toAdd.getProductId(), k -> new CopyOnWriteArrayList<>()).add(0, toAdd);
    }
}
