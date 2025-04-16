package com.levelgroup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {
    Optional<DeviceInfo> findByDeviceId(String deviceId);
    Optional<DeviceInfo> findByEmail(String email);
}
