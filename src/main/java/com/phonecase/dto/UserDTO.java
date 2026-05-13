package com.phonecase.dto;

/**
 * DTO для передачі даних користувача.
 */
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String role;
    private boolean isActive;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() { return "UserDTO{username='" + username + "', role='" + role + "'}"; }
}