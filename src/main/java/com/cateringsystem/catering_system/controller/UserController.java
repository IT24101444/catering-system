package com.cateringsystem.catering_system.controller;

import com.cateringsystem.catering_system.model.User;
import com.cateringsystem.catering_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Registration form page
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());  // Add empty user object for form binding
        return "register";  // Returns the registration page (register.html)
    }

    // Handle user registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);  // Encrypt password and save user to DB
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";  // Redirect to login page after successful registration
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";  // Redirect back to registration page on error
        }
    }

    // Login form page
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());  // Add empty user object for form binding
        return "login";  // Returns the login page (login.html)
    }

    // Handle login
    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());

            Authentication authentication = authenticationManager.authenticate(token);  // Authenticate the user
            SecurityContextHolder.getContext().setAuthentication(authentication);  // Set authentication context

            return "redirect:/dashboard";  // Redirect to dashboard after successful login
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login";  // Redirect back to login page on error
        }
    }
}