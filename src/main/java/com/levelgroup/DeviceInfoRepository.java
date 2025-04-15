package com.levelgroup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, String> {
    Optional<DeviceInfo> findByEmail(String email);
}
