package com.levelgroup.controllers;

import net.sourceforge.tess4j.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TesseractController {

    @PostMapping("/api/process-image")
    public Map<String, String> processImage(@RequestParam("image") MultipartFile image, @RequestParam("language") String language) {
        try {

            if (image.isEmpty()) {
                throw new IllegalArgumentException("No image loaded.");
            }
            if (language == null || language.trim().isEmpty()) {
                throw new IllegalArgumentException("The language is not specified.");
            }

            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            ITesseract instance = new Tesseract();

            instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
            instance.setLanguage(language);

            String recognizedText = instance.doOCR(bufferedImage);

            Map<String, String> response = new HashMap<>();
            response.put("text", recognizedText);
            return response;

        } catch (IOException e) {
            throw new RuntimeException("Error occurred while processing the image", e);
        } catch (TesseractException e) {
            throw new RuntimeException("OCR error", e);
        }
    }
}
