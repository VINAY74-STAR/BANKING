package com.bank;

import com.bank.ui.LoginFrame;
import com.bank.util.DatabaseConnection;
import javax.swing.*;

/**
 * Banking Management System - Main Entry Point
 * Comprehensive banking system with AI Finance Manager
 * 
 * @author Banking System Team
 * @version 1.0
 */
public class BankingSystem {
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Test database connection
        if (!testDatabaseConnection()) {
            JOptionPane.showMessageDialog(null,
                "Unable to connect to database.\nPlease check database configuration.",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Launch login screen
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
    
    /**
     * Test database connectivity on startup
     */
    private static boolean testDatabaseConnection() {
        try {
            DatabaseConnection.getConnection();
            System.out.println("Database connection successful!");
            return true;
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}