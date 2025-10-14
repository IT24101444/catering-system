package com.example.catering_system.controller;

import com.example.catering_system.model.User;
import com.example.catering_system.service.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SessionController {

    @GetMapping("/api/session/me")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> currentUser(@CookieValue(value = "SESSION_ID", required = false) String sessionId) {
        Map<String, Object> body = new HashMap<>();
        if (sessionId == null) {
            body.put("authenticated", false);
            return ResponseEntity.ok(body);
        }
        User user = SessionManager.getInstance().getUser(sessionId);
        if (user == null) {
            body.put("authenticated", false);
        } else {
            body.put("authenticated", true);
            body.put("username", user.getUsername());
            body.put("fullName", user.getFullName());
            body.put("role", user.getRole());
            body.put("email", user.getEmail());
        }
        return ResponseEntity.ok(body);
    }
}


