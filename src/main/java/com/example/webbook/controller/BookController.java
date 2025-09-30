package com.example.webbook.controller;


import com.example.webbook.dto.AddBookForm;
import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.Book;
import com.example.webbook.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/book")
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping("/books")
    public String viewBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String search,
            Model model) {

        // Get paginated books with search
        Page<BookInfo> bookPage = bookService.getBooksInfoPaginated(page, size, searchBy, search);
        Map<String, Object> paginationInfo = bookService.getPaginationInfo(page, size, searchBy, search);

        // Add to model
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalBooks", bookPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", bookPage.hasPrevious());
        model.addAttribute("hasNext", bookPage.hasNext());
        model.addAttribute("searchBy", searchBy != null ? searchBy : "");
        model.addAttribute("searchQuery", search != null ? search : "");

        // Generate page numbers for pagination nav
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(bookPage.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "users/admin/book_index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBook(@ModelAttribute AddBookForm addBookForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            Book newBook = bookService.createBook(addBookForm);
            response.put("success", true);
            response.put("message", "Book created successfully!");
            response.put("bookId", newBook.getId().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while creating the book.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
