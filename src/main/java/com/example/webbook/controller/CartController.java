package com.example.webbook.controller;

import com.example.webbook.dto.CartItem;
import com.example.webbook.model.User;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.CartService;
import com.example.webbook.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private WishlistService wishlistService;

    // Display cart page
    @GetMapping("/cart")
    public String viewCart(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Get cart items
        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        Map<String, Object> cartSummary = cartService.getCartSummary(user.getId());

        // Get wishlist count for navbar
        long wishlistCount = wishlistService.getWishlistBookIds(user.getId()).size();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("currentUser", user);
        model.addAttribute("wishlistCount", wishlistCount);

        return "users/customer/cart";
    }

    // Add to cart (AJAX)
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(
            @RequestParam String bookId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        try {
            UUID bookUUID = UUID.fromString(bookId);
            Map<String, Object> response = cartService.addToCart(user.getId(), bookUUID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid request: " + e.getMessage()
            ));
        }
    }

    // Remove from cart (AJAX)
    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(
            @RequestParam String bookId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        try {
            UUID bookUUID = UUID.fromString(bookId);
            Map<String, Object> response = cartService.removeFromCart(user.getId(), bookUUID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid request: " + e.getMessage()
            ));
        }
    }

    // Clear cart (AJAX)
    @PostMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, Object> response = cartService.clearCart(user.getId());
        return ResponseEntity.ok(response);
    }

    // Get cart summary (AJAX) - for navbar update
    @GetMapping("/cart/summary")
    @ResponseBody
    public ResponseEntity<?> getCartSummary(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, Object> summary = cartService.getCartSummary(user.getId());
        return ResponseEntity.ok(summary);
    }

    // Check if book is in cart (AJAX)
    @GetMapping("/cart/check")
    @ResponseBody
    public ResponseEntity<?> checkInCart(
            @RequestParam String bookId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        try {
            UUID bookUUID = UUID.fromString(bookId);
            boolean inCart = cartService.isInCart(user.getId(), bookUUID);
            return ResponseEntity.ok(Map.of("inCart", inCart));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid request"
            ));
        }
    }
}