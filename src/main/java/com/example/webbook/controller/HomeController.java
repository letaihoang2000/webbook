package com.example.webbook.controller;

import com.example.webbook.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping("/")
    public String index() {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            // User is authenticated, redirect based on role
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String roleName = userDetails.getRoleName();

            if ("ADMIN".equals(roleName)) {
                return "redirect:/user/home";
            } else if ("USER".equals(roleName)) {
                return "redirect:/customer/home";
            }
        }

        // User is not authenticated, redirect to login
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        // Check if user is already authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            // User is already logged in, redirect to appropriate home
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String roleName = userDetails.getRoleName();

            if ("ADMIN".equals(roleName)) {
                return "redirect:/user/home";
            } else if ("USER".equals(roleName)) {
                return "redirect:/customer/home";
            }
        }

        return "login";
    }
}
