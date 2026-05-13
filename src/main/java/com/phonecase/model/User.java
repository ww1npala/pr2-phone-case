package com.phonecase.model;

import java.time.LocalDateTime;

/**
 * Сутність користувача системи.
 * Реалізує інкапсуляцію (private поля, public гетери/сетери).
 */

public class User extends BaseEntity {

    private String username;
    private String password;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private boolean isActive;

    public User() {}

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.isActive = true;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}