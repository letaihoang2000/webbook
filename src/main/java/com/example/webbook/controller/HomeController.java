package com.example.webbook.controller;

import com.example.webbook.dto.UserRegister;
import com.example.webbook.exception.EmailAlreadyExistsException;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.CartService;
import com.example.webbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

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
                return "redirect:/admin/dashboard";
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
                return "redirect:/admin/dashboard";
            } else if ("USER".equals(roleName)) {
                return "redirect:/customer/home";
            }
        }

        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // Check if user is already authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            // User is already logged in, redirect to appropriate home
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String roleName = userDetails.getRoleName();

            if ("ADMIN".equals(roleName)) {
                return "redirect:/admin/dashboard";
            } else if ("USER".equals(roleName)) {
                return "redirect:/customer/home";
            }
        }

        model.addAttribute("userRegister", new UserRegister());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userRegister") UserRegister userRegister,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Register the user using UserRegister DTO
            userService.registerUser(userRegister);

            // Redirect to login with success message
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            return "redirect:/login?registered=true";

        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("emailError", "This email is already registered. Please use a different email or login.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403 page";
    }
}
