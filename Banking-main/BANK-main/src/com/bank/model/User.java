package com.bank.model;

import java.sql.Timestamp;

/**
 * User Model - Represents system users
 */
public class User {
    
    public enum Role {
        ADMIN, EMPLOYEE, CUSTOMER
    }
    
    private int userId;
    private String username;
    private String passwordHash;
    private Role role;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean isActive;
    
    // Constructors
    public User() {}
    
    public User(int userId, String username, Role role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}