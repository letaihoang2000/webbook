package com.example.webbook.dto;

public class AuthorInfo {
    private String author_id;
    private String name;
    private String image;
    private String description;
    private int book_count;

    public AuthorInfo() {
    }

    public AuthorInfo(String author_id, String name, String image, String description, int book_count) {
        this.author_id = author_id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.book_count = book_count;
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBook_count() {
        return book_count;
    }

    public void setBook_count(int book_count) {
        this.book_count = book_count;
    }
}
