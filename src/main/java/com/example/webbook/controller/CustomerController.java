package com.example.webbook.controller;

import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.User;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.BookService;
import com.example.webbook.service.CategoryService;
import com.example.webbook.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WishlistService wishlistService;

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

            // Get related books from the same category
            List<BookInfo> relatedBooks = bookService.getBooksByCategoryId(
                    Long.parseLong(bookInfo.getCategory_id()), 4
            );

            // Remove current book from related books
            relatedBooks.removeIf(book -> book.getBook_id().equals(id));

            // Get current user from CustomUserDetails
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Check if book is in wishlist
            boolean isInWishlist = wishlistService.isInWishlist(user.getId(), bookId);
            Set<String> wishlistBookIds = wishlistService.getWishlistBookIds(user.getId());

            model.addAttribute("book", bookInfo);
            model.addAttribute("relatedBooks", relatedBooks);
            model.addAttribute("isInWishlist", isInWishlist);
            model.addAttribute("wishlistBookIds", wishlistBookIds);

            return "users/customer/book_detail";
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
            return "redirect:/customer/all-books";
        }
    }
}