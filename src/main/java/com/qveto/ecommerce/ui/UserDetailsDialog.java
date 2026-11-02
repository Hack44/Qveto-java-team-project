package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog displaying complete user account details, role information,
 * address profile, order frequency, and Customer Lifetime Value (CLV).
 * 
 * Supports Member 2 (UI, Presentation & Store Management) Admin User Management requirements.
 */
public class UserDetailsDialog extends JDialog {

    public UserDetailsDialog(Frame owner, User user, int orderCount) {
        this(owner, user, orderCount, 0.0);
    }

    public UserDetailsDialog(Frame owner, User user, int orderCount, double lifetimeSpend) {
        super(owner, "User Account Dossier — " + user.getFullName(), true);
        initComponents(user, orderCount, lifetimeSpend);
    }

    private void initComponents(User user, int orderCount, double lifetimeSpend) {
        setSize(500, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("User Account Dossier");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel("  " + user.getRole().toUpperCase() + "  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setOpaque(true);
        if (user.isAdmin()) {
            roleBadge.setBackground(new Color(220, 38, 38)); // Red for Admin
            roleBadge.setForeground(Color.WHITE);
        } else {
            roleBadge.setBackground(new Color(37, 99, 235)); // Blue for Customer
            roleBadge.setForeground(Color.WHITE);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(roleBadge, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Body Panel
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(20, 24, 15, 24));

        // User Full Name Heading
        JLabel nameHeading = new JLabel(user.getFullName());
        nameHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameHeading.setForeground(new Color(15, 23, 42));
        nameHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(nameHeading);
        bodyPanel.add(Box.createVerticalStrut(12));

        // Metadata Grid Card (4 rows x 2 columns)
        JPanel metaGrid = new JPanel(new GridLayout(4, 2, 10, 8));
        metaGrid.setBackground(new Color(248, 250, 252));
        metaGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));
        metaGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        metaGrid.add(createFieldItem("User ID:", "#" + user.getUserId()));
        metaGrid.add(createFieldItem("Account Role:", user.getRole()));
        metaGrid.add(createFieldItem("Email Address:", user.getEmail()));
        metaGrid.add(createFieldItem("Phone:", user.getPhone().isEmpty() ? "N/A" : user.getPhone()));
        metaGrid.add(createFieldItem("Registered On:", user.getCreatedAt().isEmpty() ? "Recent" : user.getCreatedAt()));
        metaGrid.add(createFieldItem("Total Orders:", orderCount + " order" + (orderCount == 1 ? "" : "s")));
        metaGrid.add(createFieldItem("Lifetime Value (CLV):", String.format("$%.2f", lifetimeSpend)));
        metaGrid.add(createFieldItem("Account Status:", "Active & Verified"));

        bodyPanel.add(metaGrid);
        bodyPanel.add(Box.createVerticalStrut(14));

        // Address Section
        JLabel addressTitle = new JLabel("Primary Shipping & Contact Address:");
        addressTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addressTitle.setForeground(new Color(51, 65, 85));
        addressTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(addressTitle);
        bodyPanel.add(Box.createVerticalStrut(4));

        JTextArea addressArea = new JTextArea(user.getAddress().isEmpty() ? "No address registered." : user.getAddress());
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setEditable(false);
        addressArea.setBackground(new Color(248, 250, 252));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        addressArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addressArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(addressArea);

        bodyPanel.add(Box.createVerticalStrut(14));

        rootPanel.add(bodyPanel, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomBar.setBackground(new Color(248, 250, 252));
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close Dossier");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);

        rootPanel.add(bottomBar, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JPanel createFieldItem(String labelText, String valueText) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(valueText);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(new Color(15, 23, 42));

        panel.add(label, BorderLayout.WEST);
        panel.add(val, BorderLayout.CENTER);
        return panel;
    }
}
