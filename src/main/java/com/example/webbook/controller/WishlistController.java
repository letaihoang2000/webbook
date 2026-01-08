package com.example.webbook.controller;

import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.User;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.CartService;
import com.example.webbook.service.CategoryService;
import com.example.webbook.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    // View wishlist page
    @GetMapping
    public String viewWishlist(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        List<BookInfo> wishlistBooks = wishlistService.getUserWishlist(user.getId());
        Map<String, Object> cartSummary = cartService.getCartSummary(user.getId());

        model.addAttribute("books", wishlistBooks);
        model.addAttribute("totalBooks", wishlistBooks.size());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("wishlistCount", wishlistBooks.size());
        model.addAttribute("cartSummary", cartSummary);

        return "users/customer/wishlist";
    }

    // Add to wishlist (AJAX)
    @PostMapping("/add/{bookId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @PathVariable("bookId") String bookId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user from CustomUserDetails
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            UUID bookUUID = UUID.fromString(bookId);

            boolean added = wishlistService.addToWishlist(user.getId(), bookUUID);

            if (added) {
                response.put("success", true);
                response.put("message", "Added to wishlist");
                response.put("count", wishlistService.getWishlistCount(user.getId()));
            } else {
                response.put("success", false);
                response.put("message", "Already in wishlist");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add to wishlist: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Remove from wishlist (AJAX)
    @PostMapping("/remove/{bookId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @PathVariable("bookId") String bookId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user from CustomUserDetails
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            UUID bookUUID = UUID.fromString(bookId);

            boolean removed = wishlistService.removeFromWishlist(user.getId(), bookUUID);

            if (removed) {
                response.put("success", true);
                response.put("message", "Removed from wishlist");
                response.put("count", wishlistService.getWishlistCount(user.getId()));
            } else {
                response.put("success", false);
                response.put("message", "Not in wishlist");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to remove from wishlist: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Toggle wishlist (AJAX)
    @PostMapping("/toggle/{bookId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @PathVariable("bookId") String bookId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get current user from CustomUserDetails
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            UUID bookUUID = UUID.fromString(bookId);

            boolean isInWishlist = wishlistService.isInWishlist(user.getId(), bookUUID);

            if (isInWishlist) {
                wishlistService.removeFromWishlist(user.getId(), bookUUID);
                response.put("action", "removed");
                response.put("message", "Removed from wishlist");
            } else {
                wishlistService.addToWishlist(user.getId(), bookUUID);
                response.put("action", "added");
                response.put("message", "Added to wishlist");
            }

            response.put("success", true);
            response.put("count", wishlistService.getWishlistCount(user.getId()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update wishlist: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}