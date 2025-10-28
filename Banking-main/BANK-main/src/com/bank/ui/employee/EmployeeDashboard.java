package com.bank.ui.employee;

import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.AuthenticationService;
import com.bank.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

/**
 * Employee Dashboard
 * Interface for bank staff to manage transactions and loans
 */
public class EmployeeDashboard extends JFrame {
    
    private User currentUser;
    private JTabbedPane tabbedPane;
    private DefaultTableModel customerTableModel;
    private DefaultTableModel loanTableModel;
    
    public EmployeeDashboard(User user) {
        this.currentUser = user;
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setTitle("Employee Dashboard - " + currentUser.getUsername());
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Customer Accounts", createCustomerPanel());
        tabbedPane.addTab("Transactions", createTransactionPanel());
        tabbedPane.addTab("Loan Approval", createLoanPanel());
        
        add(tabbedPane);
        createMenuBar();
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(0, 150, 136));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Employee Dashboard - " + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        panel.add(welcomePanel, BorderLayout.NORTH);
        
        // Statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        statsPanel.add(createStatCard("Total Customers", "0", new Color(33, 150, 243)));
        statsPanel.add(createStatCard("Pending Loans", "0", new Color(255, 152, 0)));
        statsPanel.add(createStatCard("Today's Transactions", "0", new Color(76, 175, 80)));
        statsPanel.add(createStatCard("Flagged Transactions", "0", new Color(244, 67, 54)));
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Customer table
        String[] columns = {"Customer ID", "Name", "Email", "Phone", "Total Balance"};
        customerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(customerTableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton viewAccountsBtn = new JButton("View Customer Accounts");
        
        refreshBtn.addActionListener(e -> loadCustomerData());
        viewAccountsBtn.addActionListener(e -> viewCustomerAccounts(table));
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewAccountsBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Process Transactions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Transaction form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField accountField = new JTextField(20);
        JTextField amountField = new JTextField(20);
        JTextField descField = new JTextField(20);
        
        int row = 0;
        addFormField(formPanel, gbc, row++, "Account Number:", accountField);
        addFormField(formPanel, gbc, row++, "Amount:", amountField);
        addFormField(formPanel, gbc, row++, "Description:", descField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        
        depositBtn.setBackground(new Color(76, 175, 80));
        depositBtn.setForeground(Color.WHITE);
        withdrawBtn.setBackground(new Color(244, 67, 54));
        withdrawBtn.setForeground(Color.WHITE);
        
        depositBtn.addActionListener(e -> processTransaction(accountField, amountField, 
                descField, true));
        withdrawBtn.addActionListener(e -> processTransaction(accountField, amountField,
                descField, false));
        
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLoanPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Pending Loan Applications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Loan table
        String[] columns = {"Loan ID", "Customer", "Type", "Amount", "Tenure", "EMI", "Status"};
        loanTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(loanTableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        
        approveBtn.setBackground(new Color(76, 175, 80));
        approveBtn.setForeground(Color.WHITE);
        rejectBtn.setBackground(new Color(244, 67, 54));
        rejectBtn.setForeground(Color.WHITE);
        
        refreshBtn.addActionListener(e -> loadLoanData());
        approveBtn.addActionListener(e -> processLoan(table, true));
        rejectBtn.addActionListener(e -> processLoan(table, false));
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadData() {
        loadCustomerData();
        loadLoanData();
    }
    
    private void loadCustomerData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String query = "SELECT c.customer_id, CONCAT(c.first_name, ' ', c.last_name) as name, " +
                              "c.email, c.phone, COALESCE(SUM(a.balance), 0) as total_balance " +
                              "FROM customers c LEFT JOIN accounts a ON c.customer_id = a.customer_id " +
                              "GROUP BY c.customer_id ORDER BY c.customer_id";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    
                    customerTableModel.setRowCount(0);
                    while (rs.next()) {
                        customerTableModel.addRow(new Object[]{
                                rs.getInt("customer_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                "₹" + rs.getBigDecimal("total_balance")
                        });
                    }
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void loadLoanData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String query = "SELECT l.loan_id, CONCAT(c.first_name, ' ', c.last_name) as customer_name, " +
                              "l.loan_type, l.principal_amount, l.tenure_months, l.emi_amount, l.status " +
                              "FROM loans l JOIN customers c ON l.customer_id = c.customer_id " +
                              "WHERE l.status = 'PENDING' ORDER BY l.application_date";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    
                    loanTableModel.setRowCount(0);
                    while (rs.next()) {
                        loanTableModel.addRow(new Object[]{
                                rs.getInt("loan_id"),
                                rs.getString("customer_name"),
                                rs.getString("loan_type"),
                                "₹" + rs.getBigDecimal("principal_amount"),
                                rs.getInt("tenure_months") + " months",
                                "₹" + rs.getBigDecimal("emi_amount"),
                                rs.getString("status")
                        });
                    }
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void processTransaction(JTextField accountField, JTextField amountField,
                                   JTextField descField, boolean isDeposit) {
        try {
            String accountNumber = accountField.getText().trim();
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            String description = descField.getText().trim();
            
            if (isDeposit) {
                AccountService.deposit(accountNumber, amount, description);
                JOptionPane.showMessageDialog(this, "Deposit successful!");
            } else {
                AccountService.withdraw(accountNumber, amount, description);
                JOptionPane.showMessageDialog(this, "Withdrawal successful!");
            }
            
            accountField.setText("");
            amountField.setText("");
            descField.setText("");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Transaction failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void processLoan(JTable table, boolean approve) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan application");
            return;
        }
        
        int loanId = (int) table.getValueAt(row, 0);
        
        try {
            // Use LoanService to process loan
            String query = "UPDATE loans SET status = ?, approval_date = CURRENT_TIMESTAMP, " +
                          "approved_by = ? WHERE loan_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, approve ? "APPROVED" : "REJECTED");
                stmt.setInt(2, currentUser.getUserId());
                stmt.setInt(3, loanId);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this,
                        "Loan " + (approve ? "approved" : "rejected") + " successfully!");
                loadLoanData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error processing loan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewCustomerAccounts(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer");
            return;
        }
        
        int customerId = (int) table.getValueAt(row, 0);
        String customerName = (String) table.getValueAt(row, 1);
        
        // Show accounts in a dialog
        JDialog dialog = new JDialog(this, "Accounts for " + customerName, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        String[] columns = {"Account Number", "Type", "Balance", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable accountTable = new JTable(model);
        
        try {
            String query = "SELECT * FROM accounts WHERE customer_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            "₹" + rs.getBigDecimal("balance"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        dialog.add(new JScrollPane(accountTable));
        dialog.setVisible(true);
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row,
                             String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
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
}