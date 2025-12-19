package com.example.webbook.dto;

import org.springframework.web.multipart.MultipartFile;

public class AddAuthorForm {
    private String name;
    private MultipartFile image_file;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MultipartFile getImage_file() {
        return image_file;
    }

    public void setImage_file(MultipartFile image_file) {
        this.image_file = image_file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
