package com.bank.model;
/**
 * Transaction Model - Represents financial transactions
 */
class Transaction {
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }
    
    private int transactionId;
    private int accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceAccount;
    private Timestamp transactionDate;
    private Integer processedBy;
    private boolean isFlagged;
    private BigDecimal fraudScore;
    
    // Constructors
    public Transaction() {
        this.isFlagged = false;
        this.fraudScore = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { 
        this.transactionType = transactionType; 
    }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReferenceAccount() { return referenceAccount; }
    public void setReferenceAccount(String referenceAccount) { 
        this.referenceAccount = referenceAccount; 
    }
    
    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { 
        this.transactionDate = transactionDate; 
    }
    
    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }
    
    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }
    
    public BigDecimal getFraudScore() { return fraudScore; }
    public void setFraudScore(BigDecimal fraudScore) { this.fraudScore = fraudScore; }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + transactionId +
                ", type=" + transactionType +
                ", amount=" + amount +
                ", date=" + transactionDate +
                '}';
    }
}