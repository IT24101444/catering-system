package com.cateringsystem.catering_system.controller;

import com.cateringsystem.catering_system.model.Payment;
import com.cateringsystem.catering_system.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/record-payment")
    public String recordPayment(Payment payment) {
        paymentService.recordPayment(payment);
        return "redirect:/bills";
    }
}
