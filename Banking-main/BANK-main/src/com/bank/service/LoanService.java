package com.bank.service;

import com.bank.model.Loan;
import com.bank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Loan Service - Handles loan operations
 */
class LoanService {
    
    /**
     * Apply for a new loan
     */
    public static Loan applyForLoan(int customerId, Loan.LoanType loanType,
                                   BigDecimal principalAmount, BigDecimal interestRate,
                                   int tenureMonths) throws SQLException {
        
        Loan loan = new Loan();
        loan.setCustomerId(customerId);
        loan.setLoanType(loanType);
        loan.setPrincipalAmount(principalAmount);
        loan.setInterestRate(interestRate);
        loan.setTenureMonths(tenureMonths);
        loan.calculateEMI();
        loan.setStatus(Loan.LoanStatus.PENDING);
        
        String query = "INSERT INTO loans (customer_id, loan_type, principal_amount, " +
                      "interest_rate, tenure_months, emi_amount, outstanding_amount, status) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, loanType.name());
            stmt.setBigDecimal(3, principalAmount);
            stmt.setBigDecimal(4, interestRate);
            stmt.setInt(5, tenureMonths);
            stmt.setBigDecimal(6, loan.getEmiAmount());
            stmt.setBigDecimal(7, principalAmount);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                loan.setLoanId(rs.getInt(1));
                
                AuditService.logAction(AuthenticationService.getCurrentUser().getUserId(),
                                     "LOAN_APPLICATION", "LOAN", loan.getLoanId(),
                                     "Applied for " + loanType + " loan of " + principalAmount);
            }
        }
        
        return loan;
    }
    
    /**
     * Get all loans for a customer
     */
    public static List<Loan> getCustomerLoans(int customerId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE customer_id = ? ORDER BY application_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
        }
        
        return loans;
    }
    
    /**
     * Get pending loan applications (for employees)
     */
    public static List<Loan> getPendingLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT l.*, c.first_name, c.last_name FROM loans l " +
                      "JOIN customers c ON l.customer_id = c.customer_id " +
                      "WHERE l.status = 'PENDING' ORDER BY l.application_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
        }
        
        return loans;
    }
    
    /**
     * Approve or reject loan
     */
    public static boolean processLoan(int loanId, boolean approve) throws SQLException {
        String status = approve ? "APPROVED" : "REJECTED";
        String query = "UPDATE loans SET status = ?, approval_date = CURRENT_TIMESTAMP, " +
                      "approved_by = ? WHERE loan_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, AuthenticationService.getCurrentUser().getUserId());
            stmt.setInt(3, loanId);
            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                AuditService.logAction(AuthenticationService.getCurrentUser().getUserId(),
                                     "LOAN_" + status, "LOAN", loanId,
                                     "Loan " + (approve ? "approved" : "rejected"));
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Loan object
     */
    private static Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(rs.getInt("loan_id"));
        loan.setCustomerId(rs.getInt("customer_id"));
        loan.setLoanType(Loan.LoanType.valueOf(rs.getString("loan_type")));
        loan.setPrincipalAmount(rs.getBigDecimal("principal_amount"));
        loan.setInterestRate(rs.getBigDecimal("interest_rate"));
        loan.setTenureMonths(rs.getInt("tenure_months"));
        loan.setEmiAmount(rs.getBigDecimal("emi_amount"));
        loan.setOutstandingAmount(rs.getBigDecimal("outstanding_amount"));
        loan.setStatus(Loan.LoanStatus.valueOf(rs.getString("status")));
        loan.setApplicationDate(rs.getTimestamp("application_date"));
        loan.setApprovalDate(rs.getTimestamp("approval_date"));
        return loan;
    }
}