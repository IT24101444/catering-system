package com.example.catering_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("activePage", "home");
        model.addAttribute("pageTitle", "Home | Catering System");
        // No layout usage here; home.html is a full page
        return "home";
    }

    // Menus entry (alias to ChefController list)
    @GetMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("pageTitle", "Menu | Catering System");
        model.addAttribute("activePage", "menu");
        return "redirect:/menus"; // route handled by ChefController -> layouts/menus.html
    }

    // Schedules entry (alias)
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("pageTitle", "Schedules | Catering System");
        model.addAttribute("activePage", "schedules");
        return "redirect:/schedule-list"; // route handled by ChefController -> layouts/schedule-list.html
    }
}

