package com.cateringsystem.catering_system.repository;

import com.cateringsystem.catering_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
