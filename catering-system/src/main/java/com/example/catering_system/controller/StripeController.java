package com.example.catering_system.controller;

import com.example.catering_system.service.InvoiceService;
import com.example.catering_system.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    @Value("${stripe.public.key:pk_test_dummy}")
    private String publicKey;

    @Value("${stripe.secret.key:sk_test_dummy}")
    private String secretKey;

    private final InvoiceService invoiceService;
    private final OrderService orderService;

    public StripeController(InvoiceService invoiceService, OrderService orderService) {
        this.invoiceService = invoiceService;
        this.orderService = orderService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Object> payload) {
        long invoiceId = ((Number) payload.get("invoiceId")).longValue();
        double amount = ((Number) payload.get("amount")).doubleValue();
        // Normally call Stripe API to create a session and return sessionId.
        // Here, we return a mock session id to allow front-end redirect flow in dev.
        String sessionId = "cs_test_mock_session_" + invoiceId;
        return ResponseEntity.ok(Map.of(
                "publicKey", publicKey,
                "sessionId", sessionId
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody Map<String, Object> event) {
        // In real integration, verify signature and parse event type.
        // On successful payment, mark invoice paid and confirm order payment
        try {
            Object data = event.get("data");
            // Expecting payload to include invoiceId
            Long invoiceId = null;
            if (data instanceof Map<?,?> d) {
                Object inv = d.get("invoiceId");
                if (inv instanceof Number n) invoiceId = n.longValue();
            }
            if (invoiceId != null) {
                invoiceService.markPaid(invoiceId);
                // many systems map invoiceId to orderId; adapt if different relation exists
                orderService.confirmPayment(invoiceId);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok().build();
    }
}


