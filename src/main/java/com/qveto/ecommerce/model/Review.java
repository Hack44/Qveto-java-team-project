package com.qveto.ecommerce.model;

/**
 * Model entity representing verified customer product reviews and star ratings.
 * Supports Member 2 (UI, Presentation & Store Management) customer feedback requirements.
 */
public class Review {
    private int reviewId;
    private int productId;
    private int userId;
    private String customerName;
    private int rating; // 1 to 5 stars
    private String headline;
    private String comment;
    private String reviewDate;
    private boolean isVerifiedPurchase;

    public Review(int reviewId, int productId, int userId, String customerName, int rating,
                  String headline, String comment, String reviewDate, boolean isVerifiedPurchase) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.customerName = customerName;
        this.rating = Math.max(1, Math.min(5, rating));
        this.headline = headline;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.isVerifiedPurchase = isVerifiedPurchase;
    }

    public int getReviewId() { return reviewId; }
    public int getProductId() { return productId; }
    public int getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getHeadline() { return headline; }
    public String getComment() { return comment; }
    public String getReviewDate() { return reviewDate; }
    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }

    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rating; i++) sb.append("★");
        for (int i = rating; i < 5; i++) sb.append("☆");
        return sb.toString();
    }
}
