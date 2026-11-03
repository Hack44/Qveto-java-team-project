package com.qveto.ecommerce.util;

import com.qveto.ecommerce.dao.PaymentDAO;
import com.qveto.ecommerce.model.Payment;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized Payment Store coordinating between the JDBC PaymentDAO layer and
 * in-memory transactional records for standalone demo mode.
 * 
 * Supports Member 2 (UI, Presentation & Store Management) payment processing,
 * simulated gateway transactions, settlement status synchronization, and revenue analytics.
 */
public class PaymentStore {

    private static final List<Payment> inMemoryPayments = new CopyOnWriteArrayList<>();
    private static int nextMockPaymentId = 100;

    static {
        initializeSamplePayments();
    }

    private PaymentStore() {
        // Utility class
    }

    private static void initializeSamplePayments() {
        inMemoryPayments.clear();
        // Order 1 (ORD-2026-001, DELIVERED, COD, $79.98) -> COMPLETED upon delivery
        inMemoryPayments.add(new Payment(1, 1, "COD", "COMPLETED", BigDecimal.valueOf(79.98), "2026-10-15 14:30:00"));
        // Order 2 (ORD-2026-002, SHIPPED, PREPAID, $59.99) -> COMPLETED online
        inMemoryPayments.add(new Payment(2, 2, "UPI", "COMPLETED", BigDecimal.valueOf(59.99), "2026-10-18 10:15:00"));
        // Order 3 (ORD-2026-003, PENDING, COD, $94.98) -> PENDING settlement
        inMemoryPayments.add(new Payment(3, 3, "COD", "PENDING", BigDecimal.valueOf(94.98), "2026-10-20 18:45:00"));
        // Order 4 (ORD-2026-004, CANCELLED, COD, $14.99) -> REFUNDED upon cancellation
        inMemoryPayments.add(new Payment(4, 4, "COD", "REFUNDED", BigDecimal.valueOf(14.99), "2026-10-12 09:20:00"));
    }

    /**
     * Records an explicit payment entity.
     *
     * @param payment the {@link Payment} record to persist
     * @return generated payment ID
     */
    public static int recordPayment(Payment payment) {
        if (payment == null) return -1;

        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                int dbId = new PaymentDAO().recordPayment(payment);
                if (dbId > 0) {
                    payment.setPaymentId(dbId);
                }
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB recordPayment fallback: " + t.getMessage());
            }
        }

        if (payment.getPaymentId() <= 0) {
            payment.setPaymentId(nextMockPaymentId++);
        }
        inMemoryPayments.add(payment);
        return payment.getPaymentId();
    }

    /**
     * Processes simulated gateway checkout transaction for an order.
     * For online channels (UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING), status is 'COMPLETED'.
     * For Cash on Delivery ('COD'), status is initially 'PENDING'.
     *
     * @param orderId       parent order ID
     * @param paymentMethod payment method selected by customer
     * @param amount        order amount
     * @return persisted {@link Payment} record
     */
    public static Payment processPayment(int orderId, String paymentMethod, BigDecimal amount) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                Payment dbPayment = new PaymentDAO().processSimulatedPayment(orderId, paymentMethod, amount);
                if (dbPayment != null && dbPayment.getPaymentId() > 0) {
                    inMemoryPayments.add(dbPayment);
                    return dbPayment;
                }
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB processSimulatedPayment fallback: " + t.getMessage());
            }
        }

        String cleanMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty())
                ? paymentMethod.trim().toUpperCase() : "COD";
        String status = "COD".equalsIgnoreCase(cleanMethod) ? "PENDING" : "COMPLETED";

        Payment mock = new Payment(nextMockPaymentId++, orderId, cleanMethod, status, amount, null);
        inMemoryPayments.add(mock);
        return mock;
    }

    /**
     * Overload accepting double amount.
     */
    public static Payment processPayment(int orderId, String paymentMethod, double amount) {
        return processPayment(orderId, paymentMethod, BigDecimal.valueOf(amount));
    }

    /**
     * Retrieves the latest payment record for a given order ID.
     *
     * @param orderId order ID to look up
     * @return associated {@link Payment} record
     */
    public static Payment getPaymentForOrder(int orderId) {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                Payment p = new PaymentDAO().findByOrderId(orderId);
                if (p != null) return p;
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB findByOrderId fallback: " + t.getMessage());
            }
        }

        // In-memory lookup (most recent first)
        for (int i = inMemoryPayments.size() - 1; i >= 0; i--) {
            Payment p = inMemoryPayments.get(i);
            if (p.getOrderId() == orderId) {
                return p;
            }
        }

        // Fallback default record if no explicit record found
        return new Payment(0, orderId, "COD", "PENDING", BigDecimal.ZERO, "Recent");
    }

    /**
     * Updates payment status associated with an order ID.
     * Used when an order transitions to DELIVERED (settles COD) or CANCELLED (marks REFUNDED).
     *
     * @param orderId   order ID
     * @param newStatus new payment status ("COMPLETED", "PENDING", "REFUNDED", "FAILED")
     * @return true if updated
     */
    public static boolean updatePaymentStatus(int orderId, String newStatus) {
        boolean dbUpdated = false;
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                dbUpdated = new PaymentDAO().updateStatusByOrderId(orderId, newStatus);
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB updateStatusByOrderId fallback: " + t.getMessage());
            }
        }

        boolean memUpdated = false;
        for (Payment p : inMemoryPayments) {
            if (p.getOrderId() == orderId) {
                p.setPaymentStatus(newStatus);
                memUpdated = true;
            }
        }
        return dbUpdated || memUpdated;
    }

    /**
     * Calculates total collected revenue from completed settlements.
     *
     * @return gross settled revenue amount
     */
    public static double getTotalCollectedRevenue() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                BigDecimal collected = new PaymentDAO().calculateTotalCollectedRevenue();
                if (collected != null && collected.doubleValue() > 0) {
                    return collected.doubleValue();
                }
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB calculateTotalCollectedRevenue fallback: " + t.getMessage());
            }
        }

        double sum = 0.0;
        for (Payment p : inMemoryPayments) {
            if ("COMPLETED".equalsIgnoreCase(p.getPaymentStatus())) {
                sum += p.getAmountAsDouble();
            }
        }
        return sum;
    }

    /**
     * Returns distribution of payments by payment method for executive reporting.
     *
     * @return map of payment channel to transaction counts
     */
    public static Map<String, Integer> getPaymentMethodDistribution() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                Map<String, Integer> dbMap = new PaymentDAO().getPaymentMethodDistribution();
                if (dbMap != null && !dbMap.isEmpty()) {
                    return dbMap;
                }
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB getPaymentMethodDistribution fallback: " + t.getMessage());
            }
        }

        Map<String, Integer> map = new LinkedHashMap<>();
        for (Payment p : inMemoryPayments) {
            String m = p.getPaymentMethod();
            map.put(m, map.getOrDefault(m, 0) + 1);
        }
        if (map.isEmpty()) {
            map.put("COD", 2);
            map.put("UPI", 1);
        }
        return map;
    }

    /**
     * Returns all payment transactions.
     *
     * @return list of {@link Payment} records
     */
    public static List<Payment> getAllPayments() {
        if (DBStatusUtil.isDatabaseConnected()) {
            try {
                List<Payment> list = new PaymentDAO().findAll();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Throwable t) {
                System.err.println("[PaymentStore] DB findAll fallback: " + t.getMessage());
            }
        }
        return new ArrayList<>(inMemoryPayments);
    }
}
