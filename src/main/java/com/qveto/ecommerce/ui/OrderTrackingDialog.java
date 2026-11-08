package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Order;
import com.qveto.ecommerce.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Shipment Tracking & Logistics Timeline Dialog.
 * Displays courier consignment details, AWB tracking numbers, milestone progress,
 * and time-stamped transit checkpoint history.
 *
 * Supports Member 2 (UI, Presentation & Store Management) shipment tracking requirements.
 */
public class OrderTrackingDialog extends JDialog {

    private final Order order;
    private final String deliveryAddress;
    private final String awbTrackingNumber;
    private final String courierCarrier;

    public OrderTrackingDialog(Frame owner, Order order, String deliveryAddress) {
        super(owner, "Shipment Tracking — " + order.getOrderNumber(), true);
        this.order = order;
        this.deliveryAddress = (deliveryAddress != null && !deliveryAddress.trim().isEmpty())
                ? deliveryAddress : order.getShippingAddress();

        // Deterministic AWB and Courier Carrier generation for consistent tracking
        int idHash = Math.abs(order.getOrderNumber().hashCode() % 900000) + 100000;
        this.awbTrackingNumber = "BD-EXP-" + idHash;
        this.courierCarrier = (order.getOrderId() % 2 == 0) ? "BlueDart Express Priority" : "FedEx Air Logistics";

        initComponents();
    }

    private void initComponents() {
        setSize(680, 640);
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

        JLabel titleLabel = new JLabel("Consignment Tracking & Live Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Carrier: " + courierCarrier + "  |  AWB: " + awbTrackingNumber);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(254, 240, 138));

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Status Badge in Header
        JLabel badge = createStatusBadge(order.getOrderStatus());
        headerPanel.add(badge, BorderLayout.EAST);

        root.add(headerPanel, BorderLayout.NORTH);

        // Scrollable Body
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Milestone Stepper Card
        bodyPanel.add(createProgressCard());
        bodyPanel.add(Box.createVerticalStrut(14));

        // Shipment Overview Grid
        bodyPanel.add(createOverviewGrid());
        bodyPanel.add(Box.createVerticalStrut(14));

        // Checkpoint Activity Timeline
        JLabel historyTitle = new JLabel("Logistics Transit Checkpoints & Scans:");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTitle.setForeground(new Color(30, 41, 59));
        historyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(historyTitle);
        bodyPanel.add(Box.createVerticalStrut(8));

        bodyPanel.add(createTimelineList());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        root.add(scrollPane, BorderLayout.CENTER);

        // Footer Action Controls
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton copyAwbBtn = new JButton("📋 Copy Tracking #");
        copyAwbBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyAwbBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyAwbBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(awbTrackingNumber), null);
            ValidationUtil.showSuccess(this, "Tracking # <b>" + awbTrackingNumber + "</b> copied to clipboard!", "Copied");
        });
        footer.add(copyAwbBtn);

        JButton pingBtn = new JButton("🔄 Ping Courier Gateway");
        pingBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pingBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pingBtn.addActionListener(e -> {
            ValidationUtil.showSuccess(this,
                    "Consignment <b>" + awbTrackingNumber + "</b> pinged successfully!<br/>Current GPS Hub: In transit via Regional Sorting Hub.",
                    "Live Courier Sync");
        });
        footer.add(pingBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(15, 23, 42));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JLabel createStatusBadge(String status) {
        String cleanStatus = status != null ? status.toUpperCase() : "PENDING";
        JLabel badge = new JLabel("  " + cleanStatus + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setOpaque(true);

        switch (cleanStatus) {
            case "DELIVERED":
                badge.setBackground(new Color(22, 163, 74));
                badge.setForeground(Color.WHITE);
                break;
            case "SHIPPED":
                badge.setBackground(new Color(37, 99, 235));
                badge.setForeground(Color.WHITE);
                break;
            case "CONFIRMED":
                badge.setBackground(new Color(79, 70, 229));
                badge.setForeground(Color.WHITE);
                break;
            case "RETURN_REQUESTED":
            case "REFUNDED":
                badge.setBackground(new Color(217, 119, 6));
                badge.setForeground(Color.WHITE);
                break;
            case "CANCELLED":
                badge.setBackground(new Color(220, 38, 38));
                badge.setForeground(Color.WHITE);
                break;
            default: // PENDING
                badge.setBackground(new Color(234, 179, 8));
                badge.setForeground(Color.BLACK);
                break;
        }

        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return badge;
    }

    private JPanel createProgressCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] stages = {"Order Placed", "Confirmed", "Shipped & Dispatched", "Delivered"};
        int activeStage = getStageIndex(order.getOrderStatus());

        JPanel stepsPanel = new JPanel(new GridBagLayout());
        stepsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        for (int i = 0; i < stages.length; i++) {
            boolean isCompleted = (i <= activeStage) && !"CANCELLED".equals(order.getOrderStatus());
            boolean isCurrent = (i == activeStage) && !"CANCELLED".equals(order.getOrderStatus());

            // Node Circle
            JLabel circle = new JLabel(isCompleted ? "✔" : String.valueOf(i + 1), SwingConstants.CENTER);
            circle.setFont(new Font("Segoe UI", Font.BOLD, 11));
            circle.setOpaque(true);
            circle.setPreferredSize(new Dimension(28, 28));

            if (isCurrent) {
                circle.setBackground(new Color(37, 99, 235));
                circle.setForeground(Color.WHITE);
            } else if (isCompleted) {
                circle.setBackground(new Color(22, 163, 74));
                circle.setForeground(Color.WHITE);
            } else {
                circle.setBackground(new Color(226, 232, 240));
                circle.setForeground(new Color(100, 116, 139));
            }

            JLabel label = new JLabel(stages[i], SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", isCurrent ? Font.BOLD : Font.PLAIN, 11));
            label.setForeground(isCurrent ? new Color(15, 23, 42) : new Color(100, 116, 139));

            JPanel nodeBlock = new JPanel();
            nodeBlock.setLayout(new BoxLayout(nodeBlock, BoxLayout.Y_AXIS));
            nodeBlock.setOpaque(false);
            circle.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            nodeBlock.add(circle);
            nodeBlock.add(Box.createVerticalStrut(4));
            nodeBlock.add(label);

            gbc.gridx = i * 2;
            gbc.weightx = 0;
            stepsPanel.add(nodeBlock, gbc);

            if (i < stages.length - 1) {
                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setPreferredSize(new Dimension(40, 4));
                sep.setForeground(i < activeStage && !"CANCELLED".equals(order.getOrderStatus())
                        ? new Color(22, 163, 74) : new Color(226, 232, 240));
                gbc.gridx = i * 2 + 1;
                gbc.weightx = 1.0;
                stepsPanel.add(sep, gbc);
            }
        }

        card.add(stepsPanel, BorderLayout.CENTER);
        return card;
    }

    private int getStageIndex(String status) {
        if (status == null) return 0;
        switch (status.toUpperCase()) {
            case "CONFIRMED": return 1;
            case "SHIPPED": return 2;
            case "DELIVERED":
            case "RETURN_REQUESTED":
            case "REFUNDED": return 3;
            default: return 0; // PENDING
        }
    }

    private JPanel createOverviewGrid() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 8));
        grid.setBackground(new Color(248, 250, 252));
        grid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        String estDelivery = calculateEstimatedDelivery();

        grid.add(createField("Order Number:", order.getOrderNumber()));
        grid.add(createField("Estimated Delivery:", "<b>" + estDelivery + "</b>"));
        grid.add(createField("Courier Partner:", courierCarrier));
        grid.add(createField("Airway Bill (AWB):", "<font color='#1e40af'><b>" + awbTrackingNumber + "</b></font>"));
        grid.add(createField("Payment Option:", order.getPaymentMethod()));
        grid.add(createField("Shipping Destination:", (deliveryAddress != null ? deliveryAddress : "Standard Customer Address")));

        return grid;
    }

    private JLabel createField(String key, String val) {
        JLabel label = new JLabel("<html><b>" + key + "</b> " + val + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(51, 65, 85));
        return label;
    }

    private String calculateEstimatedDelivery() {
        try {
            LocalDate date = LocalDate.parse(order.getOrderDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.plusDays(3).format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (Exception e) {
            return "Within 2-3 Business Days";
        }
    }

    private JPanel createTimelineList() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Color.WHITE);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Checkpoint> checkpoints = generateCheckpoints();

        for (Checkpoint cp : checkpoints) {
            JPanel card = new JPanel(new BorderLayout(10, 4));
            card.setBackground(new Color(248, 250, 252));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    new EmptyBorder(10, 12, 10, 12)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
            left.setOpaque(false);

            JLabel title = new JLabel(cp.title);
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            title.setForeground(new Color(30, 41, 59));

            JLabel loc = new JLabel("📍 " + cp.location + "  •  " + cp.timestamp);
            loc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            loc.setForeground(new Color(100, 116, 139));

            left.add(title);
            left.add(loc);

            JLabel badge = new JLabel(cp.statusTag);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(cp.tagColor);

            card.add(left, BorderLayout.CENTER);
            card.add(badge, BorderLayout.EAST);

            list.add(card);
            list.add(Box.createVerticalStrut(6));
        }

        return list;
    }

    private List<Checkpoint> generateCheckpoints() {
        List<Checkpoint> list = new ArrayList<>();
        String status = order.getOrderStatus() != null ? order.getOrderStatus().toUpperCase() : "PENDING";
        String date = order.getOrderDate();

        // 1. Order Placed Checkpoint
        list.add(new Checkpoint("Order Placed & Payment Verified",
                "Qveto Central Gateway, Electronic City",
                date + " 10:15 AM",
                "COMPLETED",
                new Color(22, 163, 74)));

        if ("CANCELLED".equals(status)) {
            list.add(new Checkpoint("Order Cancelled by Customer",
                    "Customer Portal — Reversed Inventory Reserved",
                    date + " 11:30 AM",
                    "CANCELLED",
                    new Color(220, 38, 38)));
            return list;
        }

        // 2. Confirmed & Packed
        list.add(new Checkpoint("Package Picked & Packed into Eco-Box",
                "Qveto Mega Fulfillment Center, Bengaluru",
                date + " 02:40 PM",
                "PROCESSED",
                new Color(22, 163, 74)));

        if ("PENDING".equals(status)) {
            list.add(new Checkpoint("Awaiting Carrier Logistics Pickup",
                    "Bengaluru Outbound Bay 4",
                    "Next Scheduled Window",
                    "IN QUEUE",
                    new Color(217, 119, 6)));
            return list;
        }

        // 3. Shipped & In Transit
        list.add(new Checkpoint("Consignment Dispatched with Courier",
                courierCarrier + " — Kempegowda Air Freight Center",
                date + " 07:15 PM",
                "IN TRANSIT",
                new Color(37, 99, 235)));

        list.add(new Checkpoint("Arrived at Regional Destination Hub",
                "Destination Express Distribution Facility",
                "Following Day, 06:30 AM",
                "HUB ARRIVAL",
                new Color(37, 99, 235)));

        if ("SHIPPED".equals(status)) {
            list.add(new Checkpoint("Dispatched for Destination Delivery Area",
                    "Local Route Delivery Van #14",
                    "Today, 08:45 AM",
                    "OUT FOR DELIVERY",
                    new Color(217, 119, 6)));
            return list;
        }

        // 4. Delivered
        list.add(new Checkpoint("Consignment Handed Over to Recipient",
                (deliveryAddress != null ? deliveryAddress : "Destination Customer Address"),
                "Following Day, 01:20 PM",
                "DELIVERED",
                new Color(22, 163, 74)));

        if ("RETURN_REQUESTED".equals(status)) {
            list.add(new Checkpoint("Return & Replacement Ticket Logged",
                    "Customer Service & Reverse Logistics Bay",
                    "Recent Window",
                    "RETURN INITIATED",
                    new Color(217, 119, 6)));
        } else if ("REFUNDED".equals(status)) {
            list.add(new Checkpoint("Reverse Logistics Inspected & Refund Settle",
                    "Qveto Automated Gateway & Bank Settlement",
                    "Recent Window",
                    "REFUND CREDITED",
                    new Color(22, 163, 74)));
        }

        return list;
    }

    private static class Checkpoint {
        String title;
        String location;
        String timestamp;
        String statusTag;
        Color tagColor;

        Checkpoint(String title, String location, String timestamp, String statusTag, Color tagColor) {
            this.title = title;
            this.location = location;
            this.timestamp = timestamp;
            this.statusTag = statusTag;
            this.tagColor = tagColor;
        }
    }
}
