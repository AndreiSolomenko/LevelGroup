package com.levelgroup;

import jakarta.persistence.*;

@Entity
@Table(name = "device_info")
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "email")
    private String email;

    @Column(name = "check_counter")
    private int checkCounter = 0;

    @Column(name = "permanently_activated")
    private boolean permanentlyActivated = false;

    @Column(name = "temporarily_activated")
    private boolean temporarilyActivated = true;

    public DeviceInfo() {}

    public DeviceInfo(String deviceId) {
        this.deviceId = deviceId;
        this.temporarilyActivated = true;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

}
