package com.example.webbook.controller;

import com.example.webbook.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PayPalWebhookController {

    @Value("${paypal.webhook-id}")
    private String webhookId;

    @Autowired
    private OrderService orderService;

    /**
     * Handle PayPal webhooks
     */
    @PostMapping("/webhook/paypal")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("PAYPAL-TRANSMISSION-ID") String transmissionId,
            @RequestHeader("PAYPAL-TRANSMISSION-TIME") String transmissionTime,
            @RequestHeader("PAYPAL-TRANSMISSION-SIG") String transmissionSig,
            @RequestHeader("PAYPAL-CERT-URL") String certUrl,
            @RequestHeader("PAYPAL-AUTH-ALGO") String authAlgo) {

        try {
            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event_type");

            // Handle different event types
            switch (eventType) {
                case "PAYMENT.CAPTURE.COMPLETED":
                    // Payment completed successfully
                    System.out.println("Payment capture completed webhook received");
                    break;

                case "PAYMENT.CAPTURE.DENIED":
                    // Payment denied
                    System.out.println("Payment capture denied webhook received");
                    break;

                // Add more event types as needed
            }

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Webhook processing failed");
        }
    }
}
