package com.example.webbook.dto;

import org.springframework.web.multipart.MultipartFile;

public class UserRegister {
    private String first_name;
    private String last_name;
    private MultipartFile avatar;
    private String email;
    private String password;
    private String mobile;
    private String address;

    public UserRegister() {
    }

    public UserRegister(String first_name, String last_name, MultipartFile avatar, String email, String password, String mobile, String address) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.avatar = avatar;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.address = address;
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

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
