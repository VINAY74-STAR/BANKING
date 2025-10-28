package com.bank.model;
/**
 * Loan Model - Represents customer loans
 */
class Loan {
    
    public enum LoanType {
        PERSONAL, HOME, EDUCATION, CAR
    }
    
    public enum LoanStatus {
        PENDING, APPROVED, REJECTED, ACTIVE, CLOSED
    }
    
    private int loanId;
    private int customerId;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private int tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal outstandingAmount;
    private LoanStatus status;
    private Timestamp applicationDate;
    private Timestamp approvalDate;
    private Integer approvedBy;
    
    // Constructors
    public Transaction() {}
    
    // Getters and Setters
    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }
    
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { 
        this.principalAmount = principalAmount; 
    }
    
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }
    
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { 
        this.outstandingAmount = outstandingAmount; 
    }
    
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    
    public Timestamp getApplicationDate() { return applicationDate; }
    public void setApplicationDate(Timestamp applicationDate) { 
        this.applicationDate = applicationDate; 
    }
    
    public Timestamp getApprovalDate() { return approvalDate; }
    public void setApprovalDate(Timestamp approvalDate) { this.approvalDate = approvalDate; }
    
    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }
    
    /**
     * Calculate EMI using formula: P * r * (1+r)^n / ((1+r)^n - 1)
     */
    public void calculateEMI() {
        double p = principalAmount.doubleValue();
        double r = interestRate.doubleValue() / (12 * 100); // Monthly interest rate
        int n = tenureMonths;
        
        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        this.emiAmount = BigDecimal.valueOf(Math.round(emi * 100.0) / 100.0);
        this.outstandingAmount = principalAmount;
    }
    
    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", type=" + loanType +
                ", principal=" + principalAmount +
                ", emi=" + emiAmount +
                ", status=" + status +
                '}';
    }
}