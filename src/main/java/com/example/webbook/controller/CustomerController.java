package com.example.webbook.controller;

import com.example.webbook.dto.BookInfo;
import com.example.webbook.dto.UpdateUserForm;
import com.example.webbook.model.User;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @GetMapping("/home")
    public String customerHome(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, Object> cartSummary = cartService.getCartSummary(user.getId());
        long wishlistCount = wishlistService.getWishlistBookIds(user.getId()).size();

        model.addAttribute("currentUser", user);
        model.addAttribute("userName", userDetails.getFullName());
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("wishlistCount", wishlistCount);

        return "users/customer/home";
    }

    @GetMapping("/profile")
    public String customerProfile(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        Map<String, Object> cartSummary = cartService.getCartSummary(currentUser.getId());
        long wishlistCount = wishlistService.getWishlistBookIds(currentUser.getId()).size();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("wishlistCount", wishlistCount);

        return "users/customer/profile";
    }

    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @ModelAttribute UpdateUserForm updateUserForm,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userDetails.getUser();

            // Set the user ID from the authenticated user
            updateUserForm.setId(currentUser.getId().toString());

            // Update user
            User updatedUser = userService.updateUser(updateUserForm);

            // Return success response with updated user data
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully!");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update profile: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/all-books")
    public String allBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            Model model,
            Authentication authentication) {

        Page<BookInfo> bookPage;

        // Filter by category or search
        if (categoryId != null) {
            bookPage = bookService.getBooksByCategoryPaginated(categoryId, page, size);
        } else if (search != null && !search.trim().isEmpty()) {
            bookPage = bookService.searchBooksPaginated(search.trim(), page, size);
        } else {
            bookPage = bookService.getBooksInfoPaginated(page, size);
        }

        // Pagination info
        int totalPages = bookPage.getTotalPages();
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(totalPages - 1, page + 2);

        // Get current user from CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser(); // Get the actual User entity

        // Get wishlist book IDs for current user
        Set<String> wishlistBookIds = wishlistService.getWishlistBookIds(user.getId());

        Map<String, Object> cartSummary = cartService.getCartSummary(user.getId());
        long wishlistCount = wishlistBookIds.size();

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("wishlistBookIds", wishlistBookIds);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalBooks", bookPage.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("cartSummary", cartSummary);
        model.addAttribute("wishlistCount", wishlistCount);

        return "users/customer/all_books";
    }

    @GetMapping("/book/{id}")
    public String bookDetail(
            @PathVariable("id") String id,
            Model model,
            Authentication authentication) {
        try {
            UUID bookId = UUID.fromString(id);
            BookInfo bookInfo = bookService.getBookInfoById(bookId);

            List<BookInfo> relatedBooks = bookService.getBooksByCategoryId(
                    Long.parseLong(bookInfo.getCategory_id()), 4
            );
            relatedBooks.removeIf(book -> book.getBook_id().equals(id));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            boolean isInWishlist = wishlistService.isInWishlist(user.getId(), bookId);
            Set<String> wishlistBookIds = wishlistService.getWishlistBookIds(user.getId());

            Map<String, Object> cartSummary = cartService.getCartSummary(user.getId());
            long wishlistCount = wishlistBookIds.size();

            model.addAttribute("book", bookInfo);
            model.addAttribute("relatedBooks", relatedBooks);
            model.addAttribute("isInWishlist", isInWishlist);
            model.addAttribute("wishlistBookIds", wishlistBookIds);
            model.addAttribute("cartSummary", cartSummary);
            model.addAttribute("wishlistCount", wishlistCount);

            return "users/customer/book_detail";
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
            return "redirect:/customer/all-books";
        }
    }
}