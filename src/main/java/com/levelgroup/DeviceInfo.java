package com.levelgroup;

public class DeviceInfo {
    public final String deviceId;
    public final String ip;
    public int checkCounter = 0;
    public boolean permanentlyActivated = false;
    public boolean temporarilyActivated = true; // нове поле

    public DeviceInfo(String deviceId, String ip) {
        this.deviceId = deviceId;
        this.ip = ip;
    }
}
