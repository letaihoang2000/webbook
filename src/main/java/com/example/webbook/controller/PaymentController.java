package com.example.webbook.controller;

import com.example.webbook.dto.PaymentResult;
import com.example.webbook.model.Book;
import com.example.webbook.model.Order;
import com.example.webbook.model.User;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.BookService;
import com.example.webbook.service.CartService;
import com.example.webbook.service.OrderService;
import com.example.webbook.service.PayPalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class PaymentController {

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/cart/checkout")
    public String checkout(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            Map<String, Object> summary = cartService.getCartSummary(user.getId());
            double total = ((Number) summary.get("totalValue")).doubleValue();

            if (total <= 0) {
                return "redirect:/cart?payment=failed&reason=empty";
            }

            String approvalUrl = payPalService.createOrder(user, total, "USD");
            System.out.println("Redirecting user {} to PayPal" + user.getEmail());

            return "redirect:" + approvalUrl;

        } catch (Exception e) {
            System.out.println("Checkout error" + e);
            return "redirect:/cart?payment=failed&reason=error";
        }
    }

    @PostMapping("/book/purchase/{bookId}")
    public String purchaseBook(@PathVariable String bookId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Get book details
            UUID id = UUID.fromString(bookId);
            Book book = bookService.getBookById(id);

            if (book == null) {
                return "redirect:/customer/book/" + bookId + "?error=notfound";
            }

            double price = book.getPrice();

            if (price <= 0) {
                return "redirect:/customer/book/" + bookId + "?error=invalidprice";
            }

            // Create PayPal order for single book
            String approvalUrl = payPalService.createOrder(user, price, "USD");
            // Store bookId in session for later retrieval
            // We'll pass it as a custom parameter
            return "redirect:" + approvalUrl;

        } catch (Exception e) {
            return "redirect:/customer/book/" + bookId + "?error=payment";
        }
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("token") String orderId,
            Authentication authentication) {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            PaymentResult result = payPalService.captureOrder(orderId);

            if (result.isSuccess()) {
                Order order = orderService.createOrderFromCart(user.getId(), result);
                orderService.sendOrderConfirmation(order);
                cartService.clearCart(user.getId());

                // SUCCESS → Back to CART with modal
                return "redirect:/cart?payment=success&orderId="
                        + order.getId() + "&amount=" + order.getTotalAmount();
            } else {
                return "redirect:/cart?payment=failed&reason=capture";
            }

        } catch (Exception e) {
            System.out.println("Error processing payment" + e);
            return "redirect:/cart?payment=failed&reason=error";
        }
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "redirect:/cart?payment=cancelled";
    }
}