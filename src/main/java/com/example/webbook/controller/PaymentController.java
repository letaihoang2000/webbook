package com.example.webbook.controller;

import com.example.webbook.dto.PaymentResult;
import com.example.webbook.model.Order;
import com.example.webbook.model.User;
import com.example.webbook.security.CustomUserDetails;
import com.example.webbook.service.CartService;
import com.example.webbook.service.OrderService;
import com.example.webbook.service.PayPalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@Slf4j
public class PaymentController {

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    /**
     * Initiate checkout
     */
    @PostMapping("/cart/checkout")
    public String checkout(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Get cart summary
            Map<String, Object> summary = cartService.getCartSummary(user.getId());
            double total = ((Number) summary.get("totalValue")).doubleValue();

            if (total <= 0) {
                return "redirect:/cart?error=empty";
            }

            // Create PayPal order
            String approvalUrl = payPalService.createOrder(user.getId(), total, "USD");

            System.out.println("Redirecting user {} to PayPal: {}" + user.getEmail() + approvalUrl);

            // Redirect to PayPal
            return "redirect:" + approvalUrl;

        } catch (Exception e) {
            System.out.println("Checkout error" + e);
            return "redirect:/cart?error=payment";
        }
    }

    /**
     * PayPal returns user here after payment
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("token") String orderId,
            Authentication authentication,
            Model model) {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Capture the payment
            PaymentResult result = payPalService.captureOrder(orderId);

            if (result.isSuccess()) {
                // Create order record
                Order order = orderService.createOrderFromCart(user.getId(), result);

                // Send confirmation email
                orderService.sendOrderConfirmation(order);

                // Clear cart
                cartService.clearCart(user.getId());

                model.addAttribute("order", order);
                model.addAttribute("paymentResult", result);
                return "payment/success";

            } else {
                return "redirect:/payment/error?reason=capture_failed";
            }

        } catch (Exception e) {
            return "redirect:/payment/error?reason=processing_error";
        }
    }

    /**
     * User cancelled payment
     */
    @GetMapping("/payment/cancel")
    public String paymentCancel(Model model) {
        model.addAttribute("message", "Payment was cancelled. Your cart items are still saved.");
        return "payment/cancel";
    }

    /**
     * Payment error page
     */
    @GetMapping("/payment/error")
    public String paymentError(@RequestParam(required = false) String reason, Model model) {
        model.addAttribute("reason", reason);
        return "payment/error";
    }
}
