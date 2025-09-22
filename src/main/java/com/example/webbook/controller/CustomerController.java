package com.example.webbook.controller;

import com.example.webbook.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @GetMapping("/home")
    public String customerHome(Model model) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Add user info to model
        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("userName", userDetails.getFullName());

        return "users/customer/home";
    }

    @GetMapping("/profile")
    public String customerProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        model.addAttribute("currentUser", userDetails.getUser());

        return "users/customer/profile";
    }
}