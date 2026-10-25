package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modal dialog displaying detailed product information, specifications, and quick add-to-cart.
 * Fulfills Customer Product Discovery requirements for Member 2.
 */
public class ProductDetailsDialog extends JDialog {

    public ProductDetailsDialog(Frame owner, Product product, boolean isCustomer, Consumer<Product> onAddToCart) {
        super(owner, "Product Details — " + product.getProductName(), true);
        initComponents(product, isCustomer, onAddToCart);
    }

    private void initComponents(Product product, boolean isCustomer, Consumer<Product> onAddToCart) {
        setSize(480, 420);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Product Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel catLabel = new JLabel(product.getCategory());
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        catLabel.setForeground(new Color(200, 215, 235));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(catLabel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Body Panel
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Product Name
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(15, 23, 42));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(nameLabel);
        bodyPanel.add(Box.createVerticalStrut(12));

        // Details Grid
        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 10, 8));
        detailsGrid.setBackground(new Color(248, 250, 252));
        detailsGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsGrid.add(createMetaLabel("Product ID:", "#" + product.getProductId()));
        detailsGrid.add(createMetaLabel("Category:", product.getCategory()));
        detailsGrid.add(createMetaLabel("Unit Price:", String.format("$%.2f", product.getPrice())));
        detailsGrid.add(createMetaLabel("Available Stock:", product.getStockQuantity() + " units"));
        detailsGrid.add(createMetaLabel("Inventory Status:", product.getStockStatus()));
        detailsGrid.add(createMetaLabel("Shipping:", "Standard Free Delivery"));

        bodyPanel.add(detailsGrid);
        bodyPanel.add(Box.createVerticalStrut(14));

        // Description
        JLabel descHeader = new JLabel("Description:");
        descHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(descHeader);
        bodyPanel.add(Box.createVerticalStrut(4));

        JTextArea descArea = new JTextArea(product.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(descArea);

        rootPanel.add(bodyPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(closeBtn);

        if (isCustomer) {
            JButton addBtn = new JButton("Add to Cart");
            addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            addBtn.setBackground(new Color(16, 185, 129));
            addBtn.setForeground(Color.WHITE);
            addBtn.setEnabled(!product.isOutOfStock());
            addBtn.addActionListener(e -> {
                if (onAddToCart != null) {
                    onAddToCart.accept(product);
                }
                dispose();
            });
            actionPanel.add(addBtn);
        }

        rootPanel.add(actionPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JLabel createMetaLabel(String key, String value) {
        JLabel label = new JLabel("<html><b>" + key + "</b> " + value + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
}
