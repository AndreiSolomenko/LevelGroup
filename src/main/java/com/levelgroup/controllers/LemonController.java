package com.levelgroup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelgroup.DeviceInfo;
import com.levelgroup.DeviceInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

import org.springframework.util.StreamUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StreamUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@RestController
public class LemonController {

    @Autowired
    private DeviceInfoRepository deviceRepo;

    private static final String LEMON_SECRET = "qazwsx";

    @PostMapping("/device-register")
    public ResponseEntity<Map<String, String>> registerDevice(@RequestBody Map<String, String> body) {
        String deviceId = body.get("device_id");
        if (deviceId == null || deviceId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing device_id"));
        }

        Optional<DeviceInfo> existing = deviceRepo.findByDeviceId(deviceId);
        if (existing.isEmpty()) {
            DeviceInfo newDevice = new DeviceInfo(deviceId);
            deviceRepo.save(newDevice);
            System.out.println("📥 Зареєстровано новий пристрій: " + deviceId);
        }

        return ResponseEntity.ok(Map.of("message", "Device registered successfully"));
    }

    @PostMapping("/webhook-new")
    public ResponseEntity<Map<String, String>> handleWebhook(HttpServletRequest request) {
        try {
            String signature = request.getHeader("X-Signature");
            if (signature == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing signature"));
            }

            String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            String computedSignature = hmacSha256(LEMON_SECRET, rawBody);
            if (!computedSignature.equals(signature)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid signature"));
            }

            Map<String, Object> payload = new ObjectMapper().readValue(rawBody, Map.class);
            String eventName = (String) ((Map<String, Object>) payload.get("meta")).get("event_name");

            if ("order_created".equals(eventName) || "order_paid".equals(eventName)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                String email = (String) attributes.get("user_email");

                Map<String, Object> meta = (Map<String, Object>) payload.get("meta");
                Map<String, Object> customData = (Map<String, Object>) meta.get("custom_data");
                String deviceId = (String) customData.get("device_id");

                if (email != null && deviceId != null) {
                    Optional<DeviceInfo> infoOpt = deviceRepo.findByDeviceId(deviceId);
                    if (infoOpt.isPresent()) {
                        DeviceInfo info = infoOpt.get();
                        info.setEmail(email);
                        info.setPermanentlyActivated(true);
                        info.setTemporarilyActivated(false);
                        deviceRepo.save(info);
                        System.out.println("✅ Користувач активований: " + email + " для пристрою " + deviceId);
                    } else {
                        System.out.println("⚠️ Пристрій не знайдено у базі: " + deviceId);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("message", "Webhook received successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/check-activation-new")
    public ResponseEntity<Map<String, Object>> checkActivation(@RequestParam("device_id") String deviceId) {
        Optional<DeviceInfo> infoOpt = deviceRepo.findByDeviceId(deviceId);

        if (infoOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("activated", false));
        }

        DeviceInfo info = infoOpt.get();

        if (info.isPermanentlyActivated()) {
            return ResponseEntity.ok(Map.of("activated", true));
        }

        if (info.isTemporarilyActivated() && info.getCheckCounter() < 10) {
            info.setCheckCounter(info.getCheckCounter() + 1);
            deviceRepo.save(info);
            System.out.println("🔓 Тимчасова активація для " + deviceId + ", лічильник: " + info.getCheckCounter());
            return ResponseEntity.ok(Map.of("activated", true));
        }

        info.setTemporarilyActivated(false);
        deviceRepo.save(info);
        System.out.println("⛔ Тимчасова активація завершена для " + deviceId);
        return ResponseEntity.ok(Map.of("activated", false));
    }


    @PostMapping("/authorize")
    public ResponseEntity<String> authorize(@RequestBody DeviceInfo deviceInfo) {
        String email = deviceInfo.getEmail();
        String deviceId = deviceInfo.getDeviceId();

        // Перевіряємо, чи є хоча б один пристрій з таким email
        List<DeviceInfo> existingDevices = deviceRepo.findAllByEmail(email);

        if (existingDevices.isEmpty()) {
            // Email ще не використовувався — користувач не купував
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found. Purchase required.");
        }

        // Якщо такий deviceId вже є, повертаємо true
        boolean alreadyAuthorized = existingDevices.stream()
                .anyMatch(d -> d.getDeviceId().equals(deviceId) && d.isPermanentlyActivated());

        if (alreadyAuthorized) {
            return ResponseEntity.ok("true");
        }

        // Інакше додаємо новий deviceId для існуючого email
        DeviceInfo newDevice = new DeviceInfo();
        newDevice.setEmail(email);
        newDevice.setDeviceId(deviceId);
        newDevice.setPermanentlyActivated(true);
        newDevice.setCheckCounter(0);

        deviceRepo.save(newDevice);

        return ResponseEntity.ok("true");
    }



    // хелпер для підпису
    private static String hmacSha256(String secret, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return bytesToHex(sha256_HMAC.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error while generating HMAC", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
