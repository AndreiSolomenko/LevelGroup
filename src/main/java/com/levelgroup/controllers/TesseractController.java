package com.levelgroup.controllers;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TesseractController {

    @PostMapping("/api/process-image")
    public Response processImage(@RequestParam("image") MultipartFile image, @RequestParam("language") String language) {
        try {
            // Перевіряємо, чи вказано файл і мова
            if (image.isEmpty()) {
                throw new IllegalArgumentException("Не завантажено зображення.");
            }
            if (language == null || language.trim().isEmpty()) {
                throw new IllegalArgumentException("Мова не вказана.");
            }

            // Перетворюємо MultipartFile в BufferedImage
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Невірний формат зображення.");
            }

            // Створюємо екземпляр Tesseract
            ITesseract instance = new Tesseract();

            // Встановлюємо шлях до tessdata
            String tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata";

            // Перевіряємо, чи існує директорія tessdata
            File tessDataDir = new File(tessDataPath);
            if (!tessDataDir.exists() || !tessDataDir.isDirectory()) {
                throw new RuntimeException("Не вдалося знайти директорію 'tessdata'.");
            }

            // Встановлюємо datapath для Tesseract
            instance.setDatapath(tessDataPath);

            // Встановлюємо мову для розпізнавання
            instance.setLanguage(language);

            // Виконуємо розпізнавання тексту
            String resultText = instance.doOCR(bufferedImage);

            // Повертаємо результат як JSON
            return new Response(resultText);

        } catch (IllegalArgumentException e) {
            return new Response("Помилка: " + e.getMessage()); // Повертаємо конкретну помилку
        } catch (IOException e) {
            return new Response("Помилка при обробці зображення: " + e.getMessage());
        } catch (Exception e) {
            return new Response("Помилка OCR: " + e.getMessage());
        }
    }

    // Клас відповіді
    public static class Response {
        private String text;

        public Response(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
