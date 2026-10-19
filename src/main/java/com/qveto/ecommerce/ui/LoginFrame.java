package com.qveto.ecommerce.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Login and Registration UI Frame for the Qveto E-Commerce System.
 * 
 * Developed for Member 2 tasks:
 * - Login & Registration screens (Customer & Admin)
 * - Input validation & user-friendly error alerts
 * - Authentication flow and navigation to Dashboard
 */
public class LoginFrame extends JFrame {

    private JTabbedPane tabbedPane;

    // Customer Login Components
    private JTextField customerEmailField;
    private JPasswordField customerPasswordField;
    private JCheckBox customerRememberCheckBox;

    // Admin Login Components
    private JTextField adminUsernameField;
    private JPasswordField adminPasswordField;

    // Registration Components
    private JTextField regNameField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Qveto E-Commerce & Inventory Management");
        setSize(480, 600);
        setMinimumSize(new Dimension(440, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // --- Header Banner ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(25, 20, 20, 20));

        JLabel titleLabel = new JLabel("QVETO STORE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("E-Commerce & Inventory System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(200, 215, 235));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Tabs ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBorder(new EmptyBorder(10, 15, 15, 15));

        tabbedPane.addTab("Customer Login", createCustomerLoginPanel());
        tabbedPane.addTab("Admin Login", createAdminLoginPanel());
        tabbedPane.addTab("Register", createRegisterPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // --- Footer Note ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(5, 10, 15, 10));

        JLabel demoHint = new JLabel("<html><center><font color='#718096' size='2'>Demo Accounts: Customer: <b>customer@qveto.com</b> (pass123) | Admin: <b>admin</b> (admin123)</font></center></html>");
        footerPanel.add(demoHint);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Builds the Customer Login Form.
     */
    private JPanel createCustomerLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        // Email Label & Field
        JLabel emailLabel = new JLabel("Email / Username");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 0;
        panel.add(emailLabel, gbc);

        customerEmailField = new JTextField();
        customerEmailField.setPreferredSize(new Dimension(300, 36));
        customerEmailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1;
        panel.add(customerEmailField, gbc);

        // Password Label & Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        customerPasswordField = new JPasswordField();
        customerPasswordField.setPreferredSize(new Dimension(300, 36));
        customerPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 3;
        panel.add(customerPasswordField, gbc);

        // Remember Me Checkbox
        customerRememberCheckBox = new JCheckBox("Remember me on this device");
        customerRememberCheckBox.setBackground(Color.WHITE);
        customerRememberCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 4;
        panel.add(customerRememberCheckBox, gbc);

        // Login Button
        JButton loginBtn = createPrimaryButton("Login as Customer", new Color(37, 99, 235));
        loginBtn.addActionListener(this::handleCustomerLogin);
        gbc.gridy = 5;
        gbc.insets = new Insets(16, 0, 8, 0);
        panel.add(loginBtn, gbc);

        // Switch to Register Link
        JButton switchBtn = createLinkButton("Don't have an account? Register here");
        switchBtn.addActionListener(e -> tabbedPane.setSelectedIndex(2)); // switch to Register tab
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 0, 0, 0);
        panel.add(switchBtn, gbc);

        return panel;
    }

    /**
     * Builds the Admin Login Form.
     */
    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        JLabel badgeLabel = new JLabel("Authorized Administrator Access Only", SwingConstants.CENTER);
        badgeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        badgeLabel.setForeground(new Color(185, 28, 28));
        gbc.gridy = 0;
        panel.add(badgeLabel, gbc);

        JLabel userLabel = new JLabel("Admin Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 1;
        panel.add(userLabel, gbc);

        adminUsernameField = new JTextField();
        adminUsernameField.setPreferredSize(new Dimension(300, 36));
        adminUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 2;
        panel.add(adminUsernameField, gbc);

        JLabel passLabel = new JLabel("Admin Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 3;
        panel.add(passLabel, gbc);

        adminPasswordField = new JPasswordField();
        adminPasswordField.setPreferredSize(new Dimension(300, 36));
        adminPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 4;
        panel.add(adminPasswordField, gbc);

        JButton adminLoginBtn = createPrimaryButton("Login as Admin", new Color(17, 24, 39));
        adminLoginBtn.addActionListener(this::handleAdminLogin);
        gbc.gridy = 5;
        gbc.insets = new Insets(18, 0, 8, 0);
        panel.add(adminLoginBtn, gbc);

        return panel;
    }

    /**
     * Builds the Customer Registration Form.
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 25, 10, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;

        // Full Name
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        regNameField = new JTextField();
        regNameField.setPreferredSize(new Dimension(300, 32));
        gbc.gridy = 1;
        panel.add(regNameField, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        panel.add(emailLabel, gbc);

        regEmailField = new JTextField();
        regEmailField.setPreferredSize(new Dimension(300, 32));
        gbc.gridy = 3;
        panel.add(regEmailField, gbc);

        // Phone
        JLabel phoneLabel = new JLabel("Phone Number");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 4;
        panel.add(phoneLabel, gbc);

        regPhoneField = new JTextField();
        regPhoneField.setPreferredSize(new Dimension(300, 32));
        gbc.gridy = 5;
        panel.add(regPhoneField, gbc);

        // Password
        JLabel passLabel = new JLabel("Password (min 6 characters)");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 6;
        panel.add(passLabel, gbc);

        regPasswordField = new JPasswordField();
        regPasswordField.setPreferredSize(new Dimension(300, 32));
        gbc.gridy = 7;
        panel.add(regPasswordField, gbc);

        // Confirm Password
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 8;
        panel.add(confirmLabel, gbc);

        regConfirmPasswordField = new JPasswordField();
        regConfirmPasswordField.setPreferredSize(new Dimension(300, 32));
        gbc.gridy = 9;
        panel.add(regConfirmPasswordField, gbc);

        // Register Button
        JButton registerBtn = createPrimaryButton("Create Customer Account", new Color(16, 185, 129));
        registerBtn.addActionListener(this::handleRegistration);
        gbc.gridy = 10;
        gbc.insets = new Insets(12, 0, 4, 0);
        panel.add(registerBtn, gbc);

        // Back to login
        JButton backBtn = createLinkButton("Already have an account? Back to Login");
        backBtn.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        gbc.gridy = 11;
        gbc.insets = new Insets(2, 0, 0, 0);
        panel.add(backBtn, gbc);

        return panel;
    }

    // --- Action Handlers & Validations ---

    private void handleCustomerLogin(ActionEvent e) {
        String email = customerEmailField.getText().trim();
        String password = new String(customerPasswordField.getPassword());

        if (email.isEmpty()) {
            showError("Please enter your email or username.");
            customerEmailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            customerPasswordField.requestFocus();
            return;
        }

        // Authentication logic (connected to JDBC/DAO in later integration)
        // Accepts demo credentials or any valid non-empty entry for preview
        if (email.equalsIgnoreCase("customer@qveto.com") && password.equals("pass123") || !email.isEmpty() && password.length() >= 6) {
            JOptionPane.showMessageDialog(this,
                    "Customer Login Successful!\nWelcome back, " + email + ".",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            openDashboard(email, "CUSTOMER");
        } else {
            showError("Invalid credentials. Please check your password (min 6 chars) or use demo: customer@qveto.com / pass123.");
        }
    }

    private void handleAdminLogin(ActionEvent e) {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword());

        if (username.isEmpty()) {
            showError("Please enter the admin username.");
            adminUsernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter the admin password.");
            adminPasswordField.requestFocus();
            return;
        }

        if (username.equals("admin") && password.equals("admin123")) {
            JOptionPane.showMessageDialog(this,
                    "Admin Authentication Successful!\nOpening Management Dashboard...",
                    "Admin Authorized",
                    JOptionPane.INFORMATION_MESSAGE);
            openDashboard(username, "ADMIN");
        } else {
            showError("Invalid administrator credentials.\nHint: Use admin / admin123 for demo access.");
        }
    }

    private void handleRegistration(ActionEvent e) {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirmPassword = new String(regConfirmPasswordField.getPassword());

        if (name.isEmpty()) {
            showError("Please enter your full name.");
            regNameField.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address (e.g., user@example.com).");
            regEmailField.requestFocus();
            return;
        }
        if (phone.isEmpty() || phone.length() < 7) {
            showError("Please enter a valid phone number (at least 7 digits).");
            regPhoneField.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            regPasswordField.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match. Please re-enter.");
            regConfirmPasswordField.requestFocus();
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Account created successfully for " + name + "!\nYou can now log in with your credentials.",
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE);

        // Pre-fill email in customer login and switch tab
        customerEmailField.setText(email);
        customerPasswordField.setText("");
        tabbedPane.setSelectedIndex(0);
    }

    private void openDashboard(String username, String role) {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame(username, role);
            dashboard.setVisible(true);
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- UI Helpers ---

    private JButton createPrimaryButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 38));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return button;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(37, 99, 235));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
