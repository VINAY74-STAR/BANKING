package com.bank.service;

import com.bank.model.User;
import com.bank.util.DatabaseConnection;
import com.bank.util.SecurityUtils;

import java.sql.*;

/**
 * Authentication Service
 * Handles user login, registration, and session management
 */
public class AuthenticationService {
    
    private static User currentUser = null;
    
    /**
     * Authenticate user credentials
     */
    public static User login(String username, String password) throws SQLException {
        String query = "SELECT user_id, username, password_hash, role, is_active " +
                      "FROM users WHERE username = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                
                if (SecurityUtils.verifyPassword(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(User.Role.valueOf(rs.getString("role")));
                    user.setActive(rs.getBoolean("is_active"));
                    
                    // Update last login
                    updateLastLogin(user.getUserId());
                    
                    // Log audit
                    AuditService.logAction(user.getUserId(), "LOGIN", "USER", 
                                         user.getUserId(), "User logged in successfully");
                    
                    currentUser = user;
                    return user;
                }
            }
        }
        return null;
    }
    
    /**
     * Register new customer
     */
    public static boolean registerCustomer(String username, String password, 
                                          String firstName, String lastName,
                                          String email, String phone, String address,
                                          Date dateOfBirth, String aadhar, String pan) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Check if username exists
            if (usernameExists(username)) {
                return false;
            }
            
            // Insert user
            String userQuery = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'CUSTOMER')";
            int userId;
            try (PreparedStatement stmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, SecurityUtils.hashPassword(password));
                stmt.executeUpdate();
                
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get user ID");
                }
            }
            
            // Insert customer
            String customerQuery = "INSERT INTO customers (user_id, first_name, last_name, email, " +
                                  "phone, address, date_of_birth, aadhar_number, pan_number) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(customerQuery)) {
                stmt.setInt(1, userId);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, email);
                stmt.setString(5, phone);
                stmt.setString(6, address);
                stmt.setDate(7, dateOfBirth);
                stmt.setString(8, SecurityUtils.encrypt(aadhar));
                stmt.setString(9, SecurityUtils.encrypt(pan));
                stmt.executeUpdate();
            }
            
            conn.commit();
            AuditService.logAction(userId, "REGISTER", "USER", userId, "New customer registered");
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Check if username already exists
     */
    private static boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Update last login timestamp
     */
    private static void updateLastLogin(int userId) throws SQLException {
        String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Logout current user
     */
    public static void logout() {
        if (currentUser != null) {
            try {
                AuditService.logAction(currentUser.getUserId(), "LOGOUT", "USER", 
                                     currentUser.getUserId(), "User logged out");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            currentUser = null;
        }
    }
    
    /**
     * Get current logged-in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}