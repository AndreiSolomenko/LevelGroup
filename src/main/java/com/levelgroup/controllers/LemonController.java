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

public class LemonController {

    private static final String API_KEY= "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5NGQ1OWNlZi1kYmI4LTRlYTUtYjE3OC1kMjU0MGZjZDY5MTkiLCJqdGkiOiJmMTRmYjU2ZTBiMjE2YmIyOGRkMzlmNjRhYTUwZTU3ODZjYTJhZGY0MWJkOWVkMzk4MmYwMzY4ZGNhYjM0MzIxMWMxNTIwNTgwMjhjMzdmMCIsImlhdCI6MTc0NDMwMDUxOS44MTI1NCwibmJmIjoxNzQ0MzAwNTE5LjgxMjU0MywiZXhwIjoyMDU5ODMzMzE5Ljc4MzMxNCwic3ViIjoiNDU0MDE0MCIsInNjb3BlcyI6W119.T8z6EP6bY9HkWjhgAI0opiIEluJXx2GplqULpdUi3ACxA3o8JR7GcffRER_EeTGgh21KWf4v1zh_XZw1D0nAAHUUoDuAe7Ux57ZG2kBJPxp0uYujBIxIN0h2Hs1l_hecAOGE0gcl_j7Cdu2lo2unLriKccPr_lEjOs_eJSd64ZlwpTbckyEbvR82mwEWvx3phTSyDw2rhpRuLPzrH9qLbeBpICHupCMRzdQ0GYevqfLeE40Oe8gsK7OdMttkUXdLsyQeHT0dgpIHwlUxZspyHNi5MnJHvdwiGss_VVjig8hy-1IcymIgYdD1OvatFAojW13KWFXVUCm83FUWVQMYjNrQmzZ9YK6ZgYHT7ZauTCXGZrFkIS7eDpaTxTAO21WGK_KLrINAfRI5n_tfkUs7pam9PPbg7ldDm0d5nt8esJYbg5VoFASXQaQ9hSxISQEfthEwEvOmiZW2Hhlm1kX5KooibOpzFRZoo6n7cuo21z_v7Aa4ZvKTnsrSJWRR6Yhc";
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

    private String getLicenseKeyFromOrder(String orderId) {
        String url = "https://api.lemonsqueezy.com/v1/orders/" + orderId + "/relationships/license-keys";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);

        // Виконуємо запит до Lemon Squeezy API для отримання ліцензійного ключа
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<String, Object> licenseKeys = (Map<String, Object>) response.getBody().get("data");

            if (licenseKeys != null && !licenseKeys.isEmpty()) {
                Map<String, Object> firstLicenseKey = (Map<String, Object>) licenseKeys.get(0);
                return (String) firstLicenseKey.get("id");
            }
        } catch (Exception e) {
            System.out.println("❌ Помилка при отриманні ліцензійного ключа: " + e.getMessage());
        }
        return null;
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
