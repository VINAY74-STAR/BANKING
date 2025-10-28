package com.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Account Model - Represents bank accounts
 */
class Account {
    
    public enum AccountType {
        SAVINGS, CURRENT
    }
    
    public enum AccountStatus {
        ACTIVE, INACTIVE, FROZEN
    }
    
    private int accountId;
    private int customerId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private AccountStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    public Account() {
        this.balance = BigDecimal.ZERO;
        this.interestRate = new BigDecimal("4.00");
        this.status = AccountStatus.ACTIVE;
    }
    
    // Getters and Setters
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", type=" + accountType +
                ", balance=" + balance +
                ", status=" + status +
                '}';
    }
}

