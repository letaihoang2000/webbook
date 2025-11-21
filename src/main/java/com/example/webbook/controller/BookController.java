package com.example.webbook.controller;


import com.example.webbook.dto.AddBookForm;
import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.Book;
import com.example.webbook.repository.AuthorRepository;
import com.example.webbook.repository.CategoryRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/book")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // View all books with pagination and search
    @GetMapping("/books")
    public String viewBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String search,
            Model model) {

        // Get paginated books with search
        Page<BookInfo> bookPage = bookService.getBooksInfoPaginated(page, size, searchBy, search);

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

    // Show create book form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("bookForm", new AddBookForm());
        return "users/admin/book_create";
    }

    // Create book
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

    // Show edit book form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        try {
            UUID bookId = UUID.fromString(id);
            BookInfo bookInfo = bookService.getBookInfoById(bookId);
            Book book = bookService.getBookById(bookId);

            // Create form and populate with existing data
            AddBookForm bookForm = new AddBookForm();
            bookForm.setTitle(book.getTitle());
            bookForm.setImage(book.getImage());
            bookForm.setDescription(book.getDescription());
            bookForm.setPublished_date(book.getPublished_date());
            bookForm.setPage(book.getPage());
            bookForm.setPrice(book.getPrice());
            bookForm.setBook_content(book.getBook_content());

            if (book.getAuthor() != null) {
                bookForm.setAuthor(book.getAuthor().getId().toString());
            }
            if (book.getCategory() != null) {
                bookForm.setCategory_type(book.getCategory().getId().toString());
            }

            model.addAttribute("bookForm", bookForm);
            model.addAttribute("bookId", id);
            model.addAttribute("authors", authorRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());

            return "users/admin/book_edit";
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
            return "redirect:/book/books";
        }
    }

    // Update book
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBook(
            @PathVariable("id") String id,
            @ModelAttribute AddBookForm addBookForm) {
        Map<String, Object> response = new HashMap<>();

        try {
            UUID bookId = UUID.fromString(id);
            Book updatedBook = bookService.updateBook(bookId, addBookForm);

            response.put("success", true);
            response.put("message", "Book updated successfully!");
            response.put("bookId", updatedBook.getId().toString());
            return ResponseEntity.ok(response);

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
            model.addAttribute("book", bookInfo);
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

    // Delete multiple books
    @PostMapping("/delete-multiple")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMultipleBooks(@RequestBody Map<String, List<String>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<String> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("message", "No books selected for deletion");
                return ResponseEntity.badRequest().body(response);
            }

            List<UUID> bookIds = ids.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            bookService.deleteBooks(bookIds);

            response.put("success", true);
            response.put("message", ids.size() + " book(s) deleted successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while deleting books.");
            return ResponseEntity.status(500).body(response);
        }
    }
}




//@Controller
//@RequestMapping("/book")
//public class BookController {
//    @Autowired
//    private BookService bookService;
//
//    @GetMapping("/books")
//    public String viewBooks(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String searchBy,
//            @RequestParam(required = false) String search,
//            Model model) {
//
//        // Get paginated books with search
//        Page<BookInfo> bookPage = bookService.getBooksInfoPaginated(page, size, searchBy, search);
//        Map<String, Object> paginationInfo = bookService.getPaginationInfo(page, size, searchBy, search);
//
//        // Add to model
//        model.addAttribute("books", bookPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", bookPage.getTotalPages());
//        model.addAttribute("totalBooks", bookPage.getTotalElements());
//        model.addAttribute("pageSize", size);
//        model.addAttribute("hasPrevious", bookPage.hasPrevious());
//        model.addAttribute("hasNext", bookPage.hasNext());
//        model.addAttribute("searchBy", searchBy != null ? searchBy : "");
//        model.addAttribute("searchQuery", search != null ? search : "");
//
//        // Generate page numbers for pagination nav
//        int startPage = Math.max(0, page - 2);
//        int endPage = Math.min(bookPage.getTotalPages() - 1, page + 2);
//        model.addAttribute("startPage", startPage);
//        model.addAttribute("endPage", endPage);
//
//        return "users/admin/book_index";
//    }
//
//    @PostMapping("/add")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> createBook(@ModelAttribute AddBookForm addBookForm) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            Book newBook = bookService.createBook(addBookForm);
//            response.put("success", true);
//            response.put("message", "Book created successfully!");
//            response.put("bookId", newBook.getId().toString());
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("errorType", "general");
//            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while creating the book.");
//            return ResponseEntity.status(500).body(response);
//        }
//    }
//}
