package com.example.webbook.dto;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class UpdateBookForm {
    private String book_id;
    private String title;
    private MultipartFile imageFile;
    private String description;
    private LocalDate published_date;
    private Integer page;
    private Double price;
    private MultipartFile contentFile;
    private String author;
    private String category_type;
}
