package com.bank.ai;

import com.bank.model.Transaction;
import com.bank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI Finance Manager
 * Analyzes spending patterns, detects fraud, provides recommendations
 * Uses rule-based AI and statistical analysis
 */
public class AIFinanceManager {
    
    private static final double FRAUD_THRESHOLD = 3.0; // Standard deviations
    private static final int ANALYSIS_DAYS = 90;
    
    /**
     * Analyze customer spending patterns and generate insights
     */
    public static void analyzeAndGenerateInsights(int customerId) throws SQLException {
        // Get transaction history
        List<TransactionData> transactions = getRecentTransactions(customerId, ANALYSIS_DAYS);
        
        if (transactions.isEmpty()) {
            return;
        }
        
        // Analyze spending patterns
        Map<String, CategorySpending> categorySpending = categorizeSpending(transactions);
        
        // Generate insights
        generateSpendingPatternInsights(customerId, categorySpending);
        generateBudgetRecommendations(customerId, categorySpending, transactions);
        generateSavingTips(customerId, transactions);
        detectFraudulentTransactions(customerId, transactions);
        
        // Analyze loans and generate EMI optimization
        generateEMIOptimization(customerId);
    }
    
    /**
     * Categorize transactions and calculate spending per category
     */
    private static Map<String, CategorySpending> categorizeSpending(
            List<TransactionData> transactions) {
        Map<String, CategorySpending> categories = new HashMap<>();
        
        for (TransactionData trans : transactions) {
            String category = categorizeTransaction(trans);
            
            categories.putIfAbsent(category, new CategorySpending(category));
            CategorySpending spending = categories.get(category);
            spending.addTransaction(trans.amount);
        }
        
        return categories;
    }
    
    /**
     * Categorize individual transaction using rule-based AI
     */
    private static String categorizeTransaction(TransactionData trans) {
        String desc = trans.description.toLowerCase();
        
        if (desc.contains("atm") || desc.contains("cash")) return "Cash Withdrawal";
        if (desc.contains("grocery") || desc.contains("supermarket")) return "Groceries";
        if (desc.contains("restaurant") || desc.contains("food")) return "Dining";
        if (desc.contains("fuel") || desc.contains("petrol")) return "Transportation";
        if (desc.contains("electricity") || desc.contains("water") || desc.contains("bill")) 
            return "Utilities";
        if (desc.contains("medical") || desc.contains("pharmacy")) return "Healthcare";
        if (desc.contains("entertainment") || desc.contains("movie")) return "Entertainment";
        if (desc.contains("shopping") || desc.contains("mall")) return "Shopping";
        if (trans.transactionType.equals("TRANSFER_OUT")) return "Transfers";
        if (trans.transactionType.equals("WITHDRAWAL")) return "Withdrawals";
        
        return "Others";
    }
    
    /**
     * Generate spending pattern insights
     */
    private static void generateSpendingPatternInsights(int customerId,
            Map<String, CategorySpending> categories) throws SQLException {
        
        BigDecimal totalSpent = BigDecimal.ZERO;
        String topCategory = "";
        BigDecimal maxSpent = BigDecimal.ZERO;
        
        for (CategorySpending spending : categories.values()) {
            totalSpent = totalSpent.add(spending.totalSpent);
            if (spending.totalSpent.compareTo(maxSpent) > 0) {
                maxSpent = spending.totalSpent;
                topCategory = spending.category;
            }
        }
        
        String title = "Spending Analysis - Last 90 Days";
        StringBuilder description = new StringBuilder();
        description.append(String.format("Total Spending: ₹%.2f\n", totalSpent));
        description.append(String.format("Top Category: %s (₹%.2f)\n\n", topCategory, maxSpent));
        description.append("Category Breakdown:\n");
        
        // Sort by spending amount
        List<CategorySpending> sorted = new ArrayList<>(categories.values());
        sorted.sort((a, b) -> b.totalSpent.compareTo(a.totalSpent));
        
        for (CategorySpending spending : sorted) {
            double percentage = spending.totalSpent.divide(totalSpent, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            description.append(String.format("  %s: ₹%.2f (%.1f%%)\n", 
                    spending.category, spending.totalSpent, percentage));
        }
        
        saveInsight(customerId, "SPENDING_PATTERN", title, description.toString(), "MEDIUM");
    }
    
    /**
     * Generate budget recommendations
     */
    private static void generateBudgetRecommendations(int customerId,
            Map<String, CategorySpending> categories, 
            List<TransactionData> transactions) throws SQLException {
        
        BigDecimal totalIncome = calculateTotalIncome(transactions);
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        for (CategorySpending spending : categories.values()) {
            totalExpenses = totalExpenses.add(spending.totalSpent);
        }
        
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return; // No income data
        }
        
        BigDecimal savingsRate = totalIncome.subtract(totalExpenses)
                .divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        String title = "Budget Recommendation";
        StringBuilder description = new StringBuilder();
        
        if (savingsRate.compareTo(new BigDecimal("20")) < 0) {
            description.append("⚠️ Your savings rate is below recommended 20%\n\n");
            description.append(String.format("Current Savings Rate: %.1f%%\n", savingsRate));
            description.append("Recommended Actions:\n");
            
            // Find categories to reduce
            List<CategorySpending> sorted = new ArrayList<>(categories.values());
            sorted.sort((a, b) -> b.totalSpent.compareTo(a.totalSpent));
            
            for (int i = 0; i < Math.min(3, sorted.size()); i++) {
                CategorySpending spending = sorted.get(i);
                BigDecimal reduction = spending.totalSpent.multiply(new BigDecimal("0.15"));
                description.append(String.format("  • Reduce %s by 15%% (Save ₹%.2f)\n", 
                        spending.category, reduction));
            }
            
            saveInsight(customerId, "BUDGET_RECOMMENDATION", title, 
                    description.toString(), "HIGH");
        } else {
            description.append("✅ Great job! Your savings rate is healthy\n\n");
            description.append(String.format("Current Savings Rate: %.1f%%\n", savingsRate));
            description.append("Keep up the good financial discipline!");
            
            saveInsight(customerId, "BUDGET_RECOMMENDATION", title, 
                    description.toString(), "LOW");
        }
    }
    
    /**
     * Generate personalized saving tips
     */
    private static void generateSavingTips(int customerId, 
            List<TransactionData> transactions) throws SQLException {
        
        Map<String, CategorySpending> categories = categorizeSpending(transactions);
        List<String> tips = new ArrayList<>();
        
        // Analyze dining expenses
        CategorySpending dining = categories.get("Dining");
        if (dining != null && dining.totalSpent.compareTo(new BigDecimal("5000")) > 0) {
            tips.add(String.format("💡 You spent ₹%.2f on dining out. " +
                    "Cooking at home 2 extra times/week could save ₹%.2f monthly.", 
                    dining.totalSpent, dining.totalSpent.multiply(new BigDecimal("0.25"))));
        }
        
        // Analyze entertainment
        CategorySpending entertainment = categories.get("Entertainment");
        if (entertainment != null && entertainment.totalSpent.compareTo(new BigDecimal("3000")) > 0) {
            tips.add(String.format("💡 Entertainment expenses are ₹%.2f. " +
                    "Consider subscription services instead of individual purchases.", 
                    entertainment.totalSpent));
        }
        
        // Analyze cash withdrawals
        CategorySpending cash = categories.get("Cash Withdrawal");
        if (cash != null && cash.transactionCount > 10) {
            tips.add(String.format("💡 You made %d ATM withdrawals. " +
                    "Plan withdrawals to avoid multiple ATM fees.", 
                    cash.transactionCount));
        }
        
        // Check for impulse spending pattern
        int weekendTransactions = countWeekendTransactions(transactions);
        if (weekendTransactions > transactions.size() * 0.4) {
            tips.add("💡 40% of your transactions occur on weekends. " +
                    "Create a weekend budget to avoid impulse spending.");
        }
        
        if (!tips.isEmpty()) {
            String title = "Personalized Saving Tips";
            StringBuilder description = new StringBuilder();
            description.append("Based on your spending patterns:\n\n");
            for (String tip : tips) {
                description.append(tip).append("\n\n");
            }
            
            saveInsight(customerId, "SAVING_TIP", title, description.toString(), "MEDIUM");
        }
    }
    
    /**
     * Detect potentially fraudulent transactions using statistical analysis
     */
    private static void detectFraudulentTransactions(int customerId,
            List<TransactionData> transactions) throws SQLException {
        
        if (transactions.size() < 10) {
            return; // Need sufficient data
        }
        
        // Calculate statistics
        double mean = 0;
        for (TransactionData trans : transactions) {
            mean += trans.amount.doubleValue();
        }
        mean /= transactions.size();
        
        double variance = 0;
        for (TransactionData trans : transactions) {
            double diff = trans.amount.doubleValue() - mean;
            variance += diff * diff;
        }
        variance /= transactions.size();
        double stdDev = Math.sqrt(variance);
        
        // Flag unusual transactions
        List<TransactionData> suspicious = new ArrayList<>();
        for (TransactionData trans : transactions) {
            double zScore = Math.abs((trans.amount.doubleValue() - mean) / stdDev);
            if (zScore > FRAUD_THRESHOLD) {
                suspicious.add(trans);
                flagTransaction(trans.transactionId, BigDecimal.valueOf(zScore));
            }
        }
        
        if (!suspicious.isEmpty()) {
            String title = "⚠️ Unusual Transaction Alert";
            StringBuilder description = new StringBuilder();
            description.append("We detected unusual transactions:\n\n");
            
            for (TransactionData trans : suspicious) {
                description.append(String.format("• ₹%.2f on %s - %s\n", 
                        trans.amount, trans.date.toString(), trans.description));
            }
            
            description.append("\nIf you didn't make these transactions, please contact us immediately.");
            
            saveInsight(customerId, "FRAUD_ALERT", title, description.toString(), "HIGH");
        }
    }
    
    /**
     * Generate EMI optimization suggestions
     */
    private static void generateEMIOptimization(int customerId) throws SQLException {
        String query = "SELECT l.loan_id, l.loan_type, l.principal_amount, l.interest_rate, " +
                      "l.tenure_months, l.emi_amount, l.outstanding_amount " +
                      "FROM loans l WHERE l.customer_id = ? AND l.status = 'ACTIVE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            List<String> recommendations = new ArrayList<>();
            
            while (rs.next()) {
                String loanType = rs.getString("loan_type");
                BigDecimal emi = rs.getBigDecimal("emi_amount");
                BigDecimal outstanding = rs.getBigDecimal("outstanding_amount");
                int tenure = rs.getInt("tenure_months");
                
                // Calculate interest saved by prepayment
                BigDecimal totalPayable = emi.multiply(new BigDecimal(tenure));
                BigDecimal totalInterest = totalPayable.subtract(outstanding);
                BigDecimal prepaymentSaving = totalInterest.multiply(new BigDecimal("0.30"));
                
                if (outstanding.compareTo(new BigDecimal("50000")) > 0) {
                    recommendations.add(String.format(
                            "%s Loan:\n" +
                            "  Current EMI: ₹%.2f for %d months\n" +
                            "  Outstanding: ₹%.2f\n" +
                            "  💡 Consider prepaying ₹10,000 to save ≈₹%.2f in interest\n",
                            loanType, emi, tenure, outstanding, prepaymentSaving));
                }
            }
            
            if (!recommendations.isEmpty()) {
                String title = "EMI Optimization Tips";
                StringBuilder description = new StringBuilder();
                description.append("Optimize your loan repayment:\n\n");
                for (String rec : recommendations) {
                    description.append(rec).append("\n");
                }
                
                saveInsight(customerId, "EMI_OPTIMIZATION", title, 
                        description.toString(), "MEDIUM");
            }
        }
    }
    
    /**
     * Get recent transactions for analysis
     */
    private static List<TransactionData> getRecentTransactions(int customerId, int days) 
            throws SQLException {
        List<TransactionData> transactions = new ArrayList<>();
        
        String query = "SELECT t.transaction_id, t.transaction_type, t.amount, " +
                      "t.description, t.transaction_date " +
                      "FROM transactions t " +
                      "JOIN accounts a ON t.account_id = a.account_id " +
                      "WHERE a.customer_id = ? " +
                      "AND t.transaction_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                      "AND t.transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT') " +
                      "ORDER BY t.transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            stmt.setInt(2, days);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                TransactionData data = new TransactionData();
                data.transactionId = rs.getInt("transaction_id");
                data.transactionType = rs.getString("transaction_type");
                data.amount = rs.getBigDecimal("amount");
                data.description = rs.getString("description");
                data.date = rs.getTimestamp("transaction_date");
                transactions.add(data);
            }
        }
        
        return transactions;
    }
    
    /**
     * Calculate total income from deposits
     */
    private static BigDecimal calculateTotalIncome(List<TransactionData> transactions) {
        // This is simplified - would need to query DEPOSIT transactions
        return new BigDecimal("50000"); // Placeholder
    }
    
    /**
     * Count weekend transactions
     */
    private static int countWeekendTransactions(List<TransactionData> transactions) {
        int count = 0;
        Calendar cal = Calendar.getInstance();
        for (TransactionData trans : transactions) {
            cal.setTime(trans.date);
            int day = cal.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Save AI-generated insight to database
     */
    private static void saveInsight(int customerId, String type, String title, 
            String description, String priority) throws SQLException {
        String query = "INSERT INTO ai_finance_insights (customer_id, insight_type, title, " +
                      "description, priority) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, type);
            stmt.setString(3, title);
            stmt.setString(4, description);
            stmt.setString(5, priority);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Flag suspicious transaction
     */
    private static void flagTransaction(int transactionId, BigDecimal fraudScore) 
            throws SQLException {
        String query = "UPDATE transactions SET is_flagged = TRUE, fraud_score = ? " +
                      "WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setBigDecimal(1, fraudScore);
            stmt.setInt(2, transactionId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get all insights for customer
     */
    public static List<FinanceInsight> getCustomerInsights(int customerId) throws SQLException {
        List<FinanceInsight> insights = new ArrayList<>();
        
        String query = "SELECT * FROM ai_finance_insights WHERE customer_id = ? " +
                      "ORDER BY generated_at DESC LIMIT 20";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FinanceInsight insight = new FinanceInsight();
                insight.insightId = rs.getInt("insight_id");
                insight.insightType = rs.getString("insight_type");
                insight.title = rs.getString("title");
                insight.description = rs.getString("description");
                insight.priority = rs.getString("priority");
                insight.isRead = rs.getBoolean("is_read");
                insight.generatedAt = rs.getTimestamp("generated_at");
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    /**
     * Mark insight as read
     */
    public static void markInsightAsRead(int insightId) throws SQLException {
        String query = "UPDATE ai_finance_insights SET is_read = TRUE WHERE insight_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, insightId);
            stmt.executeUpdate();
        }
    }
    
    // Inner classes for data structures
    static class TransactionData {
        int transactionId;
        String transactionType;
        BigDecimal amount;
        String description;
        Timestamp date;
    }
    
    static class CategorySpending {
        String category;
        BigDecimal totalSpent = BigDecimal.ZERO;
        int transactionCount = 0;
        
        CategorySpending(String category) {
            this.category = category;
        }
        
        void addTransaction(BigDecimal amount) {
            totalSpent = totalSpent.add(amount);
            transactionCount++;
        }
    }
    
    public static class FinanceInsight {
        public int insightId;
        public String insightType;
        public String title;
        public String description;
        public String priority;
        public boolean isRead;
        public Timestamp generatedAt;
    }
}