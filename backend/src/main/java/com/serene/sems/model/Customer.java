package com.serene.sems.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    /** Nullable so the portal login can be removed while keeping the customer record for order history. */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", unique = true, nullable = true)
    private User user;

    /** Null when no active dealer serves the customer's location (e.g. after a city change). */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "dealer_id", nullable = true)
    private Dealer dealer;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(length = 40)
    private String phone;

    @Column(length = 500)
    private String address;

    /** ISO 3166-1 alpha-2 country code, e.g. US */
    @Column(length = 3)
    private String countryCode;

    /** State/province code from country-state dataset, e.g. CA */
    @Column(length = 16)
    private String stateCode;

    @Column(length = 120)
    private String city;

    @Column(nullable = false)
    private boolean active = true;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
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
