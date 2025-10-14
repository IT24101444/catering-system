package com.example.catering_system.controller;

import com.example.catering_system.model.Payment;
import com.example.catering_system.service.PaymentService;
import com.example.catering_system.service.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentApiController {

    private final PaymentService paymentService;

    public PaymentApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/online")
    public ResponseEntity<?> online(@CookieValue(value = "SESSION_ID", required = false) String sessionId,
                                    @RequestBody Map<String, Object> payload) {
        if (SessionManager.getInstance().getUser(sessionId) == null) return ResponseEntity.status(401).build();
        Payment p = new Payment();
        p.setInvoiceId(((Number)payload.get("invoiceId")).longValue());
        p.setAmount(((Number)payload.get("amount")).doubleValue());
        p.setPaymentMethod("ONLINE");
        boolean ok = paymentService.processViaGateway(p, "creditCard");
        paymentService.recordPayment(p);
        return ResponseEntity.ok(Map.of("processed", ok));
    }

    @PostMapping("/bank-receipt")
    public ResponseEntity<?> bankReceipt(@CookieValue(value = "SESSION_ID", required = false) String sessionId,
                                         @RequestBody Map<String, Object> payload) {
        if (SessionManager.getInstance().getUser(sessionId) == null) return ResponseEntity.status(401).build();
        Payment p = new Payment();
        p.setInvoiceId(((Number)payload.get("invoiceId")).longValue());
        p.setAmount(((Number)payload.get("amount")).doubleValue());
        p.setPaymentMethod("BANK_RECEIPT");
        // store reference, optional
        if (payload.get("reference") != null) {
            p.setNotes(String.valueOf(payload.get("reference")));
        }
        paymentService.recordPayment(p);
        return ResponseEntity.ok(Map.of("submitted", true));
    }
}


