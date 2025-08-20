package com.example.webbook.controller;

import com.example.webbook.dto.UserInfo;
import com.example.webbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<UserInfo> usersInfo = userService.getAllUsersInfo();
        model.addAttribute("users", usersInfo);
        return "users/admin/user_index";
    }
}
