package com.example.webbook.service;

import com.example.webbook.dto.CartItem;
import com.example.webbook.model.Book;
import com.example.webbook.model.Cart;
import com.example.webbook.model.User;
import com.example.webbook.repository.BookRepository;
import com.example.webbook.repository.CartRepository;
import com.example.webbook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    // Get or create cart for user
    @Transactional
    public Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    // Get all cart items for a user
    public List<CartItem> getCartItems(UUID userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Cart cart = cartOpt.get();
        return cart.getBooks().stream()
                .map(book -> convertToDTO(book, cart))
                .collect(Collectors.toList());
    }

    // Get cart summary (count and total)
    public Map<String, Object> getCartSummary(UUID userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

        long itemCount = 0;
        double totalValue = 0.0;

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            itemCount = cart.getBooks().size();
            totalValue = cart.getBooks().stream()
                    .mapToDouble(Book::getPrice)
                    .sum();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("itemCount", itemCount);
        summary.put("totalValue", totalValue);

        return summary;
    }

    // Add book to cart
    @Transactional
    public Map<String, Object> addToCart(UUID userId, UUID bookId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            Cart cart = getOrCreateCart(userId);

            // Check if book already in cart
            boolean bookExists = cart.getBooks().stream()
                    .anyMatch(b -> b.getId().equals(bookId));

            if (bookExists) {
                response.put("success", false);
                response.put("message", "Book is already in your cart");
                response.put("action", "duplicate");
            } else {
                // Add book to cart
                cart.getBooks().add(book);
                cartRepository.save(cart);

                response.put("success", true);
                response.put("message", "Book added to cart!");
                response.put("action", "added");
            }

            // Include updated cart summary
            Map<String, Object> summary = getCartSummary(userId);
            response.put("cartSummary", summary);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add to cart: " + e.getMessage());
        }

        return response;
    }

    // Remove book from cart
    @Transactional
    public Map<String, Object> removeFromCart(UUID userId, UUID bookId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            boolean removed = cart.getBooks().removeIf(book -> book.getId().equals(bookId));

            if (removed) {
                cartRepository.save(cart);
                response.put("success", true);
                response.put("message", "Book removed from cart");
            } else {
                response.put("success", false);
                response.put("message", "Book not found in cart");
            }

            // Include updated cart summary
            Map<String, Object> summary = getCartSummary(userId);
            response.put("cartSummary", summary);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to remove from cart: " + e.getMessage());
        }

        return response;
    }

    // Clear entire cart
    @Transactional
    public Map<String, Object> clearCart(UUID userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                cart.getBooks().clear();
                cartRepository.save(cart);
            }

            response.put("success", true);
            response.put("message", "Cart cleared successfully");

            Map<String, Object> summary = getCartSummary(userId);
            response.put("cartSummary", summary);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear cart: " + e.getMessage());
        }

        return response;
    }

    // Check if book is in cart
    public boolean isInCart(UUID userId, UUID bookId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            return false;
        }

        return cartOpt.get().getBooks().stream()
                .anyMatch(book -> book.getId().equals(bookId));
    }

    // Get cart book IDs (for displaying in UI)
    public Set<String> getCartBookIds(UUID userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);

        if (cartOpt.isEmpty()) {
            return new HashSet<>();
        }

        return cartOpt.get().getBooks().stream()
                .map(book -> book.getId().toString())
                .collect(Collectors.toSet());
    }

    // Convert Book entity to DTO
    private CartItem convertToDTO(Book book, Cart cart) {
        CartItem dto = new CartItem();
        dto.setBookId(book.getId().toString());
        dto.setBookTitle(book.getTitle());
        dto.setBookImage(book.getImage());
        dto.setPrice(book.getPrice());

        if (book.getAuthor() != null) {
            dto.setAuthorName(book.getAuthor().getName());
        }

        if (book.getCategory() != null) {
            dto.setCategoryName(book.getCategory().getName());
        }

        if (cart.getLastUpdated() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dto.setAddedAt(cart.getLastUpdated().format(formatter));
        }

        return dto;
    }
}
