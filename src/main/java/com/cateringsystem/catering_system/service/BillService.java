package com.cateringsystem.catering_system.service;

import com.cateringsystem.catering_system.model.Bill;
import com.cateringsystem.catering_system.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }
}
