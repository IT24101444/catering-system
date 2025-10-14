package com.example.catering_system.controller;

import com.example.catering_system.model.User;
import com.example.catering_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.catering_system.service.SessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Show the login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Returns login.html template
    }

    // Handle login logic
    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        User user = userService.findUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Login successful: create session and set cookie
            String sessionId = SessionManager.getInstance().createSession(user);
            Cookie cookie = new Cookie("SESSION_ID", sessionId);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return "redirect:/home"; // Redirect to home page
        }
        // Login failed
        return "redirect:/login"; // Redirect to login page with error message
    }

    @PostMapping("/logout")
    public String logout(@jakarta.servlet.http.CookieValue(value = "SESSION_ID", required = false) String sessionId,
                         HttpServletResponse response) {
        if (sessionId != null) {
            SessionManager.getInstance().invalidate(sessionId);
        }
        Cookie cookie = new Cookie("SESSION_ID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
