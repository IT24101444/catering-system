package com.example.catering_system.controller;

import com.example.catering_system.repository.ReviewRepository;
import com.example.catering_system.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public AdminCustomerController(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("reviews", reviewRepository.findTop10ByOrderByCreatedAtDesc());
        return "admin-customers";
    }
}


