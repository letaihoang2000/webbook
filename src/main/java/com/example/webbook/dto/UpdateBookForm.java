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

    public UpdateBookForm() {
    }

    public UpdateBookForm(String book_id, String title, MultipartFile imageFile, String description, LocalDate published_date, Integer page, Double price, MultipartFile contentFile, String author, String category_type) {
        this.book_id = book_id;
        this.title = title;
        this.imageFile = imageFile;
        this.description = description;
        this.published_date = published_date;
        this.page = page;
        this.price = price;
        this.contentFile = contentFile;
        this.author = author;
        this.category_type = category_type;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPublished_date() {
        return published_date;
    }

    public void setPublished_date(LocalDate published_date) {
        this.published_date = published_date;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public MultipartFile getContentFile() {
        return contentFile;
    }

    public void setContentFile(MultipartFile contentFile) {
        this.contentFile = contentFile;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory_type() {
        return category_type;
    }

    public void setCategory_type(String category_type) {
        this.category_type = category_type;
    }
}
