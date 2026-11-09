package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.AuditLog;
import com.qveto.ecommerce.util.AuditStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Enterprise Audit Trail & Operational Activity Journal Dialog.
 * Enables store administrators to inspect, search, filter, and export immutable logs of
 * orders, inventory alterations, pricing updates, return settlements, and security events.
 *
 * Supports Member 2 (UI, Presentation & Store Management) compliance & administrative controls.
 */
public class AuditLogDialog extends JDialog {

    private DefaultTableModel tableModel;
    private JTable auditTable;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JLabel countBadge;
    private JTextArea detailTextArea;

    public AuditLogDialog(Frame owner) {
        super(owner, "System Audit Trail & Activity Journal — Qveto Admin", true);
        initComponents();
    }

    private void initComponents() {
        setSize(880, 620);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Store Security & Operational Audit Activity Journal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Immutable audit trail tracking orders, inventory adjustments, refunds, and user privileges");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(203, 213, 225));

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        countBadge = new JLabel("0 Events Logged");
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countBadge.setForeground(new Color(254, 240, 138));
        headerPanel.add(countBadge, BorderLayout.EAST);

        root.add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Toolbar + Table + Inspector Split
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Filter Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(new Color(248, 250, 252));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(6, 12, 6, 12)
        ));

        toolbar.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{
                "ALL", "ORDER_MANAGEMENT", "INVENTORY", "STORE_SECURITY", "USER_MANAGEMENT", "RETURNS_REFUNDS", "CHECKOUT"
        });
        categoryCombo.addActionListener(e -> applyFilter());
        toolbar.add(categoryCombo);

        toolbar.add(new JLabel("Search:"));
        searchField = new JTextField(14);
        searchField.addActionListener(e -> applyFilter());
        toolbar.add(searchField);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterBtn.setBackground(new Color(37, 99, 235));
        filterBtn.setForeground(Color.WHITE);
        filterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterBtn.addActionListener(e -> applyFilter());
        toolbar.add(filterBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> {
            categoryCombo.setSelectedIndex(0);
            searchField.setText("");
            applyFilter();
        });
        toolbar.add(resetBtn);

        JButton exportBtn = new JButton("📊 Export CSV");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setBackground(new Color(241, 245, 249));
        exportBtn.setForeground(new Color(30, 41, 59));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> handleExportCsv());
        toolbar.add(exportBtn);

        centerPanel.add(toolbar, BorderLayout.NORTH);

        // Table Model & JTable
        String[] columns = {"ID", "Timestamp", "Actor", "Role", "Category", "Action", "Details", "IP Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        auditTable = new JTable(tableModel);
        auditTable.setRowHeight(26);
        auditTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        auditTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        auditTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(135);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        auditTable.getColumnModel().getColumn(6).setPreferredWidth(200);
        auditTable.getColumnModel().getColumn(7).setPreferredWidth(95);

        // Custom Category Renderer for high-contrast visual clarity
        auditTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String cat = String.valueOf(value);
                if (!isSelected) {
                    if ("INVENTORY".equals(cat)) {
                        c.setForeground(new Color(16, 185, 129));
                    } else if ("STORE_SECURITY".equals(cat)) {
                        c.setForeground(new Color(220, 38, 38));
                    } else if ("ORDER_MANAGEMENT".equals(cat)) {
                        c.setForeground(new Color(37, 99, 235));
                    } else if ("RETURNS_REFUNDS".equals(cat)) {
                        c.setForeground(new Color(217, 119, 6));
                    } else {
                        c.setForeground(new Color(30, 41, 59));
                    }
                }
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(auditTable);
        tableScroll.setBorder(null);

        // Lower Inspector Card
        JPanel inspectorPanel = new JPanel(new BorderLayout(8, 6));
        inspectorPanel.setBackground(new Color(248, 250, 252));
        inspectorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        inspectorPanel.setPreferredSize(new Dimension(800, 110));

        JLabel inspectorTitle = new JLabel("Audit Event Detailed Inspector:");
        inspectorTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inspectorTitle.setForeground(new Color(71, 85, 105));
        inspectorPanel.add(inspectorTitle, BorderLayout.NORTH);

        detailTextArea = new JTextArea("Select any log record above to inspect payload details and metadata.");
        detailTextArea.setEditable(false);
        detailTextArea.setLineWrap(true);
        detailTextArea.setWrapStyleWord(true);
        detailTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailTextArea.setBackground(new Color(248, 250, 252));
        detailTextArea.setBorder(null);

        inspectorPanel.add(new JScrollPane(detailTextArea), BorderLayout.CENTER);

        // Row Selection Listener to update inspector
        auditTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = auditTable.getSelectedRow();
                if (row >= 0) {
                    String id = String.valueOf(tableModel.getValueAt(row, 0));
                    String ts = String.valueOf(tableModel.getValueAt(row, 1));
                    String actor = String.valueOf(tableModel.getValueAt(row, 2));
                    String role = String.valueOf(tableModel.getValueAt(row, 3));
                    String cat = String.valueOf(tableModel.getValueAt(row, 4));
                    String act = String.valueOf(tableModel.getValueAt(row, 5));
                    String det = String.valueOf(tableModel.getValueAt(row, 6));
                    String ip = String.valueOf(tableModel.getValueAt(row, 7));

                    detailTextArea.setText(String.format("Log ID #%s [%s]\nActor: %s (Role: %s)  |  Origin IP: %s\nCategory: %s  |  Action: %s\nDetails: %s",
                            id, ts, actor, role, ip, cat, act, det));
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, inspectorPanel);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);

        centerPanel.add(splitPane, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        // Bottom Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton refreshBtn = new JButton("🔄 Refresh Logs");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> applyFilter());
        footer.add(refreshBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(15, 23, 42));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        // Initial Load
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText();
        String category = (String) categoryCombo.getSelectedItem();

        List<AuditLog> logs = AuditStore.searchLogs(query, category);

        tableModel.setRowCount(0);
        for (AuditLog log : logs) {
            tableModel.addRow(new Object[]{
                    log.getLogId(),
                    log.getTimestamp(),
                    log.getUsername(),
                    log.getRole(),
                    log.getCategory(),
                    log.getAction(),
                    log.getDetails(),
                    log.getIpAddress()
            });
        }

        countBadge.setText(logs.size() + " of " + AuditStore.getLogCount() + " Events");
    }

    private void handleExportCsv() {
        String fileName = "Qveto_Store_Audit_Log_" + System.currentTimeMillis() + ".csv";
        File file = new File(fileName);

        try {
            AuditStore.exportAuditCsv(file);
            ValidationUtil.showSuccess(this,
                    "Audit journal successfully exported to:<br/><b>" + file.getAbsolutePath() + "</b>",
                    "Audit Exported");
        } catch (IOException ex) {
            ValidationUtil.showError(this,
                    "Failed to export audit journal: " + ex.getMessage(),
                    "Export Error");
        }
    }
}
