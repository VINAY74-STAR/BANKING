package com.bank.service;

import com.bank.model.Loan;
import com.bank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit Service - Logs all system actions
 */
public class AuditService {
    
    public static void logAction(int userId, String action, String entityType,
                                int entityId, String details) throws SQLException {
        String query = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, entityType);
            stmt.setInt(4, entityId);
            stmt.setString(5, details);
            stmt.executeUpdate();
        }
    }
    
    public static List<AuditLog> getRecentLogs(int limit) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT a.*, u.username FROM audit_logs a " +
                      "JOIN users u ON a.user_id = u.user_id " +
                      "ORDER BY a.timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.logId = rs.getInt("log_id");
                log.username = rs.getString("username");
                log.action = rs.getString("action");
                log.entityType = rs.getString("entity_type");
                log.details = rs.getString("details");
                log.timestamp = rs.getTimestamp("timestamp");
                logs.add(log);
            }
        }
        
        return logs;
    }
    
    public static class AuditLog {
        public int logId;
        public String username;
        public String action;
        public String entityType;
        public String details;
        public Timestamp timestamp;
    }
}