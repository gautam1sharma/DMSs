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

    /** Present when this user has a dealer portal profile ({@code dealers} row). */
    private Long dealerId;

    private String dealerCompanyName;
    private String dealerPhone;
    private String dealerAddress;
    private String dealerCountryCode;
    private String dealerStateCode;
    private String dealerCity;
    private Boolean dealerActive;

    /** Present when this user has a linked {@code customers} row (portal profile). */
    private Long customerId;

    private String customerFullName;
    private String customerPhone;
    private String customerAddress;
    private String customerCountryCode;
    private String customerStateCode;
    private String customerCity;
    private Boolean customerActive;

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

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public String getDealerCompanyName() {
        return dealerCompanyName;
    }

    public void setDealerCompanyName(String dealerCompanyName) {
        this.dealerCompanyName = dealerCompanyName;
    }

    public String getDealerPhone() {
        return dealerPhone;
    }

    public void setDealerPhone(String dealerPhone) {
        this.dealerPhone = dealerPhone;
    }

    public String getDealerAddress() {
        return dealerAddress;
    }

    public void setDealerAddress(String dealerAddress) {
        this.dealerAddress = dealerAddress;
    }

    public String getDealerCountryCode() {
        return dealerCountryCode;
    }

    public void setDealerCountryCode(String dealerCountryCode) {
        this.dealerCountryCode = dealerCountryCode;
    }

    public String getDealerStateCode() {
        return dealerStateCode;
    }

    public void setDealerStateCode(String dealerStateCode) {
        this.dealerStateCode = dealerStateCode;
    }

    public String getDealerCity() {
        return dealerCity;
    }

    public void setDealerCity(String dealerCity) {
        this.dealerCity = dealerCity;
    }

    public Boolean getDealerActive() {
        return dealerActive;
    }

    public void setDealerActive(Boolean dealerActive) {
        this.dealerActive = dealerActive;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public void setCustomerFullName(String customerFullName) {
        this.customerFullName = customerFullName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerCountryCode() {
        return customerCountryCode;
    }

    public void setCustomerCountryCode(String customerCountryCode) {
        this.customerCountryCode = customerCountryCode;
    }

    public String getCustomerStateCode() {
        return customerStateCode;
    }

    public void setCustomerStateCode(String customerStateCode) {
        this.customerStateCode = customerStateCode;
    }

    public String getCustomerCity() {
        return customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }

    public Boolean getCustomerActive() {
        return customerActive;
    }

    public void setCustomerActive(Boolean customerActive) {
        this.customerActive = customerActive;
    }
}
