package com.levelgroup;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_info")
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @Column(name = "email")
    private String email;

    @Column(name = "check_counter")
    private int checkCounter = 0;

    @Column(name = "permanently_activated")
    private boolean permanentlyActivated;

    @Column(name = "temporarily_activated")
    private boolean temporarilyActivated;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "country")
    private String country;

    @Column(name = "subscription_until")
    private LocalDate subscriptionUntil;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "country_allowed")
    private boolean countryAllowed;

    public DeviceInfo() {}

    public DeviceInfo(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCheckCounter() {
        return checkCounter;
    }

    public void setCheckCounter(int checkCounter) {
        this.checkCounter = checkCounter;
    }

    public boolean isPermanentlyActivated() {
        return permanentlyActivated;
    }

    public void setPermanentlyActivated(boolean permanentlyActivated) {
        this.permanentlyActivated = permanentlyActivated;
    }

    public boolean isTemporarilyActivated() {
        return temporarilyActivated;
    }

    public void setTemporarilyActivated(boolean temporarilyActivated) {
        this.temporarilyActivated = temporarilyActivated;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getSubscriptionUntil() {
        return subscriptionUntil;
    }

    public void setSubscriptionUntil(LocalDate subscriptionUntil) {
        this.subscriptionUntil = subscriptionUntil;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public boolean isCountryAllowed() {
        return countryAllowed;
    }

    public void setCountryAllowed(boolean countryAllowed) {
        this.countryAllowed = countryAllowed;
    }
}
