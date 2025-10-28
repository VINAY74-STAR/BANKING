-- Banking Management System Database Schema
-- MySQL 8.0+

DROP DATABASE IF EXISTS banking_system;
CREATE DATABASE banking_system;
USE banking_system;

-- ============================
-- USERS TABLE
-- ============================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYEE', 'CUSTOMER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username),
    INDEX idx_role (role)
);

-- ============================
-- CUSTOMERS TABLE
-- ============================
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address TEXT,
    date_of_birth DATE NOT NULL,
    aadhar_number VARCHAR(12) UNIQUE NOT NULL,
    pan_number VARCHAR(10) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_email (email),
    INDEX idx_phone (phone)
);

-- ============================
-- EMPLOYEES TABLE
-- ============================
CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    department VARCHAR(50),
    position VARCHAR(50),
    salary DECIMAL(12, 2),
    hire_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_department (department)
);

-- ============================
-- ACCOUNTS TABLE
-- ============================
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (balance >= 0),
    interest_rate DECIMAL(5, 2) DEFAULT 4.00,
    status ENUM('ACTIVE', 'INACTIVE', 'FROZEN') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number),
    INDEX idx_customer_id (customer_id)
);

-- ============================
-- TRANSACTIONS TABLE
-- ============================
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    balance_after DECIMAL(15, 2) NOT NULL,
    description TEXT,
    reference_account VARCHAR(20),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_by INT NULL,
    is_flagged BOOLEAN DEFAULT FALSE,
    fraud_score DECIMAL(5, 2) DEFAULT 0.00,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES employees(employee_id) ON DELETE SET NULL,
    INDEX idx_account_id (account_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_flagged (is_flagged)
);

-- ============================
-- LOANS TABLE
-- ============================
CREATE TABLE loans (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    loan_type ENUM('PERSONAL', 'HOME', 'EDUCATION', 'CAR') NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL CHECK (principal_amount > 0),
    interest_rate DECIMAL(5, 2) NOT NULL,
    tenure_months INT NOT NULL CHECK (tenure_months > 0),
    emi_amount DECIMAL(12, 2) NOT NULL,
    outstanding_amount DECIMAL(15, 2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'ACTIVE', 'CLOSED') DEFAULT 'PENDING',
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approval_date TIMESTAMP NULL,
    approved_by INT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES employees(employee_id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
);

-- ============================
-- EMI PAYMENTS TABLE
-- ============================
CREATE TABLE emi_payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount_paid DECIMAL(12, 2) NOT NULL,
    principal_paid DECIMAL(12, 2) NOT NULL,
    interest_paid DECIMAL(12, 2) NOT NULL,
    outstanding_after DECIMAL(15, 2) NOT NULL,
    payment_status ENUM('PAID', 'PENDING', 'OVERDUE') DEFAULT 'PENDING',
    paid_on TIMESTAMP NULL,
    FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_status (payment_status)
);

-- ============================
-- AUDIT LOGS TABLE
-- ============================
CREATE TABLE audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INT,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
);

-- ============================
-- AI FINANCE INSIGHTS TABLE
-- ============================
CREATE TABLE ai_finance_insights (
    insight_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    insight_type ENUM('SPENDING_PATTERN', 'BUDGET_RECOMMENDATION', 'SAVING_TIP', 'FRAUD_ALERT', 'EMI_OPTIMIZATION') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    is_read BOOLEAN DEFAULT FALSE,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_generated_at (generated_at),
    INDEX idx_is_read (is_read)
);

-- ============================
-- SPENDING CATEGORIES TABLE
-- ============================
CREATE TABLE spending_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    total_spent DECIMAL(12, 2) DEFAULT 0.00,
    transaction_count INT DEFAULT 0,
    avg_transaction DECIMAL(12, 2) DEFAULT 0.00,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    UNIQUE KEY unique_customer_category_month (customer_id, category, month_year),
    INDEX idx_customer_month (customer_id, month_year)
);

-- ============================
-- SAMPLE DATA
-- ============================

-- Admin user (password: admin123)
INSERT INTO users (username, password_hash, role)
VALUES ('admin', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'ADMIN');

-- Employee user
INSERT INTO users (username, password_hash, role)
VALUES ('employee1', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'EMPLOYEE');

INSERT INTO employees (user_id, first_name, last_name, email, phone, department, position, salary, hire_date)
VALUES (2, 'Rajesh', 'Kumar', 'rajesh.kumar@bank.com', '9876543210', 'Operations', 'Senior Manager', 75000.00, '2020-01-15');

-- Customer users
INSERT INTO users (username, password_hash, role)
VALUES 
('customer1', '1a56b0e8c8c7c3d0c4f2d3d8b8f8a8c8d8e8f8a8b8c8d8e8f8a8b8c8d8e8f8a8', 'CUSTOMER'),
('customer2', '1a56b0e8c8c7c3d0c4f2d3d8b8f8a8c8d8e8f8a8b8c8d8e8f8a8b8c8d8e8f8a8', 'CUSTOMER');

INSERT INTO customers (user_id, first_name, last_name, email, phone, address, date_of_birth, aadhar_number, pan_number)
VALUES
(3, 'Amit', 'Sharma', 'amit.sharma@email.com', '9876543211', '123 MG Road, Bangalore', '1990-05-15', '123456789012', 'ABCDE1234F'),
(4, 'Priya', 'Singh', 'priya.singh@email.com', '9876543212', '456 Park Street, Delhi', '1992-08-20', '123456789013', 'ABCDE1234G');

-- Accounts
INSERT INTO accounts (customer_id, account_number, account_type, balance, status)
VALUES
(1, 'ACC1001000000001', 'SAVINGS', 50000.00, 'ACTIVE'),
(1, 'ACC1001000000002', 'CURRENT', 100000.00, 'ACTIVE'),
(2, 'ACC1002000000001', 'SAVINGS', 75000.00, 'ACTIVE');

-- Transactions
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, description, processed_by)
VALUES
(1, 'DEPOSIT', 10000.00, 50000.00, 'Initial Deposit', 1),
(2, 'DEPOSIT', 50000.00, 100000.00, 'Business Deposit', 1),
(3, 'DEPOSIT', 25000.00, 75000.00, 'Salary Credit', 1);

-- ============================
-- STORED PROCEDURES
-- ============================
DELIMITER //

CREATE PROCEDURE sp_transfer_funds(
    IN p_from_account VARCHAR(20),
    IN p_to_account VARCHAR(20),
    IN p_amount DECIMAL(15,2),
    IN p_employee_id INT,
    OUT p_result VARCHAR(100)
)
BEGIN
    DECLARE v_from_balance DECIMAL(15,2);
    DECLARE v_to_balance DECIMAL(15,2);
    DECLARE v_from_account_id INT;
    DECLARE v_to_account_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 'ERROR: Transaction failed';
    END;

    START TRANSACTION;

    SELECT account_id, balance INTO v_from_account_id, v_from_balance
    FROM accounts WHERE account_number = p_from_account FOR UPDATE;

    SELECT account_id, balance INTO v_to_account_id, v_to_balance
    FROM accounts WHERE account_number = p_to_account FOR UPDATE;

    IF v_from_account_id IS NULL THEN
        SET p_result = 'ERROR: Source account not found';
        ROLLBACK;
    ELSEIF v_to_account_id IS NULL THEN
        SET p_result = 'ERROR: Destination account not found';
        ROLLBACK;
    ELSEIF v_from_balance < p_amount THEN
        SET p_result = 'ERROR: Insufficient balance';
        ROLLBACK;
    ELSE
        UPDATE accounts SET balance = balance - p_amount WHERE account_id = v_from_account_id;
        INSERT INTO transactions (account_id, transaction_type, amount, balance_after, reference_account, processed_by)
        VALUES (v_from_account_id, 'TRANSFER_OUT', p_amount, v_from_balance - p_amount, p_to_account, p_employee_id);

        UPDATE accounts SET balance = balance + p_amount WHERE account_id = v_to_account_id;
        INSERT INTO transactions (account_id, transaction_type, amount, balance_after, reference_account, processed_by)
        VALUES (v_to_account_id, 'TRANSFER_IN', p_amount, v_to_balance + p_amount, p_from_account, p_employee_id);

        COMMIT;
        SET p_result = 'SUCCESS: Transfer completed';
    END IF;
END //

DELIMITER ;

-- ============================
-- VIEWS
-- ============================
CREATE VIEW vw_customer_account_summary AS
SELECT 
    c.customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    c.email,
    c.phone,
    COUNT(a.account_id) AS total_accounts,
    SUM(a.balance) AS total_balance,
    COUNT(CASE WHEN a.account_type = 'SAVINGS' THEN 1 END) AS savings_accounts,
    COUNT(CASE WHEN a.account_type = 'CURRENT' THEN 1 END) AS current_accounts
FROM customers c
LEFT JOIN accounts a ON c.customer_id = a.customer_id
GROUP BY c.customer_id;

CREATE VIEW vw_transaction_summary AS
SELECT 
    DATE(t.transaction_date) AS transaction_day,
    t.transaction_type,
    COUNT(*) AS transaction_count,
    SUM(t.amount) AS total_amount,
    AVG(t.amount) AS avg_amount
FROM transactions t
GROUP BY DATE(t.transaction_date), t.transaction_type;

-- ============================
-- OPTIONAL PRIVILEGES
-- ============================
-- CREATE USER 'bankapp'@'localhost' IDENTIFIED BY 'bankpass123';
-- GRANT ALL PRIVILEGES ON banking_system.* TO 'bankapp'@'localhost';
-- FLUSH PRIVILEGES;
