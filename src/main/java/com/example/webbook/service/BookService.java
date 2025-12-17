package com.example.webbook.service;

import com.example.webbook.dto.AddBookForm;
import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.Author;
import com.example.webbook.model.Category;
import com.example.webbook.repository.AuthorRepository;
import com.example.webbook.repository.BookRepository;
import com.example.webbook.model.Book;
import com.example.webbook.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Get paginated books with search support
    public Page<BookInfo> getBooksInfoPaginated(int page, int size, String searchBy, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<Book> bookPage;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String trimmedQuery = searchQuery.trim();

            if ("title".equalsIgnoreCase(searchBy)) {
                bookPage = bookRepository.findByTitleContainingIgnoreCase(trimmedQuery, pageable);
            } else if ("author".equalsIgnoreCase(searchBy)) {
                bookPage = bookRepository.findByAuthorNameContainingIgnoreCase(trimmedQuery, pageable);
            } else {
                bookPage = bookRepository.findAllBooks(pageable);
            }
        } else {
            bookPage = bookRepository.findAllBooks(pageable);
        }

        return bookPage.map(this::convertToBookInfo);
    }

    // Overloaded method for backward compatibility
    public Page<BookInfo> getBooksInfoPaginated(int page, int size) {
        return getBooksInfoPaginated(page, size, null, null);
    }

    // Get pagination info
    public Map<String, Object> getPaginationInfo(int currentPage, int pageSize, String searchBy, String searchQuery) {
        long totalBooks;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String trimmedQuery = searchQuery.trim();

            if ("title".equalsIgnoreCase(searchBy)) {
                totalBooks = bookRepository.countByTitleContaining(trimmedQuery);
            } else if ("author".equalsIgnoreCase(searchBy)) {
                totalBooks = bookRepository.countByAuthorNameContaining(trimmedQuery);
            } else {
                totalBooks = bookRepository.countAllBooks();
            }
        } else {
            totalBooks = bookRepository.countAllBooks();
        }

        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        Map<String, Object> paginationInfo = new HashMap<>();
        paginationInfo.put("currentPage", currentPage);
        paginationInfo.put("pageSize", pageSize);
        paginationInfo.put("totalBooks", totalBooks);
        paginationInfo.put("totalPages", totalPages);
        paginationInfo.put("hasPrevious", currentPage > 0);
        paginationInfo.put("hasNext", currentPage < totalPages - 1);
        paginationInfo.put("searchBy", searchBy);
        paginationInfo.put("searchQuery", searchQuery);

        return paginationInfo;
    }

    // Get book by ID
    public Book getBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    // Get book info by ID
    public BookInfo getBookInfoById(UUID id) {
        Book book = getBookById(id);
        return convertToBookInfo(book);
    }

    // Create book
    @Transactional
    public Book createBook(AddBookForm form) {
        try {
            Book book = new Book();
            populateBookFromForm(book, form);
            book.setCreated_at(LocalDateTime.now());
            book.setLast_updated(LocalDateTime.now());

            return bookRepository.save(book);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create book: " + e.getMessage());
        }
    }

    // Update book
    @Transactional
    public Book updateBook(UUID id, AddBookForm form) {
        try {
            Book book = getBookById(id);
            populateBookFromForm(book, form);
            book.setLast_updated(LocalDateTime.now());

            return bookRepository.save(book);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update book: " + e.getMessage());
        }
    }

    // Delete book
    @Transactional
    public void deleteBook(UUID id) {
        try {
            Book book = getBookById(id);
            bookRepository.delete(book);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete book: " + e.getMessage());
        }
    }

    // Delete multiple books
    @Transactional
    public void deleteBooks(List<UUID> ids) {
        try {
            List<Book> books = bookRepository.findAllById(ids);
            if (books.isEmpty()) {
                throw new RuntimeException("No books found with provided ids");
            }
            bookRepository.deleteAll(books);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete books: " + e.getMessage());
        }
    }

    // Helper method to populate book from form
    private void populateBookFromForm(Book book, AddBookForm form) {
        book.setTitle(form.getTitle());
//        book.setImage(form.getImage());
        book.setDescription(form.getDescription());
        book.setPublished_date(form.getPublished_date());
        book.setPage(form.getPage());
        book.setPrice(form.getPrice());
//        book.setBook_content(form.getBook_content());

        // Set author
        if (form.getAuthor() != null && !form.getAuthor().trim().isEmpty()) {
            UUID authorId = UUID.fromString(form.getAuthor());
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("Author not found with id: " + authorId));
            book.setAuthor(author);
        } else {
            book.setAuthor(null);
        }

        // Set category
        if (form.getCategory_type() != null && !form.getCategory_type().trim().isEmpty()) {
            Long categoryId = Long.parseLong(form.getCategory_type());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }

    // Convert Book entity to BookInfo DTO
    private BookInfo convertToBookInfo(Book book) {
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBook_id(book.getId().toString());
        bookInfo.setTitle(book.getTitle());
        bookInfo.setImage(book.getImage());
        bookInfo.setDescription(book.getDescription());

        // Format published_date
        if (book.getPublished_date() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            bookInfo.setPublished_date(book.getPublished_date().format(formatter));
        }

        bookInfo.setPage(book.getPage() != null ? book.getPage().toString() : "N/A");
        bookInfo.setPrice(book.getPrice());
        bookInfo.setBook_content(book.getBook_content());

        // Format last_updated
        if (book.getLast_updated() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            bookInfo.setLast_updated(book.getLast_updated().format(formatter));
        }

        // Set author name
        if (book.getAuthor() != null) {
            bookInfo.setAuthor_name(book.getAuthor().getName());
        }

        // Set category name
        if (book.getCategory() != null) {
            bookInfo.setCategory_name(book.getCategory().getName());
        }

        return bookInfo;
    }
}




//@Service
//public class BookService {
//    @Autowired
//    private BookRepository bookRepository;
//
//    @Autowired
//    private AuthorRepository authorRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    // Get paginated books with search support
//    public Page<BookInfo> getBooksInfoPaginated(int page, int size, String searchBy, String searchQuery) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
//        Page<Book> bookPage;
//
//        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
//            String trimmedQuery = searchQuery.trim();
//
//            if ("title".equalsIgnoreCase(searchBy)) {
//                bookPage = bookRepository.findByTitleContainingIgnoreCase(trimmedQuery, pageable);
//            } else if ("author".equalsIgnoreCase(searchBy)) {
//                bookPage = bookRepository.findByAuthorNameContainingIgnoreCase(trimmedQuery, pageable);
//            } else {
//                // Default: search all books
//                bookPage = bookRepository.findAllBooks(pageable);
//            }
//        } else {
//            bookPage = bookRepository.findAllBooks(pageable);
//        }
//
//        return bookPage.map(this::convertToBookInfo);
//    }
//
//    // Overloaded method for backward compatibility
//    public Page<BookInfo> getBooksInfoPaginated(int page, int size) {
//        return getBooksInfoPaginated(page, size, null, null);
//    }
//
//    // Get pagination info
//    public Map<String, Object> getPaginationInfo(int currentPage, int pageSize, String searchBy, String searchQuery) {
//        long totalBooks;
//
//        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
//            String trimmedQuery = searchQuery.trim();
//
//            if ("title".equalsIgnoreCase(searchBy)) {
//                totalBooks = bookRepository.countByTitleContaining(trimmedQuery);
//            } else if ("author".equalsIgnoreCase(searchBy)) {
//                totalBooks = bookRepository.countByAuthorNameContaining(trimmedQuery);
//            } else {
//                totalBooks = bookRepository.countAllBooks();
//            }
//        } else {
//            totalBooks = bookRepository.countAllBooks();
//        }
//
//        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
//
//        Map<String, Object> paginationInfo = new HashMap<>();
//        paginationInfo.put("currentPage", currentPage);
//        paginationInfo.put("pageSize", pageSize);
//        paginationInfo.put("totalBooks", totalBooks);
//        paginationInfo.put("totalPages", totalPages);
//        paginationInfo.put("hasPrevious", currentPage > 0);
//        paginationInfo.put("hasNext", currentPage < totalPages - 1);
//        paginationInfo.put("searchBy", searchBy);
//        paginationInfo.put("searchQuery", searchQuery);
//
//        return paginationInfo;
//    }
//
//
//    @Transactional
//    public Book createBook(AddBookForm form) {
//        try {
//            Book book = new Book();
//            book.setTitle(form.getTitle());
//            book.setImage(form.getImage());
//            book.setDescription(form.getDescription());
//            book.setPublished_date(form.getPublished_date());
//            book.setPage(form.getPage());
//            book.setPrice(form.getPrice());
//            book.setBook_content(form.getBook_content());
//            book.setCreated_at(LocalDateTime.now());
//            book.setLast_updated(LocalDateTime.now());
//
//            // Set author
//            if (form.getAuthor() != null && !form.getAuthor().trim().isEmpty()) {
//                UUID authorId = UUID.fromString(form.getAuthor());
//                Author author = authorRepository.findById(authorId)
//                        .orElseThrow(() -> new RuntimeException("Author not found with id: " + authorId));
//                book.setAuthor(author);
//            }
//
//            // Set category
//            if (form.getCategory_type() != null && !form.getCategory_type().trim().isEmpty()) {
//                Long categoryId = Long.parseLong(form.getCategory_type());
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
//                book.setCategory(category);
//            }
//
//            return bookRepository.save(book);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create book: " + e.getMessage());
//        }
//    }
//
//    // Convert Book entity to BookInfo DTO
//    private BookInfo convertToBookInfo(Book book) {
//        BookInfo bookInfo = new BookInfo();
//        bookInfo.setBook_id(book.getId().toString());
//        bookInfo.setTitle(book.getTitle());
//        bookInfo.setImage(book.getImage());
//        bookInfo.setDescription(book.getDescription());
//
//        System.out.println(bookInfo.getImage());
//
//        // Format published_date
//        if (book.getPublished_date() != null) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            bookInfo.setPublished_date(book.getPublished_date().format(formatter));
//        }
//
//        bookInfo.setPage(book.getPage() != null ? book.getPage().toString() : "N/A");
//        bookInfo.setPrice(book.getPrice());
//        bookInfo.setBook_content(book.getBook_content());
//
//        // Format last_updated
//        if (book.getLast_updated() != null) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
//            bookInfo.setLast_updated(book.getLast_updated().format(formatter));
//        }
//
//        // Set author name
//        if (book.getAuthor() != null) {
//            bookInfo.setAuthor_name(book.getAuthor().getName());
//        }
//
//        // Set category name
//        if (book.getCategory() != null) {
//            bookInfo.setCategory_name(book.getCategory().getName());
//        }
//
//        return bookInfo;
//    }
//}
