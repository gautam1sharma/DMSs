package com.serene.sems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCustomerRequest {

    /**
     * Admin: optional if {@code countryCode}, {@code stateCode}, and {@code city} are set — the nearest
     * active dealer for that city is chosen (fewest customers if several). Ignored for dealer API (uses
     * current dealer).
     */
    private Long dealerId;

    @NotBlank
    private String fullName;

    private String phone;
    private String address;

    @Size(max = 3)
    private String countryCode;

    @Size(max = 16)
    private String stateCode;

    @Size(max = 120)
    private String city;

    private boolean active = true;

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
