package com.levelgroup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StreamUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;


@RestController
public class LemonServer {

    @Autowired
    private MailService mailService;

    private static final String LEMON_SECRET = "qazwsx";

    private static final Set<String> activatedEmails = new HashSet<>();
    private static final Map<String, String> emailDeviceMap = new HashMap<>();
    private static final Map<String, String> confirmationTokens = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(LemonServer.class, args);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(HttpServletRequest request) {
        try {
            String signature = request.getHeader("X-Signature");
            if (signature == null) {
                System.out.println("❌ Відсутній підпис у заголовку.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing signature"));
            }

            String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            String computedSignature = hmacSha256(LEMON_SECRET, rawBody);

            if (!computedSignature.equals(signature)) {
                System.out.println("❌ Підписи не збігаються.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid signature"));
            }

            // Парсимо тіло в JSON
            Map<String, Object> payload = new ObjectMapper().readValue(rawBody, Map.class);
            String eventName = (String) ((Map<String, Object>) payload.get("meta")).get("event_name");
            System.out.println("🔔 Подія отримана: " + eventName);

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

            return ResponseEntity.ok(Map.of("message", "Webhook received successfully"));

        } catch (Exception e) {
            System.err.println("❌ Помилка обробки вебхука: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/auth")
    public Map<String, Object> authenticate(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String deviceId = request.get("device_id");

        System.out.println("🔍 Перевірка авторизації для: " + email + " (" + deviceId + ")");

        if (activatedEmails.contains(email)) {
            if (emailDeviceMap.containsKey(email)) {
                String existingDevice = emailDeviceMap.get(email);
                if (!existingDevice.equals(deviceId)) {
                    // Генеруємо токен для підтвердження
                    String token = UUID.randomUUID().toString();
                    confirmationTokens.put(token, email + "|" + deviceId);

                    mailService.sendConfirmationEmail(email, token);

                    System.out.println("🔒 Відправлено на підтвердження: " + token);

                    return Map.of(
                            "activated", false,
                            "confirm_needed", true,
                            "message", "This email is already activated on another device. Confirmation required."
                    );
                }
                return Map.of("activated", true);
            }

            emailDeviceMap.put(email, deviceId);
            return Map.of("activated", true);
        }

        return Map.of("activated", false, "message", "Email not found in the database.");
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmDeviceChange(@RequestParam("token") String token) {
        if (!confirmationTokens.containsKey(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Invalid or expired token.");
        }

        String[] parts = confirmationTokens.get(token).split("\\|");
        String email = parts[0];
        String newDeviceId = parts[1];

        emailDeviceMap.put(email, newDeviceId);
        confirmationTokens.remove(token);

        return ResponseEntity.ok("✅ Device successfully updated for " + email);
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
