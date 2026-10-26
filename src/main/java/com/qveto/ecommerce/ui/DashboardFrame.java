package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Application Dashboard & Navigation Shell.
 * 
 * Developed for Member 2 tasks:
 * - Product Catalog Discovery: Search, category filtering, and product details modal
 * - Interactive Shopping Cart with live total calculation & quantity updates
 * - Checkout workflow connecting Cart to Order History with shipping address
 * - Customer Profile management (Edit contact/address, change password)
 * - Order History screen with status filtering, invoice modal, and order cancellation
 * - Admin Product management (Add, delete, and view catalog inventory)
 * - Admin Order Management (View all orders, inspect invoices, and update order statuses)
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private final List<Product> catalogProducts;
    private final List<Order> customerOrders;
    private final List<OrderItem> shoppingCart;

    // Central navigation tabs
    private JTabbedPane tabbedPane;

    // Customer Catalog UI components
    private DefaultTableModel customerCatalogTableModel;
    private JTable customerCatalogTable;
    private JTextField customerSearchField;
    private JComboBox<String> customerCategoryCombo;

    // Admin Catalog UI components
    private DefaultTableModel adminCatalogTableModel;
    private JTable adminCatalogTable;

    // Admin Orders UI components
    private DefaultTableModel adminOrdersTableModel;
    private JTable adminOrdersTable;
    private JComboBox<String> adminOrderStatusFilterCombo;

    // Cart UI components
    private JPanel cartRootPanel;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel cartTotalSummaryLabel;

    // Profile UI components for dynamic refresh
    private JLabel profileNameVal;
    private JLabel profileEmailVal;
    private JLabel profilePhoneVal;
    private JLabel profileAddressVal;

    // Order History UI components
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;
    private JComboBox<String> statusFilterCombo;
    private JTextField orderSearchField;

    public DashboardFrame(User user) {
        this.currentUser = user;
        this.catalogProducts = initializeSampleProducts();
        this.customerOrders = initializeSampleOrders();
        this.shoppingCart = new ArrayList<>();
        initComponents();
    }

    // Overload for backwards compatibility
    public DashboardFrame(String username, String role) {
        this(new User(1, username.contains("@") ? username.split("@")[0] : username, username, role));
    }

    private void initComponents() {
        setTitle("Qveto E-Commerce — " + (currentUser.isAdmin() ? "Admin Management" : "Customer Portal"));
        setSize(960, 660);
        setMinimumSize(new Dimension(860, 540));
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

        JLabel userLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
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
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBorder(new EmptyBorder(10, 15, 15, 15));

        if (currentUser.isAdmin()) {
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

    // ==========================================
    // CUSTOMER CATALOG SCREEN (Product Discovery)
    // ==========================================

    private JPanel createCustomerCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter / Search Toolbar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBackground(Color.WHITE);

        searchBar.add(new JLabel("Search:"));
        customerSearchField = new JTextField(16);
        customerSearchField.addActionListener(e -> applyCatalogFilters());
        searchBar.add(customerSearchField);

        searchBar.add(new JLabel("Category:"));
        customerCategoryCombo = new JComboBox<>(new String[]{"All Categories", "Electronics", "Clothing", "Books", "Home"});
        customerCategoryCombo.addActionListener(e -> applyCatalogFilters());
        searchBar.add(customerCategoryCombo);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setBackground(new Color(37, 99, 235));
        filterBtn.setForeground(Color.WHITE);
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterBtn.addActionListener(e -> applyCatalogFilters());
        searchBar.add(filterBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            customerSearchField.setText("");
            customerCategoryCombo.setSelectedIndex(0);
            applyCatalogFilters();
        });
        searchBar.add(resetBtn);

        panel.add(searchBar, BorderLayout.NORTH);

        // Catalog Table
        String[] columns = {"ID", "Product Name", "Category", "Unit Price ($)", "Stock", "Availability"};
        customerCatalogTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerCatalogTable = new JTable(customerCatalogTableModel);
        customerCatalogTable.setRowHeight(28);
        customerCatalogTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        customerCatalogTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        customerCatalogTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        customerCatalogTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        customerCatalogTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        customerCatalogTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        refreshCustomerCatalogTable(catalogProducts);

        JScrollPane scrollPane = new JScrollPane(customerCatalogTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        bottomBar.setBackground(Color.WHITE);

        JButton viewDetailsBtn = new JButton("View Product Details");
        viewDetailsBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewDetailsBtn.setBackground(new Color(15, 23, 42));
        viewDetailsBtn.setForeground(Color.WHITE);
        viewDetailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDetailsBtn.addActionListener(e -> handleViewProductDetails());
        bottomBar.add(viewDetailsBtn);

        JButton addToCartBtn = new JButton("Add Selected to Cart");
        addToCartBtn.setBackground(new Color(16, 185, 129));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addToCartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToCartBtn.addActionListener(e -> handleAddToCartFromCatalog());
        bottomBar.add(addToCartBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshCustomerCatalogTable(List<Product> products) {
        customerCatalogTableModel.setRowCount(0);
        for (Product p : products) {
            customerCatalogTableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getProductName(),
                    p.getCategory(),
                    String.format("$%.2f", p.getPriceAsDouble()),
                    p.getStockQuantity(),
                    p.getStockStatus()
            });
        }
    }

    private void applyCatalogFilters() {
        String keyword = customerSearchField.getText().trim().toLowerCase();
        String selectedCat = (String) customerCategoryCombo.getSelectedItem();

        List<Product> filtered = new ArrayList<>();
        for (Product p : catalogProducts) {
            boolean matchesCat = "All Categories".equals(selectedCat) || p.getCategory().equalsIgnoreCase(selectedCat);
            boolean matchesKeyword = keyword.isEmpty()
                    || p.getProductName().toLowerCase().contains(keyword)
                    || p.getDescription().toLowerCase().contains(keyword);

            if (matchesCat && matchesKeyword) {
                filtered.add(p);
            }
        }
        refreshCustomerCatalogTable(filtered);
    }

    private Product getSelectedCustomerProduct() {
        int row = customerCatalogTable.getSelectedRow();
        if (row < 0) {
            ValidationUtil.showWarning(this, "Please select a product from the catalog table first.", "Notice");
            return null;
        }

        int productId = (int) customerCatalogTableModel.getValueAt(row, 0);
        for (Product p : catalogProducts) {
            if (p.getProductId() == productId) {
                return p;
            }
        }
        return null;
    }

    private void handleViewProductDetails() {
        Product selected = getSelectedCustomerProduct();
        if (selected == null) return;

        ProductDetailsDialog dialog = new ProductDetailsDialog(this, selected, true, p -> {
            addProductToCart(p.getProductId(), p.getProductName(), p.getPriceAsDouble());
        });
        dialog.setVisible(true);
    }

    private void handleAddToCartFromCatalog() {
        Product selected = getSelectedCustomerProduct();
        if (selected == null) return;

        if (selected.isOutOfStock()) {
            ValidationUtil.showError(this, "This product is currently out of stock.", "Out of Stock");
            return;
        }

        addProductToCart(selected.getProductId(), selected.getProductName(), selected.getPriceAsDouble());
    }

    // ==========================================
    // SHOPPING CART FRONTEND
    // ==========================================

    private JPanel createCustomerCartPanel() {
        cartRootPanel = new JPanel(new BorderLayout());
        cartRootPanel.setBackground(Color.WHITE);
        renderCartContent();
        return cartRootPanel;
    }

    private void renderCartContent() {
        cartRootPanel.removeAll();

        if (shoppingCart.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(new EmptyBorder(60, 40, 60, 40));

            JLabel emptyTitle = new JLabel("Your Shopping Cart is Empty");
            emptyTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            emptyTitle.setForeground(new Color(71, 85, 105));
            emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptySub = new JLabel("Explore our catalog and add items you want to purchase.");
            emptySub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptySub.setForeground(new Color(148, 163, 184));
            emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton browseBtn = new JButton("Browse Catalog");
            browseBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            browseBtn.setBackground(new Color(37, 99, 235));
            browseBtn.setForeground(Color.WHITE);
            browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            browseBtn.addActionListener(e -> tabbedPane.setSelectedIndex(0));

            emptyPanel.add(emptyTitle);
            emptyPanel.add(Box.createVerticalStrut(8));
            emptyPanel.add(emptySub);
            emptyPanel.add(Box.createVerticalStrut(20));
            emptyPanel.add(browseBtn);

            cartRootPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            JPanel activePanel = new JPanel(new BorderLayout(15, 15));
            activePanel.setBackground(Color.WHITE);
            activePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Table of cart items
            String[] columns = {"Product Name", "Unit Price ($)", "Quantity", "Subtotal ($)"};
            cartTableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (OrderItem item : shoppingCart) {
                cartTableModel.addRow(new Object[]{
                        item.getProductName(),
                        String.format("$%.2f", item.getUnitPrice()),
                        item.getQuantity(),
                        String.format("$%.2f", item.getSubtotal())
                });
            }

            cartTable = new JTable(cartTableModel);
            cartTable.setRowHeight(30);
            cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            cartTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
            cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

            JScrollPane scrollPane = new JScrollPane(cartTable);
            activePanel.add(scrollPane, BorderLayout.CENTER);

            // Right & Bottom Summary Bar
            JPanel footerPanel = new JPanel(new BorderLayout());
            footerPanel.setBackground(Color.WHITE);
            footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

            // Quantity adjust buttons on left
            JPanel qtyControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            qtyControls.setBackground(Color.WHITE);

            JButton plusBtn = new JButton("+ Increase Qty");
            plusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            plusBtn.addActionListener(e -> modifyCartItemQuantity(1));

            JButton minusBtn = new JButton("- Decrease Qty");
            minusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            minusBtn.addActionListener(e -> modifyCartItemQuantity(-1));

            JButton removeBtn = new JButton("Remove Item");
            removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            removeBtn.setForeground(Color.RED);
            removeBtn.addActionListener(e -> removeSelectedCartItem());

            JButton clearBtn = new JButton("Clear All");
            clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            clearBtn.addActionListener(e -> clearEntireCart());

            qtyControls.add(plusBtn);
            qtyControls.add(minusBtn);
            qtyControls.add(removeBtn);
            qtyControls.add(clearBtn);
            footerPanel.add(qtyControls, BorderLayout.WEST);

            // Summary and checkout on right
            JPanel checkoutRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            checkoutRow.setBackground(Color.WHITE);

            double total = calculateCartTotal();
            cartTotalSummaryLabel = new JLabel(String.format("Cart Total: $%.2f", total));
            cartTotalSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            cartTotalSummaryLabel.setForeground(new Color(15, 23, 42));

            JButton checkoutBtn = new JButton("Proceed to Checkout");
            checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            checkoutBtn.setBackground(new Color(16, 185, 129));
            checkoutBtn.setForeground(Color.WHITE);
            checkoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            checkoutBtn.addActionListener(e -> showCheckoutDialog());

            checkoutRow.add(cartTotalSummaryLabel);
            checkoutRow.add(checkoutBtn);
            footerPanel.add(checkoutRow, BorderLayout.EAST);

            activePanel.add(footerPanel, BorderLayout.SOUTH);
            cartRootPanel.add(activePanel, BorderLayout.CENTER);
        }

        cartRootPanel.revalidate();
        cartRootPanel.repaint();
    }

    private void addProductToCart(int productId, String productName, double price) {
        for (OrderItem item : shoppingCart) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + 1);
                updateCartBadge();
                renderCartContent();
                ValidationUtil.showSuccess(this,
                        "Increased quantity for <b>" + productName + "</b> in your cart (Total: " + item.getQuantity() + ").",
                        "Cart Updated");
                return;
            }
        }

        OrderItem newItem = new OrderItem(shoppingCart.size() + 1, 0, productId, productName, 1, price);
        shoppingCart.add(newItem);
        updateCartBadge();
        renderCartContent();

        ValidationUtil.showSuccess(this,
                "<b>" + productName + "</b> added to your shopping cart!<br/>Go to the <b>My Cart</b> tab to review and checkout.",
                "Item Added");
    }

    private void modifyCartItemQuantity(int delta) {
        int selected = cartTable.getSelectedRow();
        if (selected < 0) {
            ValidationUtil.showWarning(this, "Please select an item in the cart table first.", "Selection Needed");
            return;
        }

        OrderItem item = shoppingCart.get(selected);
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            shoppingCart.remove(selected);
        } else {
            item.setQuantity(newQty);
        }

        updateCartBadge();
        renderCartContent();
    }

    private void removeSelectedCartItem() {
        int selected = cartTable.getSelectedRow();
        if (selected < 0) {
            ValidationUtil.showWarning(this, "Please select an item in the cart table to remove.", "Selection Needed");
            return;
        }

        OrderItem item = shoppingCart.remove(selected);
        updateCartBadge();
        renderCartContent();
        ValidationUtil.showSuccess(this, item.getProductName() + " removed from your cart.", "Cart Updated");
    }

    private void clearEntireCart() {
        if (shoppingCart.isEmpty()) return;
        if (ValidationUtil.showConfirm(this, "Are you sure you want to clear all items from your cart?", "Clear Cart")) {
            shoppingCart.clear();
            updateCartBadge();
            renderCartContent();
        }
    }

    private double calculateCartTotal() {
        double sum = 0;
        for (OrderItem item : shoppingCart) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    private int getCartItemCount() {
        int count = 0;
        for (OrderItem item : shoppingCart) {
            count += item.getQuantity();
        }
        return count;
    }

    private void updateCartBadge() {
        if (!currentUser.isAdmin() && tabbedPane != null) {
            tabbedPane.setTitleAt(1, "My Cart (" + getCartItemCount() + ")");
        }
    }

    private void showCheckoutDialog() {
        if (shoppingCart.isEmpty()) {
            ValidationUtil.showWarning(this, "Your cart is empty. Add items before checking out.", "Empty Cart");
            return;
        }

        JDialog dialog = new JDialog(this, "Complete Order & Checkout", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        // Title
        JLabel title = new JLabel("Checkout Order Confirmation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridy = 0;
        panel.add(title, gbc);

        // Order Total Breakdown
        double subtotal = calculateCartTotal();
        double tax = subtotal * 0.05;
        double grandTotal = subtotal + tax;

        JLabel totalDesc = new JLabel(String.format("<html><b>Items Total:</b> $%.2f &nbsp;|&nbsp; <b>Tax (5%%):</b> $%.2f &nbsp;|&nbsp; <b>Grand Total: <font color='#16a34a'>$%.2f</font></b></html>", subtotal, tax, grandTotal));
        totalDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 1;
        panel.add(totalDesc, gbc);

        // Shipping Address
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Shipping Address:"), gbc);

        JTextField addressField = new JTextField(currentUser.getAddress());
        addressField.setPreferredSize(new Dimension(340, 32));
        gbc.gridy = 3;
        panel.add(addressField, gbc);

        // Payment Method Options
        gbc.gridy = 4;
        panel.add(new JLabel("Select Payment Method:"), gbc);

        JComboBox<String> paymentCombo = new JComboBox<>(new String[]{"Cash on Delivery (COD)", "UPI / Card Online (Test Mode)"});
        gbc.gridy = 5;
        panel.add(paymentCombo, gbc);

        // Action Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton confirmBtn = new JButton("Confirm & Place Order");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmBtn.setBackground(new Color(16, 185, 129));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.addActionListener(e -> {
            String address = addressField.getText().trim();
            if (!ValidationUtil.isNotEmpty(address)) {
                ValidationUtil.showError(dialog, "Shipping Address cannot be blank.", "Validation Error");
                return;
            }

            currentUser.setAddress(address);
            String selectedPayment = paymentCombo.getSelectedIndex() == 0 ? "COD" : "PREPAID";
            String newOrderCode = "ORD-2026-" + String.format("%03d", customerOrders.size() + 1);

            Order newOrder = new Order(customerOrders.size() + 1, newOrderCode, currentUser.getUserId(),
                    grandTotal, "PENDING", selectedPayment, address, LocalDate.now().toString());

            // Copy items to order
            for (OrderItem ci : shoppingCart) {
                newOrder.addItem(new OrderItem(ci.getOrderItemId(), newOrder.getOrderId(), ci.getProductId(), ci.getProductName(), ci.getQuantity(), ci.getUnitPrice()));
            }

            customerOrders.add(0, newOrder); // add to top of orders list
            shoppingCart.clear();
            updateCartBadge();
            renderCartContent();
            applyOrderFilters();
            refreshAdminOrdersTable();

            dialog.dispose();

            // Switch to Order History tab to view new order
            tabbedPane.setSelectedIndex(2);

            ValidationUtil.showSuccess(this,
                    "🎉 <b>Order Placed Successfully!</b><br/>"
                            + "Order Reference: <b>" + newOrderCode + "</b><br/>"
                            + "Total Amount: <b>$" + String.format("%.2f", grandTotal) + "</b> (" + selectedPayment + ")<br/>"
                            + "You can track its status in your <b>Order History</b>.",
                    "Order Placed");
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        gbc.gridy = 6;
        gbc.insets = new Insets(18, 0, 0, 0);
        panel.add(btnRow, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ==========================================
    // CUSTOMER PROFILE SCREEN
    // ==========================================

    private JPanel createCustomerProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0;

        JLabel title = new JLabel("Customer Account Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(15, 23, 42));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;

        // Full Name
        gbc.gridy = 1;
        card.add(createFieldLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        profileNameVal = new JLabel(currentUser.getFullName());
        profileNameVal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(profileNameVal, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(createFieldLabel("Email Address:"), gbc);
        gbc.gridx = 1;
        profileEmailVal = new JLabel(currentUser.getEmail());
        profileEmailVal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(profileEmailVal, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(createFieldLabel("Contact Phone:"), gbc);
        gbc.gridx = 1;
        profilePhoneVal = new JLabel(currentUser.getPhone());
        profilePhoneVal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(profilePhoneVal, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 4;
        card.add(createFieldLabel("Shipping Address:"), gbc);
        gbc.gridx = 1;
        profileAddressVal = new JLabel("<html>" + currentUser.getAddress() + "</html>");
        profileAddressVal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(profileAddressVal, gbc);

        // Account Status
        gbc.gridx = 0;
        gbc.gridy = 5;
        card.add(createFieldLabel("Membership Status:"), gbc);
        gbc.gridx = 1;
        JLabel statusLabel = new JLabel("Active Customer (Verified)");
        statusLabel.setForeground(new Color(22, 101, 52));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(statusLabel, gbc);

        // Actions: Edit Info and Change Password
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setOpaque(false);

        JButton editBtn = new JButton("Edit Contact & Address");
        editBtn.setBackground(new Color(37, 99, 235));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> showEditProfileDialog());
        buttonRow.add(editBtn);

        JButton passBtn = new JButton("Change Password");
        passBtn.setBackground(new Color(75, 85, 99));
        passBtn.setForeground(Color.WHITE);
        passBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        passBtn.addActionListener(e -> showChangePasswordDialog());
        buttonRow.add(passBtn);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(buttonRow, gbc);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(71, 85, 105));
        return label;
    }

    private void showEditProfileDialog() {
        JDialog dialog = new JDialog(this, "Edit Contact & Shipping Information", true);
        dialog.setSize(440, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        panel.add(new JLabel("Full Name:"), gbc);
        JTextField nameField = new JTextField(currentUser.getFullName());
        gbc.gridy = 1;
        panel.add(nameField, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Phone Number:"), gbc);
        JTextField phoneField = new JTextField(currentUser.getPhone());
        gbc.gridy = 3;
        panel.add(phoneField, gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Shipping Address:"), gbc);
        JTextField addressField = new JTextField(currentUser.getAddress());
        gbc.gridy = 5;
        panel.add(addressField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(16, 185, 129));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newAddress = addressField.getText().trim();

            if (!ValidationUtil.isNotEmpty(newName)) {
                ValidationUtil.showError(dialog, "Full Name cannot be empty.", "Validation Error");
                return;
            }
            if (!ValidationUtil.isValidPhone(newPhone)) {
                ValidationUtil.showError(dialog, "Please enter a valid phone number (at least 7 digits).", "Validation Error");
                return;
            }
            if (!ValidationUtil.isNotEmpty(newAddress)) {
                ValidationUtil.showError(dialog, "Shipping Address cannot be empty.", "Validation Error");
                return;
            }

            currentUser.setFullName(newName);
            currentUser.setPhone(newPhone);
            currentUser.setAddress(newAddress);

            // Refresh UI
            profileNameVal.setText(newName);
            profilePhoneVal.setText(newPhone);
            profileAddressVal.setText("<html>" + newAddress + "</html>");

            dialog.dispose();
            ValidationUtil.showSuccess(this, "Profile information updated successfully!", "Profile Updated");
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 6;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(btnRow, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "Change Password", true);
        dialog.setSize(380, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        panel.add(new JLabel("Current Password:"), gbc);
        JPasswordField currPassField = new JPasswordField();
        gbc.gridy = 1;
        panel.add(currPassField, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("New Password (min 6 characters):"), gbc);
        JPasswordField newPassField = new JPasswordField();
        gbc.gridy = 3;
        panel.add(newPassField, gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Confirm New Password:"), gbc);
        JPasswordField confirmPassField = new JPasswordField();
        gbc.gridy = 5;
        panel.add(confirmPassField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton updateBtn = new JButton("Update Password");
        updateBtn.setBackground(new Color(37, 99, 235));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.addActionListener(e -> {
            String curr = new String(currPassField.getPassword());
            String next = new String(newPassField.getPassword());
            String conf = new String(confirmPassField.getPassword());

            if (!ValidationUtil.isNotEmpty(curr)) {
                ValidationUtil.showError(dialog, "Please enter your current password.", "Validation Error");
                return;
            }
            if (!ValidationUtil.isValidPassword(next)) {
                ValidationUtil.showError(dialog, "New password must be at least 6 characters long.", "Validation Error");
                return;
            }
            if (!next.equals(conf)) {
                ValidationUtil.showError(dialog, "New passwords do not match. Please re-enter.", "Validation Error");
                return;
            }

            dialog.dispose();
            ValidationUtil.showSuccess(this, "Password has been updated successfully!", "Security Updated");
        });

        btnRow.add(cancelBtn);
        btnRow.add(updateBtn);

        gbc.gridy = 6;
        gbc.insets = new Insets(14, 0, 0, 0);
        panel.add(btnRow, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ==========================================
    // ORDER HISTORY SCREEN (Customer)
    // ==========================================

    private JPanel createCustomerOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter / Search Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        toolBar.setBackground(Color.WHITE);

        toolBar.add(new JLabel("Filter by Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"});
        statusFilterCombo.addActionListener(e -> applyOrderFilters());
        toolBar.add(statusFilterCombo);

        toolBar.add(new JLabel("Search Order #:"));
        orderSearchField = new JTextField(12);
        toolBar.add(orderSearchField);

        JButton searchBtn = new JButton("Filter");
        searchBtn.setBackground(new Color(37, 99, 235));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> applyOrderFilters());
        toolBar.add(searchBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            statusFilterCombo.setSelectedIndex(0);
            orderSearchField.setText("");
            applyOrderFilters();
        });
        toolBar.add(resetBtn);

        panel.add(toolBar, BorderLayout.NORTH);

        // Orders Table
        String[] columns = {"Order #", "Order Date", "Items", "Total ($)", "Payment Mode", "Status"};
        ordersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        ordersTable.setRowHeight(28);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        refreshOrdersTable(customerOrders);

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Controls
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        actionRow.setBackground(Color.WHITE);

        JButton viewDetailsBtn = new JButton("View Order Details / Invoice");
        viewDetailsBtn.setBackground(new Color(15, 23, 42));
        viewDetailsBtn.setForeground(Color.WHITE);
        viewDetailsBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewDetailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDetailsBtn.addActionListener(e -> handleViewOrderDetails());
        actionRow.add(viewDetailsBtn);

        JButton cancelOrderBtn = new JButton("Cancel Selected Order");
        cancelOrderBtn.setBackground(new Color(220, 38, 38));
        cancelOrderBtn.setForeground(Color.WHITE);
        cancelOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelOrderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelOrderBtn.addActionListener(e -> handleCancelOrder());
        actionRow.add(cancelOrderBtn);

        panel.add(actionRow, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshOrdersTable(List<Order> ordersToDisplay) {
        ordersTableModel.setRowCount(0);
        for (Order order : ordersToDisplay) {
            ordersTableModel.addRow(new Object[]{
                    order.getOrderNumber(),
                    order.getOrderDate(),
                    order.getTotalQuantity() > 0 ? order.getTotalQuantity() + " items" : "1 package",
                    String.format("$%.2f", order.getTotalAmount()),
                    order.getPaymentMethod(),
                    order.getOrderStatus()
            });
        }
    }

    private void applyOrderFilters() {
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String searchNum = orderSearchField.getText().trim().toUpperCase();

        List<Order> filtered = new ArrayList<>();
        for (Order o : customerOrders) {
            boolean matchesStatus = "ALL".equals(selectedStatus) || o.getOrderStatus().equalsIgnoreCase(selectedStatus);
            boolean matchesSearch = searchNum.isEmpty() || o.getOrderNumber().toUpperCase().contains(searchNum);

            if (matchesStatus && matchesSearch) {
                filtered.add(o);
            }
        }
        refreshOrdersTable(filtered);
    }

    private Order getSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            ValidationUtil.showWarning(this, "Please select an order from the table first.", "No Order Selected");
            return null;
        }

        String orderNum = (String) ordersTableModel.getValueAt(selectedRow, 0);
        for (Order o : customerOrders) {
            if (o.getOrderNumber().equalsIgnoreCase(orderNum)) {
                return o;
            }
        }
        return null;
    }

    private void handleViewOrderDetails() {
        Order selected = getSelectedOrder();
        if (selected == null) return;

        String addr = selected.getShippingAddress() != null ? selected.getShippingAddress() : currentUser.getAddress();
        OrderDetailsDialog dialog = new OrderDetailsDialog(this, selected, addr);
        dialog.setVisible(true);
    }

    private void handleCancelOrder() {
        Order selected = getSelectedOrder();
        if (selected == null) return;

        if (!selected.isCancellable()) {
            ValidationUtil.showError(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> cannot be cancelled because it is in status: <b>"
                            + selected.getOrderStatus() + "</b>.<br/>(Only PENDING or CONFIRMED orders are eligible for cancellation).",
                    "Cancellation Ineligible");
            return;
        }

        boolean confirm = ValidationUtil.showConfirm(this,
                "Are you sure you want to cancel order <b>" + selected.getOrderNumber() + "</b>?<br/>"
                        + "This will restore reserved inventory stock.",
                "Confirm Cancellation");

        if (confirm) {
            selected.setOrderStatus("CANCELLED");
            applyOrderFilters();
            refreshAdminOrdersTable();
            ValidationUtil.showSuccess(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> has been successfully cancelled.<br/>"
                            + "Inventory stock has been restored.",
                    "Order Cancelled");
        }
    }

    // ==========================================
    // ADMIN SCREENS
    // ==========================================

    private JPanel createAdminCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBackground(Color.WHITE);

        JButton addBtn = new JButton("+ Add New Product");
        addBtn.setBackground(new Color(16, 185, 129));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddProductDialog());
        toolBar.add(addBtn);

        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewBtn.addActionListener(e -> handleAdminViewProductDetails());
        toolBar.add(viewBtn);

        JButton deleteBtn = new JButton("Delete Product");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteBtn.addActionListener(e -> handleAdminDeleteProduct());
        toolBar.add(deleteBtn);

        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Product ID", "Name", "Category", "Unit Price ($)", "Stock Qty", "Status"};
        adminCatalogTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        adminCatalogTable = new JTable(adminCatalogTableModel);
        adminCatalogTable.setRowHeight(28);
        adminCatalogTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminCatalogTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        refreshAdminCatalogTable();

        panel.add(new JScrollPane(adminCatalogTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshAdminCatalogTable() {
        if (adminCatalogTableModel == null) return;
        adminCatalogTableModel.setRowCount(0);
        for (Product p : catalogProducts) {
            adminCatalogTableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getProductName(),
                    p.getCategory(),
                    String.format("$%.2f", p.getPriceAsDouble()),
                    p.getStockQuantity(),
                    p.getStockStatus()
            });
        }
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add New Catalog Product", true);
        dialog.setSize(440, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        panel.add(new JLabel("Product Name:"), gbc);
        JTextField nameField = new JTextField();
        gbc.gridy = 1;
        panel.add(nameField, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Category:"), gbc);
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Electronics", "Clothing", "Books", "Home", "Sports", "Other"});
        gbc.gridy = 3;
        panel.add(categoryCombo, gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        JTextField descField = new JTextField();
        gbc.gridy = 5;
        panel.add(descField, gbc);

        gbc.gridy = 6;
        panel.add(new JLabel("Unit Price ($):"), gbc);
        JTextField priceField = new JTextField();
        gbc.gridy = 7;
        panel.add(priceField, gbc);

        gbc.gridy = 8;
        panel.add(new JLabel("Initial Stock Quantity:"), gbc);
        JTextField stockField = new JTextField();
        gbc.gridy = 9;
        panel.add(stockField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Add Product");
        saveBtn.setBackground(new Color(16, 185, 129));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (!ValidationUtil.isNotEmpty(name)) {
                ValidationUtil.showError(dialog, "Product Name cannot be blank.", "Validation Error");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ValidationUtil.showError(dialog, "Please enter a valid non-negative price.", "Validation Error");
                return;
            }

            int stock;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                ValidationUtil.showError(dialog, "Please enter a valid non-negative stock quantity.", "Validation Error");
                return;
            }

            int newId = 100 + catalogProducts.size() + 1;
            String category = (String) categoryCombo.getSelectedItem();
            Product newProduct = new Product(newId, name, category, desc, price, stock);

            catalogProducts.add(newProduct);
            refreshAdminCatalogTable();
            if (customerCatalogTableModel != null) {
                applyCatalogFilters();
            }

            dialog.dispose();
            ValidationUtil.showSuccess(this, "Product <b>" + name + "</b> added to catalog successfully!", "Product Added");
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 10;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(btnRow, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void handleAdminViewProductDetails() {
        int row = adminCatalogTable.getSelectedRow();
        if (row < 0) {
            ValidationUtil.showWarning(this, "Please select a product from the table first.", "Notice");
            return;
        }

        int productId = (int) adminCatalogTableModel.getValueAt(row, 0);
        for (Product p : catalogProducts) {
            if (p.getProductId() == productId) {
                ProductDetailsDialog dialog = new ProductDetailsDialog(this, p, false, null);
                dialog.setVisible(true);
                return;
            }
        }
    }

    private void handleAdminDeleteProduct() {
        int row = adminCatalogTable.getSelectedRow();
        if (row < 0) {
            ValidationUtil.showWarning(this, "Please select a product to delete.", "Notice");
            return;
        }

        int productId = (int) adminCatalogTableModel.getValueAt(row, 0);
        String name = (String) adminCatalogTableModel.getValueAt(row, 1);

        if (ValidationUtil.showConfirm(this, "Are you sure you want to delete product <b>" + name + "</b> (ID: #" + productId + ")?", "Confirm Delete")) {
            catalogProducts.removeIf(p -> p.getProductId() == productId);
            refreshAdminCatalogTable();
            if (customerCatalogTableModel != null) {
                applyCatalogFilters();
            }
            ValidationUtil.showSuccess(this, "Product <b>" + name + "</b> has been removed from catalog.", "Product Deleted");
        }
    }

    // ==========================================
    // ADMIN ORDER MANAGEMENT
    // ==========================================

    private JPanel createAdminOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Admin Filter Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        toolBar.setBackground(Color.WHITE);

        toolBar.add(new JLabel("Filter Orders by Status:"));
        adminOrderStatusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"});
        adminOrderStatusFilterCombo.addActionListener(e -> applyAdminOrderFilters());
        toolBar.add(adminOrderStatusFilterCombo);

        JButton updateStatusBtn = new JButton("Update Order Status");
        updateStatusBtn.setBackground(new Color(37, 99, 235));
        updateStatusBtn.setForeground(Color.WHITE);
        updateStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateStatusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateStatusBtn.addActionListener(e -> handleAdminUpdateOrderStatus());
        toolBar.add(updateStatusBtn);

        JButton viewOrderBtn = new JButton("View Invoice & Details");
        viewOrderBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewOrderBtn.addActionListener(e -> handleAdminViewOrderDetails());
        toolBar.add(viewOrderBtn);

        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Order #", "User ID", "Date", "Total Amount ($)", "Payment Mode", "Order Status"};
        adminOrdersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        adminOrdersTable = new JTable(adminOrdersTableModel);
        adminOrdersTable.setRowHeight(28);
        adminOrdersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminOrdersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        adminOrdersTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        refreshAdminOrdersTable();

        panel.add(new JScrollPane(adminOrdersTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshAdminOrdersTable() {
        if (adminOrdersTableModel == null) return;
        applyAdminOrderFilters();
    }

    private void applyAdminOrderFilters() {
        if (adminOrdersTableModel == null) return;
        String filter = adminOrderStatusFilterCombo != null ? (String) adminOrderStatusFilterCombo.getSelectedItem() : "ALL";

        adminOrdersTableModel.setRowCount(0);
        for (Order o : customerOrders) {
            if ("ALL".equals(filter) || o.getOrderStatus().equalsIgnoreCase(filter)) {
                adminOrdersTableModel.addRow(new Object[]{
                        o.getOrderNumber(),
                        "User #" + o.getUserId(),
                        o.getOrderDate(),
                        String.format("$%.2f", o.getTotalAmount()),
                        o.getPaymentMethod(),
                        o.getOrderStatus()
                });
            }
        }
    }

    private Order getSelectedAdminOrder() {
        int row = adminOrdersTable.getSelectedRow();
        if (row < 0) {
            ValidationUtil.showWarning(this, "Please select an order from the table first.", "Notice");
            return null;
        }

        String orderNum = (String) adminOrdersTableModel.getValueAt(row, 0);
        for (Order o : customerOrders) {
            if (o.getOrderNumber().equalsIgnoreCase(orderNum)) {
                return o;
            }
        }
        return null;
    }

    private void handleAdminViewOrderDetails() {
        Order selected = getSelectedAdminOrder();
        if (selected == null) return;

        String addr = selected.getShippingAddress() != null ? selected.getShippingAddress() : "Customer Address on File";
        OrderDetailsDialog dialog = new OrderDetailsDialog(this, selected, addr);
        dialog.setVisible(true);
    }

    private void handleAdminUpdateOrderStatus() {
        Order selected = getSelectedAdminOrder();
        if (selected == null) return;

        String[] statuses = {"PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"};
        String current = selected.getOrderStatus();

        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select new status for order " + selected.getOrderNumber() + ":",
                "Update Order Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                current
        );

        if (newStatus != null && !newStatus.equalsIgnoreCase(current)) {
            selected.setOrderStatus(newStatus);
            refreshAdminOrdersTable();
            if (ordersTableModel != null) {
                applyOrderFilters();
            }
            ValidationUtil.showSuccess(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> status updated to <b>" + newStatus + "</b>.",
                    "Status Updated");
        }
    }

    private JPanel createAdminReportsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        panel.add(createKpiCard("Total Orders", String.valueOf(customerOrders.size()), new Color(37, 99, 235)));

        double revenue = 0.0;
        for (Order o : customerOrders) {
            if (!"CANCELLED".equalsIgnoreCase(o.getOrderStatus())) {
                revenue += o.getTotalAmount();
            }
        }
        panel.add(createKpiCard("Total Sales Revenue", String.format("$%.2f", revenue), new Color(16, 185, 129)));
        panel.add(createKpiCard("Active Products", String.valueOf(catalogProducts.size()), new Color(139, 92, 246)));

        long lowStockCount = catalogProducts.stream().filter(Product::isLowStock).count();
        panel.add(createKpiCard("Low Stock Alerts", lowStockCount + " Products", new Color(239, 68, 68)));
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
        if (ValidationUtil.showConfirm(this, "Are you sure you want to log out?", "Confirm Logout")) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }

    // --- Helper to build initial sample products ---
    private List<Product> initializeSampleProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(101, 1, "Electronics", "Wireless Bluetooth Headphones", "Noise-cancelling over-ear wireless headphones with deep bass", 59.99, 25));
        products.add(new Product(102, 1, "Electronics", "Mechanical Gaming Keyboard", "RGB backlit blue switches with mechanical tactile feedback", 79.99, 14));
        products.add(new Product(103, 2, "Clothing", "Classic Cotton T-Shirt", "100% organic combed cotton everyday unisex wear", 19.99, 5));
        products.add(new Product(104, 3, "Books", "Java Programming: OOP Essentials", "Comprehensive guide to OOP, Collections, and Design Patterns in Java", 39.99, 30));
        products.add(new Product(105, 4, "Home", "Stainless Steel Water Bottle", "Vacuum insulated 750ml double-walled thermal flask", 14.99, 40));
        products.add(new Product(106, 1, "Electronics", "USB-C Fast Charging Hub", "6-in-1 multi-port adapter with 4K HDMI and 65W Power Delivery", 29.99, 18));
        return products;
    }

    // --- Helper to build initial sample orders ---
    private List<Order> initializeSampleOrders() {
        List<Order> orders = new ArrayList<>();

        Order o1 = new Order(1, "ORD-2026-001", 1, 79.98, "DELIVERED", "COD", "124 Tech Park Road, Bengaluru", "2026-10-15");
        o1.addItem(new OrderItem(1, 1, 103, "Classic Cotton T-Shirt", 2, 19.99));
        o1.addItem(new OrderItem(2, 1, 104, "Java Programming: OOP Essentials", 1, 39.99));
        orders.add(o1);

        Order o2 = new Order(2, "ORD-2026-002", 1, 59.99, "SHIPPED", "PREPAID", "124 Tech Park Road, Bengaluru", "2026-10-18");
        o2.addItem(new OrderItem(3, 2, 101, "Wireless Bluetooth Headphones", 1, 59.99));
        orders.add(o2);

        Order o3 = new Order(3, "ORD-2026-003", 1, 94.98, "PENDING", "COD", "124 Tech Park Road, Bengaluru", "2026-10-20");
        o3.addItem(new OrderItem(4, 3, 102, "Mechanical Gaming Keyboard", 1, 79.99));
        o3.addItem(new OrderItem(5, 3, 105, "Stainless Steel Water Bottle", 1, 14.99));
        orders.add(o3);

        Order o4 = new Order(4, "ORD-2026-004", 1, 14.99, "CANCELLED", "COD", "124 Tech Park Road, Bengaluru", "2026-10-12");
        o4.addItem(new OrderItem(6, 4, 105, "Stainless Steel Water Bottle", 1, 14.99));
        orders.add(o4);

        return orders;
    }
}
