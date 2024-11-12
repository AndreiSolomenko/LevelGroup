package com.levelgroup;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AmegaFormData {

    private String name;
    private String phone;
    private String email;
    private String comment;
    private MultipartFile file;
}
