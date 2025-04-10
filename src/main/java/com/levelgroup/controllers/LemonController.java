package com.levelgroup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String LEMON_SECRET = "qazwsx";
    private static final Set<String> activatedEmails = new HashSet<>();
    private static final Map<String, String> emailDeviceMap = new HashMap<>();
    private static final Map<String, String> confirmationTokens = new HashMap<>();
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(LemonServer.class, args);
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

            // Парсимо тіло в JSON
            Map<String, Object> payload = new ObjectMapper().readValue(rawBody, Map.class);
            String eventName = (String) ((Map<String, Object>) payload.get("meta")).get("event_name");

            if ("order_created".equals(eventName) || "order_paid".equals(eventName)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                String email = (String) attributes.get("user_email");
                String orderId = (String) attributes.get("identifier");

                // Отримуємо custom_data з мета
                Map<String, Object> meta = (Map<String, Object>) payload.get("meta");
                Map<String, Object> customData = (Map<String, Object>) meta.get("custom_data");
                String deviceId = null;

                if (customData != null && customData.containsKey("device_id")) {
                    deviceId = (String) customData.get("device_id");
                }


                if (email != null && deviceId != null) {
                    activatedEmails.add(email);
                    emailDeviceMap.put(email, deviceId);
                    System.out.println("✅ Користувач активований НОВИЙ КОНТРОЛЛЕР: " + email + " для пристрою " + deviceId);
                } else {
                    System.out.println("⚠️ Email або device_id не знайдено у вебхуці.");
                }
            }

            return ResponseEntity.ok(Map.of("message", "Webhook received successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }


    @GetMapping("/check-activation-new")
    public ResponseEntity<Map<String, Object>> checkActivation(@RequestParam("device_id") String deviceId) {
        boolean isActivated = emailDeviceMap.containsValue(deviceId);
        return ResponseEntity.ok(Map.of("activated", isActivated));
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
