package com.qveto.ecommerce.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Main Application Dashboard & Navigation Shell.
 * 
 * Developed for Member 2 tasks:
 * - Main dashboard navigation for Customer and Admin roles
 * - Customer profile and order-history screen layouts
 * - Admin catalog & inventory management interface preview
 */
public class DashboardFrame extends JFrame {

    private final String username;
    private final String role;

    public DashboardFrame(String username, String role) {
        this.username = username;
        this.role = role.toUpperCase();
        initComponents();
    }

    private void initComponents() {
        setTitle("Qveto E-Commerce — " + (role.equals("ADMIN") ? "Admin Management" : "Customer Portal"));
        setSize(900, 620);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // --- Top Navigation & Header Bar ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(24, 43, 73));
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel brandLabel = new JLabel("QVETO STORE");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);
        topBar.add(brandLabel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("Logged in as: " + username + " (" + role + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(220, 230, 245));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(220, 38, 38));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());

        userPanel.add(userLabel);
        userPanel.add(logoutBtn);
        topBar.add(userPanel, BorderLayout.EAST);

        mainPanel.add(topBar, BorderLayout.NORTH);

        // --- Central Tabbed Navigation ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBorder(new EmptyBorder(10, 15, 15, 15));

        if (role.equals("ADMIN")) {
            tabbedPane.addTab("Inventory & Catalog", createAdminCatalogPanel());
            tabbedPane.addTab("Customer Orders", createAdminOrdersPanel());
            tabbedPane.addTab("Reports & KPIs", createAdminReportsPanel());
        } else {
            tabbedPane.addTab("Browse Products", createCustomerCatalogPanel());
            tabbedPane.addTab("My Cart (0)", createCustomerCartPanel());
            tabbedPane.addTab("Order History", createCustomerOrderHistoryPanel());
            tabbedPane.addTab("My Profile", createCustomerProfilePanel());
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // --- Customer Screens ---

    private JPanel createCustomerCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter / Search Toolbar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBackground(Color.WHITE);

        searchBar.add(new JLabel("Search Product:"));
        JTextField searchField = new JTextField(18);
        searchBar.add(searchField);

        searchBar.add(new JLabel("Category:"));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"All Categories", "Electronics", "Clothing", "Books", "Home"});
        searchBar.add(categoryCombo);

        JButton filterBtn = new JButton("Search");
        filterBtn.setBackground(new Color(37, 99, 235));
        filterBtn.setForeground(Color.WHITE);
        searchBar.add(filterBtn);

        panel.add(searchBar, BorderLayout.NORTH);

        // Catalog Table
        String[] columns = {"ID", "Product Name", "Category", "Description", "Price ($)", "Stock"};
        Object[][] data = {
                {101, "Wireless Bluetooth Headphones", "Electronics", "Noise-cancelling over-ear", "59.99", 25},
                {102, "Mechanical Gaming Keyboard", "Electronics", "RGB backlit blue switches", "79.99", 14},
                {103, "Classic Cotton T-Shirt", "Clothing", "100% organic cotton", "19.99", 50},
                {104, "Java Programming: OOP Essentials", "Books", "Comprehensive Java guide", "39.99", 30},
                {105, "Stainless Steel Water Bottle", "Home", "Vacuum insulated 750ml", "14.99", 40}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setBackground(Color.WHITE);
        JButton addToCartBtn = new JButton("Add Selected to Cart");
        addToCartBtn.setBackground(new Color(16, 185, 129));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addToCartBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String item = (String) table.getValueAt(selectedRow, 1);
                JOptionPane.showMessageDialog(this, item + " added to your cart!", "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomBar.add(addToCartBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCustomerCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel emptyNotice = new JLabel("<html><center><h3>Your Shopping Cart is currently empty</h3><p>Browse products and add items to place an order.</p></center></html>", SwingConstants.CENTER);
        panel.add(emptyNotice, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCustomerOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"Order #", "Order Date", "Total Items", "Total Amount ($)", "Payment Mode", "Status"};
        Object[][] data = {
                {"ORD-2026-001", "2026-10-15", 2, "79.98", "COD", "Delivered"},
                {"ORD-2026-004", "2026-10-18", 1, "59.99", "Prepaid", "Shipped"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCustomerProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        JLabel title = new JLabel("Customer Account Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridy = 1;
        panel.add(new JLabel("Account Email: " + username), gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Membership: Verified Customer"), gbc);

        gbc.gridy = 3;
        panel.add(new JLabel("Shipping Address: 124 Tech Park, Bangalore, India"), gbc);

        JButton editBtn = new JButton("Update Contact Information");
        editBtn.setBackground(new Color(37, 99, 235));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Profile edit dialog will be connected with the User DAO service.", "Information", JOptionPane.INFORMATION_MESSAGE));
        gbc.gridy = 4;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(editBtn, gbc);

        return panel;
    }

    // --- Admin Screens ---

    private JPanel createAdminCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBackground(Color.WHITE);

        JButton addBtn = new JButton("+ Add New Product");
        addBtn.setBackground(new Color(16, 185, 129));
        addBtn.setForeground(Color.WHITE);

        JButton editBtn = new JButton("Edit Product");
        JButton deleteBtn = new JButton("Delete Product");
        deleteBtn.setForeground(Color.RED);

        toolBar.add(addBtn);
        toolBar.add(editBtn);
        toolBar.add(deleteBtn);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Product ID", "Name", "Category", "Unit Price ($)", "Stock Qty", "Status"};
        Object[][] data = {
                {101, "Wireless Bluetooth Headphones", "Electronics", "59.99", 25, "In Stock"},
                {102, "Mechanical Gaming Keyboard", "Electronics", "79.99", 14, "In Stock"},
                {103, "Classic Cotton T-Shirt", "Clothing", "19.99", 5, "Low Stock"},
                {104, "Java Programming: OOP Essentials", "Books", "39.99", 30, "In Stock"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdminOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"Order ID", "Customer", "Date", "Amount ($)", "Payment", "Order Status"};
        Object[][] data = {
                {"ORD-1001", "rahul@example.com", "2026-10-18", "119.98", "Prepaid", "Pending"},
                {"ORD-1002", "priya@example.com", "2026-10-17", "39.99", "COD", "Confirmed"},
                {"ORD-1003", "amit@example.com", "2026-10-16", "79.99", "Prepaid", "Shipped"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdminReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        panel.add(createKpiCard("Total Orders", "142", new Color(37, 99, 235)));
        panel.add(createKpiCard("Total Revenue", "$9,450.00", new Color(16, 185, 129)));
        panel.add(createKpiCard("Active Products", "48", new Color(139, 92, 246)));
        panel.add(createKpiCard("Low Stock Alerts", "3 Products", new Color(239, 68, 68)));

        return panel;
    }

    private JPanel createKpiCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 116, 139));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
