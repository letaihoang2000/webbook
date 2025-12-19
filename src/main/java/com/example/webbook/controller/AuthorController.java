package com.example.webbook.controller;

import com.example.webbook.dto.AddAuthorForm;
import com.example.webbook.dto.AuthorInfo;
import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.Author;
import com.example.webbook.service.AuthorService;
import com.example.webbook.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/author")
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @Autowired
    private BookService bookService;

    // View all authors with pagination and search
    @GetMapping("/authors")
    public String viewAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        // Get authors info (DTO) with pagination
        Page<AuthorInfo> authorPage = authorService.getAuthorsInfoPaginated(page, size, search);
        Map<String, Object> paginationInfo = authorService.getPaginationInfo(page, size, search);

        // Calculate pagination range
        int totalPages = (int) paginationInfo.get("totalPages");
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(totalPages - 1, page + 2);

        // Add attributes to model
        model.addAttribute("authors", authorPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalAuthors", paginationInfo.get("totalAuthors"));
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", paginationInfo.get("hasPrevious"));
        model.addAttribute("hasNext", paginationInfo.get("hasNext"));
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchQuery", search);

        return "users/admin/author_index";
    }

    @GetMapping("/books/{authorId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAuthorBooks(@PathVariable("authorId") String authorId) {
        Map<String, Object> response = new HashMap<>();

        try {
            UUID id = UUID.fromString(authorId);
            AuthorInfo authorInfo = authorService.getAuthorInfoById(id);
            List<BookInfo> books = bookService.getBooksByAuthorId(id);

            response.put("success", true);
            response.put("author", authorInfo);
            response.put("books", books);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An error occurred");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Create author
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createAuthor(@ModelAttribute AddAuthorForm addAuthorForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            Author newAuthor = authorService.createAuthor(addAuthorForm);
            response.put("success", true);
            response.put("message", "Author created successfully!");
            response.put("authorId", newAuthor.getId().toString());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("errorType", "validation");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("errorType", "upload");
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Update author
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAuthor(
            @PathVariable("id") String id,
            @ModelAttribute AddAuthorForm addAuthorForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            UUID authorId = UUID.fromString(id);
            Author updatedAuthor = authorService.updateAuthor(authorId, addAuthorForm);

            response.put("success", true);
            response.put("message", "Author updated successfully!");
            response.put("authorId", updatedAuthor.getId().toString());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("errorType", "validation");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("errorType", "upload");
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Delete author
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAuthor(@PathVariable("id") String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            UUID authorId = UUID.fromString(id);
            authorService.deleteAuthor(authorId);

            response.put("success", true);
            response.put("message", "Author deleted successfully!");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
