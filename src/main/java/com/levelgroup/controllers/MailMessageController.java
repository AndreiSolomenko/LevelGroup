package com.levelgroup.controllers;

import com.levelgroup.AmegaFormData;
import com.levelgroup.FormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//@CrossOrigin(origins = "http://localhost:3000")
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



    @PostMapping("/a-mega/submit-form")
    public ResponseEntity<?> amegaSubmitForm(@ModelAttribute AmegaFormData amegaFormData) {
        File savedFile = null;
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true - для підтримки вкладень

            helper.setTo("andrsolo10@ukr.net");
            helper.setSubject("Form submission from the site");

            String text = "Name: " + amegaFormData.getName() +
                    "\nPhone: " + amegaFormData.getPhone() +
                    "\nEmail: " + amegaFormData.getEmail() +
                    "\nComment: " + amegaFormData.getComment();
            helper.setText(text);

            // Якщо файл присутній, зберігаємо його локально
            if (amegaFormData.getFile() != null && !amegaFormData.getFile().isEmpty()) {
                savedFile = new File("uploads/" + amegaFormData.getFile().getOriginalFilename());
                savedFile.getParentFile().mkdirs(); // Створити директорію, якщо її немає
                try (FileOutputStream fos = new FileOutputStream(savedFile)) {
                    fos.write(amegaFormData.getFile().getBytes());
                }
                text += "\nFile uploaded: " + amegaFormData.getFile().getOriginalFilename();
                helper.setText(text); // Оновлюємо текст повідомлення
            }

            // Відправка електронної пошти
            emailSender.send(message);

            // Видалити файл після обробки
            if (savedFile != null && savedFile.exists()) {
                if (savedFile.delete()) {
                    System.out.println("File deleted successfully: " + savedFile.getName());
                } else {
                    System.out.println("Failed to delete the file: " + savedFile.getName());
                }
            }

            return ResponseEntity.ok().body(new ResponseMessage("Email sent successfully!"));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }

    public static class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
