package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Modal dialog enabling Administrators to edit product specifications,
 * modify prices, adjust low stock alert thresholds, and restock inventory.
 * 
 * Supports Member 2 Admin Product Management requirements.
 */
public class EditProductDialog extends JDialog {

    public EditProductDialog(Frame owner, Product product, Consumer<Product> onSave) {
        super(owner, "Edit & Restock Product — #" + product.getProductId(), true);
        initComponents(product, onSave);
    }

    private void initComponents(Product product, Consumer<Product> onSave) {
        setSize(480, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Edit Product & Restock Inventory");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel idBadge = new JLabel("ID: #" + product.getProductId());
        idBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        idBadge.setForeground(new Color(254, 240, 138));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(idBadge, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Body Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;

        // Product Name
        formPanel.add(new JLabel("Product Name:"), gbc);
        JTextField nameField = new JTextField(product.getProductName());
        gbc.gridy = 1;
        formPanel.add(nameField, gbc);

        // Category
        gbc.gridy = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        String[] categories = {"Electronics", "Clothing", "Books", "Home", "Sports", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(product.getCategory());
        gbc.gridy = 3;
        formPanel.add(categoryCombo, gbc);

        // Description
        gbc.gridy = 4;
        formPanel.add(new JLabel("Description:"), gbc);
        JTextField descField = new JTextField(product.getDescription());
        gbc.gridy = 5;
        formPanel.add(descField, gbc);

        // Unit Price
        gbc.gridy = 6;
        formPanel.add(new JLabel("Unit Price ($):"), gbc);
        JTextField priceField = new JTextField(String.format("%.2f", product.getPriceAsDouble()));
        gbc.gridy = 7;
        formPanel.add(priceField, gbc);

        // Stock Quantity with Quick Restock Buttons
        gbc.gridy = 8;
        formPanel.add(new JLabel("Available Stock Quantity:"), gbc);

        JPanel stockRow = new JPanel(new BorderLayout(8, 0));
        stockRow.setBackground(Color.WHITE);

        JTextField stockField = new JTextField(String.valueOf(product.getStockQuantity()));
        stockRow.add(stockField, BorderLayout.CENTER);

        JPanel quickRestock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        quickRestock.setBackground(Color.WHITE);

        JButton add10Btn = new JButton("+10");
        add10Btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        add10Btn.setToolTipText("Add 10 units to current stock");
        add10Btn.addActionListener(e -> {
            try {
                int curr = Integer.parseInt(stockField.getText().trim());
                stockField.setText(String.valueOf(curr + 10));
            } catch (NumberFormatException ignored) {}
        });

        JButton add50Btn = new JButton("+50");
        add50Btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        add50Btn.setToolTipText("Add 50 units to current stock");
        add50Btn.addActionListener(e -> {
            try {
                int curr = Integer.parseInt(stockField.getText().trim());
                stockField.setText(String.valueOf(curr + 50));
            } catch (NumberFormatException ignored) {}
        });

        quickRestock.add(add10Btn);
        quickRestock.add(add50Btn);
        stockRow.add(quickRestock, BorderLayout.EAST);

        gbc.gridy = 9;
        formPanel.add(stockRow, gbc);

        // Low Stock Threshold
        gbc.gridy = 10;
        formPanel.add(new JLabel("Low Stock Alert Threshold (Units):"), gbc);
        JTextField thresholdField = new JTextField(String.valueOf(product.getLowStockThreshold()));
        gbc.gridy = 11;
        formPanel.add(thresholdField, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Save & Apply Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(16, 185, 129));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String threshStr = thresholdField.getText().trim();

            if (!ValidationUtil.isNotEmpty(name)) {
                ValidationUtil.showError(this, "Product Name cannot be blank.", "Validation Error");
                return;
            }

            BigDecimal price;
            try {
                double p = Double.parseDouble(priceStr);
                if (p < 0) throw new NumberFormatException();
                price = BigDecimal.valueOf(p);
            } catch (NumberFormatException ex) {
                ValidationUtil.showError(this, "Please enter a valid non-negative unit price.", "Validation Error");
                return;
            }

            int stock;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ValidationUtil.showError(this, "Please enter a valid non-negative stock quantity.", "Validation Error");
                return;
            }

            int threshold;
            try {
                threshold = Integer.parseInt(threshStr);
                if (threshold < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ValidationUtil.showError(this, "Please enter a valid non-negative low-stock threshold.", "Validation Error");
                return;
            }

            product.setProductName(name);
            product.setDescription(desc);
            product.setCategory((String) categoryCombo.getSelectedItem());
            product.setPrice(price);
            product.setStockQuantity(stock);
            product.setLowStockThreshold(threshold);

            if (onSave != null) {
                onSave.accept(product);
            }

            dispose();
            ValidationUtil.showSuccess(getOwner(),
                    "Product <b>" + name + "</b> (ID: #" + product.getProductId() + ") was updated successfully!<br/>"
                            + "Current Stock: <b>" + stock + " units</b> (Threshold: " + threshold + ").",
                    "Inventory Updated");
        });

        footerPanel.add(cancelBtn);
        footerPanel.add(saveBtn);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }
}
