package com.qveto.ecommerce.dao;

import com.qveto.ecommerce.DBConnection;
import com.qveto.ecommerce.model.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the 'payments' table.
 *
 * Provides transactional persistence, simulated payment gateway integration,
 * status transitions (PENDING -> COMPLETED / REFUNDED), and financial audit
 * aggregations for order transactions.
 *
 * All operations use PreparedStatements to prevent SQL injection.
 *
 * Member 1 — DB/JDBC layer.
 */
public class PaymentDAO {

    // ---------------------------------------------------------------
    // Create / Record
    // ---------------------------------------------------------------

    /**
     * Records a new payment transaction in the database.
     *
     * @param payment the {@link Payment} entity to persist
     * @return generated payment_id, or -1 on failure
     */
    public int recordPayment(Payment payment) {
        if (payment == null) {
            return -1;
        }

        String sql = "INSERT INTO payments (order_id, payment_method, payment_status, amount) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getOrderId());
            stmt.setString(2, payment.getPaymentMethod());
            stmt.setString(3, payment.getPaymentStatus());
            stmt.setBigDecimal(4, payment.getAmount());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        payment.setPaymentId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] recordPayment() failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Overload to record a payment with individual fields.
     */
    public int recordPayment(int orderId, String paymentMethod, String paymentStatus, BigDecimal amount) {
        Payment p = new Payment(orderId, paymentMethod, paymentStatus, amount);
        return recordPayment(p);
    }

    /**
     * Processes a test-mode payment gateway transaction.
     * For digital payments (UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING), immediately settles
     * with status 'COMPLETED'.
     * For Cash on Delivery (COD), records payment with status 'PENDING'.
     *
     * @param orderId       the associated order
     * @param paymentMethod payment channel chosen
     * @param amount        order amount
     * @return populated {@link Payment} record with generated ID
     */
    public Payment processSimulatedPayment(int orderId, String paymentMethod, BigDecimal amount) {
        String cleanMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty())
                ? paymentMethod.trim().toUpperCase() : "COD";

        String initialStatus = "COD".equalsIgnoreCase(cleanMethod) ? "PENDING" : "COMPLETED";

        Payment payment = new Payment(orderId, cleanMethod, initialStatus, amount);
        int generatedId = recordPayment(payment);
        if (generatedId > 0) {
            payment.setPaymentId(generatedId);
        }
        return payment;
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves a payment by primary key.
     *
     * @param paymentId payment ID to look up
     * @return {@link Payment} or null if not found
     */
    public Payment findById(int paymentId) {
        String sql = "SELECT payment_id, order_id, payment_method, payment_status, amount, created_at "
                   + "FROM payments WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] findById() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the most recent payment associated with a given order.
     *
     * @param orderId the order ID
     * @return the {@link Payment} for this order, or null if none
     */
    public Payment findByOrderId(int orderId) {
        String sql = "SELECT payment_id, order_id, payment_method, payment_status, amount, created_at "
                   + "FROM payments WHERE order_id = ? ORDER BY payment_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] findByOrderId() failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all payments for an order (supports split payments or retries).
     *
     * @param orderId the order ID
     * @return list of {@link Payment} records
     */
    public List<Payment> findAllByOrderId(int orderId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_method, payment_status, amount, created_at "
                   + "FROM payments WHERE order_id = ? ORDER BY payment_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] findAllByOrderId() failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all payment records ordered from newest to oldest.
     *
     * @return list of all {@link Payment} records
     */
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_method, payment_status, amount, created_at "
                   + "FROM payments ORDER BY payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] findAll() failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all payments filtered by status (e.g. "COMPLETED", "PENDING", "REFUNDED").
     *
     * @param status status filter string
     * @return list of matching {@link Payment} records
     */
    public List<Payment> findByStatus(String status) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_method, payment_status, amount, created_at "
                   + "FROM payments WHERE payment_status = ? ORDER BY payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status != null ? status.toUpperCase() : "COMPLETED");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] findByStatus() failed: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    /**
     * Updates payment status by primary key.
     *
     * @param paymentId payment ID to update
     * @param newStatus new status string
     * @return true if updated
     */
    public boolean updateStatus(int paymentId, String newStatus) {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus != null ? newStatus.toUpperCase() : "PENDING");
            stmt.setInt(2, paymentId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] updateStatus() failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates payment status for all records matching an order ID.
     * Used when an order is DELIVERED (completes COD payment) or CANCELLED (marks REFUNDED).
     *
     * @param orderId   the order ID
     * @param newStatus new status string
     * @return true if updated
     */
    public boolean updateStatusByOrderId(int orderId, String newStatus) {
        String sql = "UPDATE payments SET payment_status = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus != null ? newStatus.toUpperCase() : "PENDING");
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] updateStatusByOrderId() failed: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Analytics & Metrics
    // ---------------------------------------------------------------

    /**
     * Calculates total collected revenue from completed payments.
     *
     * @return gross settled revenue as BigDecimal
     */
    public BigDecimal calculateTotalCollectedRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0.00) AS collected FROM payments WHERE payment_status = 'COMPLETED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal collected = rs.getBigDecimal("collected");
                return collected != null ? collected : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] calculateTotalCollectedRevenue() failed: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns distribution of payments by payment method for executive reporting.
     *
     * @return map of payment method name to count of transactions
     */
    public Map<String, Integer> getPaymentMethodDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT payment_method, COUNT(*) AS cnt FROM payments GROUP BY payment_method";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString("payment_method"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] getPaymentMethodDistribution() failed: " + e.getMessage());
        }
        return map;
    }

    // ---------------------------------------------------------------
    // Private Helpers
    // ---------------------------------------------------------------

    private Payment mapRow(ResultSet rs) throws SQLException {
        return new Payment(
            rs.getInt("payment_id"),
            rs.getInt("order_id"),
            rs.getString("payment_method"),
            rs.getString("payment_status"),
            rs.getBigDecimal("amount"),
            rs.getString("created_at") != null ? rs.getString("created_at") : ""
        );
    }
}
