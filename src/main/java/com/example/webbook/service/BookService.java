package com.example.webbook.service;

import com.example.webbook.dto.BookInfo;
import com.example.webbook.repository.BookRepository;
import com.example.webbook.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

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
                // Default: search all books
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

    // Convert Book entity to BookInfo DTO
    private BookInfo convertToBookInfo(Book book) {
        BookInfo bookInfo = new BookInfo();
        bookInfo.setBook_id(book.getId().toString());
        bookInfo.setTitle(book.getTitle());
        bookInfo.setImage(book.getImage());
        bookInfo.setDescription(book.getDescription());

        System.out.println(bookInfo.getImage());

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
