package com.example.webbook.service;

import com.example.webbook.dto.PaymentResult;
import com.example.webbook.model.User;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
public class PayPalService {

    @Autowired
    private PayPalHttpClient payPalClient;

    @Autowired
    private UserService userService;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Create PayPal order with pre-populated user information
     */
    public String createOrder(User user, double total, String currency) {
        try {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            // Amount
            AmountWithBreakdown amount = new AmountWithBreakdown()
                    .currencyCode(currency)
                    .value(String.format("%.2f", total));

            // Purchase unit with user reference
            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .referenceId(user.getId().toString()) // Store user ID for later
                    .amountWithBreakdown(amount)
                    .description("BookSaw - Online Book Purchase")
                    .customId(user.getEmail()); // Store email as custom reference

            orderRequest.purchaseUnits(Arrays.asList(purchaseUnit));

            // PRE-POPULATE payer information from user data
            Payer payer = new Payer();

            // Use stored PayPal email if available, otherwise use user's email
            String emailToUse = (user.getPaypalEmail() != null && !user.getPaypalEmail().trim().isEmpty())
                    ? user.getPaypalEmail()
                    : user.getEmail();
            payer.email(emailToUse);

            // Set name
            Name payerName = new Name()
                    .givenName(user.getFirst_name())
                    .surname(user.getLast_name());
            payer.name(payerName);

            // Set phone if available
            if (user.getMobile() != null && !user.getMobile().trim().isEmpty()) {
                // Clean phone number (remove spaces, dashes, parentheses)
                String cleanPhone = user.getMobile().replaceAll("[\\s\\-\\(\\)]", "");

                PhoneWithType phone = new PhoneWithType()
                        .phoneNumber(new Phone().nationalNumber(cleanPhone));
                payer.phoneWithType(phone);
            }

            orderRequest.payer(payer);

            // Application context - Return URLs
            ApplicationContext appContext = new ApplicationContext()
                    .returnUrl(baseUrl + "/payment/success")
                    .cancelUrl(baseUrl + "/payment/cancel")
                    .brandName("BookSaw")
                    .landingPage("BILLING")
                    .shippingPreference("NO_SHIPPING") // Digital products
                    .userAction("PAY_NOW"); // Shows "Pay Now" instead of "Continue"

            orderRequest.applicationContext(appContext);

            // Create order
            OrdersCreateRequest request = new OrdersCreateRequest();
            request.requestBody(orderRequest);

            HttpResponse<Order> response = payPalClient.execute(request);
            Order order = response.result();

            // Get approval URL
            return order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElseThrow(() -> new RuntimeException("No approval URL found"));

        } catch (IOException e) {
            throw new RuntimeException("Failed to create payment", e);
        }
    }

    /**
     * Capture payment and sync payer information back to user
     */
    public PaymentResult captureOrder(String orderId) {
        try {
            OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
            HttpResponse<Order> response = payPalClient.execute(request);
            Order order = response.result();

            PaymentResult result = new PaymentResult();
            result.setOrderId(order.id());
            result.setStatus(order.status());
            result.setSuccess("COMPLETED".equals(order.status()));

            // Extract payer information
            if (order.payer() != null) {
                String paypalPayerId = order.payer().payerId();
                String paypalEmail = order.payer().email();

                result.setPayerPayerId(paypalPayerId);
                result.setPayerEmail(paypalEmail);

                if (order.payer().name() != null) {
                    String firstName = order.payer().name().givenName();
                    String lastName = order.payer().name().surname();
                    result.setPayerName(firstName + " " + lastName);
                }

                // Get user ID from purchase unit reference and sync PayPal info
                if (order.purchaseUnits() != null && !order.purchaseUnits().isEmpty()) {
                    String userIdStr = order.purchaseUnits().get(0).referenceId();
                    try {
                        UUID userId = UUID.fromString(userIdStr);

                        // Sync PayPal info back to user profile
                        userService.updatePayPalInfo(userId, paypalPayerId, paypalEmail);
                    } catch (Exception e) {
                        System.out.println("Could not sync PayPal info for user: {}" + userIdStr + e);
                        // Don't fail the payment if sync fails
                    }
                }
            }

            // Extract amount
            if (order.purchaseUnits() != null && !order.purchaseUnits().isEmpty()) {
                PurchaseUnit unit = order.purchaseUnits().get(0);
                if (unit.payments() != null && unit.payments().captures() != null) {
                    Capture capture = unit.payments().captures().get(0);
                    result.setAmount(Double.parseDouble(capture.amount().value()));
                    result.setCurrency(capture.amount().currencyCode());
                }
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to capture payment", e);
        }
    }

    /**
     * Get order details
     */
    public Order getOrderDetails(String orderId) {
        try {
            OrdersGetRequest request = new OrdersGetRequest(orderId);
            HttpResponse<Order> response = payPalClient.execute(request);
            return response.result();
        } catch (IOException e) {
            return null;
        }
    }
}