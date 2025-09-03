package com.example.webbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserForm {
    private String first_name;
    private String last_name;
    private String email;
    private String mobile;
    private String address;
    private String password;
}
