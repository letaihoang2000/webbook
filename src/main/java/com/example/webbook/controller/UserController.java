package com.example.webbook.controller;

import com.example.webbook.dto.AddUserForm;
import com.example.webbook.dto.UserInfo;
import com.example.webbook.exception.EmailAlreadyExistsException;
import com.example.webbook.model.User;
import com.example.webbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String adminIndex(){
        return "users/admin/home";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserInfo> usersInfo = userService.getAllUsersInfo();
        model.addAttribute("users", usersInfo);
        return "users/admin/user_index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createUser(@ModelAttribute AddUserForm addUserForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            User newUser = userService.createUser(addUserForm);
            response.put("success", true);
            response.put("message", "User created successfully!");
            return ResponseEntity.ok(response);

        } catch (EmailAlreadyExistsException e) {
            // Handle email already exists specifically
            response.put("success", false);
            response.put("errorType", "email");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            // Handle all other exceptions
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while creating the user.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
