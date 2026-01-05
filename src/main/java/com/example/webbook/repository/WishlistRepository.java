package com.example.webbook.repository;

import com.example.webbook.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    // Find all wishlist items for a user
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId ORDER BY w.added_at DESC")
    List<Wishlist> findByUserId(@Param("userId") UUID userId);

    // Check if book is in user's wishlist
    @Query("SELECT COUNT(w) > 0 FROM Wishlist w WHERE w.user.id = :userId AND w.book.id = :bookId")
    boolean existsByUserIdAndBookId(@Param("userId") UUID userId, @Param("bookId") UUID bookId);

    // Find specific wishlist item
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.book.id = :bookId")
    Optional<Wishlist> findByUserIdAndBookId(@Param("userId") UUID userId, @Param("bookId") UUID bookId);

    // Delete by user and book
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.book.id = :bookId")
    void deleteByUserIdAndBookId(@Param("userId") UUID userId, @Param("bookId") UUID bookId);

    // Count wishlist items for user
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);
}
