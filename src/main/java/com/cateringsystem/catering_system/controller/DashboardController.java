package com.cateringsystem.catering_system.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // Display User Dashboard after login
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get the currently logged-in user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        // Here you can fetch additional details like orders, bookings, etc., for the logged-in user
        // For now, we will just show the username on the dashboard

        model.addAttribute("username", username);
        return "dashboard";  // Display the dashboard.html page
    }
}
