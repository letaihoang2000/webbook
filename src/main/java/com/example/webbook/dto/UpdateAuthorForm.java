package com.example.webbook.dto;

import org.springframework.web.multipart.MultipartFile;

public class UpdateAuthorForm {
    private String author_id;
    private String name;
    private MultipartFile image_file;
    private String description;

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

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
