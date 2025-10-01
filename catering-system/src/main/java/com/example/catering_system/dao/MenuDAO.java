package com.example.catering_system.dao;

import com.example.catering_system.model.Menu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    private final Connection conn;

    public MenuDAO(Connection conn) {
        this.conn = conn;
    }

    // ---------------------------
    // Create
    // ---------------------------

    // Create a new menu and return generated id
    public int insertMenu(String name, String status) throws SQLException {
        // Provide a default event_id = 0 to avoid NOT NULL violations if column is required
        final String sql = "INSERT INTO Menus (menu_name, status, event_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, safeText(name));
            ps.setString(2, safeStatus(status));
            ps.setInt(3, 0); // default event_id; adjust if caller provides it elsewhere
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        // Fallback for DBs without getGeneratedKeys()
        try (PreparedStatement ps2 = conn.prepareStatement("SELECT MAX(menu_id) FROM Menus");
             ResultSet rs2 = ps2.executeQuery()) {
            return rs2.next() ? rs2.getInt(1) : 0;
        }
    }

    // Overload to allow explicit eventId
    public int insertMenu(String name, String status, Integer eventId) throws SQLException {
        final String sql = "INSERT INTO Menus (menu_name, status, event_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, safeText(name));
            ps.setString(2, safeStatus(status));
            ps.setInt(3, eventId == null ? 0 : eventId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps2 = conn.prepareStatement("SELECT MAX(menu_id) FROM Menus");
             ResultSet rs2 = ps2.executeQuery()) {
            return rs2.next() ? rs2.getInt(1) : 0;
        }
    }

    // ---------------------------
    // Read
    // ---------------------------

    // Default list (newest first)
    public List<Menu> getAllMenusNewestFirst() throws SQLException {
        final String sql = """
                SELECT menu_id AS id, menu_name AS name, status, event_id AS eventId
                FROM Menus
                ORDER BY menu_id DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Menu> menus = new ArrayList<>();
            while (rs.next()) menus.add(mapRow(rs));
            return menus;
        }
    }

    // Only confirmed menus
    public List<Menu> getConfirmedMenus() throws SQLException {
        final String sql = """
                SELECT menu_id AS id, menu_name AS name, status, event_id AS eventId
                FROM Menus
                WHERE LOWER(status) = 'confirmed'
                ORDER BY menu_id DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Menu> menus = new ArrayList<>();
            while (rs.next()) menus.add(mapRow(rs));
            return menus;
        }
    }

    // Filtered list: optional status, q (name contains), optional eventId via category, sort
    public List<Menu> getMenusFiltered(String category, String status, String query, String sort) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT menu_id AS id, menu_name AS name, status, event_id AS eventId FROM Menus WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (hasText(status)) {
            sql.append(" AND LOWER(status) = LOWER(?)");
            params.add(status.trim());
        }
        if (hasText(query)) {
            sql.append(" AND LOWER(menu_name) LIKE LOWER(?)");
            params.add("%" + query.trim() + "%");
        }
        if (hasText(category)) {
            Integer eventId = parseIntOrNull(category);
            if (eventId != null) {
                sql.append(" AND event_id = ?");
                params.add(eventId);
            }
        }

        if ("name_asc".equalsIgnoreCase(sort)) {
            sql.append(" ORDER BY menu_name ASC");
        } else {
            sql.append(" ORDER BY menu_id DESC"); // default/newest
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Menu> menus = new ArrayList<>();
                while (rs.next()) menus.add(mapRow(rs));
                return menus;
            }
        }
    }

    // Single by id
    public Menu getMenuById(int id) throws SQLException {
        final String sql = """
                SELECT menu_id AS id, menu_name AS name, status, event_id AS eventId
                FROM Menus
                WHERE menu_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // Optional: pagination (SQL Server 2012+ syntax; adjust for MySQL as LIMIT ? OFFSET ?)
    public List<Menu> getMenusPaged(int page, int size) throws SQLException {
        int limit = Math.max(1, size);
        int offset = Math.max(0, page - 1) * limit;

        final String sql = """
                SELECT menu_id AS id, menu_name AS name, status, event_id AS eventId
                FROM Menus
                ORDER BY menu_id DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Menu> menus = new ArrayList<>();
                while (rs.next()) menus.add(mapRow(rs));
                return menus;
            }
        }
    }

    public int countAll() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Menus");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---------------------------
    // Update / Delete
    // ---------------------------

    public boolean updateMenu(int id, String name, String status) throws SQLException {
        final String sql = "UPDATE Menus SET menu_name = ?, status = ? WHERE menu_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safeText(name));
            ps.setString(2, safeStatus(status));
            ps.setInt(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteMenu(int id) throws SQLException {
        final String sql = "DELETE FROM Menus WHERE menu_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // ---------------------------
    // Guest count stub
    // ---------------------------

    public int getGuestCountByMenu(int menuId) throws SQLException {
        return 0;
    }

    // ---------------------------
    // Mapper & helpers
    // ---------------------------

    private Menu mapRow(ResultSet rs) throws SQLException {
        Menu m = new Menu();
        // Menu now uses Integer id; JDBC getInt returns 0 on NULL; check wasNull if needed
        int id = rs.getInt("id");
        if (rs.wasNull()) {
            m.setId(nullSafeInteger(null));
        } else {
            m.setId(id);
        }

        m.setName(rs.getString("name"));

        String st = rs.getString("status");
        m.setStatus(st == null || st.isBlank() ? "draft" : st);

        int event = rs.getInt("eventId");
        if (rs.wasNull()) {
            m.setEventId(nullSafeInteger(null));
        } else {
            m.setEventId(event);
        }
        return m;
    }

    private static boolean hasText(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private static String safeText(String v) {
        return v == null ? "" : v;
    }

    private static String safeStatus(String v) {
        return hasText(v) ? v.trim() : "draft";
    }

    private static Integer parseIntOrNull(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer nullSafeInteger(Integer v) {
        return v == null ? 0 : v;
    }
}
