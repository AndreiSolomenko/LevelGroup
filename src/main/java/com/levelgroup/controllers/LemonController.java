package com.levelgroup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelgroup.CountryService;
import com.levelgroup.DeviceInfo;
import com.levelgroup.DeviceInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.*;

import org.springframework.util.StreamUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;

@RestController
public class LemonController {

    @Autowired
    private DeviceInfoRepository deviceRepo;

    @Autowired
    private CountryService countryService;

    private static final String LEMON_SECRET = "qazwsx";

    @PostMapping("/device-register")
    public ResponseEntity<Map<String, Object>> registerDevice(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String deviceId = body.get("device_id");
        if (deviceId == null || deviceId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing device_id"));
        }

        Optional<DeviceInfo> existing = deviceRepo.findByDeviceId(deviceId);
        boolean countryAllowed = false;

        if (existing.isEmpty()) {
            DeviceInfo newDevice = new DeviceInfo(deviceId);

            // define country
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            String country = null;
            try {
                RestTemplate restTemplate = new RestTemplate();
                String url = "http://ip-api.com/json/" + ipAddress;
                ResponseEntity<Map> geoResponse = restTemplate.getForEntity(url, Map.class);
                country = (String) geoResponse.getBody().get("country");
                newDevice.setCountry(country);
            } catch (Exception e) {
                System.out.println("⚠️ Unable to determine country for IP: " + ipAddress);
            }

            //activation
            if (country == null || !countryService.isAllowed(country)) {
                // Permanent activation if country is not defined or not allowed
                newDevice.setPermanentlyActivated(true);
                newDevice.setTemporarilyActivated(false);
                newDevice.setCountryAllowed(false);
                System.out.println("🌍 Country " + (country != null ? country : "unknown") + " not in the list — activation is permanent.");
            } else {
                // Temporary activation for allowed countries
                newDevice.setPermanentlyActivated(false);
                newDevice.setTemporarilyActivated(true);
                newDevice.setCountryAllowed(true);
                countryAllowed = true;
                System.out.println("✅ Country " + country + " allowed — temporary activation.");
            }

            deviceRepo.save(newDevice);
            System.out.println("📥 New device registered: " + deviceId + " | Country: " + newDevice.getCountry());
        } else {
            countryAllowed = existing.get().isCountryAllowed();
        }

        return ResponseEntity.ok(Map.of(
                "activated", false,
                "tempActivated", true,
                "countryAllowed", countryAllowed
        ));
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


                Map<String, Object> firstOrderItem = (Map<String, Object>) attributes.get("first_order_item");
                String productName = (String) firstOrderItem.get("product_name");


                Map<String, Object> meta = (Map<String, Object>) payload.get("meta");
                Map<String, Object> customData = (Map<String, Object>) meta.get("custom_data");
                String deviceId = (String) customData.get("device_id");

                if (email != null && deviceId != null) {
                    Optional<DeviceInfo> infoOpt = deviceRepo.findByDeviceId(deviceId);
                    if (infoOpt.isPresent()) {
                        DeviceInfo info = infoOpt.get();
                        info.setEmail(email);
                        info.setTemporarilyActivated(false);

                        LocalDate today = LocalDate.now();

                        switch (productName) {
                            case "Youtube Pop Out Player (MONTHLY PLAN - $2 / month)":
                                info.setSubscriptionUntil(today.plusMonths(1));
                                info.setPermanentlyActivated(false);
                                break;
                            case "Youtube Pop Out Player (YEARLY PLAN - $1 / month)":
                                info.setSubscriptionUntil(today.plusYears(1));
                                info.setPermanentlyActivated(false);
                                break;
                            case "Youtube Pop Out Player (LIFETIME PLAN - $20 / lifetime)":
                                info.setSubscriptionUntil(null);
                                info.setPermanentlyActivated(true);
                                break;
                            default:
                                System.out.println("⚠️ Unknown subscription type: " + productName);
                        }

                        deviceRepo.save(info);
                        System.out.println("✅ User activated: " + email + " for the device " + deviceId);
                    } else {
                        System.out.println("⚠️ Device not found in the database: " + deviceId);
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
            return ResponseEntity.ok(Map.of(
                    "activated", false,
                    "tempActivated", false
            ));
        }

        DeviceInfo info = infoOpt.get();

        if (info.isPermanentlyActivated()) {
            return ResponseEntity.ok(Map.of(
                    "activated", true,
                    "tempActivated", false
            ));
        }

        if (info.getSubscriptionUntil() != null && LocalDate.now().isBefore(info.getSubscriptionUntil())) {
            return ResponseEntity.ok(Map.of(
                    "activated", true,
                    "tempActivated", false
            ));
        }

        if (info.isTemporarilyActivated() && info.getCheckCounter() < 3) {
            info.setCheckCounter(info.getCheckCounter() + 1);
            deviceRepo.save(info);
            System.out.println("🔓 Temporary activation for " + deviceId + ", counter: " + info.getCheckCounter());
            return ResponseEntity.ok(Map.of(
                    "activated", false,
                    "tempActivated", true
            ));
        }

        info.setTemporarilyActivated(false);
        deviceRepo.save(info);
        System.out.println("⛔ Temporary activation completed for " + deviceId);
        return ResponseEntity.ok(Map.of(
                "activated", false,
                "tempActivated", false
        ));
    }


    @PostMapping("/authorize")
    public ResponseEntity<String> authorize(@RequestBody DeviceInfo deviceInfo) {
        String email = deviceInfo.getEmail();
        String deviceId = deviceInfo.getDeviceId();

        // 1. find all devices with this email
        List<DeviceInfo> existingByEmail = deviceRepo.findAllByEmail(email);

        if (existingByEmail.isEmpty()) {
            // Email has not been used yet — user has not purchased
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found. Purchase required.");
        }

        // 2. Check if at least one of them is activated
        boolean isEmailActivated = existingByEmail.stream().anyMatch(info ->
                info.isPermanentlyActivated() ||
                        (info.getSubscriptionUntil() != null && LocalDate.now().isBefore(info.getSubscriptionUntil())) ||
                        (info.isTemporarilyActivated() && info.getCheckCounter() < 10)
        );

        if (!isEmailActivated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email is not activated.");
        }

        // 3. Searching for a device by deviceId
        Optional<DeviceInfo> existingDevice = deviceRepo.findByDeviceId(deviceId);

        if (existingDevice.isPresent()) {
            DeviceInfo info = existingDevice.get();
            info.setEmail(email);
            info.setCheckCounter(0);

            // Copy the activation type from activated devices
            DeviceInfo source = existingByEmail.stream().filter(e ->
                    e.isPermanentlyActivated() ||
                            (e.getSubscriptionUntil() != null && LocalDate.now().isBefore(e.getSubscriptionUntil())) ||
                            (e.isTemporarilyActivated() && e.getCheckCounter() < 10)
            ).findFirst().get();

            info.setPermanentlyActivated(source.isPermanentlyActivated());
            info.setTemporarilyActivated(source.isTemporarilyActivated());
            info.setSubscriptionUntil(source.getSubscriptionUntil());

            deviceRepo.save(info);
        } else {
            DeviceInfo newDevice = new DeviceInfo();
            newDevice.setDeviceId(deviceId);
            newDevice.setEmail(email);
            newDevice.setCheckCounter(0);

            // Copy from the active device
            DeviceInfo source = existingByEmail.stream().filter(e ->
                    e.isPermanentlyActivated() ||
                            (e.getSubscriptionUntil() != null && LocalDate.now().isBefore(e.getSubscriptionUntil())) ||
                            (e.isTemporarilyActivated() && e.getCheckCounter() < 10)
            ).findFirst().get();

            newDevice.setPermanentlyActivated(source.isPermanentlyActivated());
            newDevice.setTemporarilyActivated(source.isTemporarilyActivated());
            newDevice.setSubscriptionUntil(source.getSubscriptionUntil());

            deviceRepo.save(newDevice);
        }

        System.out.println("✅ Device activation " + deviceId + " for email: " + email);
        return ResponseEntity.ok("true");
    }

    @GetMapping("/devices")
    public ModelAndView showAllDevices() {
        List<DeviceInfo> devices = deviceRepo.findAll();
        ModelAndView mav = new ModelAndView("devices");
        mav.addObject("devices", devices);
        return mav;
    }


    // signature helper
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

    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "ok";
    }


    @GetMapping("/")
    public String root() {
        return "ok";
    }
}
