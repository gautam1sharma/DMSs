package com.serene.sems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;

public class UpdateUserRequest {

    @Email
    private String email;

    @Size(min = 6, max = 100)
    private String password;

    private Boolean enabled;

    private Set<String> roleNames;

    private Instant accountExpiry;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }

    public Instant getAccountExpiry() {
        return accountExpiry;
    }

    public void setAccountExpiry(Instant accountExpiry) {
        this.accountExpiry = accountExpiry;
    }
}
