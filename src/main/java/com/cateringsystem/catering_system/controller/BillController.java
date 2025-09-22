package com.cateringsystem.catering_system.controller;

import com.cateringsystem.catering_system.model.Bill;
import com.cateringsystem.catering_system.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BillController {
    @Autowired
    private BillService billService;

    @GetMapping("/bills")
    public String viewBills(Model model) {
        model.addAttribute("bills", billService.getAllBills());
        return "bill-list";
    }
}
