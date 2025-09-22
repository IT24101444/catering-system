package com.cateringsystem.catering_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Maps the home page to the root URL (http://localhost:8080/)
    @GetMapping("/")
    public String home() {
        return "home";  // This corresponds to home.html in src/main/resources/templates
    }
}
