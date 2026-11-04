package com.qveto.ecommerce.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized Promotional Coupon and Discount Store for managing e-commerce marketing promotions,
 * percentage and flat-rate rebates, minimum cart order thresholds, and checkout validation.
 *
 * Supports Member 2 (UI, Presentation & Store Management) checkout discount engine requirements.
 */
public class CouponStore {

    public enum DiscountType {
        PERCENTAGE,
        FLAT
    }

    public static class Coupon {
        private final String code;
        private final String description;
        private final DiscountType discountType;
        private final double discountValue;
        private final double minOrderAmount;
        private final double maxDiscount;

        public Coupon(String code, String description, DiscountType discountType,
                      double discountValue, double minOrderAmount, double maxDiscount) {
            this.code = code.toUpperCase();
            this.description = description;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.minOrderAmount = minOrderAmount;
            this.maxDiscount = maxDiscount;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
        public DiscountType getDiscountType() { return discountType; }
        public double getDiscountValue() { return discountValue; }
        public double getMinOrderAmount() { return minOrderAmount; }
        public double getMaxDiscount() { return maxDiscount; }

        public String getFormattedSummary() {
            if (discountType == DiscountType.PERCENTAGE) {
                return String.format("%s: %.0f%% off (Min: $%.0f, Max: $%.0f)", code, discountValue, minOrderAmount, maxDiscount);
            } else {
                return String.format("%s: $%.0f flat off (Min: $%.0f)", code, discountValue, minOrderAmount);
            }
        }
    }

    public static class CouponResult {
        private final boolean valid;
        private final String message;
        private final double discountAmount;
        private final Coupon coupon;

        public CouponResult(boolean valid, String message, double discountAmount, Coupon coupon) {
            this.valid = valid;
            this.message = message;
            this.discountAmount = Math.max(0.0, discountAmount);
            this.coupon = coupon;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public double getDiscountAmount() { return discountAmount; }
        public Coupon getCoupon() { return coupon; }
    }

    private static final Map<String, Coupon> ACTIVE_COUPONS = new ConcurrentHashMap<>();

    static {
        registerCoupon(new Coupon("QVETO10", "10% Off All Store Orders", DiscountType.PERCENTAGE, 10.0, 20.0, 50.0));
        registerCoupon(new Coupon("SAVE20", "20% Off Orders Above $80", DiscountType.PERCENTAGE, 20.0, 80.0, 40.0));
        registerCoupon(new Coupon("FLAT15", "$15 Instant Flat Discount", DiscountType.FLAT, 15.0, 60.0, 15.0));
        registerCoupon(new Coupon("WELCOME", "15% Welcome Customer Discount", DiscountType.PERCENTAGE, 15.0, 30.0, 25.0));
        registerCoupon(new Coupon("MEGA50", "$50 Off Bulk Orders Above $200", DiscountType.FLAT, 50.0, 200.0, 50.0));
    }

    private CouponStore() {
        // Utility class
    }

    public static void registerCoupon(Coupon coupon) {
        ACTIVE_COUPONS.put(coupon.getCode(), coupon);
    }

    public static List<Coupon> getAllCoupons() {
        return new ArrayList<>(ACTIVE_COUPONS.values());
    }

    public static Coupon findCoupon(String code) {
        if (code == null) return null;
        return ACTIVE_COUPONS.get(code.trim().toUpperCase());
    }

    /**
     * Validates a promotional code against the cart subtotal and calculates the discount amount.
     *
     * @param code promotional voucher code entered by customer
     * @param cartSubtotal current raw items subtotal before taxes
     * @return validation and discount calculation result
     */
    public static CouponResult validateAndApply(String code, double cartSubtotal) {
        if (code == null || code.trim().isEmpty()) {
            return new CouponResult(false, "Please enter a promotional discount code.", 0.0, null);
        }

        String cleanCode = code.trim().toUpperCase();
        Coupon coupon = ACTIVE_COUPONS.get(cleanCode);

        if (coupon == null) {
            return new CouponResult(false, "Invalid coupon code '" + cleanCode + "'. Please check spelling.", 0.0, null);
        }

        if (cartSubtotal < coupon.getMinOrderAmount()) {
            return new CouponResult(false,
                    String.format("Coupon '%s' requires a minimum order of $%.2f. Current subtotal: $%.2f.",
                            coupon.getCode(), coupon.getMinOrderAmount(), cartSubtotal),
                    0.0, coupon);
        }

        double discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = (cartSubtotal * coupon.getDiscountValue()) / 100.0;
            if (coupon.getMaxDiscount() > 0 && discount > coupon.getMaxDiscount()) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = Math.min(coupon.getDiscountValue(), cartSubtotal);
        }

        // Round to 2 decimal places
        discount = Math.round(discount * 100.0) / 100.0;

        String successMsg = String.format("Promo code '%s' applied! Discount: -$%.2f", coupon.getCode(), discount);
        return new CouponResult(true, successMsg, discount, coupon);
    }
}
