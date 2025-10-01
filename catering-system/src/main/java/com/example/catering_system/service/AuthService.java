package com.example.catering_system.service;

import com.example.catering_system.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class AuthService {

    private final DataSource dataSource;

    @Autowired
    public AuthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean authenticate(String username, String password) {
        String u = (username == null) ? "" : username.trim();
        String p = (password == null) ? "" : password;

        if (u.isEmpty() || p.isEmpty()) return false;

        try (Connection conn = dataSource.getConnection()) {
            UserDAO userDAO = new UserDAO(conn);
            return userDAO.isValidUser(u, p);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Optional: get role after successful login
    public String roleOf(String username) {
        String u = (username == null) ? "" : username.trim();
        if (u.isEmpty()) return null;
        try (Connection conn = dataSource.getConnection()) {
            UserDAO userDAO = new UserDAO(conn);
            return userDAO.getUserRole(u);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
