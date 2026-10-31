package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Category;
import com.qveto.ecommerce.util.CategoryStore;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog enabling Administrators to manage product categories:
 * - Inspect existing categories and active product count per category
 * - Create new categories with duplicate-name validation
 * - Rename existing categories
 * - Safely delete categories (safeguarded against deleting categories with active products)
 * 
 * Supports Member 2 (UI, Login & User Management) Admin Inventory requirements.
 */
public class CategoryManagementDialog extends JDialog {

    private DefaultTableModel tableModel;
    private JTable categoryTable;
    private final Runnable onCategoriesChanged;

    public CategoryManagementDialog(Frame owner) {
        this(owner, null);
    }

    public CategoryManagementDialog(Frame owner, Runnable onCategoriesChanged) {
        super(owner, "Category Management — Qveto Admin", true);
        this.onCategoriesChanged = onCategoriesChanged;
        initComponents();
    }

    private void initComponents() {
        setSize(520, 480);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Product Category Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Organize and classify store catalog");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 215, 235));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Content: Table & Toolbar
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolBar.setBackground(Color.WHITE);

        JButton addBtn = new JButton("+ Add Category");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(16, 185, 129));
        addBtn.setForeground(Color.WHITE);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> handleAddCategory());
        toolBar.add(addBtn);

        JButton renameBtn = new JButton("Rename");
        renameBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        renameBtn.addActionListener(e -> handleRenameCategory());
        toolBar.add(renameBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> handleDeleteCategory());
        toolBar.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> refreshTable());
        toolBar.add(refreshBtn);

        centerPanel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Category Name", "Active Products", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(28);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        categoryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        categoryTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        refreshTable();
        centerPanel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        footerPanel.add(closeBtn);

        rootPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Category> categories = CategoryStore.getAllCategoriesWithCounts();
        for (Category c : categories) {
            String status = c.getProductCount() > 0 ? "In Use (" + c.getProductCount() + ")" : "Empty";
            tableModel.addRow(new Object[]{
                    "#" + c.getCategoryId(),
                    c.getCategoryName(),
                    c.getProductCount() + " items",
                    status
            });
        }
    }

    private void handleAddCategory() {
        String newName = JOptionPane.showInputDialog(
                this,
                "Enter new category name:",
                "Add Category",
                JOptionPane.PLAIN_MESSAGE
        );

        if (newName == null) return;
        newName = newName.trim();

        if (newName.isEmpty()) {
            ValidationUtil.showError(this, "Category name cannot be blank.", "Validation Error");
            return;
        }

        if (CategoryStore.existsByName(newName)) {
            ValidationUtil.showError(this, "A category named '<b>" + newName + "</b>' already exists.", "Duplicate Category");
            return;
        }

        Category created = CategoryStore.addCategory(newName);
        if (created != null) {
            refreshTable();
            if (onCategoriesChanged != null) onCategoriesChanged.run();
            ValidationUtil.showSuccess(this, "Category '<b>" + newName + "</b>' added successfully!", "Category Created");
        } else {
            ValidationUtil.showError(this, "Failed to create category.", "Operation Error");
        }
    }

    private void handleRenameCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            ValidationUtil.showWarning(this, "Please select a category from the table first.", "Notice");
            return;
        }

        String idStr = (String) tableModel.getValueAt(selectedRow, 0);
        int catId = Integer.parseInt(idStr.replace("#", ""));
        String oldName = (String) tableModel.getValueAt(selectedRow, 1);

        String newName = (String) JOptionPane.showInputDialog(
                this,
                "Enter new name for category '" + oldName + "':",
                "Rename Category",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                oldName
        );

        if (newName == null) return;
        newName = newName.trim();

        if (newName.isEmpty()) {
            ValidationUtil.showError(this, "Category name cannot be blank.", "Validation Error");
            return;
        }

        if (!newName.equalsIgnoreCase(oldName) && CategoryStore.existsByName(newName)) {
            ValidationUtil.showError(this, "A category named '<b>" + newName + "</b>' already exists.", "Duplicate Category");
            return;
        }

        boolean ok = CategoryStore.updateCategory(catId, newName);
        if (ok) {
            refreshTable();
            if (onCategoriesChanged != null) onCategoriesChanged.run();
            ValidationUtil.showSuccess(this, "Category renamed from '<b>" + oldName + "</b>' to '<b>" + newName + "</b>'.", "Category Renamed");
        } else {
            ValidationUtil.showError(this, "Failed to rename category.", "Operation Error");
        }
    }

    private void handleDeleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            ValidationUtil.showWarning(this, "Please select a category to delete.", "Notice");
            return;
        }

        String idStr = (String) tableModel.getValueAt(selectedRow, 0);
        int catId = Integer.parseInt(idStr.replace("#", ""));
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String countStr = (String) tableModel.getValueAt(selectedRow, 2);

        int productCount = 0;
        try {
            productCount = Integer.parseInt(countStr.split(" ")[0]);
        } catch (Exception ignored) {}

        if (productCount > 0) {
            ValidationUtil.showError(this,
                    "Cannot delete category '<b>" + name + "</b>' because <b>" + productCount + "</b> product(s) are currently assigned to it.<br/>"
                            + "Please reassign or delete those products before removing this category.",
                    "Category In Use");
            return;
        }

        boolean confirm = ValidationUtil.showConfirm(this,
                "Are you sure you want to delete category '<b>" + name + "</b>'?",
                "Confirm Deletion");

        if (confirm) {
            boolean deleted = CategoryStore.deleteCategory(catId);
            if (deleted) {
                refreshTable();
                if (onCategoriesChanged != null) onCategoriesChanged.run();
                ValidationUtil.showSuccess(this, "Category '<b>" + name + "</b>' has been deleted.", "Category Deleted");
            } else {
                ValidationUtil.showError(this, "Failed to delete category.", "Operation Error");
            }
        }
    }
}
