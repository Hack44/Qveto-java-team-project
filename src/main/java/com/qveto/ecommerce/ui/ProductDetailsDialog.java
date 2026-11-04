package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.model.Review;
import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.ReviewStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modal dialog displaying detailed product information, specifications,
 * live customer ratings, verified reviews, and add-to-cart action.
 *
 * Supports Member 2 (UI, Presentation & Store Management) product discovery requirements.
 */
public class ProductDetailsDialog extends JDialog {

    private final Product product;
    private final User currentUser;
    private final boolean isCustomer;
    private final Consumer<Product> onAddToCart;

    private JPanel reviewsListPanel;
    private JLabel ratingSummaryLabel;

    public ProductDetailsDialog(Frame owner, Product product, boolean isCustomer, Consumer<Product> onAddToCart) {
        this(owner, product, isCustomer ? new User(1, "Customer", "customer@qveto.com", "CUSTOMER") : null, onAddToCart);
    }

    public ProductDetailsDialog(Frame owner, Product product, User currentUser, Consumer<Product> onAddToCart) {
        super(owner, "Product Details — " + product.getProductName(), true);
        this.product = product;
        this.currentUser = currentUser;
        this.isCustomer = (currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole()))
                || (currentUser != null && currentUser.getRole() == null);
        this.onAddToCart = onAddToCart;

        initComponents();
    }

    private void initComponents() {
        setSize(580, 620);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel titleLabel = new JLabel("Product Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel catBadge = new JLabel(" " + product.getCategory().toUpperCase() + " ");
        catBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        catBadge.setForeground(new Color(24, 43, 73));
        catBadge.setOpaque(true);
        catBadge.setBackground(new Color(224, 231, 255));
        catBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(catBadge, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Tabbed Pane (Overview / Customer Reviews)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Product Overview", createOverviewPanel());
        tabbedPane.addTab("Customer Reviews (" + ReviewStore.getReviewCount(product.getProductId()) + ")", createReviewsPanel());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(closeBtn);

        if (isCustomer) {
            JButton addBtn = new JButton("Add to Cart — " + String.format("$%.2f", product.getPriceAsDouble()));
            addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            addBtn.setBackground(new Color(16, 185, 129));
            addBtn.setForeground(Color.WHITE);
            addBtn.setFocusPainted(false);
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

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Name
        JLabel nameLabel = new JLabel(product.getProductName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(15, 23, 42));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(6));

        // Rating line
        double rating = ReviewStore.getAverageRating(product.getProductId());
        int reviewsCount = ReviewStore.getReviewCount(product.getProductId());
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ratingRow.setOpaque(false);
        ratingRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel stars = new JLabel(formatStars(rating));
        stars.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stars.setForeground(new Color(245, 158, 11));

        JLabel ratingInfo = new JLabel(String.format(" %.1f / 5.0 (%d customer reviews)", rating, reviewsCount));
        ratingInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ratingInfo.setForeground(new Color(100, 116, 139));

        ratingRow.add(stars);
        ratingRow.add(ratingInfo);
        panel.add(ratingRow);
        panel.add(Box.createVerticalStrut(14));

        // Specifications Grid
        JPanel specsGrid = new JPanel(new GridLayout(3, 2, 12, 10));
        specsGrid.setBackground(new Color(248, 250, 252));
        specsGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        specsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        specsGrid.add(createMetaLabel("Product ID:", "#" + product.getProductId()));
        specsGrid.add(createMetaLabel("Category:", product.getCategory()));
        specsGrid.add(createMetaLabel("Unit Price:", String.format("$%.2f", product.getPriceAsDouble())));
        specsGrid.add(createMetaLabel("Available Stock:", product.getStockQuantity() + " units"));
        specsGrid.add(createMetaLabel("Inventory Status:", product.getStockStatus()));
        specsGrid.add(createMetaLabel("Fulfillment:", "Standard Free Delivery"));

        panel.add(specsGrid);
        panel.add(Box.createVerticalStrut(14));

        // Description
        JLabel descHeader = new JLabel("Product Description:");
        descHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descHeader.setForeground(new Color(30, 41, 59));
        descHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descHeader);
        panel.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea(product.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descArea);

        return panel;
    }

    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Rating summary header & write review button
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setBorder(new EmptyBorder(0, 0, 12, 0));

        double rating = ReviewStore.getAverageRating(product.getProductId());
        int reviewsCount = ReviewStore.getReviewCount(product.getProductId());

        ratingSummaryLabel = new JLabel(String.format("Average Rating: %.1f / 5.0 (%d verified reviews)", rating, reviewsCount));
        ratingSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratingSummaryLabel.setForeground(new Color(30, 41, 59));
        topHeader.add(ratingSummaryLabel, BorderLayout.WEST);

        if (isCustomer) {
            JButton writeBtn = new JButton("★ Write Review");
            writeBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            writeBtn.setBackground(new Color(59, 130, 246));
            writeBtn.setForeground(Color.WHITE);
            writeBtn.setFocusPainted(false);
            writeBtn.addActionListener(e -> {
                ProductReviewDialog reviewDialog = new ProductReviewDialog(
                        (Frame) getOwner(),
                        product,
                        currentUser != null ? currentUser : new User(1, "Customer", "customer@qveto.com", "CUSTOMER"),
                        this::reloadReviews
                );
                reviewDialog.setVisible(true);
            });
            topHeader.add(writeBtn, BorderLayout.EAST);
        }

        panel.add(topHeader, BorderLayout.NORTH);

        reviewsListPanel = new JPanel();
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        reviewsListPanel.setBackground(Color.WHITE);

        reloadReviews();

        JScrollPane scrollPane = new JScrollPane(reviewsListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void reloadReviews() {
        reviewsListPanel.removeAll();
        List<Review> reviews = ReviewStore.getReviewsForProduct(product.getProductId());

        if (ratingSummaryLabel != null) {
            double rating = ReviewStore.getAverageRating(product.getProductId());
            ratingSummaryLabel.setText(String.format("Average Rating: %.1f / 5.0 (%d verified reviews)", rating, reviews.size()));
        }

        if (reviews.isEmpty()) {
            JPanel emptyBox = new JPanel();
            emptyBox.setBackground(Color.WHITE);
            emptyBox.setBorder(new EmptyBorder(30, 20, 30, 20));
            JLabel emptyLabel = new JLabel("No reviews yet. Be the first customer to leave feedback!");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(new Color(148, 163, 184));
            emptyBox.add(emptyLabel);
            reviewsListPanel.add(emptyBox);
        } else {
            for (Review r : reviews) {
                reviewsListPanel.add(createReviewCard(r));
                reviewsListPanel.add(Box.createVerticalStrut(8));
            }
        }

        reviewsListPanel.revalidate();
        reviewsListPanel.repaint();
    }

    private JPanel createReviewCard(Review r) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        // Header of card: Star rating, Headline, Customer name, Date
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JLabel stars = new JLabel(formatStars(r.getRating()) + "  " + r.getHeadline());
        stars.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stars.setForeground(new Color(15, 23, 42));

        String badge = r.isVerifiedPurchase() ? " ✓ Verified Buyer" : "";
        JLabel meta = new JLabel(r.getCustomerName() + badge + " • " + r.getReviewDate());
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(new Color(100, 116, 139));

        cardHeader.add(stars, BorderLayout.WEST);
        cardHeader.add(meta, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        JLabel comment = new JLabel("<html><body style='width: 380px;'>" + r.getComment() + "</body></html>");
        comment.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comment.setForeground(new Color(51, 65, 85));
        comment.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(comment, BorderLayout.CENTER);

        return card;
    }

    private String formatStars(double rating) {
        int rounded = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rounded && i < 5; i++) {
            sb.append("★");
        }
        while (sb.length() < 5) {
            sb.append("☆");
        }
        return sb.toString();
    }

    private JLabel createMetaLabel(String key, String value) {
        JLabel label = new JLabel("<html><b>" + key + "</b> " + value + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
}
