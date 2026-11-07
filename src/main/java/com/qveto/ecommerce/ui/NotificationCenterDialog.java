package com.qveto.ecommerce.ui;

import com.qveto.ecommerce.model.Notification;
import com.qveto.ecommerce.model.User;
import com.qveto.ecommerce.util.NotificationStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog presenting an interactive notification center for both customers and administrators.
 * Displays order tracking milestones, inventory alerts, promotional announcements, and return updates.
 *
 * Supports Member 2 (UI, Presentation & Store Management) in-app communication requirements.
 */
public class NotificationCenterDialog extends JDialog {

    private final User currentUser;
    private final Runnable onDismiss;
    private JPanel listContainer;
    private JLabel unreadBadge;

    public NotificationCenterDialog(Frame owner, User currentUser, Runnable onDismiss) {
        super(owner, "Notification Center — Qveto Store Broadcasts", true);
        this.currentUser = currentUser;
        this.onDismiss = onDismiss;

        initComponents();
    }

    private void initComponents() {
        setSize(560, 580);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Store Notifications & Activity Hub");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Real-time order delivery updates, inventory reorders, and promotional alerts");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(203, 213, 225));

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        root.add(headerPanel, BorderLayout.NORTH);

        // Center Content & Control Bar
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Control Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(248, 250, 252));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(10, 16, 10, 16)
        ));

        unreadBadge = new JLabel("0 unread alerts");
        unreadBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        unreadBadge.setForeground(new Color(30, 41, 59));
        toolbar.add(unreadBadge, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton markAllBtn = new JButton("✓ Mark All as Read");
        markAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        markAllBtn.setBackground(Color.WHITE);
        markAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markAllBtn.addActionListener(e -> {
            NotificationStore.markAllAsRead(currentUser.getUserId(), currentUser.getRole());
            reloadNotifications();
        });

        JButton clearBtn = new JButton("🗑 Clear All");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> {
            NotificationStore.clearNotifications(currentUser.getUserId(), currentUser.getRole());
            reloadNotifications();
        });

        btnRow.add(markAllBtn);
        btnRow.add(clearBtn);
        toolbar.add(btnRow, BorderLayout.EAST);
        centerPanel.add(toolbar, BorderLayout.NORTH);

        // Notification list scrollable area
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        listContainer.setBorder(new EmptyBorder(10, 14, 10, 14));

        reloadNotifications();

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        // Footer Action Panel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> {
            if (onDismiss != null) onDismiss.run();
            dispose();
        });
        footer.add(closeBtn);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void reloadNotifications() {
        listContainer.removeAll();
        int userId = currentUser.getUserId();
        String role = currentUser.getRole();

        List<Notification> list = NotificationStore.getNotificationsForUser(userId, role);
        int unreadCount = NotificationStore.getUnreadCount(userId, role);

        unreadBadge.setText(unreadCount + " unread alert" + (unreadCount == 1 ? "" : "s"));

        if (list.isEmpty()) {
            JPanel emptyBox = new JPanel();
            emptyBox.setBackground(Color.WHITE);
            emptyBox.setBorder(new EmptyBorder(50, 20, 50, 20));
            JLabel emptyLabel = new JLabel("No active notifications at this time.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(new Color(148, 163, 184));
            emptyBox.add(emptyLabel);
            listContainer.add(emptyBox);
        } else {
            for (Notification n : list) {
                listContainer.add(createNotificationCard(n));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNotificationCard(Notification n) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(n.isRead() ? new Color(250, 250, 250) : new Color(240, 249, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(n.isRead() ? new Color(226, 232, 240) : new Color(186, 230, 253), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        // Card Top Row: Type Icon, Title, Timestamp, Read/Unread Status
        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftHeader.setOpaque(false);

        JLabel iconLabel = new JLabel(n.getTypeIcon());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));

        JLabel titleLabel = new JLabel(n.getTitle());
        titleLabel.setFont(new Font("Segoe UI", n.isRead() ? Font.PLAIN : Font.BOLD, 13));
        titleLabel.setForeground(new Color(15, 23, 42));

        leftHeader.add(iconLabel);
        leftHeader.add(titleLabel);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightHeader.setOpaque(false);

        JLabel timeLabel = new JLabel(n.getTimestamp());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(100, 116, 139));

        if (!n.isRead()) {
            JLabel unreadDot = new JLabel("● New");
            unreadDot.setFont(new Font("Segoe UI", Font.BOLD, 10));
            unreadDot.setForeground(new Color(2, 132, 199));
            rightHeader.add(unreadDot);
        }
        rightHeader.add(timeLabel);

        topRow.add(leftHeader, BorderLayout.WEST);
        topRow.add(rightHeader, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Body Message
        JLabel msgLabel = new JLabel("<html><body style='width: 440px;'>" + n.getMessage() + "</body></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        msgLabel.setForeground(new Color(51, 65, 85));
        msgLabel.setBorder(new EmptyBorder(4, 26, 0, 0));
        card.add(msgLabel, BorderLayout.CENTER);

        // Click to toggle read
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                n.setRead(!n.isRead());
                reloadNotifications();
            }
        });

        return card;
    }
}
