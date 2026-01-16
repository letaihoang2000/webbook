package com.example.webbook.service;

import com.example.webbook.dto.AddBookForm;
import com.example.webbook.dto.BookInfo;
import com.example.webbook.dto.UpdateBookForm;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private OrderService orderService;

    // Convert Book to BookInfo with purchased status for a specific user
    public BookInfo convertToBookInfo(Book book, UUID userId) {
        BookInfo bookInfo = convertToBookInfo(book);

        // Check if user has purchased this book
        if (userId != null) {
            boolean isPurchased = orderService.hasUserPurchasedBook(userId, book.getId());
            bookInfo.setPurchased(isPurchased);
        }

        return bookInfo;
    }

    // Original conversion without user context (for admin pages)
    public BookInfo convertToBookInfo(Book book) {
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
            bookInfo.setCategory_id(book.getCategory().getId().toString());
        }

        bookInfo.setPurchased(false); // Default to false when no user context

        return bookInfo;
    }

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

    // Get paginated books for customer with purchased status
    public Page<BookInfo> getBooksInfoPaginatedForUser(int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findAllBooks(pageable);
        return bookPage.map(book -> convertToBookInfo(book, userId));
    }

    // Get books by author ID
    public List<BookInfo> getBooksByAuthorId(UUID authorId) {
        List<Book> books = bookRepository.findByAuthorId(authorId);
        return books.stream()
                .map(this::convertToBookInfo)
                .collect(Collectors.toList());
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

    // Get book info by ID for specific user
    public BookInfo getBookInfoByIdForUser(UUID id, UUID userId) {
        Book book = getBookById(id);
        return convertToBookInfo(book, userId);
    }

    // Search books by title or author
    public Page<BookInfo> searchBooksPaginated(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findByTitleOrAuthorContaining(query, pageable);
        return bookPage.map(this::convertToBookInfo);
    }

    // Search books by title or author for specific user
    public Page<BookInfo> searchBooksPaginatedForUser(String query, int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findByTitleOrAuthorContaining(query, pageable);
        return bookPage.map(book -> convertToBookInfo(book, userId));
    }

    // Get books by category
    public Page<BookInfo> getBooksByCategoryPaginated(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findByCategoryId(categoryId, pageable);
        return bookPage.map(this::convertToBookInfo);
    }

    // Get books by category for specific user
    public Page<BookInfo> getBooksByCategoryPaginatedForUser(Long categoryId, int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findByCategoryId(categoryId, pageable);
        return bookPage.map(book -> convertToBookInfo(book, userId));
    }

    // Get books by category ID (limited results for related books)
    public List<BookInfo> getBooksByCategoryId(Long categoryId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findByCategoryId(categoryId, pageable);
        return bookPage.getContent().stream()
                .map(this::convertToBookInfo)
                .collect(Collectors.toList());
    }

    // Get books by category ID for specific user
    public List<BookInfo> getBooksByCategoryIdForUser(Long categoryId, int limit, UUID userId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("last_updated").descending());
        Page<Book> bookPage = bookRepository.findByCategoryId(categoryId, pageable);
        return bookPage.getContent().stream()
                .map(book -> convertToBookInfo(book, userId))
                .collect(Collectors.toList());
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
    public Book updateBook(UUID id, UpdateBookForm form) throws IOException {
        Book book = getBookById(id);
        String oldImageUrl = book.getImage();
        String oldContentUrl = book.getBook_content();
        String oldTitle = book.getTitle();
        boolean titleChanged = !oldTitle.equals(form.getTitle());

        System.out.println("\n=== UPDATE BOOK OPERATION ===");
        System.out.println("Book ID: " + id);
        System.out.println("Old Title: " + oldTitle);
        System.out.println("New Title: " + form.getTitle());
        System.out.println("Title Changed: " + titleChanged);
        System.out.println("Old Image URL: " + oldImageUrl);
        System.out.println("Old Content URL: " + oldContentUrl);
        System.out.println("New Image File: " + (form.getImageFile() != null && !form.getImageFile().isEmpty() ? "YES" : "NO"));
        System.out.println("New Content File: " + (form.getContentFile() != null && !form.getContentFile().isEmpty() ? "YES" : "NO"));

        // Update basic fields
        book.setTitle(form.getTitle());
        book.setDescription(form.getDescription());
        book.setPublished_date(form.getPublished_date());
        book.setPage(form.getPage());
        book.setPrice(form.getPrice());

        // Handle image update
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            System.out.println("\n--- IMAGE UPDATE: New image uploaded ---");

            // Delete old image FIRST if it exists
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                System.out.println("Deleting old image before upload...");
                supabaseStorageService.deleteFile(oldImageUrl);
                // Wait a bit to ensure deletion completes
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }

            // Upload new image
            System.out.println("Uploading new image...");
            String newImageUrl = supabaseStorageService.uploadBookImage(form.getImageFile(), form.getTitle());
            book.setImage(newImageUrl);
            System.out.println("New Image URL: " + newImageUrl);

        } else if (titleChanged && oldImageUrl != null && !oldImageUrl.isEmpty()) {
            System.out.println("\n--- IMAGE UPDATE: Title changed, renaming image file ---");
            try {
                String oldFilename = extractFilenameFromUrl(oldImageUrl);
                String extension = getFileExtension(oldFilename);

                // Download the old file
                byte[] imageData = supabaseStorageService.downloadFile(oldImageUrl);

                if (imageData != null && imageData.length > 0) {
                    // Delete old file FIRST
                    System.out.println("Deleting old image before re-upload...");
                    supabaseStorageService.deleteFile(oldImageUrl);
                    try { Thread.sleep(500); } catch (InterruptedException e) {}

                    // Re-upload with new filename
                    String contentType = "image/" + extension.replace(".", "");
                    String newImageUrl = supabaseStorageService.uploadBookImageFromBytes(
                            imageData,
                            form.getTitle(),
                            extension,
                            contentType
                    );
                    book.setImage(newImageUrl);
                    System.out.println("New Image URL after rename: " + newImageUrl);
                }
            } catch (Exception e) {
                System.err.println("Failed to rename image file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("\n--- IMAGE UPDATE: No changes, keeping existing image ---");
        }

        // Handle PDF content update
        if (form.getContentFile() != null && !form.getContentFile().isEmpty()) {
            System.out.println("\n--- CONTENT UPDATE: New PDF uploaded ---");

            // Delete old PDF FIRST if it exists
            if (oldContentUrl != null && !oldContentUrl.isEmpty()) {
                System.out.println("Deleting old PDF before upload...");
                supabaseStorageService.deleteFile(oldContentUrl);
                // Wait a bit to ensure deletion completes
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }

            // Upload new PDF
            System.out.println("Uploading new PDF...");
            String newContentUrl = supabaseStorageService.uploadBookContent(form.getContentFile(), form.getTitle());
            book.setBook_content(newContentUrl);
            System.out.println("New Content URL: " + newContentUrl);

        } else if (titleChanged && oldContentUrl != null && !oldContentUrl.isEmpty()) {
            System.out.println("\n--- CONTENT UPDATE: Title changed, renaming PDF file ---");
            try {
                // Download old PDF
                byte[] pdfData = supabaseStorageService.downloadFile(oldContentUrl);

                if (pdfData != null && pdfData.length > 0) {
                    // Delete old file FIRST
                    System.out.println("Deleting old PDF before re-upload...");
                    supabaseStorageService.deleteFile(oldContentUrl);
                    try { Thread.sleep(500); } catch (InterruptedException e) {}

                    // Re-upload with new filename
                    String newContentUrl = supabaseStorageService.uploadBookContentFromBytes(
                            pdfData,
                            form.getTitle()
                    );
                    book.setBook_content(newContentUrl);
                    System.out.println("New Content URL after rename: " + newContentUrl);
                }
            } catch (Exception e) {
                System.err.println("Failed to rename PDF file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("\n--- CONTENT UPDATE: No changes, keeping existing PDF ---");
        }

        // Update author
        if (form.getAuthor() != null && !form.getAuthor().trim().isEmpty()) {
            Author author = authorRepository.findByName(form.getAuthor())
                    .orElseThrow(() -> new RuntimeException("Author not found: " + form.getAuthor()));
            book.setAuthor(author);
        }

        // Update category
        if (form.getCategory_type() != null && !form.getCategory_type().trim().isEmpty()) {
            Long categoryId = Long.parseLong(form.getCategory_type());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            book.setCategory(category);
        }

        book.setLast_updated(LocalDateTime.now());
        Book savedBook = bookRepository.save(book);

        System.out.println("\n✓ Book updated successfully in database");
        System.out.println("Final Image URL: " + savedBook.getImage());
        System.out.println("Final Content URL: " + savedBook.getBook_content());
        System.out.println("=== END UPDATE BOOK OPERATION ===\n");

        return savedBook;
    }

    // Helper method to extract filename from URL
    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        String[] parts = url.split("/");
        String encodedFilename = parts[parts.length - 1];
        // Decode URL encoding
        return java.net.URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8);
    }

    // Helper method to get file extension
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // default
        }
        return filename.substring(filename.lastIndexOf("."));
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
}

