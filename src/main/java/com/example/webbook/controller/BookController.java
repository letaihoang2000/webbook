package com.example.webbook.controller;


import com.example.webbook.dto.AddBookForm;
import com.example.webbook.dto.BookInfo;
import com.example.webbook.dto.UpdateBookForm;
import com.example.webbook.model.Book;
import com.example.webbook.repository.AuthorRepository;
import com.example.webbook.repository.CategoryRepository;
import com.example.webbook.service.AuthorService;
import com.example.webbook.service.BookService;
import com.example.webbook.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/book")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthorService authorService;

    // View all books with pagination and search
    @GetMapping("/books")
    public String viewBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        // Get books with pagination
        Page<BookInfo> bookPage = bookService.getBooksInfoPaginated(page, size, searchBy, search);
        Map<String, Object> paginationInfo = bookService.getPaginationInfo(page, size, searchBy, search);

        // Calculate pagination range
        int totalPages = (int) paginationInfo.get("totalPages");
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(totalPages - 1, page + 2);

        // Add attributes to model
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalBooks", paginationInfo.get("totalBooks"));
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", paginationInfo.get("hasPrevious"));
        model.addAttribute("hasNext", paginationInfo.get("hasNext"));
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("searchQuery", search);
        model.addAttribute("categoryId", categoryId);

        // Add categories and authors for dropdowns
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("authors", authorService.getAllAuthors());

        return "users/admin/book_index";
    }

    // Show create book form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("bookForm", new AddBookForm());
        return "users/admin/book_create";
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

        } catch (IllegalArgumentException e) {
            // Validation errors (file size, type, etc.)
            response.put("success", false);
            response.put("errorType", "validation");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            // File upload errors
            response.put("success", false);
            response.put("errorType", "upload");
            response.put("message", "Failed to upload files: " + e.getMessage());
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            // General errors
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while creating the book.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        try {
            UUID bookId = UUID.fromString(id);
            Book book = bookService.getBookById(bookId);

            // Create UpdateBookForm and populate with existing data
            UpdateBookForm bookForm = new UpdateBookForm();
            bookForm.setBook_id(id);
            bookForm.setTitle(book.getTitle());
            bookForm.setDescription(book.getDescription());
            bookForm.setPublished_date(book.getPublished_date());
            bookForm.setPage(book.getPage());
            bookForm.setPrice(book.getPrice());

            if (book.getAuthor() != null) {
                bookForm.setAuthor(book.getAuthor().getName()); // Use author name, not ID
            }
            if (book.getCategory() != null) {
                bookForm.setCategory_type(book.getCategory().getId().toString());
            }

            model.addAttribute("bookForm", bookForm);
            model.addAttribute("book", book); // Add book for displaying current image/pdf
            model.addAttribute("bookId", id);
            model.addAttribute("authors", authorRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());

            return "users/admin/book_edit";
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
            return "redirect:/book/books";
        }
    }

    // Update book - Use UpdateBookForm
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBook(
            @PathVariable("id") String id,
            @ModelAttribute UpdateBookForm updateBookForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            UUID bookId = UUID.fromString(id);
            Book updatedBook = bookService.updateBook(bookId, updateBookForm);

            response.put("success", true);
            response.put("message", "Book updated successfully!");
            response.put("bookId", updatedBook.getId().toString());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("errorType", "validation");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("errorType", "upload");
            response.put("message", "Failed to upload files: " + e.getMessage());
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("errorType", "general");
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while updating the book.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // View book details
    @GetMapping("/view/{id}")
    public String viewBookDetail(@PathVariable("id") String id, Model model) {
        try {
            UUID bookId = UUID.fromString(id);
            BookInfo bookInfo = bookService.getBookInfoById(bookId);

            // Add book info
            model.addAttribute("book", bookInfo);

            // Add categories and authors for update modal dropdowns
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("authors", authorService.getAllAuthors());

            return "users/admin/book_detail";
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
            return "redirect:/book/books";
        }
    }

    // Delete single book
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable("id") String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            UUID bookId = UUID.fromString(id);
            bookService.deleteBook(bookId);

            response.put("success", true);
            response.put("message", "Book deleted successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while deleting the book.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
