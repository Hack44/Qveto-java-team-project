package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Payment;
import com.qveto.ecommerce.util.PaymentStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Modal dialog displaying detailed order breakdown, items, pricing, payment settlement,
 * and official invoice summary with receipt export.
 *
 * Supports Member 2 (UI, Presentation & Store Management) customer order history and invoice requirements.
 */
public class OrderDetailsDialog extends JDialog {

    public OrderDetailsDialog(Frame owner, Order order, String customerAddress) {
        super(owner, "Order Details & Invoice — " + order.getOrderNumber(), true);
        initComponents(order, customerAddress);
    }

    private void initComponents(Order order, String customerAddress) {
        setSize(620, 640);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Invoice & Order Summary");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel orderNumLabel = new JLabel(order.getOrderNumber());
        orderNumLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        orderNumLabel.setForeground(new Color(254, 240, 138));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(orderNumLabel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Visual Order Tracking Stepper
        contentPanel.add(createTrackingStepperPanel(order));
        contentPanel.add(Box.createVerticalStrut(12));

        // Retrieve associated payment record
        Payment payment = PaymentStore.getPaymentForOrder(order.getOrderId());
        String payStatus = payment.getPaymentStatus();
        String statusColor = "COMPLETED".equalsIgnoreCase(payStatus) ? "#16a34a"
                : ("PENDING".equalsIgnoreCase(payStatus) ? "#d97706" : "#dc2626");

        // Info Grid (4 rows x 2 columns)
        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 10, 6));
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        infoGrid.add(createMetaLabel("Order Date:", order.getOrderDate()));
        infoGrid.add(createMetaLabel("Order Status:", order.getOrderStatus()));
        infoGrid.add(createMetaLabel("Payment Mode:", payment.getPaymentMethod()));
        infoGrid.add(createMetaLabel("Payment Status:", "<font color='" + statusColor + "'><b>" + payStatus + "</b></font>"));
        infoGrid.add(createMetaLabel("Transaction Ref:", payment.getTransactionRef()));
        infoGrid.add(createMetaLabel("Delivery:", "Standard Courier"));
        infoGrid.add(createMetaLabel("Shipping To:", customerAddress));
        infoGrid.add(createMetaLabel("Invoice Ref:", "INV-" + order.getOrderId()));

        contentPanel.add(infoGrid);
        contentPanel.add(Box.createVerticalStrut(12));

        // Line Items Table
        JLabel itemsLabel = new JLabel("Ordered Items:");
        itemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(itemsLabel);
        contentPanel.add(Box.createVerticalStrut(6));

        String[] cols = {"Item Description", "Qty", "Unit Price", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                model.addRow(new Object[]{
                        item.getProductName(),
                        item.getQuantity(),
                        String.format("$%.2f", item.getUnitPrice()),
                        String.format("$%.2f", item.getSubtotal())
                });
            }
        } else {
            // Default fallback if item breakdown not attached
            model.addRow(new Object[]{"Standard E-Commerce Items Package", 1, String.format("$%.2f", order.getTotalAmount()), String.format("$%.2f", order.getTotalAmount())});
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(540, 130));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(scrollPane);
        contentPanel.add(Box.createVerticalStrut(14));

        // Total Summary Box
        JPanel summaryBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        summaryBox.setBackground(Color.WHITE);
        summaryBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        double itemsSum = 0;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem it : order.getItems()) {
                itemsSum += it.getSubtotal();
            }
        } else {
            itemsSum = order.getTotalAmount();
        }
        double estimatedTax = itemsSum * 0.05;
        double promoSavings = (itemsSum + estimatedTax) - order.getTotalAmount();
        if (promoSavings > 0.5) {
            JLabel savingsLabel = new JLabel(String.format("<html><font color='#16a34a'><b>Promotional Savings: -$%.2f</b></font> &nbsp;|&nbsp;</html>", promoSavings));
            savingsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            summaryBox.add(savingsLabel);
        }

        JLabel totalLabel = new JLabel(String.format("Total Amount: $%.2f", order.getTotalAmount()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(new Color(15, 23, 42));
        summaryBox.add(totalLabel);

        contentPanel.add(summaryBox);
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton exportBtn = new JButton("Export Invoice (.txt)");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setBackground(new Color(37, 99, 235));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> handleExportInvoice(order, payment, customerAddress));
        actionPanel.add(exportBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(closeBtn);

        rootPanel.add(actionPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JPanel createTrackingStepperPanel(Order order) {
        JPanel stepperCard = new JPanel(new BorderLayout(6, 6));
        stepperCard.setBackground(Color.WHITE);
        stepperCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepperCard.setMaximumSize(new Dimension(580, 85));

        String rawStatus = order.getOrderStatus() != null ? order.getOrderStatus().trim().toUpperCase() : "PENDING";

        if ("CANCELLED".equals(rawStatus)) {
            JPanel cancelCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            cancelCard.setBackground(new Color(254, 242, 242));
            cancelCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(248, 113, 113), 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            JLabel cancelIcon = new JLabel("✖");
            cancelIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
            cancelIcon.setForeground(new Color(220, 38, 38));

            JLabel cancelText = new JLabel("<html><b>Order Cancelled & Voided</b> — Inventory stock was replenished, and payment was refunded.</html>");
            cancelText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cancelText.setForeground(new Color(153, 27, 27));

            cancelCard.add(cancelIcon);
            cancelCard.add(cancelText);
            stepperCard.add(cancelCard, BorderLayout.CENTER);
            return stepperCard;
        }

        int currentStage;
        String stageDesc;
        switch (rawStatus) {
            case "CONFIRMED":
                currentStage = 2;
                stageDesc = "Order verified by merchant and queued for warehouse packing.";
                break;
            case "SHIPPED":
                currentStage = 3;
                stageDesc = "Order dispatched with Standard Courier and is currently in transit.";
                break;
            case "DELIVERED":
                currentStage = 4;
                stageDesc = "Shipment successfully delivered to recipient address.";
                break;
            case "PENDING":
            default:
                currentStage = 1;
                stageDesc = "Order placed successfully. Awaiting merchant confirmation.";
                break;
        }

        JPanel barPanel = new JPanel(new GridBagLayout());
        barPanel.setBackground(new Color(248, 250, 252));
        barPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        String[] stepNames = {"Placed", "Confirmed", "Shipped", "Delivered"};

        for (int i = 0; i < 4; i++) {
            int stepNum = i + 1;
            boolean isCompleted = stepNum < currentStage;
            boolean isCurrent = stepNum == currentStage;

            JPanel node = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            node.setOpaque(false);

            JLabel circle = new JLabel(isCompleted ? "✓" : String.valueOf(stepNum), SwingConstants.CENTER);
            circle.setPreferredSize(new Dimension(22, 22));
            circle.setOpaque(true);
            circle.setFont(new Font("Segoe UI", Font.BOLD, 11));

            if (isCompleted) {
                circle.setBackground(new Color(22, 163, 74));
                circle.setForeground(Color.WHITE);
            } else if (isCurrent) {
                circle.setBackground(new Color(37, 99, 235));
                circle.setForeground(Color.WHITE);
            } else {
                circle.setBackground(new Color(226, 232, 240));
                circle.setForeground(new Color(100, 116, 139));
            }
            circle.setBorder(BorderFactory.createLineBorder(circle.getBackground(), 1));

            JLabel label = new JLabel(stepNames[i]);
            label.setFont(new Font("Segoe UI", isCurrent ? Font.BOLD : Font.PLAIN, 11));
            label.setForeground(isCurrent ? new Color(15, 23, 42) : (isCompleted ? new Color(22, 163, 74) : new Color(148, 163, 184)));

            node.add(circle);
            node.add(label);

            gbc.gridx = i * 2;
            gbc.weightx = 0;
            barPanel.add(node, gbc);

            if (i < 3) {
                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setForeground(stepNum < currentStage ? new Color(22, 163, 74) : new Color(203, 213, 225));
                gbc.gridx = i * 2 + 1;
                gbc.weightx = 1.0;
                barPanel.add(sep, gbc);
            }
        }

        JPanel footerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        footerRow.setOpaque(false);
        JLabel descLabel = new JLabel("<html><b>Fulfillment Status:</b> " + stageDesc + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(71, 85, 105));
        footerRow.add(descLabel);

        stepperCard.add(barPanel, BorderLayout.CENTER);
        stepperCard.add(footerRow, BorderLayout.SOUTH);
        return stepperCard;
    }

    private JLabel createMetaLabel(String key, String value) {
        JLabel label = new JLabel("<html><b>" + key + "</b> " + value + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private void handleExportInvoice(Order order, Payment payment, String customerAddress) {
        String fileName = "Invoice_" + order.getOrderNumber() + ".txt";
        File file = new File(fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("==============================================================\n");
            writer.write("                   QVETO E-COMMERCE STORE\n");
            writer.write("                  Official Customer Invoice\n");
            writer.write("==============================================================\n");
            writer.write(String.format("Invoice Ref:      INV-%d\n", order.getOrderId()));
            writer.write(String.format("Order Number:     %s\n", order.getOrderNumber()));
            writer.write(String.format("Order Date:       %s\n", order.getOrderDate()));
            writer.write(String.format("Order Status:     %s\n", order.getOrderStatus()));
            writer.write(String.format("Payment Method:   %s\n", payment.getPaymentMethod()));
            writer.write(String.format("Payment Status:   %s\n", payment.getPaymentStatus()));
            writer.write(String.format("Transaction Ref:  %s\n", payment.getTransactionRef()));
            writer.write(String.format("Shipping Address: %s\n", customerAddress));
            writer.write("--------------------------------------------------------------\n");
            writer.write(String.format("%-30s %-6s %-12s %-10s\n", "Item Description", "Qty", "Unit Price", "Subtotal"));
            writer.write("--------------------------------------------------------------\n");

            double itemsSubtotal = 0;
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    itemsSubtotal += item.getSubtotal();
                    writer.write(String.format("%-30s %-6d $%-11.2f $%-9.2f\n",
                            item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
                }
            } else {
                itemsSubtotal = order.getTotalAmount();
                writer.write(String.format("%-30s %-6d $%-11.2f $%-9.2f\n",
                        "Standard E-Commerce Items Package", 1, order.getTotalAmount(), order.getTotalAmount()));
            }

            writer.write("--------------------------------------------------------------\n");
            double taxAmount = itemsSubtotal * 0.05;
            double savingsAmount = (itemsSubtotal + taxAmount) - order.getTotalAmount();
            writer.write(String.format("Items Subtotal:   $%.2f\n", itemsSubtotal));
            if (savingsAmount > 0.5) {
                writer.write(String.format("Promo Savings:   -$%.2f\n", savingsAmount));
            }
            writer.write(String.format("Estimated Tax:    $%.2f\n", taxAmount));
            writer.write("--------------------------------------------------------------\n");
            writer.write(String.format("GRAND TOTAL:      $%.2f\n", order.getTotalAmount()));
            writer.write("==============================================================\n");
            writer.write("Thank you for shopping with Qveto Store!\n");

            ValidationUtil.showSuccess(this,
                    "Invoice successfully exported to: <b>" + file.getAbsolutePath() + "</b>",
                    "Invoice Exported");
        } catch (IOException ex) {
            ValidationUtil.showError(this,
                    "Failed to export invoice: " + ex.getMessage(),
                    "Export Error");
        }
    }
}
