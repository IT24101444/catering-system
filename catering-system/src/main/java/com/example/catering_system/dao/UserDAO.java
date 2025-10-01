package com.example.catering_system.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    // Simple credential check (demo). In production, store hashed passwords and verify hashes.
    public boolean isValidUser(String username, String password) throws Exception {
        String u = username == null ? "" : username.trim();
        String p = password == null ? "" : password;

        final String sql = "SELECT COUNT(*) FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Optional: fetch user role after login
    public String getUserRole(String username) throws Exception {
        String u = username == null ? "" : username.trim();
        final String sql = "SELECT role FROM Users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("role") : null;
            }
        }
    }
}
