package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog displaying complete user account details, role information,
 * address profile, and platform activity metrics.
 * 
 * Supports Member 2 Admin User Management requirements.
 */
public class UserDetailsDialog extends JDialog {

    public UserDetailsDialog(Frame owner, User user, int orderCount) {
        super(owner, "User Account Dossier — " + user.getFullName(), true);
        initComponents(user, orderCount);
    }

    private void initComponents(User user, int orderCount) {
        setSize(480, 470);
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

        // Metadata Grid Card
        JPanel metaGrid = new JPanel(new GridLayout(3, 2, 10, 8));
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

        // Account Status Info Pill
        JPanel statusCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusCard.setBackground(new Color(240, 253, 244));
        statusCard.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208)));
        statusCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel statusDot = new JLabel("● Account Status: Active & In Good Standing");
        statusDot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusDot.setForeground(new Color(22, 101, 52));
        statusCard.add(statusDot);
        bodyPanel.add(statusCard);

        rootPanel.add(bodyPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close Dossier");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setPreferredSize(new Dimension(120, 32));
        closeBtn.addActionListener(e -> dispose());
        footerPanel.add(closeBtn);

        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createFieldItem(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(new Color(248, 250, 252));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(new Color(15, 23, 42));

        p.add(lbl);
        p.add(val);
        return p;
    }
}
