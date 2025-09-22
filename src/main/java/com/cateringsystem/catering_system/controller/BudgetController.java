package com.cateringsystem.catering_system.controller;

import com.cateringsystem.catering_system.model.Budget;
import com.cateringsystem.catering_system.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    // Correct use of @PathVariable for dynamic URL parameters
    @GetMapping("/budget/{eventId}")
    public String viewBudget(@PathVariable("eventId") int eventId, Model model) {
        // Fetch budget based on eventId
        Budget budget = budgetService.getBudgetByEventId(eventId);
        model.addAttribute("budget", budget);
        return "budget-management";  // Thymeleaf page to render
    }
}
