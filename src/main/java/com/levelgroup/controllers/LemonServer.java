package com.levelgroup.controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@RestController
public class LemonServer {
    private static final String LEMON_SECRET = "твій_секретний_ключ";
    private final Set<String> activatedUsers = new HashSet<>();

    private static final Set<String> activatedEmails = new HashSet<>();
    private static final Map<String, String> emailDeviceMap = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(LemonServer.class, args);
    }

//    @PostMapping("/webhook")
//    public Map<String, String> handleWebhook(@RequestHeader("x-signature") String signature, @RequestBody Map<String, Object> payload) {
//        String computedSignature = hmacSha256(LEMON_SECRET, payload.toString());
//
//        if (!signature.equals(computedSignature)) {
//            return Map.of("message", "Invalid signature");
//        }
//
//        String eventName = (String) payload.get("event_name");
//        Map<String, Object> data = (Map<String, Object>) payload.get("data");
//        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
//        String email = (String) attributes.get("user_email");
//
//        if ("subscription_created".equals(eventName) || "order_paid".equals(eventName)) {
//            activatedUsers.add(email);
//            System.out.println("User activated: " + email);
//        }
//
//        return Map.of("message", "Webhook received");
//    }


    @PostMapping("/webhook")
    public Map<String, String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Отримуємо назву події
            String eventName = (String) ((Map<String, Object>) payload.get("meta")).get("event_name");
            System.out.println("🔔 Подія отримана: " + eventName);

            // Перевіряємо, чи це подія про успішну оплату
            if ("order_created".equals(eventName) || "order_paid".equals(eventName)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                String email = (String) attributes.get("user_email");

                if (email != null) {
                    activatedEmails.add(email);
                    System.out.println("✅ Користувач активований: " + email);
                } else {
                    System.out.println("⚠️ Email не знайдено у вебхуці.");
                }
            } else {
                System.out.println("ℹ️ Інша подія, ігноруємо.");
            }

            return Map.of("message", "Webhook received successfully");

        } catch (Exception e) {
            System.err.println("❌ Помилка обробки вебхука: " + e.getMessage());
            return Map.of("error", "Internal server error");
        }
    }

    @PostMapping("/auth")
    public Map<String, Object> authenticate(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String deviceId = request.get("device_id");

        System.out.println("🔍 Перевірка авторизації для: " + email + " (" + deviceId + ")");

        if (activatedEmails.contains(email)) {
            if (emailDeviceMap.containsKey(email)) {
                return Map.of("message", "This email is already activated on another device.", "activated", false);
            }
            emailDeviceMap.put(email, deviceId);
            return Map.of("activated", true);
        }
        return Map.of("message", "Email not found in the database.", "activated", false);
    }



    @PostMapping("/check-activation")
    public Map<String, Boolean> checkActivation(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String deviceId = request.get("device_id");

        if (email == null || deviceId == null) {
            return Map.of("activated", false);
        }

        // Перевіряємо, чи email активований і чи збігається device_id
        if (emailDeviceMap.containsKey(email) && emailDeviceMap.get(email).equals(deviceId)) {
            return Map.of("activated", true);
        }

        return Map.of("activated", false);
    }


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
