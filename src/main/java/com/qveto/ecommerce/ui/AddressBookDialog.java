package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Address;
import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.AddressBookStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog enabling customers to manage their saved delivery addresses,
 * set default destinations, and add new delivery locations.
 *
 * Supports Member 2 (UI, Presentation & Store Management) address book requirements.
 */
public class AddressBookDialog extends JDialog {

    private final User customer;
    private final Runnable onAddressChanged;
    private JPanel listContainer;
    private JLabel countLabel;

    public AddressBookDialog(Frame owner, User customer, Runnable onAddressChanged) {
        super(owner, "My Delivery Address Book — Qveto", true);
        this.customer = customer;
        this.onAddressChanged = onAddressChanged;

        initComponents();
    }

    private void initComponents() {
        setSize(580, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Saved Delivery Addresses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Manage primary delivery locations for express checkout");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(203, 213, 225));

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        root.add(headerPanel, BorderLayout.NORTH);

        // Center Toolbar & List
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(248, 250, 252));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(10, 16, 10, 16)
        ));

        countLabel = new JLabel("0 saved addresses");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(new Color(30, 41, 59));
        toolbar.add(countLabel, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add New Address");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(16, 185, 129));
        addBtn.setForeground(Color.WHITE);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddressFormDialog(null));
        toolbar.add(addBtn, BorderLayout.EAST);

        centerPanel.add(toolbar, BorderLayout.NORTH);

        // Addresses list container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        listContainer.setBorder(new EmptyBorder(12, 16, 12, 16));

        reloadAddresses();

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> {
            if (onAddressChanged != null) onAddressChanged.run();
            dispose();
        });
        footer.add(closeBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void reloadAddresses() {
        listContainer.removeAll();
        List<Address> addresses = AddressBookStore.getAddresses(customer.getUserId());
        countLabel.setText(addresses.size() + " saved delivery destination" + (addresses.size() == 1 ? "" : "s"));

        if (addresses.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setBackground(Color.WHITE);
            empty.setBorder(new EmptyBorder(40, 20, 40, 20));
            JLabel lbl = new JLabel("No addresses saved yet. Click '+ Add New Address' above.");
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lbl.setForeground(new Color(148, 163, 184));
            empty.add(lbl);
            listContainer.add(empty);
        } else {
            for (Address a : addresses) {
                listContainer.add(createAddressCard(a));
                listContainer.add(Box.createVerticalStrut(10));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createAddressCard(Address a) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(a.isDefault() ? new Color(240, 249, 255) : new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(a.isDefault() ? new Color(59, 130, 246) : new Color(226, 232, 240), a.isDefault() ? 2 : 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Card Header (Label, Default Badge, Recipient)
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JPanel leftBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftBadgeRow.setOpaque(false);

        JLabel labelBadge = new JLabel(" " + a.getLabel().toUpperCase() + " ");
        labelBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelBadge.setForeground(Color.WHITE);
        labelBadge.setOpaque(true);
        labelBadge.setBackground(new Color(24, 43, 73));
        labelBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        leftBadgeRow.add(labelBadge);

        if (a.isDefault()) {
            JLabel defaultBadge = new JLabel(" ★ Primary Default ");
            defaultBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            defaultBadge.setForeground(new Color(5, 150, 105));
            defaultBadge.setOpaque(true);
            defaultBadge.setBackground(new Color(209, 250, 229));
            defaultBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
            leftBadgeRow.add(defaultBadge);
        }

        cardHeader.add(leftBadgeRow, BorderLayout.WEST);

        JLabel nameLabel = new JLabel(a.getRecipientName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(new Color(15, 23, 42));
        cardHeader.add(nameLabel, BorderLayout.EAST);

        card.add(cardHeader, BorderLayout.NORTH);

        // Address Body
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 2));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel streetLabel = new JLabel(a.getFormattedAddress());
        streetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        streetLabel.setForeground(new Color(51, 65, 85));

        JLabel phoneLabel = new JLabel("Mobile Contact: " + a.getPhoneNumber());
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        phoneLabel.setForeground(new Color(100, 116, 139));

        body.add(streetLabel);
        body.add(phoneLabel);
        card.add(body, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        if (!a.isDefault()) {
            JButton makeDefaultBtn = new JButton("Set as Default");
            makeDefaultBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            makeDefaultBtn.setBackground(Color.WHITE);
            makeDefaultBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            makeDefaultBtn.addActionListener(e -> {
                AddressBookStore.setDefault(customer.getUserId(), a.getAddressId());
                reloadAddresses();
                if (onAddressChanged != null) onAddressChanged.run();
            });
            actions.add(makeDefaultBtn);
        }

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        editBtn.setBackground(Color.WHITE);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> showAddressFormDialog(a));
        actions.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deleteBtn.setBackground(new Color(254, 226, 226));
        deleteBtn.setForeground(new Color(220, 38, 38));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            if (ValidationUtil.showConfirm(this, "Are you sure you want to delete this delivery address?", "Delete Address")) {
                AddressBookStore.deleteAddress(customer.getUserId(), a.getAddressId());
                reloadAddresses();
                if (onAddressChanged != null) onAddressChanged.run();
            }
        });
        actions.add(deleteBtn);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void showAddressFormDialog(Address existing) {
        JDialog formDialog = new JDialog(this, existing == null ? "Add Delivery Address" : "Edit Delivery Address", true);
        formDialog.setSize(440, 480);
        formDialog.setLocationRelativeTo(this);
        formDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int row = 0;

        // Label
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Address Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JComboBox<String> labelCombo = new JComboBox<>(new String[]{"Home", "Work", "Office", "Other"});
        if (existing != null) labelCombo.setSelectedItem(existing.getLabel());
        formPanel.add(labelCombo, gbc);

        // Recipient Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Recipient Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField nameField = new JTextField(existing != null ? existing.getRecipientName() : customer.getFullName());
        formPanel.add(nameField, gbc);

        // Street Address
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Street Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField streetField = new JTextField(existing != null ? existing.getStreetAddress() : "");
        formPanel.add(streetField, gbc);

        // City
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField cityField = new JTextField(existing != null ? existing.getCity() : "Bengaluru");
        formPanel.add(cityField, gbc);

        // State
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("State:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField stateField = new JTextField(existing != null ? existing.getState() : "Karnataka");
        formPanel.add(stateField, gbc);

        // Postal Code
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Postal Code (PIN):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField pinField = new JTextField(existing != null ? existing.getPostalCode() : "");
        formPanel.add(pinField, gbc);

        // Phone
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField phoneField = new JTextField(existing != null ? existing.getPhoneNumber() : customer.getPhone());
        formPanel.add(phoneField, gbc);

        // Default Checkbox
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JCheckBox defaultCheck = new JCheckBox("Set as Primary Default Delivery Address", existing != null && existing.isDefault());
        defaultCheck.setBackground(Color.WHITE);
        formPanel.add(defaultCheck, gbc);

        formDialog.add(formPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.setBackground(new Color(248, 250, 252));
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> formDialog.dispose());
        btnBar.add(cancelBtn);

        JButton saveBtn = new JButton("Save Address");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(16, 185, 129));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            String recipient = nameField.getText().trim();
            String street = streetField.getText().trim();
            String city = cityField.getText().trim();
            String state = stateField.getText().trim();
            String pin = pinField.getText().trim();
            String phone = phoneField.getText().trim();

            if (recipient.isEmpty() || street.isEmpty() || city.isEmpty() || pin.isEmpty()) {
                ValidationUtil.showWarning(formDialog, "Please fill in recipient name, street, city, and postal code.", "Missing Details");
                return;
            }

            if (existing == null) {
                Address newAddr = new Address(0, customer.getUserId(), (String) labelCombo.getSelectedItem(),
                        recipient, street, city, state, pin, phone, defaultCheck.isSelected());
                AddressBookStore.addAddress(newAddr);
            } else {
                existing.setLabel((String) labelCombo.getSelectedItem());
                existing.setRecipientName(recipient);
                existing.setStreetAddress(street);
                existing.setCity(city);
                existing.setState(state);
                existing.setPostalCode(pin);
                existing.setPhoneNumber(phone);
                existing.setDefault(defaultCheck.isSelected());
                AddressBookStore.updateAddress(existing);
            }

            reloadAddresses();
            if (onAddressChanged != null) onAddressChanged.run();
            formDialog.dispose();
        });
        btnBar.add(saveBtn);

        formDialog.add(btnBar, BorderLayout.SOUTH);
        formDialog.setVisible(true);
    }
}
