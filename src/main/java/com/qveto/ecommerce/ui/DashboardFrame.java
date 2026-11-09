package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.dao.ReportDAO.CustomerSpendStat;
import com.qveto.ecommerce.dao.ReportDAO.ProductSalesStat;
import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.model.OrderItem;
import com.qveto.ecommerce.model.Payment;
import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.CartStore;
import com.qveto.ecommerce.util.CategoryStore;
import com.qveto.ecommerce.util.CouponStore;
import com.qveto.ecommerce.util.DBStatusUtil;
import com.qveto.ecommerce.model.Address;
import com.qveto.ecommerce.model.AuditLog;
import com.qveto.ecommerce.util.AddressBookStore;
import com.qveto.ecommerce.util.AuditStore;
import com.qveto.ecommerce.util.OrderStore;
import com.qveto.ecommerce.util.PaymentStore;
import com.qveto.ecommerce.util.ProductStore;
import com.qveto.ecommerce.util.ReportStore;
import com.qveto.ecommerce.util.UserStore;
import com.qveto.ecommerce.util.ValidationUtil;
import com.qveto.ecommerce.util.WishlistStore;
import com.qveto.ecommerce.util.NotificationStore;
import com.qveto.ecommerce.model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Main Application Dashboard & Navigation Shell.
 * 
 * Developed for Member 2 tasks:
 * - Product Catalog Discovery: Search, category filtering, and product details modal
 * - Interactive Shopping Cart with live total calculation & quantity updates
 * - Checkout workflow connecting Cart to Order History with automatic inventory deduction
 * - Customer Profile management (Edit contact/address, change password synced with DB)
 * - Order History screen with status filtering, invoice export, and order cancellation with stock replenishment
 * - Admin Product management (Add, edit & restock inventory, delete, low stock alerts)
 * - Admin Order Management (View all orders, inspect invoices, and update order statuses)
 * - Admin User Management (Discover, search, filter, inspect dossier, and deactivate accounts)
 * - Executive Sales & KPIs Dashboard: Real-time revenue metrics, product velocity leaderboard, order lifecycle distribution, and category revenue breakdown
 * - Category Management: Interactive category management dialog with product dependency validation
 * - Cart Persistence: Seamless synchronization between CartDAO and session carts
 * - Real-time Database Connectivity Indicator (MySQL vs Standalone Demo Mode)
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
    private JComboBox<String> customerSortCombo;
    private JCheckBox customerInStockOnlyCheck;

    // Customer Cart Promo components
    private JTextField promoCodeField;
    private JLabel promoStatusLabel;
    private CouponStore.Coupon appliedCoupon = null;
    private double appliedDiscount = 0.0;

    // Admin Catalog UI components
    private DefaultTableModel adminCatalogTableModel;
    private JTable adminCatalogTable;
    private JCheckBox adminLowStockFilterCheck;

    // Admin Orders UI components
    private DefaultTableModel adminOrdersTableModel;
    private JTable adminOrdersTable;
    private JComboBox<String> adminOrderStatusFilterCombo;
    private JTextField adminOrderSearchField;

    // Admin User Management UI components
    private DefaultTableModel adminUsersTableModel;
    private JTable adminUsersTable;
    private JTextField adminUserSearchField;
    private JComboBox<String> adminUserRoleFilterCombo;
    private JPanel adminUsersKpiPanel;

    // Admin Reports & Analytics UI components
    private JLabel kpiTotalOrdersVal;
    private JLabel kpiTotalRevenueVal;
    private JLabel kpiCollectedRevenueVal;
    private JLabel kpiAvgOrderVal;
    private JLabel kpiTotalCustomersVal;
    private JLabel kpiLowStockVal;
    private DefaultTableModel topSellingTableModel;
    private DefaultTableModel orderStatusDistTableModel;
    private DefaultTableModel categoryRevenueTableModel;
    private DefaultTableModel customerSpendTableModel;
    private DefaultTableModel paymentDistTableModel;

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

    // Header & Wishlist UI components
    private JButton notificationBtn;
    private DefaultTableModel wishlistTableModel;
    private JTable wishlistTable;

    public DashboardFrame(User user) {
        this.currentUser = user;
        this.catalogProducts = new ArrayList<>(ProductStore.getAllProducts());
        this.customerOrders = new ArrayList<>(currentUser.isAdmin() ? OrderStore.getAllOrders() : OrderStore.getOrdersByUser(currentUser.getUserId()));
        this.shoppingCart = new ArrayList<>(currentUser.isAdmin() ? Collections.emptyList() : CartStore.getCart(currentUser.getUserId()));
        initComponents();
        updateCartBadge();
    }

    // Overload for backwards compatibility
    public DashboardFrame(String username, String role) {
        this(new User(1, username.contains("@") ? username.split("@")[0] : username, username, role));
    }

    private void initComponents() {
        setTitle("Qveto E-Commerce — " + (currentUser.isAdmin() ? "Admin Management" : "Customer Portal"));
        setSize(980, 680);
        setMinimumSize(new Dimension(880, 560));
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

        JLabel dbStatusBadge = new JLabel(DBStatusUtil.getStatusBadgeHtml());
        userPanel.add(dbStatusBadge);

        // Real-Time Notification Center Button
        notificationBtn = new JButton();
        updateNotificationBadge();
        notificationBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        notificationBtn.setBackground(new Color(30, 58, 102));
        notificationBtn.setForeground(Color.WHITE);
        notificationBtn.setFocusPainted(false);
        notificationBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBtn.addActionListener(e -> {
            NotificationCenterDialog dialog = new NotificationCenterDialog(this, currentUser, this::updateNotificationBadge);
            dialog.setVisible(true);
            updateNotificationBadge();
        });
        userPanel.add(notificationBtn);

        JButton dossierBtn = new JButton("ℹ️ Project Dossier");
        dossierBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dossierBtn.setBackground(new Color(30, 58, 102));
        dossierBtn.setForeground(Color.WHITE);
        dossierBtn.setFocusPainted(false);
        dossierBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dossierBtn.addActionListener(e -> new ProjectOverviewDialog(this).setVisible(true));
        userPanel.add(dossierBtn);

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
            tabbedPane.addTab("User Management", createAdminUsersPanel());
            tabbedPane.addTab("Reports & KPIs", createAdminReportsPanel());
            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedIndex() == 3) {
                    refreshAdminReportsPanel();
                }
            });
        } else {
            tabbedPane.addTab("Browse Products", createCustomerCatalogPanel());
            tabbedPane.addTab("My Cart (" + getCartItemCount() + ")", createCustomerCartPanel());
            tabbedPane.addTab("Wishlist (" + WishlistStore.getWishlistCount(currentUser.getUserId()) + ")", createCustomerWishlistPanel());
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
        List<String> catOptions = new ArrayList<>();
        catOptions.add("All Categories");
        catOptions.addAll(CategoryStore.getCategoryNames());
        customerCategoryCombo = new JComboBox<>(catOptions.toArray(new String[0]));
        customerCategoryCombo.addActionListener(e -> applyCatalogFilters());
        searchBar.add(customerCategoryCombo);

        searchBar.add(new JLabel("Sort By:"));
        String[] sortOptions = {
                "Featured (Default)",
                "Price: Low to High",
                "Price: High to Low",
                "Name: A to Z",
                "Stock: Highest Available"
        };
        customerSortCombo = new JComboBox<>(sortOptions);
        customerSortCombo.addActionListener(e -> applyCatalogFilters());
        searchBar.add(customerSortCombo);

        customerInStockOnlyCheck = new JCheckBox("In-Stock Only");
        customerInStockOnlyCheck.setBackground(Color.WHITE);
        customerInStockOnlyCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        customerInStockOnlyCheck.addActionListener(e -> applyCatalogFilters());
        searchBar.add(customerInStockOnlyCheck);

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
            if (customerSortCombo != null) customerSortCombo.setSelectedIndex(0);
            if (customerInStockOnlyCheck != null) customerInStockOnlyCheck.setSelected(false);
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

        JButton wishlistBtn = new JButton("♡ Save to Wishlist");
        wishlistBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        wishlistBtn.setBackground(new Color(241, 245, 249));
        wishlistBtn.setForeground(new Color(51, 65, 85));
        wishlistBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        wishlistBtn.addActionListener(e -> handleAddSelectedToWishlist());
        bottomBar.add(wishlistBtn);

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
        String keyword = customerSearchField != null ? customerSearchField.getText().trim() : "";
        String selectedCat = customerCategoryCombo != null ? (String) customerCategoryCombo.getSelectedItem() : "All Categories";

        List<Product> filtered = ProductStore.searchProducts(keyword, selectedCat);

        if (customerInStockOnlyCheck != null && customerInStockOnlyCheck.isSelected()) {
            filtered.removeIf(p -> p.getStockQuantity() <= 0);
        }

        if (customerSortCombo != null) {
            int sortIndex = customerSortCombo.getSelectedIndex();
            switch (sortIndex) {
                case 1: // Price: Low to High
                    filtered.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
                    break;
                case 2: // Price: High to Low
                    filtered.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));
                    break;
                case 3: // Name: A to Z
                    filtered.sort((a, b) -> a.getProductName().compareToIgnoreCase(b.getProductName()));
                    break;
                case 4: // Stock: Highest Available
                    filtered.sort((a, b) -> Integer.compare(b.getStockQuantity(), a.getStockQuantity()));
                    break;
                default: // Featured (Default)
                    break;
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

        ProductDetailsDialog dialog = new ProductDetailsDialog(this, selected, currentUser, p -> {
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
            JPanel footerPanel = new JPanel(new BorderLayout(0, 10));
            footerPanel.setBackground(Color.WHITE);
            footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

            // Promotional Coupon Panel
            JPanel promoPanel = new JPanel(new BorderLayout());
            promoPanel.setBackground(new Color(248, 250, 252));
            promoPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            JPanel promoLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            promoLeft.setOpaque(false);
            JLabel promoIcon = new JLabel("🏷️");
            JLabel promoHint = new JLabel("<html><b>Promotions:</b> Apply <code>QVETO10</code> (10% off), <code>SAVE20</code> (20% off), <code>FLAT15</code> ($15 off), or <code>WELCOME</code></html>");
            promoHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            promoHint.setForeground(new Color(71, 85, 105));
            promoLeft.add(promoIcon);
            promoLeft.add(promoHint);
            promoPanel.add(promoLeft, BorderLayout.WEST);

            JPanel promoRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            promoRight.setOpaque(false);

            promoCodeField = new JTextField(8);
            promoCodeField.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (appliedCoupon != null) {
                promoCodeField.setText(appliedCoupon.getCode());
            }
            promoCodeField.addActionListener(e -> handleApplyPromoCode());
            promoRight.add(promoCodeField);

            JButton applyPromoBtn = new JButton("Apply Coupon");
            applyPromoBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            applyPromoBtn.setBackground(new Color(37, 99, 235));
            applyPromoBtn.setForeground(Color.WHITE);
            applyPromoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            applyPromoBtn.addActionListener(e -> handleApplyPromoCode());
            promoRight.add(applyPromoBtn);

            JButton removePromoBtn = new JButton("Remove");
            removePromoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            removePromoBtn.addActionListener(e -> handleRemovePromoCode());
            promoRight.add(removePromoBtn);

            promoPanel.add(promoRight, BorderLayout.EAST);
            footerPanel.add(promoPanel, BorderLayout.NORTH);

            // Bottom action row: Quantity adjust on left, summary/checkout on right
            JPanel actionRow = new JPanel(new BorderLayout());
            actionRow.setBackground(Color.WHITE);

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
            actionRow.add(qtyControls, BorderLayout.WEST);

            // Summary and checkout on right
            JPanel checkoutRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            checkoutRow.setBackground(Color.WHITE);

            double subtotal = calculateCartTotal();
            if (appliedCoupon != null) {
                CouponStore.CouponResult res = CouponStore.validateAndApply(appliedCoupon.getCode(), subtotal);
                if (res.isValid()) {
                    appliedDiscount = res.getDiscountAmount();
                } else {
                    appliedCoupon = null;
                    appliedDiscount = 0.0;
                }
            }

            if (appliedCoupon != null && appliedDiscount > 0) {
                double discounted = Math.max(0, subtotal - appliedDiscount);
                cartTotalSummaryLabel = new JLabel(String.format("<html>Subtotal: $%.2f &nbsp;|&nbsp; <font color='#16a34a'><b>Discount (%s): -$%.2f</b></font> &nbsp;|&nbsp; <b>Net: $%.2f</b></html>",
                        subtotal, appliedCoupon.getCode(), appliedDiscount, discounted));
            } else {
                cartTotalSummaryLabel = new JLabel(String.format("Cart Total: $%.2f", subtotal));
            }
            cartTotalSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            cartTotalSummaryLabel.setForeground(new Color(15, 23, 42));

            JButton checkoutBtn = new JButton("Proceed to Checkout");
            checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            checkoutBtn.setBackground(new Color(16, 185, 129));
            checkoutBtn.setForeground(Color.WHITE);
            checkoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            checkoutBtn.addActionListener(e -> showCheckoutDialog());

            checkoutRow.add(cartTotalSummaryLabel);
            checkoutRow.add(checkoutBtn);
            actionRow.add(checkoutRow, BorderLayout.EAST);

            footerPanel.add(actionRow, BorderLayout.SOUTH);
            activePanel.add(footerPanel, BorderLayout.SOUTH);
            cartRootPanel.add(activePanel, BorderLayout.CENTER);
        }

        cartRootPanel.revalidate();
        cartRootPanel.repaint();
    }

    private void addProductToCart(int productId, String productName, double price) {
        CartStore.addItem(currentUser.getUserId(), productId, productName, price, 1);
        shoppingCart.clear();
        shoppingCart.addAll(CartStore.getCart(currentUser.getUserId()));
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
        CartStore.updateQuantity(currentUser.getUserId(), item.getProductId(), newQty);
        shoppingCart.clear();
        shoppingCart.addAll(CartStore.getCart(currentUser.getUserId()));

        updateCartBadge();
        renderCartContent();
    }

    private void removeSelectedCartItem() {
        int selected = cartTable.getSelectedRow();
        if (selected < 0) {
            ValidationUtil.showWarning(this, "Please select an item in the cart table to remove.", "Selection Needed");
            return;
        }

        OrderItem item = shoppingCart.get(selected);
        CartStore.removeItem(currentUser.getUserId(), item.getProductId());
        shoppingCart.clear();
        shoppingCart.addAll(CartStore.getCart(currentUser.getUserId()));

        updateCartBadge();
        renderCartContent();
        ValidationUtil.showSuccess(this, item.getProductName() + " removed from your cart.", "Cart Updated");
    }

    private void clearEntireCart() {
        if (shoppingCart.isEmpty()) return;
        if (ValidationUtil.showConfirm(this, "Are you sure you want to clear all items from your cart?", "Clear Cart")) {
            CartStore.clearCart(currentUser.getUserId());
            shoppingCart.clear();
            appliedCoupon = null;
            appliedDiscount = 0.0;
            updateCartBadge();
            renderCartContent();
        }
    }

    private void handleApplyPromoCode() {
        if (promoCodeField == null) return;
        String code = promoCodeField.getText().trim();
        double subtotal = calculateCartTotal();
        CouponStore.CouponResult res = CouponStore.validateAndApply(code, subtotal);
        if (res.isValid()) {
            appliedCoupon = res.getCoupon();
            appliedDiscount = res.getDiscountAmount();
            ValidationUtil.showSuccess(this, res.getMessage(), "Coupon Applied");
            renderCartContent();
        } else {
            ValidationUtil.showWarning(this, res.getMessage(), "Invalid Coupon");
        }
    }

    private void handleRemovePromoCode() {
        appliedCoupon = null;
        appliedDiscount = 0.0;
        if (promoCodeField != null) {
            promoCodeField.setText("");
        }
        renderCartContent();
        ValidationUtil.showSuccess(this, "Coupon discount removed from cart.", "Coupon Cleared");
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
            if (tabbedPane.getTabCount() > 2) {
                tabbedPane.setTitleAt(2, "Wishlist (" + WishlistStore.getWishlistCount(currentUser.getUserId()) + ")");
            }
        }
    }

    private void showCheckoutDialog() {
        if (shoppingCart.isEmpty()) {
            ValidationUtil.showWarning(this, "Your cart is empty. Add items before checking out.", "Empty Cart");
            return;
        }

        JDialog dialog = new JDialog(this, "Complete Order & Checkout", true);
        dialog.setSize(520, 520);
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
        double discount = (appliedCoupon != null) ? appliedDiscount : 0.0;
        double taxableAmount = Math.max(0.0, subtotal - discount);
        double tax = taxableAmount * 0.05;
        double grandTotal = taxableAmount + tax;

        String breakdownHtml;
        if (discount > 0 && appliedCoupon != null) {
            breakdownHtml = String.format("<html><b>Items Total:</b> $%.2f &nbsp;|&nbsp; <font color='#16a34a'><b>Discount (%s):</b> -$%.2f</font> &nbsp;|&nbsp; <b>Tax (5%%):</b> $%.2f &nbsp;|&nbsp; <b>Grand Total: <font color='#16a34a'>$%.2f</font></b></html>",
                    subtotal, appliedCoupon.getCode(), discount, tax, grandTotal);
        } else {
            breakdownHtml = String.format("<html><b>Items Total:</b> $%.2f &nbsp;|&nbsp; <b>Tax (5%%):</b> $%.2f &nbsp;|&nbsp; <b>Grand Total: <font color='#16a34a'>$%.2f</font></b></html>",
                    subtotal, tax, grandTotal);
        }

        JLabel totalDesc = new JLabel(breakdownHtml);
        totalDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 1;
        panel.add(totalDesc, gbc);

        // Shipping Address with Address Book selector
        gbc.gridy = 2;
        JPanel addressHeaderRow = new JPanel(new BorderLayout());
        addressHeaderRow.setOpaque(false);
        addressHeaderRow.add(new JLabel("Select Delivery Destination:"), BorderLayout.WEST);

        JButton manageAddressesBtn = new JButton("Manage Address Book");
        manageAddressesBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        manageAddressesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addressHeaderRow.add(manageAddressesBtn, BorderLayout.EAST);
        panel.add(addressHeaderRow, gbc);

        // Address combo box
        List<Address> savedAddresses = AddressBookStore.getAddresses(currentUser.getUserId());
        if (savedAddresses.isEmpty() && currentUser.getAddress() != null && !currentUser.getAddress().trim().isEmpty()) {
            Address initialAddr = new Address(0, currentUser.getUserId(), "Home", currentUser.getFullName(),
                    currentUser.getAddress(), "Bengaluru", "Karnataka", "560001", currentUser.getPhone(), true);
            AddressBookStore.addAddress(initialAddr);
            savedAddresses = AddressBookStore.getAddresses(currentUser.getUserId());
        }

        DefaultComboBoxModel<Address> addressComboModel = new DefaultComboBoxModel<>();
        for (Address addr : savedAddresses) {
            addressComboModel.addElement(addr);
        }

        JComboBox<Address> addressCombo = new JComboBox<>(addressComboModel);
        Address defaultAddr = AddressBookStore.getDefaultAddress(currentUser.getUserId());
        if (defaultAddr != null) {
            addressCombo.setSelectedItem(defaultAddr);
        }

        gbc.gridy = 3;
        panel.add(addressCombo, gbc);

        // Editable Address Line Details Field for customized delivery instructions
        gbc.gridy = 4;
        panel.add(new JLabel("Delivery Address & Landmark Details:"), gbc);

        JTextField addressField = new JTextField(defaultAddr != null ? defaultAddr.getFormattedAddress() : currentUser.getAddress());
        addressField.setPreferredSize(new Dimension(340, 32));
        gbc.gridy = 5;
        panel.add(addressField, gbc);

        addressCombo.addActionListener(e -> {
            Address selected = (Address) addressCombo.getSelectedItem();
            if (selected != null) {
                addressField.setText(selected.getFormattedAddress());
            }
        });

        manageAddressesBtn.addActionListener(e -> {
            AddressBookDialog bookDialog = new AddressBookDialog(this, currentUser, () -> {
                addressComboModel.removeAllElements();
                List<Address> refreshed = AddressBookStore.getAddresses(currentUser.getUserId());
                for (Address a : refreshed) addressComboModel.addElement(a);
                Address curDefault = AddressBookStore.getDefaultAddress(currentUser.getUserId());
                if (curDefault != null) {
                    addressCombo.setSelectedItem(curDefault);
                    addressField.setText(curDefault.getFormattedAddress());
                }
            });
            bookDialog.setVisible(true);
        });

        // Payment Method Options
        gbc.gridy = 6;
        panel.add(new JLabel("Select Payment Method:"), gbc);

        JComboBox<String> paymentCombo = new JComboBox<>(new String[]{
                "Cash on Delivery (COD)",
                "UPI (Instant Online Pay - Simulated)",
                "Credit / Debit Card (Online Gateway)",
                "Net Banking (Instant Settle)"
        });
        gbc.gridy = 7;
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
            int pIndex = paymentCombo.getSelectedIndex();
            String selectedPayment;
            switch (pIndex) {
                case 1: selectedPayment = "UPI"; break;
                case 2: selectedPayment = "CREDIT_CARD"; break;
                case 3: selectedPayment = "NET_BANKING"; break;
                default: selectedPayment = "COD"; break;
            }

            String newOrderCode = "ORD-2026-" + String.format("%03d", customerOrders.size() + 1);

            Order newOrder = new Order(customerOrders.size() + 1, newOrderCode, currentUser.getUserId(),
                    grandTotal, "PENDING", selectedPayment, address, LocalDate.now().toString());

            // Copy items to order
            for (OrderItem ci : shoppingCart) {
                newOrder.addItem(new OrderItem(ci.getOrderItemId(), newOrder.getOrderId(), ci.getProductId(), ci.getProductName(), ci.getQuantity(), ci.getUnitPrice()));
            }

            // Persist order & deduct inventory stock
            OrderStore.placeOrder(newOrder);
            customerOrders.add(0, newOrder); // add to top of local orders list

            // Audit journal logging
            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "CHECKOUT", "ORDER_PLACED",
                    "Order " + newOrderCode + " placed for $" + String.format("%.2f", grandTotal)
                            + " (" + selectedPayment + ") delivered to: " + address);

            // Process payment transaction via PaymentStore / PaymentDAO
            Payment payment = PaymentStore.processPayment(newOrder.getOrderId(), selectedPayment, grandTotal);

            // Refresh products catalog after stock adjustment
            catalogProducts.clear();
            catalogProducts.addAll(ProductStore.getAllProducts());

            CartStore.clearCart(currentUser.getUserId());
            shoppingCart.clear();
            updateCartBadge();
            renderCartContent();
            applyOrderFilters();
            refreshAdminOrdersTable();
            refreshCustomerCatalogTable(catalogProducts);
            refreshAdminReportsPanel();

            dialog.dispose();

            // Switch to Order History tab to view new order
            tabbedPane.setSelectedIndex(2);

            String promoMsg = (discount > 0 && appliedCoupon != null)
                    ? ("<br/>Promotional Discount Applied: <b>-$" + String.format("%.2f", discount) + "</b> (" + appliedCoupon.getCode() + ")")
                    : "";
            appliedCoupon = null;
            appliedDiscount = 0.0;

            String payBadge = "COMPLETED".equalsIgnoreCase(payment.getPaymentStatus())
                    ? "<font color='#16a34a'><b>COMPLETED</b></font>"
                    : "<font color='#d97706'><b>PENDING (COD)</b></font>";

            ValidationUtil.showSuccess(this,
                    "🎉 <b>Order Placed Successfully!</b><br/>"
                            + "Order Reference: <b>" + newOrderCode + "</b><br/>"
                            + "Total Amount: <b>$" + String.format("%.2f", grandTotal) + "</b> (" + selectedPayment + ")" + promoMsg + "<br/>"
                            + "Payment Status: " + payBadge + " (Ref: <code>" + payment.getTransactionRef() + "</code>)<br/>"
                            + "Inventory stock has been reserved. You can track status in <b>Order History</b>.",
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

        // Lifetime Spend / Value
        gbc.gridx = 0;
        gbc.gridy = 6;
        card.add(createFieldLabel("Lifetime Purchases:"), gbc);
        gbc.gridx = 1;
        double myTotalSpend = customerOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getOrderStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
        JLabel spendLabel = new JLabel(String.format("$%.2f (%d order%s placed)", myTotalSpend, customerOrders.size(), customerOrders.size() == 1 ? "" : "s"));
        spendLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spendLabel.setForeground(new Color(15, 23, 42));
        card.add(spendLabel, gbc);

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

        JButton addressBookBtn = new JButton("Manage Address Book");
        addressBookBtn.setBackground(new Color(16, 185, 129));
        addressBookBtn.setForeground(Color.WHITE);
        addressBookBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addressBookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addressBookBtn.addActionListener(e -> {
            AddressBookDialog dlg = new AddressBookDialog(this, currentUser, () -> {
                Address def = AddressBookStore.getDefaultAddress(currentUser.getUserId());
                if (def != null) {
                    profileAddressVal.setText("<html>" + def.getFormattedAddress() + "</html>");
                }
            });
            dlg.setVisible(true);
        });
        buttonRow.add(addressBookBtn);

        JButton passBtn = new JButton("Change Password");
        passBtn.setBackground(new Color(75, 85, 99));
        passBtn.setForeground(Color.WHITE);
        passBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        passBtn.addActionListener(e -> showChangePasswordDialog());
        buttonRow.add(passBtn);

        gbc.gridx = 0;
        gbc.gridy = 7;
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

            // Sync with UserStore / UserDAO
            UserStore.updateProfile(currentUser.getUserId(), newName, newPhone, newAddress);

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

            // Sync with UserStore / UserDAO
            boolean changed = UserStore.changePassword(currentUser.getUserId(), curr, next);
            if (!changed) {
                ValidationUtil.showError(dialog, "Current password is incorrect. Please try again.", "Authentication Error");
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
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "RETURN_REQUESTED", "REFUNDED", "CANCELLED"});
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

        JButton rateBtn = new JButton("★ Rate & Review");
        rateBtn.setBackground(new Color(245, 158, 11));
        rateBtn.setForeground(Color.WHITE);
        rateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rateBtn.addActionListener(e -> handleReviewDeliveredOrder());
        actionRow.add(rateBtn);

        JButton returnBtn = new JButton("🔄 Request Return / Refund");
        returnBtn.setBackground(new Color(217, 119, 6));
        returnBtn.setForeground(Color.WHITE);
        returnBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        returnBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnBtn.addActionListener(e -> handleRequestOrderReturn());
        actionRow.add(returnBtn);

        JButton trackShipmentBtn = new JButton("📍 Track Shipment");
        trackShipmentBtn.setBackground(new Color(37, 99, 235));
        trackShipmentBtn.setForeground(Color.WHITE);
        trackShipmentBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trackShipmentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trackShipmentBtn.addActionListener(e -> handleTrackShipment());
        actionRow.add(trackShipmentBtn);

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

    private void handleTrackShipment() {
        Order selected = getSelectedOrder();
        if (selected == null) return;

        String addr = selected.getShippingAddress() != null ? selected.getShippingAddress() : currentUser.getAddress();
        OrderTrackingDialog dialog = new OrderTrackingDialog(this, selected, addr);
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
            OrderStore.cancelOrder(selected);
            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "ORDER_MANAGEMENT", "ORDER_CANCELLED",
                    "Customer cancelled order " + selected.getOrderNumber() + "; inventory stock restored.");
            catalogProducts.clear();
            catalogProducts.addAll(ProductStore.getAllProducts());

            applyOrderFilters();
            refreshAdminOrdersTable();
            refreshCustomerCatalogTable(catalogProducts);
            refreshAdminCatalogTable();
            refreshAdminReportsPanel();

            ValidationUtil.showSuccess(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> has been successfully cancelled.<br/>"
                            + "Inventory stock has been replenished and payment marked as REFUNDED.",
                    "Order Cancelled");
        }
    }

    private void handleReviewDeliveredOrder() {
        Order selected = getSelectedOrder();
        if (selected == null) return;

        if (!"DELIVERED".equalsIgnoreCase(selected.getOrderStatus())) {
            ValidationUtil.showInfo(this,
                    "Verified customer reviews can only be submitted for delivered orders (Current status: " + selected.getOrderStatus() + ").",
                    "Order In Transit");
            return;
        }

        List<OrderItem> items = selected.getItems();
        if (items == null || items.isEmpty()) {
            ValidationUtil.showWarning(this, "No products found in this order to review.", "No Products");
            return;
        }

        OrderItem targetItem = items.get(0);
        if (items.size() > 1) {
            String[] itemNames = items.stream().map(OrderItem::getProductName).toArray(String[]::new);
            String picked = (String) JOptionPane.showInputDialog(
                    this,
                    "Select which purchased product you would like to review:",
                    "Choose Product to Review",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    itemNames,
                    itemNames[0]
            );
            if (picked == null) return;
            for (OrderItem oi : items) {
                if (oi.getProductName().equals(picked)) {
                    targetItem = oi;
                    break;
                }
            }
        }

        Product p = ProductStore.findById(targetItem.getProductId());
        if (p == null) {
            p = new Product(targetItem.getProductId(), targetItem.getProductName(), "Electronics", "Purchased item", targetItem.getUnitPrice(), 10);
        }

        ProductReviewDialog dialog = new ProductReviewDialog(this, p, currentUser, () -> {
            ValidationUtil.showSuccess(this, "Thank you! Your verified review has been published.", "Review Submitted");
        });
        dialog.setVisible(true);
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

        JButton editBtn = new JButton("Edit / Restock");
        editBtn.setBackground(new Color(37, 99, 235));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> handleAdminEditProduct());
        toolBar.add(editBtn);

        JButton viewBtn = new JButton("View Details");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewBtn.addActionListener(e -> handleAdminViewProductDetails());
        toolBar.add(viewBtn);

        JButton deleteBtn = new JButton("Delete Product");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteBtn.addActionListener(e -> handleAdminDeleteProduct());
        toolBar.add(deleteBtn);

        JButton manageCategoriesBtn = new JButton("Manage Categories");
        manageCategoriesBtn.setBackground(new Color(79, 70, 229));
        manageCategoriesBtn.setForeground(Color.WHITE);
        manageCategoriesBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        manageCategoriesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        manageCategoriesBtn.addActionListener(e -> {
            CategoryManagementDialog dialog = new CategoryManagementDialog(this);
            dialog.setVisible(true);
            reloadCategoryDropdowns();
            refreshAdminCatalogTable();
            refreshAdminReportsPanel();
        });
        toolBar.add(manageCategoriesBtn);

        JButton procurementBtn = new JButton("📦 Restock Hub");
        procurementBtn.setBackground(new Color(217, 119, 6));
        procurementBtn.setForeground(Color.WHITE);
        procurementBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        procurementBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        procurementBtn.addActionListener(e -> {
            LowStockProcurementDialog dialog = new LowStockProcurementDialog(this, () -> {
                refreshAdminCatalogTable();
                refreshAdminReportsPanel();
            });
            dialog.setVisible(true);
        });
        toolBar.add(procurementBtn);

        adminLowStockFilterCheck = new JCheckBox("Low Stock Alerts Only");
        adminLowStockFilterCheck.setBackground(Color.WHITE);
        adminLowStockFilterCheck.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminLowStockFilterCheck.setForeground(new Color(185, 28, 28));
        adminLowStockFilterCheck.addActionListener(e -> filterAdminCatalogByLowStock(adminLowStockFilterCheck.isSelected()));
        toolBar.add(adminLowStockFilterCheck);

        JButton exportCatalogCsvBtn = new JButton("📊 Export CSV");
        exportCatalogCsvBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportCatalogCsvBtn.setBackground(new Color(241, 245, 249));
        exportCatalogCsvBtn.setForeground(new Color(30, 41, 59));
        exportCatalogCsvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportCatalogCsvBtn.addActionListener(e -> handleAdminExportCatalogCsv());
        toolBar.add(exportCatalogCsvBtn);

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

        // Center renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        adminCatalogTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        adminCatalogTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Stock quantity custom renderer with low stock highlighting
        adminCatalogTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof Number) {
                    int qty = ((Number) value).intValue();
                    if (qty <= 0) {
                        lbl.setText("<html><b style='color: #dc2626;'>0 (OUT OF STOCK)</b></html>");
                    } else if (qty <= 5) {
                        lbl.setText("<html><b style='color: #d97706;'>" + qty + " (LOW)</b></html>");
                    } else {
                        lbl.setText(String.valueOf(qty));
                    }
                }
                return lbl;
            }
        });

        // Double click to edit/restock
        adminCatalogTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    handleAdminEditProduct();
                }
            }
        });

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

    private void filterAdminCatalogByLowStock(boolean lowStockOnly) {
        if (adminCatalogTableModel == null) return;
        adminCatalogTableModel.setRowCount(0);
        List<Product> list = lowStockOnly ? ProductStore.getLowStockProducts() : catalogProducts;
        for (Product p : list) {
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

    private void handleAdminEditProduct() {
        int row = adminCatalogTable.getSelectedRow();
        if (row < 0) {
            ValidationUtil.showWarning(this, "Please select a product from the table to edit or restock.", "Notice");
            return;
        }

        int productId = (int) adminCatalogTableModel.getValueAt(row, 0);
        Product selected = null;
        for (Product p : catalogProducts) {
            if (p.getProductId() == productId) {
                selected = p;
                break;
            }
        }
        if (selected == null) return;

        EditProductDialog dialog = new EditProductDialog(this, selected, updatedProduct -> {
            ProductStore.updateProduct(updatedProduct);
            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "INVENTORY", "PRODUCT_UPDATED",
                    "Updated product '" + updatedProduct.getProductName() + "' (ID #" + updatedProduct.getProductId()
                            + "): Stock=" + updatedProduct.getStockQuantity() + ", Price=$" + updatedProduct.getPrice());
            catalogProducts.clear();
            catalogProducts.addAll(ProductStore.getAllProducts());
            refreshAdminCatalogTable();
            if (customerCatalogTableModel != null) {
                applyCatalogFilters();
            }
            refreshAdminReportsPanel();
        });
        dialog.setVisible(true);
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
        JComboBox<String> categoryCombo = new JComboBox<>(CategoryStore.getCategoryNames().toArray(new String[0]));
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

            String category = (String) categoryCombo.getSelectedItem();
            Product newProduct = new Product(0, 1, category, name, desc, price, stock);

            Product added = ProductStore.addProduct(newProduct);
            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "INVENTORY", "PRODUCT_ADDED",
                    "Added new product '" + newProduct.getProductName() + "' (Category: " + newProduct.getCategory()
                            + ", Stock: " + newProduct.getStockQuantity() + ", Price: $" + newProduct.getPrice() + ")");
            catalogProducts.clear();
            catalogProducts.addAll(ProductStore.getAllProducts());

            refreshAdminCatalogTable();
            if (customerCatalogTableModel != null) {
                applyCatalogFilters();
            }
            refreshAdminReportsPanel();

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
            ProductStore.deleteProduct(productId);
            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "INVENTORY", "PRODUCT_DELETED",
                    "Deleted product '" + name + "' (ID #" + productId + ")");
            catalogProducts.clear();
            catalogProducts.addAll(ProductStore.getAllProducts());
            refreshAdminCatalogTable();
            if (customerCatalogTableModel != null) {
                applyCatalogFilters();
            }
            refreshAdminReportsPanel();
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
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBackground(Color.WHITE);

        toolBar.add(new JLabel("Status:"));
        adminOrderStatusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "RETURN_REQUESTED", "REFUNDED", "CANCELLED"});
        adminOrderStatusFilterCombo.addActionListener(e -> applyAdminOrderFilters());
        toolBar.add(adminOrderStatusFilterCombo);

        toolBar.add(new JLabel("Search:"));
        adminOrderSearchField = new JTextField(10);
        adminOrderSearchField.addActionListener(e -> applyAdminOrderFilters());
        toolBar.add(adminOrderSearchField);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterBtn.setBackground(new Color(37, 99, 235));
        filterBtn.setForeground(Color.WHITE);
        filterBtn.addActionListener(e -> applyAdminOrderFilters());
        toolBar.add(filterBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> {
            adminOrderStatusFilterCombo.setSelectedIndex(0);
            adminOrderSearchField.setText("");
            applyAdminOrderFilters();
        });
        toolBar.add(resetBtn);

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.setBackground(new Color(16, 185, 129));
        updateStatusBtn.setForeground(Color.WHITE);
        updateStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateStatusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateStatusBtn.addActionListener(e -> handleAdminUpdateOrderStatus());
        toolBar.add(updateStatusBtn);

        JButton approveReturnBtn = new JButton("⚡ Approve Return & Refund");
        approveReturnBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveReturnBtn.setBackground(new Color(217, 119, 6));
        approveReturnBtn.setForeground(Color.WHITE);
        approveReturnBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        approveReturnBtn.addActionListener(e -> handleApproveReturnFromAdmin());
        toolBar.add(approveReturnBtn);

        JButton viewOrderBtn = new JButton("View Invoice");
        viewOrderBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewOrderBtn.addActionListener(e -> handleAdminViewOrderDetails());
        toolBar.add(viewOrderBtn);

        JButton trackShipmentAdminBtn = new JButton("📍 Track Shipment");
        trackShipmentAdminBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trackShipmentAdminBtn.setBackground(new Color(37, 99, 235));
        trackShipmentAdminBtn.setForeground(Color.WHITE);
        trackShipmentAdminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trackShipmentAdminBtn.addActionListener(e -> handleAdminTrackShipment());
        toolBar.add(trackShipmentAdminBtn);

        JButton auditLogBtn = new JButton("📜 Audit Journal");
        auditLogBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        auditLogBtn.setBackground(new Color(15, 23, 42));
        auditLogBtn.setForeground(Color.WHITE);
        auditLogBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        auditLogBtn.addActionListener(e -> new AuditLogDialog(this).setVisible(true));
        toolBar.add(auditLogBtn);

        JButton exportOrdersCsvBtn = new JButton("📊 Export CSV");
        exportOrdersCsvBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportOrdersCsvBtn.setBackground(new Color(241, 245, 249));
        exportOrdersCsvBtn.setForeground(new Color(30, 41, 59));
        exportOrdersCsvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportOrdersCsvBtn.addActionListener(e -> handleAdminExportOrdersCsv());
        toolBar.add(exportOrdersCsvBtn);

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
        String keyword = adminOrderSearchField != null ? adminOrderSearchField.getText().trim().toLowerCase() : "";

        adminOrdersTableModel.setRowCount(0);
        for (Order o : customerOrders) {
            boolean matchesStatus = "ALL".equals(filter) || o.getOrderStatus().equalsIgnoreCase(filter);
            boolean matchesKeyword = keyword.isEmpty()
                    || o.getOrderNumber().toLowerCase().contains(keyword)
                    || String.valueOf(o.getUserId()).contains(keyword)
                    || (o.getShippingAddress() != null && o.getShippingAddress().toLowerCase().contains(keyword));

            if (matchesStatus && matchesKeyword) {
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

    private void handleAdminTrackShipment() {
        Order selected = getSelectedAdminOrder();
        if (selected == null) return;

        String addr = selected.getShippingAddress() != null ? selected.getShippingAddress() : "Customer Address on File";
        OrderTrackingDialog dialog = new OrderTrackingDialog(this, selected, addr);
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
            OrderStore.updateStatus(selected.getOrderId(), newStatus);
            selected.setOrderStatus(newStatus);

            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "ORDER_MANAGEMENT", "ORDER_STATUS_CHANGED",
                    "Updated order " + selected.getOrderNumber() + " status from " + current + " to " + newStatus);

            // Synchronize payment settlement
            if ("DELIVERED".equalsIgnoreCase(newStatus)) {
                PaymentStore.updatePaymentStatus(selected.getOrderId(), "COMPLETED");
            } else if ("CANCELLED".equalsIgnoreCase(newStatus)) {
                PaymentStore.updatePaymentStatus(selected.getOrderId(), "REFUNDED");
            }

            refreshAdminOrdersTable();
            if (ordersTableModel != null) {
                applyOrderFilters();
            }
            refreshAdminReportsPanel();
            ValidationUtil.showSuccess(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> status updated to <b>" + newStatus + "</b>.",
                    "Status Updated");
        }
    }

    private void handleAdminExportCatalogCsv() {
        File file = new File("qveto_inventory_catalog.csv");
        try {
            ProductStore.exportCatalogCsv(file);
            ValidationUtil.showSuccess(this,
                    "Inventory catalog exported to: <b>" + file.getAbsolutePath() + "</b>",
                    "Catalog Exported");
        } catch (Exception ex) {
            ValidationUtil.showError(this, "Failed to export catalog CSV: " + ex.getMessage(), "Export Error");
        }
    }

    private void handleAdminExportOrdersCsv() {
        File file = new File("qveto_orders_master.csv");
        try {
            OrderStore.exportOrdersCsv(file);
            ValidationUtil.showSuccess(this,
                    "Platform orders master file exported to: <b>" + file.getAbsolutePath() + "</b>",
                    "Orders Exported");
        } catch (Exception ex) {
            ValidationUtil.showError(this, "Failed to export orders CSV: " + ex.getMessage(), "Export Error");
        }
    }

    // ==========================================
    // ADMIN USER MANAGEMENT
    // ==========================================

    private JPanel createAdminUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // North Container: KPI Summary Cards + Filter Toolbar
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);

        // Metric Cards Row
        adminUsersKpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        adminUsersKpiPanel.setOpaque(false);
        adminUsersKpiPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        updateAdminUserKpis();
        northContainer.add(adminUsersKpiPanel);

        // Filter / Search Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBackground(Color.WHITE);

        toolBar.add(new JLabel("Search:"));
        adminUserSearchField = new JTextField(15);
        adminUserSearchField.addActionListener(e -> applyAdminUserFilters());
        toolBar.add(adminUserSearchField);

        toolBar.add(new JLabel("Role:"));
        adminUserRoleFilterCombo = new JComboBox<>(new String[]{"ALL", "CUSTOMER", "ADMIN"});
        adminUserRoleFilterCombo.addActionListener(e -> applyAdminUserFilters());
        toolBar.add(adminUserRoleFilterCombo);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setBackground(new Color(37, 99, 235));
        filterBtn.setForeground(Color.WHITE);
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBtn.addActionListener(e -> applyAdminUserFilters());
        toolBar.add(filterBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> {
            adminUserSearchField.setText("");
            adminUserRoleFilterCombo.setSelectedIndex(0);
            applyAdminUserFilters();
        });
        toolBar.add(resetBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> refreshAdminUsersTable());
        toolBar.add(refreshBtn);

        JButton viewUserBtn = new JButton("View User Dossier");
        viewUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewUserBtn.setBackground(new Color(15, 23, 42));
        viewUserBtn.setForeground(Color.WHITE);
        viewUserBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewUserBtn.addActionListener(e -> handleAdminViewUserDetails());
        toolBar.add(viewUserBtn);

        JButton deleteUserBtn = new JButton("Deactivate User");
        deleteUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteUserBtn.setBackground(new Color(220, 38, 38));
        deleteUserBtn.setForeground(Color.WHITE);
        deleteUserBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteUserBtn.addActionListener(e -> handleAdminDeleteUser());
        toolBar.add(deleteUserBtn);

        northContainer.add(toolBar);
        panel.add(northContainer, BorderLayout.NORTH);

        // Table Model & JTable
        String[] columns = {"User ID", "Full Name", "Email Address", "Role", "Phone Number", "Shipping Address", "Registration Date"};
        adminUsersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        adminUsersTable = new JTable(adminUsersTableModel);
        adminUsersTable.setRowHeight(30);
        adminUsersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminUsersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center renderers for ID and Date
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        adminUsersTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        adminUsersTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Custom Role Badge Renderer
        adminUsersTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String role = value != null ? value.toString() : "";
                if ("ADMIN".equalsIgnoreCase(role)) {
                    lbl.setText("<html><b style='color: #dc2626;'>[ADMIN]</b></html>");
                } else {
                    lbl.setText("<html><b style='color: #2563eb;'>CUSTOMER</b></html>");
                }
                return lbl;
            }
        });

        // Double click to view dossier
        adminUsersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    handleAdminViewUserDetails();
                }
            }
        });

        panel.add(new JScrollPane(adminUsersTable), BorderLayout.CENTER);
        refreshAdminUsersTable();

        return panel;
    }

    private void updateAdminUserKpis() {
        if (adminUsersKpiPanel == null) return;
        adminUsersKpiPanel.removeAll();

        List<User> allUsers = UserStore.getAllUsers();
        long totalUsers = allUsers.size();
        long customerCount = allUsers.stream().filter(u -> !u.isAdmin()).count();
        long adminCount = allUsers.stream().filter(User::isAdmin).count();

        adminUsersKpiPanel.add(createKpiCard("Total Registered Users", String.valueOf(totalUsers), new Color(37, 99, 235)));
        adminUsersKpiPanel.add(createKpiCard("Active Customers", String.valueOf(customerCount), new Color(16, 185, 129)));
        adminUsersKpiPanel.add(createKpiCard("Administrators", String.valueOf(adminCount), new Color(220, 38, 38)));
        adminUsersKpiPanel.add(createKpiCard("Database Mode", DBStatusUtil.getStatusText(), new Color(79, 70, 229)));

        adminUsersKpiPanel.revalidate();
        adminUsersKpiPanel.repaint();
    }

    private void refreshAdminUsersTable() {
        if (adminUsersTableModel == null) return;
        applyAdminUserFilters();
        updateAdminUserKpis();
    }

    private void applyAdminUserFilters() {
        if (adminUsersTableModel == null) return;
        String search = adminUserSearchField != null ? adminUserSearchField.getText().trim().toLowerCase() : "";
        String roleFilter = adminUserRoleFilterCombo != null ? (String) adminUserRoleFilterCombo.getSelectedItem() : "ALL";

        List<User> allUsers = UserStore.getAllUsers();
        adminUsersTableModel.setRowCount(0);

        for (User u : allUsers) {
            boolean matchesSearch = search.isEmpty()
                    || u.getFullName().toLowerCase().contains(search)
                    || u.getEmail().toLowerCase().contains(search)
                    || String.valueOf(u.getUserId()).contains(search);

            boolean matchesRole = "ALL".equalsIgnoreCase(roleFilter)
                    || u.getRole().equalsIgnoreCase(roleFilter);

            if (matchesSearch && matchesRole) {
                adminUsersTableModel.addRow(new Object[]{
                        "#" + u.getUserId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole(),
                        u.getPhone().isEmpty() ? "N/A" : u.getPhone(),
                        u.getAddress().isEmpty() ? "N/A" : u.getAddress(),
                        u.getCreatedAt().isEmpty() ? "Recent" : u.getCreatedAt()
                });
            }
        }
    }

    private void handleAdminViewUserDetails() {
        int selectedRow = adminUsersTable.getSelectedRow();
        if (selectedRow == -1) {
            ValidationUtil.showWarning(this, "Please select a user from the table to view their dossier.", "No User Selected");
            return;
        }

        String idStr = (String) adminUsersTableModel.getValueAt(selectedRow, 0);
        int userId = Integer.parseInt(idStr.replace("#", ""));

        List<User> allUsers = UserStore.getAllUsers();
        User selectedUser = null;
        for (User u : allUsers) {
            if (u.getUserId() == userId) {
                selectedUser = u;
                break;
            }
        }

        if (selectedUser == null) {
            ValidationUtil.showError(this, "User could not be found.", "Error");
            return;
        }

        final int targetId = userId;
        int orderCount = (int) customerOrders.stream().filter(o -> o.getUserId() == targetId).count();
        double lifetimeSpend = customerOrders.stream()
                .filter(o -> o.getUserId() == targetId && !"CANCELLED".equalsIgnoreCase(o.getOrderStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        UserDetailsDialog dialog = new UserDetailsDialog(this, selectedUser, orderCount, lifetimeSpend);
        dialog.setVisible(true);
    }

    private void handleAdminDeleteUser() {
        int selectedRow = adminUsersTable.getSelectedRow();
        if (selectedRow == -1) {
            ValidationUtil.showWarning(this, "Please select a user to deactivate.", "No User Selected");
            return;
        }

        String idStr = (String) adminUsersTableModel.getValueAt(selectedRow, 0);
        int targetUserId = Integer.parseInt(idStr.replace("#", ""));
        String targetUserName = (String) adminUsersTableModel.getValueAt(selectedRow, 1);
        String targetUserRole = (String) adminUsersTableModel.getValueAt(selectedRow, 3);

        // Security safeguard: Admin cannot delete their own active account
        if (targetUserId == currentUser.getUserId()) {
            ValidationUtil.showError(this, "You cannot deactivate or delete your own active administrator account.", "Action Prohibited");
            return;
        }

        boolean confirm = ValidationUtil.showConfirm(this,
                "Are you sure you want to deactivate and remove account: <b>" + targetUserName + "</b> (#" + targetUserId + " - " + targetUserRole + ")?<br/><br/>This operation will remove the account from the active registry.",
                "Confirm Account Deactivation");

        if (confirm) {
            boolean success = UserStore.deleteUser(targetUserId);
            if (success) {
                AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "USER_MANAGEMENT", "USER_DEACTIVATED",
                        "Deactivated account #" + targetUserId + " (" + targetUserName + " - " + targetUserRole + ")");
                ValidationUtil.showSuccess(this, "Account <b>" + targetUserName + "</b> was successfully deactivated.", "Account Removed");
                refreshAdminUsersTable();
            } else {
                ValidationUtil.showError(this, "Failed to deactivate account.", "Operation Failed");
            }
        }
    }

    private JPanel createAdminReportsPanel() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Header & Controls Panel
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setBackground(Color.WHITE);

        // Header Title & Action Buttons
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Executive Performance, Revenue & Customer Lifetime Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel("Real-time sales velocity, settled collections, customer lifetime spend (CLV), and payment audit");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);
        headerRow.add(titleBlock, BorderLayout.WEST);

        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnRow.setBackground(Color.WHITE);

        JButton refreshBtn = new JButton("↻ Refresh Analytics");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setForeground(new Color(30, 41, 59));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            refreshAdminReportsPanel();
            ValidationUtil.showSuccess(this, "Executive analytics & payment settlement metrics synchronized.", "Analytics Refreshed");
        });

        JButton exportBtn = new JButton("Export Executive Report (.txt)");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setBackground(new Color(16, 185, 129));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> handleExportExecutiveReport());

        JButton restockHubBtn = new JButton("📦 Restock Procurement");
        restockHubBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        restockHubBtn.setBackground(new Color(217, 119, 6));
        restockHubBtn.setForeground(Color.WHITE);
        restockHubBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restockHubBtn.addActionListener(e -> {
            LowStockProcurementDialog dialog = new LowStockProcurementDialog(this, () -> {
                refreshAdminCatalogTable();
                refreshAdminReportsPanel();
            });
            dialog.setVisible(true);
        });

        actionBtnRow.add(refreshBtn);
        actionBtnRow.add(restockHubBtn);
        JButton exportCsvBtn = new JButton("Export Sales Audit (CSV)");
        exportCsvBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportCsvBtn.setBackground(new Color(37, 99, 235));
        exportCsvBtn.setForeground(Color.WHITE);
        exportCsvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportCsvBtn.addActionListener(e -> handleExportSalesAuditCSV());
        actionBtnRow.add(exportCsvBtn);
        actionBtnRow.add(exportBtn);

        JButton auditLogReportBtn = new JButton("📜 Audit Journal");
        auditLogReportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        auditLogReportBtn.setBackground(new Color(15, 23, 42));
        auditLogReportBtn.setForeground(Color.WHITE);
        auditLogReportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        auditLogReportBtn.addActionListener(e -> new AuditLogDialog(this).setVisible(true));
        actionBtnRow.add(auditLogReportBtn);
        headerRow.add(actionBtnRow, BorderLayout.EAST);
        northPanel.add(headerRow, BorderLayout.NORTH);

        // Date Filter Toolbar
        JPanel filterToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterToolbar.setBackground(new Color(248, 250, 252));
        filterToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel filterLabel = new JLabel("📅 Date Range Preset:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLabel.setForeground(new Color(30, 41, 59));
        filterToolbar.add(filterLabel);

        JComboBox<String> datePresetCombo = new JComboBox<>(new String[]{"All Time", "Last 7 Days", "Last 30 Days", "This Month"});
        datePresetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterToolbar.add(datePresetCombo);

        filterToolbar.add(new JLabel("From (YYYY-MM-DD):"));
        JTextField fromDateField = new JTextField(9);
        fromDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterToolbar.add(fromDateField);

        filterToolbar.add(new JLabel("To (YYYY-MM-DD):"));
        JTextField toDateField = new JTextField(9);
        toDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterToolbar.add(toDateField);

        datePresetCombo.addActionListener(e -> {
            String sel = (String) datePresetCombo.getSelectedItem();
            LocalDate today = LocalDate.now();
            if ("All Time".equals(sel)) {
                fromDateField.setText("");
                toDateField.setText("");
            } else if ("Last 7 Days".equals(sel)) {
                fromDateField.setText(today.minusDays(7).toString());
                toDateField.setText(today.toString());
            } else if ("Last 30 Days".equals(sel)) {
                fromDateField.setText(today.minusDays(30).toString());
                toDateField.setText(today.toString());
            } else if ("This Month".equals(sel)) {
                fromDateField.setText(today.withDayOfMonth(1).toString());
                toDateField.setText(today.toString());
            }
        });

        JButton applyFilterBtn = new JButton("Filter Analytics");
        applyFilterBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        applyFilterBtn.setBackground(new Color(37, 99, 235));
        applyFilterBtn.setForeground(Color.WHITE);
        applyFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyFilterBtn.addActionListener(e -> {
            String from = fromDateField.getText().trim();
            String to = toDateField.getText().trim();
            refreshAdminReportsPanel(from, to);
        });
        filterToolbar.add(applyFilterBtn);

        JButton resetFilterBtn = new JButton("Reset");
        resetFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resetFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetFilterBtn.addActionListener(e -> {
            datePresetCombo.setSelectedIndex(0);
            fromDateField.setText("");
            toDateField.setText("");
            refreshAdminReportsPanel();
        });
        filterToolbar.add(resetFilterBtn);

        // KPI Deck (6 cards: 2 rows x 3 columns)
        JPanel kpiGrid = new JPanel(new GridLayout(2, 3, 12, 8));
        kpiGrid.setBackground(Color.WHITE);
        kpiGrid.setBorder(new EmptyBorder(8, 0, 4, 0));

        kpiTotalRevenueVal = new JLabel("$0.00");
        kpiCollectedRevenueVal = new JLabel("$0.00");
        kpiTotalOrdersVal = new JLabel("0");
        kpiAvgOrderVal = new JLabel("$0.00");
        kpiTotalCustomersVal = new JLabel("0");
        kpiLowStockVal = new JLabel("0 SKUs");

        kpiGrid.add(createKpiCard("Gross Sales Revenue", kpiTotalRevenueVal, new Color(16, 185, 129)));
        kpiGrid.add(createKpiCard("Settled Collections", kpiCollectedRevenueVal, new Color(5, 150, 105)));
        kpiGrid.add(createKpiCard("Total Orders", kpiTotalOrdersVal, new Color(37, 99, 235)));
        kpiGrid.add(createKpiCard("Avg Order Value (AOV)", kpiAvgOrderVal, new Color(124, 58, 237)));
        kpiGrid.add(createKpiCard("Active Customers", kpiTotalCustomersVal, new Color(139, 92, 246)));
        kpiGrid.add(createKpiCard("Low Stock Exposure", kpiLowStockVal, new Color(239, 68, 68)));

        JPanel controlsStack = new JPanel(new BorderLayout(0, 8));
        controlsStack.setBackground(Color.WHITE);
        controlsStack.add(filterToolbar, BorderLayout.NORTH);
        controlsStack.add(kpiGrid, BorderLayout.CENTER);

        northPanel.add(controlsStack, BorderLayout.CENTER);
        root.add(northPanel, BorderLayout.NORTH);

        // Center Split Panel (2 Columns)
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        centerGrid.setBackground(Color.WHITE);

        // --- Left Card: Top Selling Products Leaderboard ---
        JPanel leftCard = new JPanel(new BorderLayout(8, 8));
        leftCard.setBackground(Color.WHITE);
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel leftTitle = new JLabel("Top-Selling Products (Velocity Leaderboard)");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftTitle.setForeground(new Color(30, 41, 59));
        leftCard.add(leftTitle, BorderLayout.NORTH);

        String[] topSellingCols = {"Rank", "Product Name", "Category", "Units Sold", "Revenue ($)"};
        topSellingTableModel = new DefaultTableModel(topSellingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable topSellingTable = new JTable(topSellingTableModel);
        topSellingTable.setRowHeight(26);
        topSellingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topSellingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        DefaultTableCellRenderer centerCellRenderer = new DefaultTableCellRenderer();
        centerCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        topSellingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        topSellingTable.getColumnModel().getColumn(0).setCellRenderer(centerCellRenderer);
        topSellingTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        topSellingTable.getColumnModel().getColumn(3).setCellRenderer(centerCellRenderer);

        DefaultTableCellRenderer rightCellRenderer = new DefaultTableCellRenderer();
        rightCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        topSellingTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        topSellingTable.getColumnModel().getColumn(4).setCellRenderer(rightCellRenderer);
        topSellingTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        topSellingTable.getColumnModel().getColumn(2).setPreferredWidth(90);

        leftCard.add(new JScrollPane(topSellingTable), BorderLayout.CENTER);
        centerGrid.add(leftCard);

        // --- Right Card: Multi-Tab Analytics Breakdown ---
        JTabbedPane rightTabbedPane = new JTabbedPane();
        rightTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // Tab 1: Order Lifecycle Distribution
        JPanel orderDistCard = new JPanel(new BorderLayout(6, 6));
        orderDistCard.setBackground(Color.WHITE);
        orderDistCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] orderDistCols = {"Status", "Orders Count", "% Share"};
        orderStatusDistTableModel = new DefaultTableModel(orderDistCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable orderDistTable = new JTable(orderStatusDistTableModel);
        orderDistTable.setRowHeight(24);
        orderDistTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderDistTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        orderDistTable.getColumnModel().getColumn(1).setCellRenderer(centerCellRenderer);
        orderDistTable.getColumnModel().getColumn(2).setCellRenderer(centerCellRenderer);
        orderDistCard.add(new JScrollPane(orderDistTable), BorderLayout.CENTER);
        rightTabbedPane.addTab("Order Lifecycle", orderDistCard);

        // Tab 2: Category Revenue Breakdown
        JPanel catRevCard = new JPanel(new BorderLayout(6, 6));
        catRevCard.setBackground(Color.WHITE);
        catRevCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] catRevCols = {"Category", "Revenue ($)", "% Share"};
        categoryRevenueTableModel = new DefaultTableModel(catRevCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable catRevTable = new JTable(categoryRevenueTableModel);
        catRevTable.setRowHeight(24);
        catRevTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        catRevTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        catRevTable.getColumnModel().getColumn(1).setCellRenderer(rightCellRenderer);
        catRevTable.getColumnModel().getColumn(2).setCellRenderer(centerCellRenderer);
        catRevCard.add(new JScrollPane(catRevTable), BorderLayout.CENTER);
        rightTabbedPane.addTab("Category Revenue", catRevCard);

        // Tab 3: Customer Lifetime Value (CLV)
        JPanel clvCard = new JPanel(new BorderLayout(6, 6));
        clvCard.setBackground(Color.WHITE);
        clvCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] clvCols = {"Rank", "Customer", "Orders", "Lifetime ($)"};
        customerSpendTableModel = new DefaultTableModel(clvCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable clvTable = new JTable(customerSpendTableModel);
        clvTable.setRowHeight(24);
        clvTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clvTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        clvTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        clvTable.getColumnModel().getColumn(0).setCellRenderer(centerCellRenderer);
        clvTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        clvTable.getColumnModel().getColumn(2).setCellRenderer(centerCellRenderer);
        clvTable.getColumnModel().getColumn(3).setCellRenderer(rightCellRenderer);
        clvCard.add(new JScrollPane(clvTable), BorderLayout.CENTER);
        rightTabbedPane.addTab("Customer Lifetime Value", clvCard);

        // Tab 4: Payment Channels
        JPanel payDistCard = new JPanel(new BorderLayout(6, 6));
        payDistCard.setBackground(Color.WHITE);
        payDistCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] payCols = {"Payment Channel", "Transactions Count"};
        paymentDistTableModel = new DefaultTableModel(payCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable payDistTable = new JTable(paymentDistTableModel);
        payDistTable.setRowHeight(24);
        payDistTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        payDistTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        payDistTable.getColumnModel().getColumn(1).setCellRenderer(centerCellRenderer);
        payDistCard.add(new JScrollPane(payDistTable), BorderLayout.CENTER);
        rightTabbedPane.addTab("Payment Channels", payDistCard);

        centerGrid.add(rightTabbedPane);
        root.add(centerGrid, BorderLayout.CENTER);

        // Initial population of metrics
        refreshAdminReportsPanel();

        return root;
    }

    public void refreshAdminReportsPanel() {
        refreshAdminReportsPanel(null, null);
    }

    public void refreshAdminReportsPanel(String fromDate, String toDate) {
        if (kpiTotalOrdersVal == null) return;

        boolean hasDateFilter = (fromDate != null && !fromDate.trim().isEmpty())
                || (toDate != null && !toDate.trim().isEmpty());

        double revenue = hasDateFilter ? ReportStore.getTotalRevenueBetween(fromDate, toDate) : ReportStore.getTotalRevenue();
        int orders = hasDateFilter ? ReportStore.getTotalOrdersBetween(fromDate, toDate) : ReportStore.getTotalOrders();
        double aov = hasDateFilter ? ReportStore.getAverageOrderValueBetween(fromDate, toDate) : ReportStore.getAverageOrderValue();
        double collected = hasDateFilter ?
                ReportStore.getOrdersBetween(fromDate, toDate).stream()
                        .filter(o -> "DELIVERED".equalsIgnoreCase(o.getOrderStatus()) || "SHIPPED".equalsIgnoreCase(o.getOrderStatus()))
                        .mapToDouble(Order::getTotalAmount).sum()
                : ReportStore.getTotalCollectedRevenue();
        int customers = ReportStore.getTotalCustomers();
        int lowStock = ReportStore.getLowStockExposureCount();

        kpiTotalRevenueVal.setText(String.format("$%.2f", revenue));
        if (kpiCollectedRevenueVal != null) {
            kpiCollectedRevenueVal.setText(String.format("$%.2f", collected));
        }
        kpiTotalOrdersVal.setText(String.valueOf(orders));
        if (kpiAvgOrderVal != null) {
            kpiAvgOrderVal.setText(String.format("$%.2f", aov));
        }
        kpiTotalCustomersVal.setText(String.valueOf(customers));
        kpiLowStockVal.setText(lowStock + " SKUs");

        // Top Selling Leaderboard
        if (topSellingTableModel != null) {
            topSellingTableModel.setRowCount(0);
            List<ProductSalesStat> topList = ReportStore.getTopSellingProducts(8);
            int rank = 1;
            for (ProductSalesStat stat : topList) {
                topSellingTableModel.addRow(new Object[]{
                        "#" + rank++,
                        stat.getProductName(),
                        stat.getCategoryName(),
                        stat.getTotalUnitsSold(),
                        String.format("$%.2f", stat.getTotalRevenueAsDouble())
                });
            }
        }

        // Status Distribution
        if (orderStatusDistTableModel != null) {
            orderStatusDistTableModel.setRowCount(0);
            Map<String, Integer> statusDist = hasDateFilter ?
                    ReportStore.getOrderStatusDistributionBetween(fromDate, toDate) :
                    ReportStore.getOrderStatusDistribution();
            for (Map.Entry<String, Integer> e : statusDist.entrySet()) {
                double pct = orders > 0 ? (e.getValue() * 100.0 / orders) : 0.0;
                orderStatusDistTableModel.addRow(new Object[]{
                        e.getKey(),
                        e.getValue(),
                        String.format("%.1f%%", pct)
                });
            }
        }

        // Category Revenue Breakdown
        if (categoryRevenueTableModel != null) {
            categoryRevenueTableModel.setRowCount(0);
            Map<String, Double> catRev = ReportStore.getRevenueByCategory();
            for (Map.Entry<String, Double> e : catRev.entrySet()) {
                double pct = revenue > 0 ? (e.getValue() * 100.0 / revenue) : 0.0;
                categoryRevenueTableModel.addRow(new Object[]{
                        e.getKey(),
                        String.format("$%.2f", e.getValue()),
                        String.format("%.1f%%", pct)
                });
            }
        }

        // Customer Lifetime Value Leaderboard
        if (customerSpendTableModel != null) {
            customerSpendTableModel.setRowCount(0);
            List<CustomerSpendStat> clvList = ReportStore.getCustomerSpendLeaderboard(6);
            int cRank = 1;
            for (CustomerSpendStat cs : clvList) {
                customerSpendTableModel.addRow(new Object[]{
                        "#" + cRank++,
                        cs.getFullName(),
                        cs.getLifetimeOrders() + " orders",
                        String.format("$%.2f", cs.getLifetimeSpent().doubleValue())
                });
            }
        }

        // Payment Method Distribution
        if (paymentDistTableModel != null) {
            paymentDistTableModel.setRowCount(0);
            Map<String, Integer> payDist = ReportStore.getPaymentMethodDistribution();
            for (Map.Entry<String, Integer> e : payDist.entrySet()) {
                paymentDistTableModel.addRow(new Object[]{
                        e.getKey(),
                        e.getValue() + " txns"
                });
            }
        }
    }

    private void handleExportSalesAuditCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Sales Orders Audit (CSV)");
        fileChooser.setSelectedFile(new File("qveto_sales_orders_audit.csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                int rows = com.qveto.ecommerce.util.ReportExportUtil.exportSalesOrdersCSV(fileToSave);
                ValidationUtil.showSuccess(this,
                        "Sales Orders Audit CSV exported successfully (" + rows + " transactions):<br/><b>" + fileToSave.getAbsolutePath() + "</b>",
                        "CSV Audit Exported");
            } catch (Exception ex) {
                ValidationUtil.showError(this, "Failed to export CSV: " + ex.getMessage(), "Export Error");
            }
        }
    }

    private void handleExportExecutiveReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Executive Performance & Sales Report");
        fileChooser.setSelectedFile(new File("qveto_executive_report.txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ReportStore.exportExecutiveReport(fileToSave);
                ValidationUtil.showSuccess(this,
                        "Executive Report exported successfully to:<br/><b>" + fileToSave.getAbsolutePath() + "</b>",
                        "Report Exported");
            } catch (Exception ex) {
                ValidationUtil.showError(this, "Failed to export report: " + ex.getMessage(), "Export Error");
            }
        }
    }

    private void reloadCategoryDropdowns() {
        if (customerCategoryCombo != null) {
            String selected = (String) customerCategoryCombo.getSelectedItem();
            customerCategoryCombo.removeAllItems();
            customerCategoryCombo.addItem("All Categories");
            for (String c : CategoryStore.getCategoryNames()) {
                customerCategoryCombo.addItem(c);
            }
            if (selected != null) {
                customerCategoryCombo.setSelectedItem(selected);
            }
        }
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createKpiCard(String title, String value, Color accentColor) {
        JLabel val = new JLabel(value);
        return createKpiCard(title, val, accentColor);
    }

    // ==========================================
    // NOTIFICATION CENTER BADGE & CONTROLS
    // ==========================================

    public void updateNotificationBadge() {
        if (notificationBtn == null) return;
        int count = NotificationStore.getUnreadCount(currentUser.getUserId(), currentUser.getRole());
        if (count > 0) {
            notificationBtn.setText("🔔 Alerts (" + count + ")");
            notificationBtn.setBackground(new Color(220, 38, 38));
        } else {
            notificationBtn.setText("🔔 Alerts");
            notificationBtn.setBackground(new Color(30, 58, 102));
        }
    }

    // ==========================================
    // CUSTOMER WISHLIST SCREEN & CONTROLS
    // ==========================================

    private JPanel createCustomerWishlistPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Wishlist Top Banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(248, 250, 252));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("My Saved Items & Wishlist");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));

        JLabel sub = new JLabel("Save products you love and quickly transfer them to your cart when ready to purchase.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));

        JPanel textStack = new JPanel(new GridLayout(2, 1, 0, 2));
        textStack.setOpaque(false);
        textStack.add(title);
        textStack.add(sub);
        banner.add(textStack, BorderLayout.WEST);

        panel.add(banner, BorderLayout.NORTH);

        // Wishlist Table
        String[] columns = {"ID", "Product Name", "Category", "Unit Price ($)", "Stock Status", "Availability"};
        wishlistTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        wishlistTable = new JTable(wishlistTableModel);
        wishlistTable.setRowHeight(28);
        wishlistTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        wishlistTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        wishlistTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        wishlistTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        wishlistTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        wishlistTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        refreshWishlistTable();

        JScrollPane scrollPane = new JScrollPane(wishlistTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action Controls
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        actionRow.setBackground(Color.WHITE);

        JButton clearBtn = new JButton("Clear All");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> {
            if (WishlistStore.getWishlistCount(currentUser.getUserId()) == 0) return;
            if (ValidationUtil.showConfirm(this, "Are you sure you want to clear your wishlist?", "Clear Wishlist")) {
                WishlistStore.clearWishlist(currentUser.getUserId());
                refreshWishlistTable();
                updateCartBadge();
            }
        });
        actionRow.add(clearBtn);

        JButton removeBtn = new JButton("🗑 Remove");
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeBtn.addActionListener(e -> handleRemoveFromWishlist());
        actionRow.add(removeBtn);

        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewDetailsBtn.setBackground(new Color(15, 23, 42));
        viewDetailsBtn.setForeground(Color.WHITE);
        viewDetailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDetailsBtn.addActionListener(e -> handleViewWishlistProductDetails());
        actionRow.add(viewDetailsBtn);

        JButton moveToCartBtn = new JButton("🛒 Move to Cart");
        moveToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        moveToCartBtn.setBackground(new Color(16, 185, 129));
        moveToCartBtn.setForeground(Color.WHITE);
        moveToCartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        moveToCartBtn.addActionListener(e -> handleMoveWishlistToCart());
        actionRow.add(moveToCartBtn);

        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshWishlistTable() {
        if (wishlistTableModel == null) return;
        wishlistTableModel.setRowCount(0);
        List<Product> wishlist = WishlistStore.getWishlist(currentUser.getUserId());
        for (Product p : wishlist) {
            wishlistTableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getProductName(),
                    p.getCategory(),
                    String.format("%.2f", p.getPriceAsDouble()),
                    p.getStockQuantity() + " units",
                    p.getStockStatus()
            });
        }
    }

    private Product getSelectedWishlistProduct() {
        if (wishlistTable == null) return null;
        int row = wishlistTable.getSelectedRow();
        if (row < 0) {
            ValidationUtil.showWarning(this, "Please select an item from your wishlist table.", "No Item Selected");
            return null;
        }
        int productId = (int) wishlistTableModel.getValueAt(row, 0);
        for (Product p : WishlistStore.getWishlist(currentUser.getUserId())) {
            if (p.getProductId() == productId) {
                return p;
            }
        }
        return null;
    }

    private void handleRemoveFromWishlist() {
        Product p = getSelectedWishlistProduct();
        if (p == null) return;

        WishlistStore.removeFromWishlist(currentUser.getUserId(), p.getProductId());
        refreshWishlistTable();
        updateCartBadge();
        ValidationUtil.showSuccess(this, "<b>" + p.getProductName() + "</b> removed from your wishlist.", "Item Removed");
    }

    private void handleViewWishlistProductDetails() {
        Product p = getSelectedWishlistProduct();
        if (p == null) return;

        ProductDetailsDialog dialog = new ProductDetailsDialog(this, p, currentUser, prod -> {
            addProductToCart(prod.getProductId(), prod.getProductName(), prod.getPriceAsDouble());
        });
        dialog.setVisible(true);
        refreshWishlistTable();
        updateCartBadge();
    }

    private void handleMoveWishlistToCart() {
        Product p = getSelectedWishlistProduct();
        if (p == null) return;

        if (p.isOutOfStock()) {
            ValidationUtil.showWarning(this, "This product is currently out of stock and cannot be added to cart.", "Out of Stock");
            return;
        }

        addProductToCart(p.getProductId(), p.getProductName(), p.getPriceAsDouble());
        WishlistStore.removeFromWishlist(currentUser.getUserId(), p.getProductId());
        refreshWishlistTable();
        updateCartBadge();
    }

    private void handleAddSelectedToWishlist() {
        Product selected = getSelectedCustomerProduct();
        if (selected == null) return;

        boolean added = WishlistStore.addToWishlist(currentUser.getUserId(), selected);
        if (added) {
            updateCartBadge();
            refreshWishlistTable();
            ValidationUtil.showSuccess(this,
                    "<b>" + selected.getProductName() + "</b> has been saved to your wishlist!",
                    "Saved to Wishlist");
        } else {
            ValidationUtil.showInfo(this,
                    "<b>" + selected.getProductName() + "</b> is already in your wishlist.",
                    "Already in Wishlist");
        }
    }

    // ==========================================
    // ORDER RETURN / REFUND CONTROLS
    // ==========================================

    private void handleRequestOrderReturn() {
        Order selected = getSelectedOrder();
        if (selected == null) return;

        if ("RETURN_REQUESTED".equalsIgnoreCase(selected.getOrderStatus())) {
            ValidationUtil.showInfo(this,
                    "A return request has already been logged for order <b>" + selected.getOrderNumber() + "</b> and is under review.",
                    "Return Pending");
            return;
        }

        if ("REFUNDED".equalsIgnoreCase(selected.getOrderStatus())) {
            ValidationUtil.showInfo(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> has already been refunded.",
                    "Already Refunded");
            return;
        }

        if (!selected.isReturnable()) {
            ValidationUtil.showWarning(this,
                    "Returns can only be requested for delivered orders (Current status: " + selected.getOrderStatus() + ").",
                    "Not Eligible for Return");
            return;
        }

        OrderReturnDialog dialog = new OrderReturnDialog(this, selected, () -> {
            applyOrderFilters();
            refreshAdminOrdersTable();
            refreshAdminReportsPanel();
            updateNotificationBadge();
        });
        dialog.setVisible(true);
    }

    private void handleApproveReturnFromAdmin() {
        Order selected = getSelectedAdminOrder();
        if (selected == null) return;

        if (!"RETURN_REQUESTED".equalsIgnoreCase(selected.getOrderStatus())) {
            ValidationUtil.showInfo(this,
                    "Order <b>" + selected.getOrderNumber() + "</b> is in status: <b>" + selected.getOrderStatus()
                            + "</b>.<br/>(Only orders with status <b>RETURN_REQUESTED</b> can be approved for refund).",
                    "Return Not Requested");
            return;
        }

        boolean confirm = ValidationUtil.showConfirm(this,
                "Approve return and issue full refund of <b>$" + String.format("%.2f", selected.getTotalAmount())
                        + "</b> for order <b>" + selected.getOrderNumber() + "</b>?<br/>"
                        + "This will replenish product inventory and mark payment as REFUNDED.",
                "Confirm Return Approval");

        if (confirm) {
            OrderStore.approveReturn(selected);
            AuditStore.log(currentUser.getUsername(), currentUser.getRole(), "RETURNS_REFUNDS", "RETURN_APPROVED",
                    "Approved return and issued refund for order " + selected.getOrderNumber()
                            + " ($" + String.format("%.2f", selected.getTotalAmount()) + "); replenished inventory items.");
            catalogProducts.clear();
            catalogProducts.addAll(ProductStore.getAllProducts());
            applyAdminOrderFilters();
            refreshCustomerCatalogTable(catalogProducts);
            refreshAdminCatalogTable();
            refreshAdminReportsPanel();
            updateNotificationBadge();

            ValidationUtil.showSuccess(this,
                    "Return approved for order <b>" + selected.getOrderNumber() + "</b>.<br/>"
                            + "Inventory has been replenished and customer notified.",
                    "Return & Refund Processed");
        }
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
}
