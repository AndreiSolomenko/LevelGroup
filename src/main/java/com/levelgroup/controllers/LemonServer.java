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
import java.nio.charset.StandardCharsets;
import org.springframework.web.client.RestTemplate;


@RestController
public class LemonServer {

    @Autowired
    private MailService mailService;

    private static final String LEMON_SECRET = "qazwsx";
    private static final String API_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5NGQ1OWNlZi1kYmI4LTRlYTUtYjE3OC1kMjU0MGZjZDY5MTkiLCJqdGkiOiJjNDMyYTAyNWJkYmEzZTg1OWY1NjJhNDkzYWRkODMwMDM3NmU1N2IwZDBkZjJlODA0ODYwODkxNTM2ZjgwYmRjODdiZTA2YzIyOTgyN2EyOSIsImlhdCI6MTc0NDI4Nzk3NC40NjY4OTgsIm5iZiI6MTc0NDI4Nzk3NC40NjY5LCJleHAiOjIwNTk4MjA3NzQuNDI5OTE2LCJzdWIiOiI0NTQwMTQwIiwic2NvcGVzIjpbXX0.uzsaOq7Iy6vP3_1IoVaO3fF3qqAMeY1Z57AYVATI_MNZMB3ipLfcw01BCA8lO7kWErSEZm6xt44VsVFjppvikqdC97rLWMYh3yruZ-5NGtz8DVE1v4Jq76Fme6wN5M08Fn_U9r_HwVxTKiOIoi8neoABLPkWKD-iWfqulvYUCsHfvf5e-Bj39-_pKs2UmYo0SuAjr12GRbYns51HQocnbWQKssXrb4zBDrLKtdKAYpOe4Srd4H5VBM04ynhe9KOUoSxNR8LydaYNLrn07mg8TR_c-pTS5iHmMYNnPA7mPMUthQokstqFsBKkSSkv8kAI1gJ2cAJl3BQh0s6JCnRMCTWbwO-UJZNWen93Yx96ezRlWBkjxsS2DY32D79CdrLcEspbRBCskHxY9wCvpCF4o5pYax5TY731dhjuKddlnrzm8bUZELlW_0ilWYIYniwgs-XvxayVh-lrOGIkhqFOS3ynTkrozVTRDiKsk9r95wfrV3G_MrI163JpU6TyPcDr";

    private static final Set<String> activatedEmails = new HashSet<>();
    private static final Map<String, String> emailDeviceMap = new HashMap<>();
    private static final Map<String, String> confirmationTokens = new HashMap<>();
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(LemonServer.class, args);
    }

    @PostMapping("/webhook")
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

                // Отримуємо license_key
                String licenseKey = getLicenseKeyFromOrder(orderId);

                if (email != null && licenseKey != null) {
                    activatedEmails.add(email);
                    emailDeviceMap.put(email, licenseKey);
                    System.out.println("✅ Користувач активований: " + email + " для пристрою з ключем " + licenseKey);
                } else {
                    System.out.println("⚠️ Email або license_key не знайдено у вебхуці.");
                }
            }

            return ResponseEntity.ok(Map.of("message", "Webhook received successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    private String getLicenseKeyFromOrder(String orderId) {
        String url = "https://api.lemonsqueezy.com/v1/orders/" + orderId + "/license-keys";
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

                    return Map.of("activated", true);
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
