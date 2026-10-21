package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Modal dialog displaying detailed order breakdown, items, pricing, and invoice summary.
 * Fulfills Member 2 responsibility: "Create the customer profile and order-history screens."
 */
public class OrderDetailsDialog extends JDialog {

    public OrderDetailsDialog(Frame owner, Order order, String customerAddress) {
        super(owner, "Order Details & Invoice — " + order.getOrderNumber(), true);
        initComponents(order, customerAddress);
    }

    private void initComponents(Order order, String customerAddress) {
        setSize(560, 520);
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
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Info Grid
        JPanel infoGrid = new JPanel(new GridLayout(3, 2, 10, 6));
        infoGrid.setBackground(new Color(248, 250, 252));
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        infoGrid.add(createMetaLabel("Order Date:", order.getOrderDate()));
        infoGrid.add(createMetaLabel("Status:", order.getOrderStatus()));
        infoGrid.add(createMetaLabel("Payment Mode:", order.getPaymentMethod()));
        infoGrid.add(createMetaLabel("Delivery:", "Standard Courier"));
        infoGrid.add(createMetaLabel("Shipping To:", customerAddress));
        infoGrid.add(createMetaLabel("Invoice Ref:", "INV-" + order.getOrderId()));

        contentPanel.add(infoGrid);
        contentPanel.add(Box.createVerticalStrut(14));

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
        scrollPane.setPreferredSize(new Dimension(500, 120));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(scrollPane);
        contentPanel.add(Box.createVerticalStrut(14));

        // Total Summary Box
        JPanel summaryBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryBox.setBackground(Color.WHITE);
        summaryBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel totalLabel = new JLabel(String.format("Total Paid: $%.2f", order.getTotalAmount()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(new Color(15, 23, 42));
        summaryBox.add(totalLabel);

        contentPanel.add(summaryBox);
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(closeBtn);

        rootPanel.add(actionPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JLabel createMetaLabel(String key, String value) {
        JLabel label = new JLabel("<html><b>" + key + "</b> " + value + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
}
