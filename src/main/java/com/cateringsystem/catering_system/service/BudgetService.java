package com.cateringsystem.catering_system.service;

import com.cateringsystem.catering_system.model.Budget;
import com.cateringsystem.catering_system.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;

    public Budget getBudgetByEventId(int eventId) {
        return budgetRepository.findById(eventId).orElse(null);
    }

    public void updateBudget(Budget budget) {
        budgetRepository.save(budget);
    }
}
