package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.util.OrderStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog enabling customers to submit post-delivery return and refund requests.
 * Supports Member 2 (UI, Presentation & Store Management) customer post-purchase lifecycle requirements.
 */
public class OrderReturnDialog extends JDialog {

    private final Order order;
    private final Runnable onReturnSubmitted;

    private JComboBox<String> reasonCombo;
    private JComboBox<String> refundMethodCombo;
    private JTextArea explanationArea;

    public OrderReturnDialog(Frame owner, Order order, Runnable onReturnSubmitted) {
        super(owner, "Request Return / Refund — " + order.getOrderNumber(), true);
        this.order = order;
        this.onReturnSubmitted = onReturnSubmitted;

        initComponents();
    }

    private void initComponents() {
        setSize(520, 540);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Return & Refund Request");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel orderBadge = new JLabel(order.getOrderNumber());
        orderBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        orderBadge.setForeground(new Color(24, 43, 73));
        orderBadge.setOpaque(true);
        orderBadge.setBackground(new Color(254, 240, 138));
        orderBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(orderBadge, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Body Form
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 22, 16, 22));

        // Order Summary Card
        JPanel summaryCard = new JPanel(new GridLayout(2, 2, 8, 6));
        summaryCard.setBackground(new Color(248, 250, 252));
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        summaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        summaryCard.add(new JLabel("<html><b>Order Date:</b> " + order.getOrderDate() + "</html>"));
        summaryCard.add(new JLabel("<html><b>Refund Amount:</b> $" + String.format("%.2f", order.getTotalAmount()) + "</html>"));
        summaryCard.add(new JLabel("<html><b>Total Units:</b> " + order.getTotalQuantity() + " item(s)</html>"));
        summaryCard.add(new JLabel("<html><b>Payment Method:</b> " + order.getPaymentMethod() + "</html>"));

        body.add(summaryCard);
        body.add(Box.createVerticalStrut(14));

        // Reason Dropdown
        JLabel reasonLabel = new JLabel("Reason for Return / Refund Request *");
        reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(reasonLabel);
        body.add(Box.createVerticalStrut(4));

        String[] reasons = {
                "Defective / Damaged on Arrival",
                "Received Incorrect Item",
                "Product Does Not Match Description",
                "Quality Not Satisfactory",
                "Ordered by Mistake / Changed Mind"
        };
        reasonCombo = new JComboBox<>(reasons);
        reasonCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reasonCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        reasonCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(reasonCombo);
        body.add(Box.createVerticalStrut(12));

        // Refund Preference Dropdown
        JLabel refundLabel = new JLabel("Preferred Refund Destination *");
        refundLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refundLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(refundLabel);
        body.add(Box.createVerticalStrut(4));

        String[] refundDestinations = {
                "Original Payment Source (Bank / Card / UPI)",
                "Qveto Store Credit (Instant Balance)"
        };
        refundMethodCombo = new JComboBox<>(refundDestinations);
        refundMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refundMethodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        refundMethodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(refundMethodCombo);
        body.add(Box.createVerticalStrut(12));

        // Detailed Explanation
        JLabel explainLabel = new JLabel("Detailed Comments & Issue Description *");
        explainLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        explainLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(explainLabel);
        body.add(Box.createVerticalStrut(4));

        explanationArea = new JTextArea(4, 20);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane explainScroll = new JScrollPane(explanationArea);
        explainScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(explainScroll);

        root.add(body, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());
        actionPanel.add(cancelBtn);

        JButton submitBtn = new JButton("Submit Return Request");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitBtn.setBackground(new Color(217, 119, 6));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> handleSubmitReturn());
        actionPanel.add(submitBtn);

        root.add(actionPanel, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void handleSubmitReturn() {
        String explanation = explanationArea.getText().trim();
        if (explanation.isEmpty()) {
            ValidationUtil.showWarning(this, "Please provide a detailed comment describing the return reason.", "Missing Explanation");
            return;
        }

        String reason = (String) reasonCombo.getSelectedItem();
        boolean success = OrderStore.requestReturn(order, reason + ": " + explanation);

        if (success) {
            ValidationUtil.showSuccess(this,
                    "Your return request for order <b>" + order.getOrderNumber() + "</b> has been submitted successfully.<br/>"
                            + "Our fulfillment team will review your request shortly.",
                    "Return Request Logged");
            if (onReturnSubmitted != null) {
                onReturnSubmitted.run();
            }
            dispose();
        } else {
            ValidationUtil.showError(this, "Failed to submit return request. Please try again.", "Error");
        }
    }
}
