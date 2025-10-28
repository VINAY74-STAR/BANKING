package com.bank.ui;

import com.bank.model.User;
import com.bank.service.AuthenticationService;
import com.bank.ui.admin.AdminDashboard;
import com.bank.ui.customer.CustomerDashboard;
import com.bank.ui.employee.EmployeeDashboard;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Login Frame - Main entry point for authentication
 */
public class LoginFrame extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    public LoginFrame() {
        initComponents();
        setupLayout();
        attachListeners();
    }
    
    private void initComponents() {
        setTitle("Banking Management System - Login");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register as Customer");
        
        // Styling
        loginButton.setBackground(new Color(25, 118, 210));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        registerButton.setFocusPainted(false);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Banking Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 118, 210));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Secure Login Portal");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Username panel
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.setBackground(Color.WHITE);
        usernamePanel.add(new JLabel("Username:"));
        usernamePanel.add(usernameField);
        mainPanel.add(usernamePanel);
        
        // Password panel
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.add(new JLabel("Password: "));
        passwordPanel.add(passwordField);
        mainPanel.add(passwordPanel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel);
        
        // Demo credentials info
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel demoLabel = new JLabel("<html><center>Demo Credentials:<br/>" +
                "Admin: admin/admin123<br/>" +
                "Employee: employee1/emp123<br/>" +
                "Customer: customer1/cust123</center></html>");
        demoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        demoLabel.setForeground(Color.GRAY);
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(demoLabel);
        
        add(mainPanel);
    }
    
    private void attachListeners() {
        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> showRegistrationDialog());
        
        // Enter key login
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter username and password",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        
        // Perform login in background
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                try {
                    return AuthenticationService.login(username, password);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        openDashboard(user);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Invalid username or password",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Error during login: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    passwordField.setText("");
                }
            }
        };
        
        worker.execute();
    }
    
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN:
                new AdminDashboard(user).setVisible(true);
                break;
            case EMPLOYEE:
                new EmployeeDashboard(user).setVisible(true);
                break;
            case CUSTOMER:
                new CustomerDashboard(user).setVisible(true);
                break;
        }
    }
    
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Customer Registration", true);
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Fields
        JTextField regUsername = new JTextField(20);
        JPasswordField regPassword = new JPasswordField(20);
        JPasswordField confirmPassword = new JPasswordField(20);
        JTextField firstName = new JTextField(20);
        JTextField lastName = new JTextField(20);
        JTextField email = new JTextField(20);
        JTextField phone = new JTextField(20);
        JTextArea address = new JTextArea(3, 20);
        JTextField dob = new JTextField(20); // Format: YYYY-MM-DD
        JTextField aadhar = new JTextField(20);
        JTextField pan = new JTextField(20);
        
        address.setLineWrap(true);
        JScrollPane addressScroll = new JScrollPane(address);
        
        // Add components
        int row = 0;
        addFormField(panel, gbc, row++, "Username:", regUsername);
        addFormField(panel, gbc, row++, "Password:", regPassword);
        addFormField(panel, gbc, row++, "Confirm Password:", confirmPassword);
        addFormField(panel, gbc, row++, "First Name:", firstName);
        addFormField(panel, gbc, row++, "Last Name:", lastName);
        addFormField(panel, gbc, row++, "Email:", email);
        addFormField(panel, gbc, row++, "Phone:", phone);
        addFormField(panel, gbc, row++, "Address:", addressScroll);
        addFormField(panel, gbc, row++, "Date of Birth (YYYY-MM-DD):", dob);
        addFormField(panel, gbc, row++, "Aadhar Number:", aadhar);
        addFormField(panel, gbc, row++, "PAN Number:", pan);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        
        submitButton.addActionListener(e -> {
            // Validate
            String pass = new String(regPassword.getPassword());
            String confPass = new String(confirmPassword.getPassword());
            
            if (!pass.equals(confPass)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!");
                return;
            }
            
            if (regUsername.getText().trim().isEmpty() || pass.isEmpty() ||
                firstName.getText().trim().isEmpty() || email.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields!");
                return;
            }
            
            // Register
            try {
                boolean success = AuthenticationService.registerCustomer(
                        regUsername.getText().trim(),
                        pass,
                        firstName.getText().trim(),
                        lastName.getText().trim(),
                        email.getText().trim(),
                        phone.getText().trim(),
                        address.getText().trim(),
                        java.sql.Date.valueOf(dob.getText().trim()),
                        aadhar.getText().trim(),
                        pan.getText().trim()
                );
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "Registration successful! You can now login.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Registration failed. Username might already exist.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row,
                             String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}