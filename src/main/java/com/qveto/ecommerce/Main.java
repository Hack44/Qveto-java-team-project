package com.qveto.ecommerce;

import com.qveto.ecommerce.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main Application Launcher for Project 1: E-commerce & Inventory Management System.
 * 
 * Configures the native desktop Look & Feel and displays the LoginFrame.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system native look and feel for modern window decoration and controls
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Note: Using default Swing Look and Feel (" + e.getMessage() + ")");
            }

            // Launch the authentication UI
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
