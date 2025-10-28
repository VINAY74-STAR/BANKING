package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.DatabaseConnection;
import com.bank.util.SecurityUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Account Service
 * Handles account operations: create, deposit, withdraw, transfer
 */
public class AccountService {
    
    /**
     * Create new account for customer
     */
    public static Account createAccount(int customerId, Account.AccountType accountType) 
            throws SQLException {
        String accountNumber = SecurityUtils.generateAccountNumber(customerId);
        
        String query = "INSERT INTO accounts (customer_id, account_number, account_type, " +
                      "balance, interest_rate, status) VALUES (?, ?, ?, 0, 4.00, 'ACTIVE')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, accountNumber);
            stmt.setString(3, accountType.name());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getInt(1));
                account.setCustomerId(customerId);
                account.setAccountNumber(accountNumber);
                account.setAccountType(accountType);
                account.setStatus(Account.AccountStatus.ACTIVE);
                
                AuditService.logAction(AuthenticationService.getCurrentUser().getUserId(),
                                     "CREATE_ACCOUNT", "ACCOUNT", account.getAccountId(),
                                     "New " + accountType + " account created");
                
                return account;
            }
        }
        throw new SQLException("Failed to create account");
    }
    
    /**
     * Get all accounts for a customer
     */
    public static List<Account> getCustomerAccounts(int customerId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }
    
    /**
     * Get account by account number
     */
    public static Account getAccountByNumber(String accountNumber) throws SQLException {
        String query = "SELECT * FROM accounts WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        }
        return null;
    }
    
    /**
     * Deposit money into account
     */
    public static synchronized boolean deposit(String accountNumber, BigDecimal amount, 
                                               String description) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Lock and update account
            String updateQuery = "UPDATE accounts SET balance = balance + ? " +
                               "WHERE account_number = ? AND status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setBigDecimal(1, amount);
                stmt.setString(2, accountNumber);
                int updated = stmt.executeUpdate();
                
                if (updated == 0) {
                    throw new SQLException("Account not found or inactive");
                }
            }
            
            // Get updated balance
            BigDecimal newBalance = getBalance(accountNumber, conn);
            
            // Record transaction
            String transQuery = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                              "balance_after, description, processed_by) " +
                              "SELECT account_id, 'DEPOSIT', ?, ?, ?, ? FROM accounts " +
                              "WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(transQuery)) {
                stmt.setBigDecimal(1, amount);
                stmt.setBigDecimal(2, newBalance);
                stmt.setString(3, description);
                stmt.setInt(4, AuthenticationService.getCurrentUser().getUserId());
                stmt.setString(5, accountNumber);
                stmt.executeUpdate();
            }
            
            conn.commit();
            AuditService.logAction(AuthenticationService.getCurrentUser().getUserId(),
                                 "DEPOSIT", "TRANSACTION", 0,
                                 "Deposited " + amount + " to " + accountNumber);
            return true;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }
    
    /**
     * Withdraw money from account
     */
    public static synchronized boolean withdraw(String accountNumber, BigDecimal amount,
                                                String description) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Check balance
            BigDecimal currentBalance = getBalance(accountNumber, conn);
            if (currentBalance.compareTo(amount) < 0) {
                throw new SQLException("Insufficient balance");
            }
            
            // Update account
            String updateQuery = "UPDATE accounts SET balance = balance - ? " +
                               "WHERE account_number = ? AND status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setBigDecimal(1, amount);
                stmt.setString(2, accountNumber);
                stmt.executeUpdate();
            }
            
            BigDecimal newBalance = currentBalance.subtract(amount);
            
            // Record transaction
            String transQuery = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                              "balance_after, description, processed_by) " +
                              "SELECT account_id, 'WITHDRAWAL', ?, ?, ?, ? FROM accounts " +
                              "WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(transQuery)) {
                stmt.setBigDecimal(1, amount);
                stmt.setBigDecimal(2, newBalance);
                stmt.setString(3, description);
                stmt.setInt(4, AuthenticationService.getCurrentUser().getUserId());
                stmt.setString(5, accountNumber);
                stmt.executeUpdate();
            }
            
            conn.commit();
            AuditService.logAction(AuthenticationService.getCurrentUser().getUserId(),
                                 "WITHDRAWAL", "TRANSACTION", 0,
                                 "Withdrew " + amount + " from " + accountNumber);
            return true;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }
    
    /**
     * Transfer money between accounts
     */
    public static synchronized boolean transfer(String fromAccount, String toAccount,
                                               BigDecimal amount, String description) 
            throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Call stored procedure
            String call = "{CALL sp_transfer_funds(?, ?, ?, ?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(call)) {
                stmt.setString(1, fromAccount);
                stmt.setString(2, toAccount);
                stmt.setBigDecimal(3, amount);
                stmt.setInt(4, AuthenticationService.getCurrentUser().getUserId());
                stmt.registerOutParameter(5, Types.VARCHAR);
                
                stmt.execute();
                String result = stmt.getString(5);
                
                if (result.startsWith("SUCCESS")) {
                    AuditService.logAction(AuthenticationService.getCurrentUser().getUserId(),
                                         "TRANSFER", "TRANSACTION", 0,
                                         "Transferred " + amount + " from " + fromAccount + 
                                         " to " + toAccount);
                    return true;
                } else {
                    throw new SQLException(result);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }
    
    /**
     * Get account balance
     */
    private static BigDecimal getBalance(String accountNumber, Connection conn) 
            throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("balance");
            }
        }
        throw new SQLException("Account not found");
    }
    
    /**
     * Map ResultSet to Account object
     */
    private static Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setCustomerId(rs.getInt("customer_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountType(Account.AccountType.valueOf(rs.getString("account_type")));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setInterestRate(rs.getBigDecimal("interest_rate"));
        account.setStatus(Account.AccountStatus.valueOf(rs.getString("status")));
        account.setCreatedAt(rs.getTimestamp("created_at"));
        account.setUpdatedAt(rs.getTimestamp("updated_at"));
        return account;
    }
}