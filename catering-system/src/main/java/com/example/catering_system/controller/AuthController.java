package com.example.catering_system.controller;

import com.example.catering_system.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Login page
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "next", required = false) String next,
                            Model model) {
        if (logout != null) {
            model.addAttribute("message", "Logged out successfully");
        }
        if (next != null && !next.isBlank()) {
            model.addAttribute("next", next);
        }
        return "layouts/login"; // POINTS TO templates/layouts/login.html
    }

    // Handle login
    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam(value = "next", required = false) String next,
                              HttpSession session,
                              RedirectAttributes redirect) {
        String u = username == null ? "" : username.trim();
        String p = password == null ? "" : password;

        boolean validUser = authService.authenticate(u, p);
        if (validUser) {
            session.setAttribute("USER", u);
            if (next != null && !next.isBlank() && next.startsWith("/") && !next.startsWith("//")) {
                return "redirect:" + next;
            }
            return "redirect:/chef/menus";
        } else {
            redirect.addFlashAttribute("error", "Invalid username or password");
            if (next != null && !next.isBlank()) {
                redirect.addFlashAttribute("next", next);
            }
            return "redirect:/login";
        }
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        try { session.invalidate(); } catch (Exception ignored) {}
        return "redirect:/login?logout=1";
    }
}
