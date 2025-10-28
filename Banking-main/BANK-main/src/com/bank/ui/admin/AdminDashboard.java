package com.bank.ui.admin;

// Java core imports
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Swing imports
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.JFormattedTextField;

// Date picker imports
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.UtilDateModel;

// Application imports
import com.bank.model.User;
import com.bank.service.AuditService;
import com.bank.service.AuthenticationService;
import com.bank.util.DatabaseConnection;
import com.bank.util.SecurityUtils;

/**
 * Admin Dashboard
 * System administration and monitoring interface
 */
public class AdminDashboard extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 700;
    
    // Database queries
    private static final String QUERY_ALL_USERS = "SELECT * FROM users";
    private static final String QUERY_ALL_EMPLOYEES = "SELECT * FROM employees";
    private static final String QUERY_AUDIT_LOGS = "SELECT * FROM audit_logs ORDER BY timestamp DESC";
    
    // UI components
    private final User currentUser;
    private final JTabbedPane tabbedPane;
    private final DefaultTableModel userTableModel;
    private final DefaultTableModel auditTableModel;
    
    public AdminDashboard(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        this.currentUser = user;
        this.tabbedPane = new JTabbedPane();
        this.userTableModel = createUserTableModel();
        this.auditTableModel = createAuditTableModel();
        
        initComponents();
        loadData();
        
        setTitle("Admin Dashboard - " + user.getUsername());
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private DefaultTableModel createUserTableModel() {
        String[] columns = {"User ID", "Username", "Role", "Status", "Created", "Last Login"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
    
    private DefaultTableModel createAuditTableModel() {
        String[] columns = {"Timestamp", "User", "Action", "Entity", "Details"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
    
    private void loadSystemStatistics(JLabel... labels) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String[] queries = {
                "SELECT COUNT(*) FROM users",
                "SELECT COUNT(*) FROM customers",
                "SELECT COUNT(*) FROM employees",
                "SELECT COUNT(*) FROM accounts",
                "SELECT COUNT(*) FROM transactions",
                "SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE'"
            };
            
            for (int i = 0; i < queries.length; i++) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(queries[i])) {
                    if (rs.next() && labels[i] != null) {
                        labels[i].setText(String.valueOf(rs.getInt(1)));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading system statistics", e);
            showError("Failed to load system statistics");
        }
    }
    
    private void toggleUserStatus(JTable table, boolean activate) {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Please select a user first");
            return;
        }
        
        try {
            int userId = (int) userTableModel.getValueAt(row, 0);
            String status = activate ? "ACTIVE" : "INACTIVE";
            
            executeUpdate(SQL_UPDATE_USER_STATUS, status, userId);
            
            AuditService.logAction(currentUser.getUserId(), 
                "USER_STATUS_CHANGE", 
                "User #" + userId, 
                "Status changed to " + status);
                
            loadUserData();
            showInfo("User status updated successfully");
        } catch (SQLException e) {
            handleDatabaseError("update user status", e);
        }
    }
    
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", 
                JOptionPane.ERROR_MESSAGE));
    }
    
    private void showWarning(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Warning", 
                JOptionPane.WARNING_MESSAGE));
    }
    
    private void showInfo(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Information", 
                JOptionPane.INFORMATION_MESSAGE));
    }
    
    @Override
    public void dispose() {
        DatabaseConnection.closeConnection();
        super.dispose();
    }
    
    private void initComponents() {
        // Removed duplicate setTitle, setSize, setDefaultCloseOperation, setLocationRelativeTo
        // They're already set in constructor
        
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Employee Management", createEmployeePanel());
        tabbedPane.addTab("Audit Logs", createAuditPanel());
        tabbedPane.addTab("System Reports", createReportsPanel());
        
        add(tabbedPane);
        createMenuBar();
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(63, 81, 181));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Administrator Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);
        
        panel.add(welcomePanel, BorderLayout.NORTH);
        
        // System statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel totalUsersLabel = new JLabel("0", SwingConstants.CENTER);
        JLabel totalCustomersLabel = new JLabel("0", SwingConstants.CENTER);
        JLabel totalEmployeesLabel = new JLabel("0", SwingConstants.CENTER);
        JLabel totalAccountsLabel = new JLabel("0", SwingConstants.CENTER);
        JLabel totalTransactionsLabel = new JLabel("0", SwingConstants.CENTER);
        JLabel totalLoansLabel = new JLabel("0", SwingConstants.CENTER);
        
        statsPanel.add(createStatCard("Total Users", totalUsersLabel, new Color(33, 150, 243)));
        statsPanel.add(createStatCard("Total Customers", totalCustomersLabel, new Color(76, 175, 80)));
        statsPanel.add(createStatCard("Total Employees", totalEmployeesLabel, new Color(255, 152, 0)));
        statsPanel.add(createStatCard("Total Accounts", totalAccountsLabel, new Color(156, 39, 176)));
        statsPanel.add(createStatCard("Total Transactions", totalTransactionsLabel, new Color(0, 150, 136)));
        statsPanel.add(createStatCard("Active Loans", totalLoansLabel, new Color(244, 67, 54)));
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        // Load statistics
        loadSystemStatistics(totalUsersLabel, totalCustomersLabel, totalEmployeesLabel,
                totalAccountsLabel, totalTransactionsLabel, totalLoansLabel);
        
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Users table - removed duplicate declaration, use class field
        JTable table = new JTable(userTableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton activateBtn = new JButton("Activate User");
        JButton deactivateBtn = new JButton("Deactivate User");
        JButton resetPasswordBtn = new JButton("Reset Password");
        
        activateBtn.setBackground(new Color(76, 175, 80));
        activateBtn.setForeground(Color.WHITE);
        deactivateBtn.setBackground(new Color(244, 67, 54));
        deactivateBtn.setForeground(Color.WHITE);
        
        refreshBtn.addActionListener(e -> loadUserData());
        activateBtn.addActionListener(e -> toggleUserStatus(table, true));
        deactivateBtn.addActionListener(e -> toggleUserStatus(table, false));
        resetPasswordBtn.addActionListener(e -> resetUserPassword(table));
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(activateBtn);
        buttonPanel.add(deactivateBtn);
        buttonPanel.add(resetPasswordBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Employee Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Employee table
        String[] columns = {"ID", "Name", "Email", "Department", "Position", "Salary", "Hire Date"};
        DefaultTableModel employeeModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(employeeModel);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton addEmployeeBtn = new JButton("Add Employee");
        
        addEmployeeBtn.setBackground(new Color(76, 175, 80));
        addEmployeeBtn.setForeground(Color.WHITE);
        
        refreshBtn.addActionListener(e -> loadEmployeeData(employeeModel));
        addEmployeeBtn.addActionListener(e -> showAddEmployeeDialog());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addEmployeeBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load data
        loadEmployeeData(employeeModel);
        
        return panel;
    }
    
    private JPanel createAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("System Audit Logs");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Audit table - removed duplicate declaration, use class field
        JTable table = new JTable(auditTableModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export Logs");
        
        refreshBtn.addActionListener(e -> loadAuditData());
        exportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, 
                "Export feature - requires implementation"));
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("System Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        
        // Report buttons
        JButton customerReportBtn = new JButton("Customer Summary Report");
        JButton transactionReportBtn = new JButton("Transaction Report");
        JButton loanReportBtn = new JButton("Loan Analysis Report");
        JButton fraudReportBtn = new JButton("Fraud Detection Report");
        
        styleReportButton(customerReportBtn);
        styleReportButton(transactionReportBtn);
        styleReportButton(loanReportBtn);
        styleReportButton(fraudReportBtn);
        
        customerReportBtn.addActionListener(e -> generateReport("CUSTOMER"));
        transactionReportBtn.addActionListener(e -> generateReport("TRANSACTION"));
        loanReportBtn.addActionListener(e -> generateReport("LOAN"));
        fraudReportBtn.addActionListener(e -> generateFraudReport());
        
        panel.add(customerReportBtn, gbc);
        gbc.gridy++;
        panel.add(transactionReportBtn, gbc);
        gbc.gridy++;
        panel.add(loanReportBtn, gbc);
        gbc.gridy++;
        panel.add(fraudReportBtn, gbc);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        card.add(titleLabel, BorderLayout.NORTH);
        
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(color.darker());
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu userMenu = new JMenu("Users");
        JMenuItem addUserItem = new JMenuItem("Add User");
        JMenuItem viewUsersItem = new JMenuItem("View Users");
        addUserItem.addActionListener(e -> showAddUserDialog());
        viewUsersItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        userMenu.add(addUserItem);
        userMenu.add(viewUsersItem);
        
        JMenu reportMenu = new JMenu("Reports");
        JMenuItem generateReportItem = new JMenuItem("Generate Report");
        generateReportItem.addActionListener(e -> tabbedPane.setSelectedIndex(4));
        reportMenu.add(generateReportItem);
        
        menuBar.add(fileMenu);
        menuBar.add(userMenu);
        menuBar.add(reportMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void loadData() {
        loadUserData();
        loadEmployeeData();  // Remove null parameter
        loadAuditData();
    }
    
    private void loadUserData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_USERS)) {
            
            userTableModel.setRowCount(0);
            while (rs.next()) {
                userTableModel.addRow(new Object[] {
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("last_login")
                });
            }
        } catch (SQLException e) {
            handleDatabaseError("load user data", e);
        }
    }
    
    private void loadEmployeeData() {
        // Get employee table from the employee panel
        JTable employeeTable = getEmployeeTable();
        if (employeeTable != null) {
            DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
            loadEmployeeData(model);
        }
    }
    
    private JTable getEmployeeTable() {
        Component employeePanel = tabbedPane.getComponentAt(2); // Employee tab index
        if (employeePanel instanceof JPanel) {
            Component scrollPane = ((JPanel) employeePanel).getComponent(1); // Table scroll pane
            if (scrollPane instanceof JScrollPane) {
                Component view = ((JScrollPane) scrollPane).getViewport().getView();
                if (view instanceof JTable) {
                    return (JTable) view;
                }
            }
        }
        return null;
    }
    
    private void loadAuditData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM audit_logs ORDER BY timestamp DESC")) {
            
            auditTableModel.setRowCount(0);
            while (rs.next()) {
                auditTableModel.addRow(new Object[] {
                    rs.getTimestamp("timestamp"),
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getString("entity"),
                    rs.getString("details")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading audit data", e);
            showError("Failed to load audit data");
        }
    }
    
    private void showAddEmployeeDialog() {
        JDialog dialog = new JDialog(this, "Add Employee", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField deptField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField salaryField = new JTextField();
        JDatePickerImpl hireDatePicker = createDatePicker();
        
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Department:"));
        panel.add(deptField);
        panel.add(new JLabel("Position:"));
        panel.add(positionField);
        panel.add(new JLabel("Salary:"));
        panel.add(salaryField);
        panel.add(new JLabel("Hire Date:"));
        panel.add(hireDatePicker);
        
        JButton addButton = new JButton("Add Employee");
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String department = deptField.getText();
            String position = positionField.getText();
            String salaryText = salaryField.getText();
            
            // Fixed: getDate() returns java.util.Date, convert to LocalDate
            java.util.Date selectedDate = (java.util.Date) hireDatePicker.getModel().getValue();
            
            if (name.isEmpty() || email.isEmpty() || department.isEmpty() || 
                    position.isEmpty() || salaryText.isEmpty() || selectedDate == null) {
                showWarning("Please fill all fields");
                return;
            }
            
            try {
                double salary = Double.parseDouble(salaryText);
                java.sql.Date sqlHireDate = new java.sql.Date(selectedDate.getTime());
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO employees (name, email, department, position, salary, hire_date) VALUES (?, ?, ?, ?, ?, ?)")) {
                    
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, department);
                    stmt.setString(4, position);
                    stmt.setDouble(5, salary);
                    stmt.setDate(6, sqlHireDate);
                    stmt.executeUpdate();
                    
                    AuditService.logAction(currentUser.getUserId(), 
                        "EMPLOYEE_ADDITION", 
                        "Employee " + name, 
                        "Added new employee with email " + email);
                        
                    dialog.dispose();
                    loadEmployeeData(null);
                    showInfo("Employee added successfully");
                }
            } catch (NumberFormatException ex) {
                showWarning("Salary must be a valid number");
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error adding employee", ex);
                showError("Failed to add employee");
            }
        });
        
        panel.add(new JLabel());
        panel.add(addButton);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new DateLabelFormatter());
    }
    
    private void showAddUserDialog() {
        // Implementation for adding users
        JOptionPane.showMessageDialog(this, 
                "Add user functionality - not yet implemented",
                "Add User", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void resetUserPassword(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Please select a user first");
            return;
        }
        
        int userId = (int) userTableModel.getValueAt(row, 0);
        String newPassword = JOptionPane.showInputDialog(this, 
                "Enter new password for user ID " + userId);
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            showWarning("Password cannot be empty");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET password = ? WHERE user_id = ?")) {
            
            stmt.setString(1, SecurityUtils.hashPassword(newPassword));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            
            AuditService.logAction(currentUser.getUserId(), 
                "PASSWORD_RESET", 
                "User #" + userId, 
                "Password reset successful");
                
            showInfo("Password reset successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error resetting password", e);
            showError("Failed to reset password");
        }
    }
    
    private void generateReport(String reportType) {
        // Implementation for generating different types of reports
        JOptionPane.showMessageDialog(this, 
                "Report generation for " + reportType + " - not implemented",
                "Report", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void generateFraudReport() {
        // Special handling for fraud detection report
        JOptionPane.showMessageDialog(this, 
                "Fraud detection report - not implemented",
                "Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleReportButton(JButton button) {
        button.setPreferredSize(new Dimension(200, 40));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    private class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }
}