package com.serene.sems.dto;

import java.time.Instant;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private int failedAttempts;
    private Instant lockTime;
    private Instant accountExpiry;
    private Instant lastLoginAt;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Instant getLockTime() {
        return lockTime;
    }

    public void setLockTime(Instant lockTime) {
        this.lockTime = lockTime;
    }

    public Instant getAccountExpiry() {
        return accountExpiry;
    }

    public void setAccountExpiry(Instant accountExpiry) {
        this.accountExpiry = accountExpiry;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
