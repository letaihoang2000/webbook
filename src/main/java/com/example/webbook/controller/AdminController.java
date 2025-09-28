package com.example.webbook.controller;

import com.example.webbook.dto.AddUserForm;
import com.example.webbook.dto.UpdateUserForm;
import com.example.webbook.dto.UserInfo;
import com.example.webbook.exception.EmailAlreadyExistsException;
import com.example.webbook.model.User;
import com.example.webbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class AdminController {
    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboardView(){
        return "users/admin/dashboard";
    }

    // Get all users, except Admin
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        // Get paginated users with search
        Page<UserInfo> userPage = userService.getUsersInfoPaginated(page, size, search);
        Map<String, Object> paginationInfo = userService.getPaginationInfo(page, size, search);

        // Add to model
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalUsers", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", userPage.hasPrevious());
        model.addAttribute("hasNext", userPage.hasNext());
        model.addAttribute("searchQuery", search != null ? search : "");

        // Generate page numbers for pagination nav
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(userPage.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "users/admin/user_index";
    }

    // Add user
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createUser(@ModelAttribute AddUserForm addUserForm) {

        Map<String, Object> response = new HashMap<>();

        try {
            User newUser = userService.createUser(addUserForm);
            response.put("success", true);
            response.put("message", "User created successfully!");
            response.put("userId", newUser.getId().toString());
            return ResponseEntity.ok(response);

        } catch (EmailAlreadyExistsException e) {
            response.put("success", false);
            response.put("errorType", "email");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while creating the user.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Update user
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUser(@ModelAttribute UpdateUserForm updateUserForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            User updatedUser = userService.updateUser(updateUserForm);
            response.put("success", true);
            response.put("message", "User updated successfully!");
            response.put("userId", updatedUser.getId().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while updating the user.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Delete user
    @DeleteMapping("/delete/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.deleteUser(userId);
            response.put("success", true);
            response.put("message", "User deleted successfully!");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An unexpected error occurred while deleting the user.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
