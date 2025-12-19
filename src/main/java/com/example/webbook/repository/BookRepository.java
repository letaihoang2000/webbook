package com.example.webbook.repository;
import com.example.webbook.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    // Get all books with pagination
    @Query("SELECT b FROM Book b")
    Page<Book> findAllBooks(Pageable pageable);

    // Search by title
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    // Search by author name
    @Query("SELECT b FROM Book b JOIN b.author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<Book> findByAuthorNameContainingIgnoreCase(@Param("authorName") String authorName, Pageable pageable);

    // Find books by author ID
    @Query("SELECT b FROM Book b WHERE b.author.id = :authorId ORDER BY b.last_updated DESC")
    List<Book> findByAuthorId(@Param("authorId") UUID authorId);

    // Count methods for pagination info
    @Query("SELECT COUNT(b) FROM Book b")
    long countAllBooks();

    @Query("SELECT COUNT(b) FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    long countByTitleContaining(@Param("title") String title);

    @Query("SELECT COUNT(b) FROM Book b JOIN b.author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    long countByAuthorNameContaining(@Param("authorName") String authorName);
}