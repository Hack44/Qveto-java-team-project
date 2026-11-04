package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.model.Review;
import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.ReviewStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Modal dialog enabling customers to submit verified product reviews and 5-star ratings.
 * Supports Member 2 (UI, Presentation & Store Management) customer feedback requirements.
 */
public class ProductReviewDialog extends JDialog {

    private final Product product;
    private final User customer;
    private final Runnable onReviewSubmitted;

    private int selectedRating = 5;
    private JButton[] starButtons;
    private JTextField headlineField;
    private JTextArea commentArea;

    public ProductReviewDialog(Frame owner, Product product, User customer, Runnable onReviewSubmitted) {
        super(owner, "Write a Review — " + product.getProductName(), true);
        this.product = product;
        this.customer = customer;
        this.onReviewSubmitted = onReviewSubmitted;

        initComponents();
    }

    private void initComponents() {
        setSize(500, 480);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Customer Product Review");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel itemLabel = new JLabel(product.getProductName());
        itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemLabel.setForeground(new Color(203, 213, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(itemLabel, BorderLayout.SOUTH);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Body Panel
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        // 1. Star Rating Selector
        JLabel ratingLabel = new JLabel("Overall Rating:");
        ratingLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(ratingLabel);
        bodyPanel.add(Box.createVerticalStrut(6));

        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starRow.setOpaque(false);
        starRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        starButtons = new JButton[5];
        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            JButton starBtn = new JButton("★");
            starBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
            starBtn.setForeground(new Color(245, 158, 11)); // Amber
            starBtn.setFocusPainted(false);
            starBtn.setBorderPainted(false);
            starBtn.setContentAreaFilled(false);
            starBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            starBtn.addActionListener(e -> updateStars(starIndex));
            starButtons[i] = starBtn;
            starRow.add(starBtn);
        }

        bodyPanel.add(starRow);
        bodyPanel.add(Box.createVerticalStrut(14));

        // 2. Review Headline
        JLabel headLabel = new JLabel("Headline / Summary:");
        headLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(headLabel);
        bodyPanel.add(Box.createVerticalStrut(4));

        headlineField = new JTextField(24);
        headlineField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headlineField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        headlineField.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(headlineField);
        bodyPanel.add(Box.createVerticalStrut(14));

        // 3. Comments / Detailed Feedback
        JLabel commentLabel = new JLabel("Your Detailed Experience:");
        commentLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(commentLabel);
        bodyPanel.add(Box.createVerticalStrut(4));

        commentArea = new JTextArea(5, 24);
        commentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(commentScroll);

        rootPanel.add(bodyPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actionPanel.setBackground(new Color(248, 250, 252));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        actionPanel.add(cancelBtn);

        JButton submitBtn = new JButton("Submit Review");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitBtn.setBackground(new Color(16, 185, 129));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> handleSubmit());
        actionPanel.add(submitBtn);

        rootPanel.add(actionPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);

        updateStars(5);
    }

    private void updateStars(int rating) {
        this.selectedRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                starButtons[i].setText("★");
                starButtons[i].setForeground(new Color(245, 158, 11));
            } else {
                starButtons[i].setText("☆");
                starButtons[i].setForeground(new Color(203, 213, 225));
            }
        }
    }

    private void handleSubmit() {
        String headline = headlineField.getText().trim();
        String comment = commentArea.getText().trim();

        if (headline.isEmpty()) {
            ValidationUtil.showError(this, "Please enter a short headline for your review.", "Validation Error");
            return;
        }

        if (comment.isEmpty()) {
            ValidationUtil.showError(this, "Please share a few words about your experience with this item.", "Validation Error");
            return;
        }

        Review rev = new Review(
                0,
                product.getProductId(),
                customer.getUserId(),
                customer.getFullName(),
                selectedRating,
                headline,
                comment,
                LocalDate.now().toString(),
                true
        );

        ReviewStore.addReview(rev);

        ValidationUtil.showSuccess(this,
                "Thank you! Your verified review has been published.",
                "Review Submitted");

        if (onReviewSubmitted != null) {
            onReviewSubmitted.run();
        }

        dispose();
    }
}
