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

import java.io.IOException;
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

    @Autowired
    private SupabaseStorageService supabaseStorageService;

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

    @Transactional
    public Book createBook(AddBookForm form) throws IOException {
        Book book = new Book();

        // Set basic fields
        book.setTitle(form.getTitle());
        book.setDescription(form.getDescription());
        book.setPublished_date(form.getPublished_date());
        book.setPage(form.getPage());
        book.setPrice(form.getPrice());

        // Upload image if provided - using book title as filename
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            String imageUrl = supabaseStorageService.uploadBookImage(form.getImageFile(), form.getTitle());
            book.setImage(imageUrl);
        }

        // Upload PDF if provided - using book title as filename
        if (form.getContentFile() != null && !form.getContentFile().isEmpty()) {
            String pdfUrl = supabaseStorageService.uploadBookContent(form.getContentFile(), form.getTitle());
            book.setBook_content(pdfUrl);
        }

        // Find and set author
        if (form.getAuthor() != null && !form.getAuthor().trim().isEmpty()) {
            Author author = authorRepository.findByName(form.getAuthor())
                    .orElseThrow(() -> new RuntimeException("Author not found: " + form.getAuthor()));
            book.setAuthor(author);
        }

        // Find and set category
        if (form.getCategory_type() != null && !form.getCategory_type().trim().isEmpty()) {
            Long categoryId = Long.parseLong(form.getCategory_type());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            book.setCategory(category);
        }

        book.setCreated_at(LocalDateTime.now());
        book.setLast_updated(LocalDateTime.now());

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(UUID id, AddBookForm form) throws IOException {
        Book book = getBookById(id);
        String oldImageUrl = book.getImage();
        String oldContentUrl = book.getBook_content();
        String oldTitle = book.getTitle();

        // Update basic fields
        book.setTitle(form.getTitle());
        book.setDescription(form.getDescription());
        book.setPublished_date(form.getPublished_date());
        book.setPage(form.getPage());
        book.setPrice(form.getPrice());

        // Update image if new one provided
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            String newImageUrl = supabaseStorageService.uploadBookImage(form.getImageFile(), form.getTitle());
            book.setImage(newImageUrl);

            // Delete old image (only if title changed or it's a different file)
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                supabaseStorageService.deleteFile(oldImageUrl);
            }
        } else if (!oldTitle.equals(form.getTitle()) && oldImageUrl != null) {
            // Title changed but no new image - we might want to rename the file
            // For now, keep the old URL
        }

        // Update PDF if new one provided
        if (form.getContentFile() != null && !form.getContentFile().isEmpty()) {
            String newContentUrl = supabaseStorageService.uploadBookContent(form.getContentFile(), form.getTitle());
            book.setBook_content(newContentUrl);

            // Delete old PDF
            if (oldContentUrl != null && !oldContentUrl.isEmpty()) {
                supabaseStorageService.deleteFile(oldContentUrl);
            }
        } else if (!oldTitle.equals(form.getTitle()) && oldContentUrl != null) {
            // Title changed but no new content - we might want to rename the file
            // For now, keep the old URL
        }

        // Update author and category
        if (form.getAuthor() != null && !form.getAuthor().trim().isEmpty()) {
            Author author = authorRepository.findByName(form.getAuthor())
                    .orElseThrow(() -> new RuntimeException("Author not found: " + form.getAuthor()));
            book.setAuthor(author);
        }

        if (form.getCategory_type() != null && !form.getCategory_type().trim().isEmpty()) {
            Long categoryId = Long.parseLong(form.getCategory_type());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            book.setCategory(category);
        }

        book.setLast_updated(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(UUID id) {
        Book book = getBookById(id);

        // Delete files from Supabase before deleting book record
        if (book.getImage() != null && !book.getImage().isEmpty()) {
            supabaseStorageService.deleteFile(book.getImage());
        }
        if (book.getBook_content() != null && !book.getBook_content().isEmpty()) {
            supabaseStorageService.deleteFile(book.getBook_content());
        }

        bookRepository.delete(book);
    }

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

        // Set category name and ID
        if (book.getCategory() != null) {
            bookInfo.setCategory_name(book.getCategory().getName());
            bookInfo.setCategory_id(book.getCategory().getId().toString());  // Add this line
        }

        return bookInfo;
    }
}

