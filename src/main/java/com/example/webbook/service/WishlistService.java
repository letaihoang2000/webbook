package com.example.webbook.service;

import com.example.webbook.dto.BookInfo;
import com.example.webbook.model.Book;
import com.example.webbook.model.User;
import com.example.webbook.model.Wishlist;
import com.example.webbook.repository.BookRepository;
import com.example.webbook.repository.UserRepository;
import com.example.webbook.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookService bookService;

    // Add book to wishlist
    @Transactional
    public boolean addToWishlist(UUID userId, UUID bookId) {
        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setBook(book);
        wishlist.setAdded_at(LocalDateTime.now());

        wishlistRepository.save(wishlist);
        return true;
    }

    // Remove book from wishlist
    @Transactional
    public boolean removeFromWishlist(UUID userId, UUID bookId) {
        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            return false;
        }

        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
        return true;
    }

    // Get all wishlist items for user
    public List<BookInfo> getUserWishlist(UUID userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        return wishlists.stream()
                .map(wishlist -> bookService.convertToBookInfo(wishlist.getBook()))
                .collect(Collectors.toList());
    }

    // Check if book is in wishlist
    public boolean isInWishlist(UUID userId, UUID bookId) {
        return wishlistRepository.existsByUserIdAndBookId(userId, bookId);
    }

    // Get wishlist count
    public long getWishlistCount(UUID userId) {
        return wishlistRepository.countByUserId(userId);
    }

    // Get wishlist IDs for checking multiple books at once
    public Set<String> getWishlistBookIds(UUID userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        return wishlists.stream()
                .map(w -> w.getBook().getId().toString())
                .collect(Collectors.toSet());
    }
}
