package com.bank.ui.customer;

import com.bank.ai.AIFinanceManager;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.AuthenticationService;
import com.bank.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * Customer Dashboard
 * Main interface for customer operations including AI Finance Manager
 */
public class CustomerDashboard extends JFrame {
    
    private final User currentUser;
    private int customerId;
    private final JTabbedPane tabbedPane;
    private final JLabel balanceLabel;
    private final DefaultTableModel accountTableModel;
    private final DefaultTableModel transactionTableModel;
    private final JPanel aiInsightsPanel;
    
    public CustomerDashboard(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        this.currentUser = user;
        this.tabbedPane = new JTabbedPane();
        this.balanceLabel = new JLabel("₹0.00", SwingConstants.CENTER);
        this.accountTableModel = createAccountTableModel();
        this.transactionTableModel = createTransactionTableModel();
        this.aiInsightsPanel = new JPanel();
        
        fetchCustomerId();
        initComponents();
        loadData();
        generateAIInsights();
        
        // Set frame properties
        setTitle("Customer Dashboard - " + user.getUsername());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private DefaultTableModel createAccountTableModel() {
        String[] columns = {"Account Number", "Type", "Balance", "Status", "Created"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private DefaultTableModel createTransactionTableModel() {
        String[] columns = {"Date", "Type", "Amount", "Balance After", "Description"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void fetchCustomerId() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT customer_id FROM customers WHERE user_id = ?")) {
            
            stmt.setInt(1, currentUser.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                } else {
                    throw new SQLException("Customer ID not found for user: " + currentUser.getUserId());
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error fetching customer data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            dispose(); // Close the window if we can't get essential data
        }
    }
    
    private void initComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Accounts", createAccountsPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Transfer Money", createTransferPanel());
        tabbedPane.addTab("Loans", createLoansPanel());
        tabbedPane.addTab("AI Finance Manager", createAIPanel());
        
        add(tabbedPane);
        
        // Menu bar
        createMenuBar();
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(25, 118, 210));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel, BorderLayout.WEST);
        
        panel.add(welcomePanel, BorderLayout.NORTH);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Total balance card
        balanceLabel = new JLabel("₹0.00", SwingConstants.CENTER);
        JPanel balanceCard = createInfoCard("Total Balance", balanceLabel, 
                new Color(76, 175, 80));
        summaryPanel.add(balanceCard);
        
        // Accounts count card
        JLabel accountsLabel = new JLabel("0", SwingConstants.CENTER);
        JPanel accountsCard = createInfoCard("Total Accounts", accountsLabel,
                new Color(33, 150, 243));
        summaryPanel.add(accountsCard);
        
        // Loans count card
        JLabel loansLabel = new JLabel("0", SwingConstants.CENTER);
        JPanel loansCard = createInfoCard("Active Loans", loansLabel,
                new Color(255, 152, 0));
        summaryPanel.add(loansCard);
        
        panel.add(summaryPanel, BorderLayout.CENTER);
        
        // Quick actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton newAccountBtn = new JButton("Open New Account");
        JButton transferBtn = new JButton("Transfer Money");
        JButton loanBtn = new JButton("Apply for Loan");
        JButton aiBtn = new JButton("View AI Insights");
        
        styleButton(newAccountBtn, new Color(25, 118, 210));
        styleButton(transferBtn, new Color(76, 175, 80));
        styleButton(loanBtn, new Color(255, 152, 0));
        styleButton(aiBtn, new Color(156, 39, 176));
        
        newAccountBtn.addActionListener(e -> openNewAccount());
        transferBtn.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        loanBtn.addActionListener(e -> tabbedPane.setSelectedIndex(4));
        aiBtn.addActionListener(e -> tabbedPane.setSelectedIndex(5));
        
        actionsPanel.add(newAccountBtn);
        actionsPanel.add(transferBtn);
        actionsPanel.add(loanBtn);
        actionsPanel.add(aiBtn);
        
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Accounts table
        String[] columns = {"Account Number", "Type", "Balance", "Status", "Created"};
        accountTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(accountTableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton newAccountBtn = new JButton("Open New Account");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        
        refreshBtn.addActionListener(e -> loadAccountsData());
        newAccountBtn.addActionListener(e -> openNewAccount());
        depositBtn.addActionListener(e -> performDeposit(table));
        withdrawBtn.addActionListener(e -> performWithdrawal(table));
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(newAccountBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Transactions table
        String[] columns = {"Date", "Type", "Amount", "Balance After", "Description"};
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(transactionTableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton generateStatementBtn = new JButton("Generate PDF Statement");
        
        refreshBtn.addActionListener(e -> loadTransactionsData());
        generateStatementBtn.addActionListener(e -> generateStatement());
        
        filterPanel.add(refreshBtn);
        filterPanel.add(generateStatementBtn);
        
        panel.add(filterPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Transfer Money");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        JTextField fromAccountField = new JTextField(20);
        JTextField toAccountField = new JTextField(20);
        JTextField amountField = new JTextField(20);
        JTextField descriptionField = new JTextField(20);
        
        int row = 1;
        addTransferField(panel, gbc, row++, "From Account:", fromAccountField);
        addTransferField(panel, gbc, row++, "To Account:", toAccountField);
        addTransferField(panel, gbc, row++, "Amount:", amountField);
        addTransferField(panel, gbc, row++, "Description:", descriptionField);
        
        JButton transferBtn = new JButton("Transfer");
        styleButton(transferBtn, new Color(76, 175, 80));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(transferBtn, gbc);
        
        transferBtn.addActionListener(e -> {
            try {
                String from = fromAccountField.getText().trim();
                String to = toAccountField.getText().trim();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                String desc = descriptionField.getText().trim();
                
                boolean success = AccountService.transfer(from, to, amount, desc);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Transfer successful!");
                    fromAccountField.setText("");
                    toAccountField.setText("");
                    amountField.setText("");
                    descriptionField.setText("");
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Transfer failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }
    
    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Loan Management");
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.NORTH);
        
        // Loans table
        String[] columns = {"Loan ID", "Type", "Principal", "EMI", "Outstanding", "Status"};
        DefaultTableModel loanModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(loanModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Button to apply for loan
        JButton applyBtn = new JButton("Apply for New Loan");
        applyBtn.addActionListener(e -> showLoanApplication());
        panel.add(applyBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAIPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("🤖 AI Finance Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(156, 39, 176));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(titleLabel);
        
        JButton refreshBtn = new JButton("Refresh Insights");
        refreshBtn.addActionListener(e -> {
            generateAIInsights();
            loadAIInsights();
        });
        titlePanel.add(refreshBtn);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Insights panel
        aiInsightsPanel = new JPanel();
        aiInsightsPanel.setLayout(new BoxLayout(aiInsightsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(aiInsightsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void generateAIInsights() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                AIFinanceManager.analyzeAndGenerateInsights(customerId);
                return null;
            }
            
            @Override
            protected void done() {
                loadAIInsights();
            }
        };
        worker.execute();
    }
    
    private void loadAIInsights() {
        SwingWorker<List<AIFinanceManager.FinanceInsight>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AIFinanceManager.FinanceInsight> doInBackground() throws Exception {
                return AIFinanceManager.getCustomerInsights(customerId);
            }
            
            @Override
            protected void done() {
                try {
                    List<AIFinanceManager.FinanceInsight> insights = get();
                    aiInsightsPanel.removeAll();
                    
                    if (insights.isEmpty()) {
                        JLabel noInsights = new JLabel("No insights available. Complete some transactions first.");
                        noInsights.setFont(new Font("Arial", Font.ITALIC, 14));
                        aiInsightsPanel.add(noInsights);
                    } else {
                        for (AIFinanceManager.FinanceInsight insight : insights) {
                            aiInsightsPanel.add(createInsightCard(insight));
                            aiInsightsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                        }
                    }
                    
                    aiInsightsPanel.revalidate();
                    aiInsightsPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private JPanel createInsightCard(AIFinanceManager.FinanceInsight insight) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getPriorityColor(insight.priority), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(insight.title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(titleLabel, BorderLayout.NORTH);
        
        JTextArea descArea = new JTextArea(insight.description);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setBackground(Color.WHITE);
        card.add(descArea, BorderLayout.CENTER);
        
        JLabel dateLabel = new JLabel(insight.generatedAt.toString());
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        dateLabel.setForeground(Color.GRAY);
        card.add(dateLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private Color getPriorityColor(String priority) {
        switch (priority) {
            case "HIGH": return new Color(244, 67, 54);
            case "MEDIUM": return new Color(255, 152, 0);
            default: return new Color(76, 175, 80);
        }
    }
    
    private void loadData() {
        loadAccountsData();
        loadTransactionsData();
        updateDashboardSummary();
    }
    
    private void loadAccountsData() {
        SwingWorker<List<Account>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                return AccountService.getCustomerAccounts(customerId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Account> accounts = get();
                    accountTableModel.setRowCount(0);
                    
                    for (Account account : accounts) {
                        accountTableModel.addRow(new Object[]{
                                account.getAccountNumber(),
                                account.getAccountType(),
                                "₹" + account.getBalance(),
                                account.getStatus(),
                                account.getCreatedAt()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void loadTransactionsData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String query = "SELECT t.* FROM transactions t " +
                              "JOIN accounts a ON t.account_id = a.account_id " +
                              "WHERE a.customer_id = ? ORDER BY t.transaction_date DESC LIMIT 100";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();
                    
                    transactionTableModel.setRowCount(0);
                    while (rs.next()) {
                        transactionTableModel.addRow(new Object[]{
                                rs.getTimestamp("transaction_date"),
                                rs.getString("transaction_type"),
                                "₹" + rs.getBigDecimal("amount"),
                                "₹" + rs.getBigDecimal("balance_after"),
                                rs.getString("description")
                        });
                    }
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void updateDashboardSummary() {
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                String query = "SELECT SUM(balance) FROM accounts WHERE customer_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getBigDecimal(1);
                    }
                }
                return BigDecimal.ZERO;
            }
            
            @Override
            protected void done() {
                try {
                    BigDecimal total = get();
                    balanceLabel.setText(String.format("₹%.2f", total));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void openNewAccount() {
        String[] types = {"SAVINGS", "CURRENT"};
        String type = (String) JOptionPane.showInputDialog(this,
                "Select account type:",
                "Open New Account",
                JOptionPane.QUESTION_MESSAGE,
                null,
                types,
                types[0]);
        
        if (type != null) {
            try {
                Account account = AccountService.createAccount(customerId,
                        Account.AccountType.valueOf(type));
                JOptionPane.showMessageDialog(this,
                        "Account created successfully!\nAccount Number: " + account.getAccountNumber(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAccountsData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error creating account: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void performDeposit(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account");
            return;
        }
        
        String accountNumber = (String) table.getValueAt(row, 0);
        String amountStr = JOptionPane.showInputDialog(this, "Enter deposit amount:");
        
        if (amountStr != null && !amountStr.trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                AccountService.deposit(accountNumber, amount, "Cash Deposit");
                JOptionPane.showMessageDialog(this, "Deposit successful!");
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void performWithdrawal(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account");
            return;
        }
        
        String accountNumber = (String) table.getValueAt(row, 0);
        String amountStr = JOptionPane.showInputDialog(this, "Enter withdrawal amount:");
        
        if (amountStr != null && !amountStr.trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                AccountService.withdraw(accountNumber, amount, "Cash Withdrawal");
                JOptionPane.showMessageDialog(this, "Withdrawal successful!");
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void generateStatement() {
        JOptionPane.showMessageDialog(this,
                "PDF generation feature requires iText library.\nPlease add iText dependency.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showLoanApplication() {
        JOptionPane.showMessageDialog(this, "Loan application feature - to be implemented");
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        JMenuItem logoutItem = new JMenuItem("Logout");
        
        refreshItem.addActionListener(e -> loadData());
        logoutItem.addActionListener(e -> {
            AuthenticationService.logout();
            dispose();
            new com.bank.ui.LoginFrame().setVisible(true);
        });
        
        fileMenu.add(refreshItem);
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JPanel createInfoCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }
    
    private void addTransferField(JPanel panel, GridBagConstraints gbc, int row,
                                 String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    @Override
    public void dispose() {
        // Cleanup resources
        DatabaseConnection.closeConnection();
        super.dispose();
    }
}
