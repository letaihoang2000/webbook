package com.example.webbook.dto;

import org.springframework.web.multipart.MultipartFile;

public class UpdateUserForm {
    private String id;
    private String first_name;
    private String last_name;
    // Email is not included since it's unchangeable
    private String mobile;
    private String address;
    private String currentPassword;
    private String password;
    private MultipartFile imageFile;

    public UpdateUserForm() {}

    public UpdateUserForm(String id, String first_name, String last_name, String mobile,
                          String address, String password, MultipartFile imageFile) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.mobile = mobile;
        this.address = address;
        this.password = password;
        this.imageFile = imageFile;
    }

    public UpdateUserForm(String id, String first_name, String last_name, String mobile, String address, String currentPassword, String password, MultipartFile imageFile) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.mobile = mobile;
        this.address = address;
        this.currentPassword = currentPassword;
        this.password = password;
        this.imageFile = imageFile;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
}
