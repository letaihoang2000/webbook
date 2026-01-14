package com.example.webbook.service;

import com.example.webbook.dto.PaymentResult;
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

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Create PayPal order
     */
    public String createOrder(UUID userId, double total, String currency) {
        try {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            // Amount
            AmountWithBreakdown amount = new AmountWithBreakdown()
                    .currencyCode(currency)
                    .value(String.format("%.2f", total));

            // Purchase unit
            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .referenceId(userId.toString()) // Store user ID for later
                    .amountWithBreakdown(amount)
                    .description("BookSaw - Online Book Purchase");

            orderRequest.purchaseUnits(Arrays.asList(purchaseUnit));

            // Return URLs - using your Railway domain
            ApplicationContext appContext = new ApplicationContext()
                    .returnUrl(baseUrl + "/payment/success")
                    .cancelUrl(baseUrl + "/payment/cancel")
                    .brandName("BookSaw")
                    .landingPage("BILLING")
                    .shippingPreference("NO_SHIPPING"); // Digital products

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
     * Capture payment (called when user returns from PayPal)
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

            // Extract payer info
            if (order.payer() != null && order.payer().name() != null) {
                result.setPayerName(order.payer().name().givenName() + " " +
                        order.payer().name().surname());
                result.setPayerEmail(order.payer().email());
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
