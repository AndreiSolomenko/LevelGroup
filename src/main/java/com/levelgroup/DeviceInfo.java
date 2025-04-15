package com.levelgroup;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "device_info")
public class DeviceInfo {

    @Id
    private String deviceId;

    private String email; // може бути null до покупки
    private int checkCounter = 0;
    private boolean permanentlyActivated = false;
    private boolean temporarilyActivated = true;

    public DeviceInfo() {}

    public DeviceInfo(String deviceId) {
        this.deviceId = deviceId;
        this.temporarilyActivated = true;
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

    // Геттери та сеттери
}
