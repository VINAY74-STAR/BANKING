# Banking Management System - Project Structure

## 📁 Complete File Organization

```
BankingManagementSystem/
│
├── src/
│   └── com/
│       └── bank/
│           ├── BankingSystem.java                    # Main entry point
│           │
│           ├── model/                                # Data Models (POJOs)
│           │   ├── User.java                        # User authentication model
│           │   ├── Customer.java                    # Customer details
│           │   ├── Employee.java                    # Employee information
│           │   ├── Account.java                     # Bank account model
│           │   ├── Transaction.java                 # Transaction records
│           │   └── Loan.java                        # Loan details
│           │
│           ├── service/                             # Business Logic Layer
│           │   ├── AuthenticationService.java       # Login, registration, session
│           │   ├── AccountService.java              # Account operations
│           │   ├── TransactionService.java          # Transaction processing
│           │   ├── LoanService.java                 # Loan management
│           │   ├── CustomerService.java             # Customer operations
│           │   ├── EmployeeService.java             # Employee operations
│           │   └── AuditService.java                # Audit logging
│           │
│           ├── ai/                                  # AI Finance Manager
│           │   ├── AIFinanceManager.java            # Main AI engine
│           │   ├── SpendingAnalyzer.java            # Spending pattern analysis
│           │   ├── FraudDetector.java               # Fraud detection algorithm
│           │   ├── BudgetAdvisor.java               # Budget recommendations
│           │   └── InsightGenerator.java            # Insight generation
│           │
│           ├── ui/                                  # User Interface Layer
│           │   ├── LoginFrame.java                  # Login screen
│           │   ├── RegisterFrame.java               # Registration screen
│           │   │
│           │   ├── customer/                        # Customer UI
│           │   │   ├── CustomerDashboard.java       # Main customer dashboard
│           │   │   ├── AccountPanel.java            # Account management panel
│           │   │   ├── TransactionPanel.java        # Transaction history
│           │   │   ├── TransferPanel.java           # Money transfer UI
│           │   │   ├── LoanPanel.java               # Loan application
│           │   │   └── AIInsightsPanel.java         # AI recommendations display
│           │   │
│           │   ├── employee/                        # Employee UI
│           │   │   ├── EmployeeDashboard.java       # Employee main screen
│           │   │   ├── CustomerManagementPanel.java # Manage customers
│           │   │   ├── TransactionProcessPanel.java # Process transactions
│           │   │   └── LoanApprovalPanel.java       # Approve/reject loans
│           │   │
│           │   ├── admin/                           # Admin UI
│           │   │   ├── AdminDashboard.java          # Admin main screen
│           │   │   ├── UserManagementPanel.java     # User administration
│           │   │   ├── EmployeePanel.java           # Employee management
│           │   │   ├── AuditLogPanel.java           # View audit logs
│           │   │   └── ReportsPanel.java            # Generate reports
│           │   │
│           │   └── components/                      # Reusable UI components
│           │       ├── StatCard.java                # Statistics display card
│           │       ├── DataTable.java               # Custom table component
│           │       └── InsightCard.java             # AI insight display card
│           │
│           └── util/                                # Utility Classes
│               ├── DatabaseConnection.java          # JDBC connection manager
│               ├── SecurityUtils.java               # Encryption & hashing
│               ├── ValidationUtils.java             # Input validation
│               ├── DateUtils.java                   # Date formatting utilities
│               ├── CurrencyFormatter.java           # Currency display
│               └── PDFGenerator.java                # PDF report generation
│
├── lib/                                             # External Libraries
│   ├── mysql-connector-java-8.0.33.jar             # MySQL JDBC driver
│   ├── itext-7.2.5.jar                             # PDF generation (optional)
│   └── weka-3.8.6.jar                              # ML library (optional)
│
├── resources/                                       # Resources
│   ├── images/                                      # UI images/icons
│   │   ├── logo.png
│   │   ├── user-icon.png
│   │   └── bank-icon.png
│   │
│   ├── config/                                      # Configuration files
│   │   ├── database.properties                     # DB configuration
│   │   └── application.properties                  # App settings
│   │
│   └── sql/                                        # SQL Scripts
│       ├── schema.sql                              # Database schema
│       ├── data.sql                                # Sample data
│       └── procedures.sql                          # Stored procedures
│
├── test/                                           # Unit Tests (JUnit)
│   └── com/
│       └── bank/
│           ├── service/
│           │   ├── AuthenticationServiceTest.java
│           │   ├── AccountServiceTest.java
│           │   └── AIFinanceManagerTest.java
│           └── util/
│               └── SecurityUtilsTest.java
│
├── docs/                                           # Documentation
│   ├── README.md                                   # Main documentation
│   ├── API_DOCUMENTATION.md                        # API reference
│   ├── DATABASE_SCHEMA.md                          # Database details
│   ├── USER_GUIDE.md                              # End-user guide
│   └── DEVELOPER_GUIDE.md                         # Developer documentation
│
├── sql/                                           # Database Scripts
│   └── banking_system_schema.sql                  # Complete DB setup
│
├── .gitignore                                     # Git ignore file
├── pom.xml                                        # Maven configuration (optional)
└── build.xml                                      # Ant build file (optional)
```

---

## 🎯 Component Responsibilities

### 1. Model Layer (`model/`)
**Purpose**: Data structures and domain objects

**Files**:
- `User.java` - Authentication entity with role enum
- `Customer.java` - Customer profile with encrypted fields
- `Employee.java` - Employee details
- `Account.java` - Account information with balance
- `Transaction.java` - Transaction records
- `Loan.java` - Loan details with EMI calculation

**Key Features**:
- Plain Old Java Objects (POJOs)
- Getters and setters
- Enums for type safety
- toString() methods
- Business logic methods (e.g., calculateEMI())

---

### 2. Service Layer (`service/`)
**Purpose**: Business logic and database operations

**Files**:
- `AuthenticationService.java` - User login/logout/registration
- `AccountService.java` - CRUD operations for accounts
- `TransactionService.java` - Deposit/withdraw/transfer
- `LoanService.java` - Loan application and approval
- `AuditService.java` - System activity logging

**Key Features**:
- Database transactions
- Exception handling
- Thread safety (synchronized methods)
- Business rule validation
- Audit trail logging

---

### 3. AI Module (`ai/`)
**Purpose**: Intelligent financial analysis and recommendations

**Files**:
- `AIFinanceManager.java` - Main AI coordinator
- `SpendingAnalyzer.java` - Transaction categorization
- `FraudDetector.java` - Anomaly detection
- `BudgetAdvisor.java` - Savings recommendations
- `InsightGenerator.java` - Generate insights

**AI Algorithms**:
1. **Rule-Based Categorization**: Pattern matching on descriptions
2. **Statistical Analysis**: Z-score for fraud detection
3. **Heuristic Rules**: Budget recommendations
4. **Time-Series Analysis**: Spending trends

---

### 4. UI Layer (`ui/`)
**Purpose**: User interface using Java Swing

**Structure**:
- **Login/Register**: Entry point
- **Customer Dashboard**: 6 tabs (Dashboard, Accounts, Transactions, Transfer, Loans, AI)
- **Employee Dashboard**: Customer management, transaction processing
- **Admin Dashboard**: System administration, reports

**UI Components**:
- JFrame (main windows)
- JPanel (containers)
- JTable (data display)
- JButton (actions)
- JTabbedPane (navigation)
- Custom components for reusability

---

### 5. Utility Layer (`util/`)
**Purpose**: Helper classes and common functionality

**Files**:
- `DatabaseConnection.java` - Singleton connection manager
- `SecurityUtils.java` - Encryption, hashing, validation
- `ValidationUtils.java` - Input validation
- `DateUtils.java` - Date parsing and formatting
- `CurrencyFormatter.java` - Money display
- `PDFGenerator.java` - Report generation

---

## 📦 Dependencies & Libraries

### Required

```xml
<!-- MySQL JDBC Driver -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### Optional (for enhancements)

```xml
<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- Machine Learning -->
<dependency>
    <groupId>nz.ac.waikato.cms.weka</groupId>
    <artifactId>weka-stable</artifactId>
    <version>3.8.6</version>
</dependency>

<!-- JUnit Testing -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

---

## 🗂️ Database Scripts Organization

### schema.sql
```sql
-- Database creation
-- Table definitions
-- Indexes
-- Foreign keys
-- Views
-- Stored procedures
```

### data.sql
```sql
-- Sample users
-- Sample customers
-- Sample accounts
-- Sample transactions
```

### procedures.sql
```sql
-- sp_transfer_funds
-- sp_calculate_interest
-- sp_generate_statement
```

---

## 🔧 Build Configuration

### Maven (pom.xml)

```xml
<project>
    <groupId>com.bank</groupId>
    <artifactId>banking-system</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- Dependencies here -->
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.bank.BankingSystem</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Ant (build.xml)

```xml
<project name="BankingSystem" default="dist" basedir=".">
    <property name="src" location="src"/>
    <property name="build" location="bin"/>
    <property name="dist" location="dist"/>
    
    <target name="compile">
        <javac srcdir="${src}" destdir="${build}" 
               classpath="lib/mysql-connector-java-8.0.33.jar"/>
    </target>
    
    <target name="dist" depends="compile">
        <jar jarfile="${dist}/BankingSystem.jar" basedir="${build}">
            <manifest>
                <attribute name="Main-Class" value="com.bank.BankingSystem"/>
            </manifest>
        </jar>
    </target>
</project>
```

---

## 📋 File Naming Conventions

### Java Classes
- **Models**: Singular noun (User, Account, Transaction)
- **Services**: Noun + "Service" (AccountService, LoanService)
- **UI**: Noun + Type (CustomerDashboard, LoginFrame)
- **Utilities**: Noun + "Utils" (SecurityUtils, DateUtils)

### Database Tables
- **Naming**: Lowercase, plural (users, accounts, transactions)
- **Columns**: Snake_case (first_name, account_number)
- **Indexes**: idx_[table]_[column] (idx_customers_email)
- **Foreign Keys**: fk_[table]_[referenced_table]

### Constants
```java
public static final String DB_URL = "jdbc:mysql://localhost:3306/banking_system";
public static final int MAX_LOGIN_ATTEMPTS = 3;
public static final double FRAUD_THRESHOLD = 3.0;
```

---

## 🎨 Code Style Guidelines

### Java Formatting
```java
// Class structure
public class AccountService {
    // 1. Static variables
    private static final String TABLE_NAME = "accounts";
    
    // 2. Instance variables
    private Connection connection;
    
    // 3. Constructor
    public AccountService() {
        // Initialization
    }
    
    // 4. Public methods
    public Account getAccount(int id) {
        // Implementation
    }
    
    // 5. Private helper methods
    private void validateAccount(Account account) {
        // Validation
    }
}
```

### Documentation
```java
/**
 * Service class for managing bank accounts
 * 
 * @author Banking Team
 * @version 1.0
 * @since 2025-01-01
 */
public class AccountService {
    
    /**
     * Creates a new bank account for a customer
     * 
     * @param customerId The customer's unique identifier
     * @param type The account type (SAVINGS or CURRENT)
     * @return The newly created Account object
     * @throws SQLException if database operation fails
     */
    public static Account createAccount(int customerId, AccountType type) 
            throws SQLException {
        // Implementation
    }
}
```

---

## 🚀 Deployment Structure

### Production Package
```
BankingSystem-1.0/
├── BankingSystem.jar
├── lib/
│   └── mysql-connector-java-8.0.33.jar
├── config/
│   ├── database.properties
│   └── application.properties
├── sql/
│   └── schema.sql
├── README.txt
└── LICENSE.txt
```

### Run Script (run.sh)
```bash
#!/bin/bash
java -cp BankingSystem.jar:lib/* com.bank.BankingSystem
```

### Run Script (run.bat)
```batch
@echo off
java -cp BankingSystem.jar;lib/* com.bank.BankingSystem
pause
```

---

## 📊 Project Metrics

| Metric | Count |
|--------|-------|
| Total Java Files | 25+ |
| Model Classes | 6 |
| Service Classes | 7 |
| UI Classes | 12+ |
| Utility Classes | 6 |
| Database Tables | 10 |
| Stored Procedures | 3 |
| Total LOC | 3500+ |

---

## ✅ Implementation Checklist

### Core Functionality
- [x] User authentication system
- [x] Account management (CRUD)
- [x] Transaction processing
- [x] Loan management
- [x] AI Finance Manager
- [x] Fraud detection
- [x] Audit logging

### UI Components
- [x] Login screen
- [x] Registration dialog
- [x] Customer dashboard (6 tabs)
- [x] Employee dashboard (4 tabs)
- [x] Admin dashboard (5 tabs)

### Database
- [x] 10 tables with relationships
- [x] Stored procedures
- [x] Views for reporting
- [x] Indexes for performance

### Security
- [x] Password hashing (SHA-256)
- [x] Data encryption (AES)
- [x] SQL injection prevention
- [x] Role-based access control

### AI Features
- [x] Spending categorization
- [x] Pattern analysis
- [x] Budget recommendations
- [x] Fraud detection
- [x] EMI optimization

---

**Structure Last Updated**: October 2025  
**Version**: 1.0.0  
**Status**: Production Ready ✅