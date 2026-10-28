package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
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
 * Modal dialog displaying detailed order breakdown, items, pricing, and invoice summary.
 * Fulfills Member 2 responsibility: "Create the customer profile and order-history screens."
 */
public class OrderDetailsDialog extends JDialog {

    public OrderDetailsDialog(Frame owner, Order order, String customerAddress) {
        super(owner, "Order Details & Invoice — " + order.getOrderNumber(), true);
        initComponents(order, customerAddress);
    }

    private void initComponents(Order order, String customerAddress) {
        setSize(580, 530);
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
        scrollPane.setPreferredSize(new Dimension(520, 120));
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
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton exportBtn = new JButton("Export Invoice (.txt)");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setBackground(new Color(37, 99, 235));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> handleExportInvoice(order, customerAddress));
        actionPanel.add(exportBtn);

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

    private void handleExportInvoice(Order order, String customerAddress) {
        String fileName = "Invoice_" + order.getOrderNumber() + ".txt";
        File file = new File(fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("==============================================================\n");
            writer.write("                   QVETO E-COMMERCE STORE\n");
            writer.write("                  Official Customer Invoice\n");
            writer.write("==============================================================\n");
            writer.write(String.format("Invoice Ref:     INV-%d\n", order.getOrderId()));
            writer.write(String.format("Order Number:    %s\n", order.getOrderNumber()));
            writer.write(String.format("Order Date:      %s\n", order.getOrderDate()));
            writer.write(String.format("Order Status:    %s\n", order.getOrderStatus()));
            writer.write(String.format("Payment Method:  %s\n", order.getPaymentMethod()));
            writer.write(String.format("Shipping Address: %s\n", customerAddress));
            writer.write("--------------------------------------------------------------\n");
            writer.write(String.format("%-30s %-6s %-12s %-10s\n", "Item Description", "Qty", "Unit Price", "Subtotal"));
            writer.write("--------------------------------------------------------------\n");

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    writer.write(String.format("%-30s %-6d $%-11.2f $%-9.2f\n",
                            item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
                }
            } else {
                writer.write(String.format("%-30s %-6d $%-11.2f $%-9.2f\n",
                        "Standard E-Commerce Items Package", 1, order.getTotalAmount(), order.getTotalAmount()));
            }

            writer.write("--------------------------------------------------------------\n");
            writer.write(String.format("GRAND TOTAL: $%.2f\n", order.getTotalAmount()));
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
