package com.levelgroup.controllers;

import com.levelgroup.FormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class MailMessageController {

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/submit-form")
    public ResponseEntity<String> submitForm(@RequestBody FormData formData) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("andrsolo10@ukr.net");
            message.setSubject("Form Submission");
            message.setText("Name: " + formData.getName() +
                    "\nPhone: " + formData.getPhone() +
                    "\nEmail: " + formData.getEmail());
            emailSender.send(message);
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }
}
