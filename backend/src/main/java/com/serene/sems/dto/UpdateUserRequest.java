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

    /** When adding DEALER role to a user without a dealer row, these are required (see {@link CreateUserRequest}). */
    @Size(max = 200)
    private String companyName;

    @Size(max = 40)
    private String phone;

    @Size(max = 500)
    private String address;

    @Size(max = 3)
    private String countryCode;

    @Size(max = 16)
    private String stateCode;

    @Size(max = 120)
    private String city;

    private Boolean dealerActive;

    /** When adding CUSTOMER role without a customer row, required with country/state/city (see {@link CreateUserRequest}). */
    @Size(max = 200)
    private String customerFullName;

    private Boolean customerActive;

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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getDealerActive() {
        return dealerActive;
    }

    public void setDealerActive(Boolean dealerActive) {
        this.dealerActive = dealerActive;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public void setCustomerFullName(String customerFullName) {
        this.customerFullName = customerFullName;
    }

    public Boolean getCustomerActive() {
        return customerActive;
    }

    public void setCustomerActive(Boolean customerActive) {
        this.customerActive = customerActive;
    }
}
