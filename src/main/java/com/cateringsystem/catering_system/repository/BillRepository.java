package com.cateringsystem.catering_system.repository;

import com.cateringsystem.catering_system.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Integer> {
}
