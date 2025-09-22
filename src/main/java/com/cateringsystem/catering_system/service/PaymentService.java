package com.cateringsystem.catering_system.service;

import com.cateringsystem.catering_system.model.Payment;
import com.cateringsystem.catering_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public void recordPayment(Payment payment) {
        paymentRepository.save(payment);
    }
}
