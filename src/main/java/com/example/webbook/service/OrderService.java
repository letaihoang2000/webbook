package com.example.webbook.service;

import com.example.webbook.dto.CartItem;
import com.example.webbook.dto.PaymentResult;
import com.example.webbook.model.Book;
import com.example.webbook.model.Order;
import com.example.webbook.repository.BookRepository;
import com.example.webbook.repository.OrderRepository;
import com.example.webbook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Create order from cart
     */
    @Transactional
    public Order createOrderFromCart(UUID userId, PaymentResult paymentResult) {
        // Get cart items
        List<CartItem> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create order
        Order order = new Order();
        order.setUser(userRepository.findById(userId).orElseThrow());
        order.setPaypalOrderId(paymentResult.getOrderId());
        order.setPaypalPayerId(paymentResult.getPayerPayerId()); // Store PayPal payer ID
        order.setTotalAmount(paymentResult.getAmount());
        order.setCurrency(paymentResult.getCurrency());
        order.setStatus("COMPLETED");
        order.setPayerName(paymentResult.getPayerName());
        order.setPayerEmail(paymentResult.getPayerEmail());

        // Add books
        List<Book> books = cartItems.stream()
                .map(item -> bookRepository.findById(UUID.fromString(item.getBookId())).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        order.setBooks(books);

        return orderRepository.save(order);
    }

    /**
     * Create order from single book purchase
     */
    @Transactional
    public Order createOrderFromSingleBook(UUID userId, UUID bookId, PaymentResult paymentResult) {
        // Get the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Create order
        Order order = new Order();
        order.setUser(userRepository.findById(userId).orElseThrow());
        order.setPaypalOrderId(paymentResult.getOrderId());
        order.setPaypalPayerId(paymentResult.getPayerPayerId()); // Store PayPal payer ID
        order.setTotalAmount(paymentResult.getAmount());
        order.setCurrency(paymentResult.getCurrency());
        order.setStatus("COMPLETED");
        order.setPayerName(paymentResult.getPayerName());
        order.setPayerEmail(paymentResult.getPayerEmail());

        // Add single book
        List<Book> books = new ArrayList<>();
        books.add(book);
        order.setBooks(books);

        return orderRepository.save(order);
    }

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmation(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser().getEmail());
            message.setSubject("Order Confirmation - BookSaw #" + order.getId());

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Dear ").append(order.getUser().getFirst_name()).append(",\n\n");
            emailBody.append("Thank you for your purchase!\n\n");
            emailBody.append("Order Details:\n");
            emailBody.append("Order ID: ").append(order.getId()).append("\n");
            emailBody.append("PayPal Transaction: ").append(order.getPaypalOrderId()).append("\n");
            emailBody.append("Total Amount: $").append(String.format("%.2f", order.getTotalAmount())).append("\n\n");
            emailBody.append("Books Purchased:\n");

            for (Book book : order.getBooks()) {
                emailBody.append("- ").append(book.getTitle()).append(" ($")
                        .append(String.format("%.2f", book.getPrice())).append(")\n");
            }

            emailBody.append("\n\nYou can access your books at: https://webbook-production-6c11.up.railway.app/customer/my-books\n\n");
            emailBody.append("Best regards,\nBookSaw Team");

            message.setText(emailBody.toString());

            mailSender.send(message);
            System.out.println("Order confirmation email sent to: " + order.getUser().getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    /**
     * Get all purchased books for a user
     */
    public Set<Book> getPurchasedBooks(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        Set<Book> purchasedBooks = new HashSet<>();
        for (Order order : orders) {
            if ("COMPLETED".equals(order.getStatus())) {
                purchasedBooks.addAll(order.getBooks());
            }
        }

        return purchasedBooks;
    }
}