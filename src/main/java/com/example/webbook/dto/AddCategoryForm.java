package com.example.webbook.dto;

public class AddCategoryForm {
    private String name;

    // Constructors
    public AddCategoryForm() {
    }

    public AddCategoryForm(String name) {
        this.name = name;
    }

    // Getter
    public String getName() {
        return name;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }
}
