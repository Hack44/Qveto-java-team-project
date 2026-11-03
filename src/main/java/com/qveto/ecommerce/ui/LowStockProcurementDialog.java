package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.dao.ReportDAO.LowStockAlert;
import com.qveto.ecommerce.model.Product;
import com.qveto.ecommerce.util.DBStatusUtil;
import com.qveto.ecommerce.util.ProductStore;
import com.qveto.ecommerce.util.ReportStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog providing real-time low-stock inventory inspection and 1-click procurement restocking.
 * Integrates directly with view_low_stock_alerts and ProductStore.
 *
 * Supports Member 2 (UI, Presentation & Store Management) inventory procurement & warehouse restock requirements.
 */
public class LowStockProcurementDialog extends JDialog {

    private final Runnable onInventoryUpdated;
    private DefaultTableModel tableModel;
    private JTable alertsTable;
    private JLabel totalExposureVal;
    private JLabel outOfStockVal;
    private JLabel totalDeficitVal;
    private JSpinner restockSpinner;
    private List<LowStockAlert> currentAlerts;

    public LowStockProcurementDialog(Frame owner) {
        this(owner, null);
    }

    public LowStockProcurementDialog(Frame owner, Runnable onInventoryUpdated) {
        super(owner, "Inventory Procurement & Low-Stock Restock Hub", true);
        this.onInventoryUpdated = onInventoryUpdated;
        initComponents();
        loadAlertsData();
    }

    private void initComponents() {
        setSize(820, 580);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Inventory Procurement & Restock Hub");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Live SKU exposure alerts and replenishment coordination powered by view_low_stock_alerts (" + DBStatusUtil.getStatusText() + ")");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(203, 213, 225));

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Summary Cards + Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Summary Metric Row
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 14, 0));
        summaryRow.setOpaque(false);

        totalExposureVal = new JLabel("0 SKUs");
        outOfStockVal = new JLabel("0 SKUs");
        totalDeficitVal = new JLabel("0 Units");

        summaryRow.add(createMiniCard("Total Low-Stock Exposure", totalExposureVal, new Color(239, 68, 68)));
        summaryRow.add(createMiniCard("Out of Stock (Zero Units)", outOfStockVal, new Color(185, 28, 28)));
        summaryRow.add(createMiniCard("Total Stock Deficit", totalDeficitVal, new Color(217, 119, 6)));

        centerPanel.add(summaryRow, BorderLayout.NORTH);

        // Alerts Table
        String[] columns = {"Product ID", "Product Name", "Category", "Current Stock", "Threshold", "Stock Status", "Units Deficit"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        alertsTable = new JTable(tableModel);
        alertsTable.setRowHeight(28);
        alertsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alertsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        alertsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        alertsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        alertsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        alertsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        alertsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        alertsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        alertsTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        alertsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        alertsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        alertsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        alertsTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        alertsTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        // Status column badge renderer
        alertsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String statusStr = value != null ? value.toString() : "";
                if ("OUT_OF_STOCK".equalsIgnoreCase(statusStr)) {
                    setForeground(new Color(220, 38, 38));
                    setText("● OUT OF STOCK");
                } else {
                    setForeground(new Color(217, 119, 6));
                    setText("▲ LOW STOCK");
                }
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(alertsTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 18, 12, 18)
        ));

        // Left Controls: Quick Single Restock
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        leftControls.add(new JLabel("Restock Units:"));
        restockSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 500, 5));
        restockSpinner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        restockSpinner.setPreferredSize(new Dimension(65, 28));
        leftControls.add(restockSpinner);

        JButton restockSingleBtn = new JButton("Restock Selected SKU");
        restockSingleBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        restockSingleBtn.setBackground(new Color(37, 99, 235));
        restockSingleBtn.setForeground(Color.WHITE);
        restockSingleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restockSingleBtn.addActionListener(e -> handleRestockSelected());
        leftControls.add(restockSingleBtn);

        JButton batchRestockBtn = new JButton("⚡ Batch Restock All Deficits");
        batchRestockBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        batchRestockBtn.setBackground(new Color(16, 185, 129));
        batchRestockBtn.setForeground(Color.WHITE);
        batchRestockBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        batchRestockBtn.addActionListener(e -> handleBatchRestockAll());
        leftControls.add(batchRestockBtn);

        bottomPanel.add(leftControls, BorderLayout.WEST);

        // Right Controls: Refresh & Close
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton refreshBtn = new JButton("↻ Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadAlertsData());
        rightControls.add(refreshBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        rightControls.add(closeBtn);

        bottomPanel.add(rightControls, BorderLayout.EAST);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel createMiniCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadAlertsData() {
        tableModel.setRowCount(0);
        currentAlerts = ReportStore.getDetailedLowStockAlerts();

        int outOfStockCount = 0;
        int totalDeficit = 0;

        for (LowStockAlert alert : currentAlerts) {
            if (alert.getStockQuantity() == 0) {
                outOfStockCount++;
            }
            totalDeficit += alert.getUnitsBelowThreshold();

            tableModel.addRow(new Object[]{
                    alert.getProductId(),
                    alert.getProductName(),
                    alert.getCategoryName(),
                    alert.getStockQuantity(),
                    alert.getThreshold(),
                    alert.getStatus(),
                    alert.getUnitsBelowThreshold() > 0 ? ("-" + alert.getUnitsBelowThreshold() + " units") : "At Threshold"
            });
        }

        totalExposureVal.setText(currentAlerts.size() + " SKUs");
        outOfStockVal.setText(outOfStockCount + " SKUs");
        totalDeficitVal.setText(totalDeficit + " Units");

        if (currentAlerts.isEmpty()) {
            ValidationUtil.showSuccess(this, "Excellent! All catalog products are currently above their low-stock thresholds.", "Inventory Healthy");
        }
    }

    private void handleRestockSelected() {
        int selectedRow = alertsTable.getSelectedRow();
        if (selectedRow < 0) {
            ValidationUtil.showWarning(this, "Please select an SKU row from the table to restock.", "Selection Required");
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);
        int restockUnits = (int) restockSpinner.getValue();

        boolean success = ProductStore.adjustStock(productId, restockUnits);
        if (success) {
            Product updated = ProductStore.findById(productId);
            int newStock = updated != null ? updated.getStockQuantity() : restockUnits;

            ValidationUtil.showSuccess(this,
                    "Successfully restocked <b>" + productName + "</b> with +" + restockUnits + " units.<br>Current Available Stock: <b>" + newStock + "</b>",
                    "Restock Confirmed");

            loadAlertsData();
            if (onInventoryUpdated != null) {
                onInventoryUpdated.run();
            }
        } else {
            ValidationUtil.showError(this, "Failed to restock product #" + productId + ". Check database connection.", "Restock Failed");
        }
    }

    private void handleBatchRestockAll() {
        if (currentAlerts == null || currentAlerts.isEmpty()) {
            ValidationUtil.showSuccess(this, "No low-stock products require replenishment at this time.", "Inventory Clear");
            return;
        }

        int restockDefault = (int) restockSpinner.getValue();
        int totalProducts = currentAlerts.size();
        int totalUnits = totalProducts * restockDefault;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html>Procure and restock all <b>" + totalProducts + "</b> low-stock SKUs with <b>" + restockDefault + " units</b> each?<br>"
                        + "Total inventory procurement volume: <b>" + totalUnits + " units</b>.</html>",
                "Confirm Batch Procurement",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int successCount = 0;
        for (LowStockAlert alert : currentAlerts) {
            boolean ok = ProductStore.adjustStock(alert.getProductId(), restockDefault);
            if (ok) {
                successCount++;
            }
        }

        ValidationUtil.showSuccess(this,
                "Batch procurement completed!<br>Successfully replenished <b>" + successCount + " of " + totalProducts + "</b> SKUs.",
                "Procurement Successful");

        loadAlertsData();
        if (onInventoryUpdated != null) {
            onInventoryUpdated.run();
        }
    }
}
