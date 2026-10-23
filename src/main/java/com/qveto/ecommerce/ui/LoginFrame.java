package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Login and Registration UI Frame for the Qveto E-Commerce System.
 * 
 * Developed for Member 2 tasks:
 * - Login & Registration screens (Customer & Admin)
 * - Password visibility toggle for better UX
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
        setSize(500, 620);
        setMinimumSize(new Dimension(460, 580));
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
        customerEmailField.setPreferredSize(new Dimension(320, 36));
        customerEmailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 1;
        panel.add(customerEmailField, gbc);

        // Password Label & Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        customerPasswordField = new JPasswordField();
        customerPasswordField.setPreferredSize(new Dimension(320, 36));
        customerPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 3;
        panel.add(customerPasswordField, gbc);

        // Options Row: Remember Me & Show Password
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setBackground(Color.WHITE);

        customerRememberCheckBox = new JCheckBox("Remember me");
        customerRememberCheckBox.setBackground(Color.WHITE);
        customerRememberCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        optionsRow.add(customerRememberCheckBox, BorderLayout.WEST);

        JCheckBox showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setBackground(Color.WHITE);
        showPassCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPassCheck.addActionListener(e -> customerPasswordField.setEchoChar(showPassCheck.isSelected() ? (char) 0 : '\u2022'));
        optionsRow.add(showPassCheck, BorderLayout.EAST);

        gbc.gridy = 4;
        panel.add(optionsRow, gbc);

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
        adminUsernameField.setPreferredSize(new Dimension(320, 36));
        adminUsernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 2;
        panel.add(adminUsernameField, gbc);

        JLabel passLabel = new JLabel("Admin Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 3;
        panel.add(passLabel, gbc);

        adminPasswordField = new JPasswordField();
        adminPasswordField.setPreferredSize(new Dimension(320, 36));
        adminPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 4;
        panel.add(adminPasswordField, gbc);

        // Show Password Option for Admin
        JCheckBox showAdminPassCheck = new JCheckBox("Show Password");
        showAdminPassCheck.setBackground(Color.WHITE);
        showAdminPassCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showAdminPassCheck.addActionListener(e -> adminPasswordField.setEchoChar(showAdminPassCheck.isSelected() ? (char) 0 : '\u2022'));
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 8, 0);
        panel.add(showAdminPassCheck, gbc);

        JButton adminLoginBtn = createPrimaryButton("Login as Admin", new Color(17, 24, 39));
        adminLoginBtn.addActionListener(this::handleAdminLogin);
        gbc.gridy = 6;
        gbc.insets = new Insets(12, 0, 8, 0);
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
        regNameField.setPreferredSize(new Dimension(320, 32));
        gbc.gridy = 1;
        panel.add(regNameField, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        panel.add(emailLabel, gbc);

        regEmailField = new JTextField();
        regEmailField.setPreferredSize(new Dimension(320, 32));
        gbc.gridy = 3;
        panel.add(regEmailField, gbc);

        // Phone
        JLabel phoneLabel = new JLabel("Phone Number");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 4;
        panel.add(phoneLabel, gbc);

        regPhoneField = new JTextField();
        regPhoneField.setPreferredSize(new Dimension(320, 32));
        gbc.gridy = 5;
        panel.add(regPhoneField, gbc);

        // Password
        JLabel passLabel = new JLabel("Password (min 6 characters)");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 6;
        panel.add(passLabel, gbc);

        regPasswordField = new JPasswordField();
        regPasswordField.setPreferredSize(new Dimension(320, 32));
        gbc.gridy = 7;
        panel.add(regPasswordField, gbc);

        // Confirm Password
        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 8;
        panel.add(confirmLabel, gbc);

        regConfirmPasswordField = new JPasswordField();
        regConfirmPasswordField.setPreferredSize(new Dimension(320, 32));
        gbc.gridy = 9;
        panel.add(regConfirmPasswordField, gbc);

        // Show Passwords Checkbox
        JCheckBox showRegPassCheck = new JCheckBox("Show Passwords");
        showRegPassCheck.setBackground(Color.WHITE);
        showRegPassCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showRegPassCheck.addActionListener(e -> {
            char echo = showRegPassCheck.isSelected() ? (char) 0 : '\u2022';
            regPasswordField.setEchoChar(echo);
            regConfirmPasswordField.setEchoChar(echo);
        });
        gbc.gridy = 10;
        panel.add(showRegPassCheck, gbc);

        // Register Button
        JButton registerBtn = createPrimaryButton("Create Customer Account", new Color(16, 185, 129));
        registerBtn.addActionListener(this::handleRegistration);
        gbc.gridy = 11;
        gbc.insets = new Insets(10, 0, 4, 0);
        panel.add(registerBtn, gbc);

        // Back to login
        JButton backBtn = createLinkButton("Already have an account? Back to Login");
        backBtn.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        gbc.gridy = 12;
        gbc.insets = new Insets(2, 0, 0, 0);
        panel.add(backBtn, gbc);

        return panel;
    }

    // --- Action Handlers & Validations ---

    private void handleCustomerLogin(ActionEvent e) {
        String email = customerEmailField.getText().trim();
        String password = new String(customerPasswordField.getPassword());

        if (!ValidationUtil.isNotEmpty(email)) {
            ValidationUtil.showError(this, "Please enter your email or username.", "Validation Error");
            customerEmailField.requestFocus();
            return;
        }
        if (!ValidationUtil.isNotEmpty(password)) {
            ValidationUtil.showError(this, "Please enter your password.", "Validation Error");
            customerPasswordField.requestFocus();
            return;
        }

        // Authentication logic
        if (email.equalsIgnoreCase("customer@qveto.com") && password.equals("pass123") || !email.isEmpty() && password.length() >= 6) {
            String name = email.contains("@") ? email.split("@")[0] : email;
            name = name.substring(0, 1).toUpperCase() + name.substring(1);

            User user = new User(1, name, email, "CUSTOMER");
            ValidationUtil.showSuccess(this, "Customer Login Successful!<br/>Welcome back, <b>" + user.getFullName() + "</b>.", "Login Successful");
            openDashboard(user);
        } else {
            ValidationUtil.showError(this, "Invalid credentials. Please check your password (min 6 chars) or use demo: <b>customer@qveto.com</b> / <b>pass123</b>.", "Authentication Failed");
        }
    }

    private void handleAdminLogin(ActionEvent e) {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword());

        if (!ValidationUtil.isNotEmpty(username)) {
            ValidationUtil.showError(this, "Please enter the admin username.", "Validation Error");
            adminUsernameField.requestFocus();
            return;
        }
        if (!ValidationUtil.isNotEmpty(password)) {
            ValidationUtil.showError(this, "Please enter the admin password.", "Validation Error");
            adminPasswordField.requestFocus();
            return;
        }

        if (username.equals("admin") && password.equals("admin123")) {
            User adminUser = new User(99, "System Administrator", "admin@qveto.com", "ADMIN");
            ValidationUtil.showSuccess(this, "Admin Authentication Successful!<br/>Opening Management Dashboard...", "Admin Authorized");
            openDashboard(adminUser);
        } else {
            ValidationUtil.showError(this, "Invalid administrator credentials.<br/>Hint: Use <b>admin</b> / <b>admin123</b> for demo access.", "Authentication Failed");
        }
    }

    private void handleRegistration(ActionEvent e) {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirmPassword = new String(regConfirmPasswordField.getPassword());

        if (!ValidationUtil.isNotEmpty(name)) {
            ValidationUtil.showError(this, "Please enter your full name.", "Validation Error");
            regNameField.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            ValidationUtil.showError(this, "Please enter a valid email address (e.g., user@example.com).", "Validation Error");
            regEmailField.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            ValidationUtil.showError(this, "Please enter a valid phone number (at least 7 digits).", "Validation Error");
            regPhoneField.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            ValidationUtil.showError(this, "Password must be at least 6 characters long.", "Validation Error");
            regPasswordField.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            ValidationUtil.showError(this, "Passwords do not match. Please re-enter.", "Validation Error");
            regConfirmPasswordField.requestFocus();
            return;
        }

        ValidationUtil.showSuccess(this,
                "Account created successfully for <b>" + name + "</b>!<br/>You can now log in with your credentials.",
                "Registration Successful");

        // Pre-fill email in customer login and switch tab
        customerEmailField.setText(email);
        customerPasswordField.setText("");
        tabbedPane.setSelectedIndex(0);
    }

    private void openDashboard(User user) {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
        });
    }

    // --- UI Helpers ---

    private JButton createPrimaryButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(320, 38));
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
