package com.example.catering_system.dao;

import com.example.catering_system.model.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    private final Connection conn;

    public ScheduleDAO(Connection conn) {
        this.conn = conn;
    }

    // Create: Save new schedule
    public boolean saveSchedule(int eventId, int chefId, String planText, String status) throws SQLException {
        final String sql = "INSERT INTO ChefSchedules (event_id, chef_id, plan_text, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, chefId);
            ps.setString(3, safeText(planText));
            ps.setString(4, safeStatus(status));
            return ps.executeUpdate() == 1;
        }
    }

    // Read: list all schedules (newest first)
    public List<Schedule> getSchedules() throws SQLException {
        final String sql = "SELECT schedule_id AS id, event_id, chef_id, plan_text, status " +
                "FROM ChefSchedules ORDER BY schedule_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Schedule> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        }
    }

    // Optional: filter by status (case-insensitive)
    public List<Schedule> getSchedulesByStatus(String status) throws SQLException {
        final String sql = "SELECT schedule_id AS id, event_id, chef_id, plan_text, status " +
                "FROM ChefSchedules WHERE LOWER(status) = LOWER(?) ORDER BY schedule_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safeStatus(status));
            try (ResultSet rs = ps.executeQuery()) {
                List<Schedule> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    // Optional: search by plan text
    public List<Schedule> searchByPlan(String q) throws SQLException {
        final String sql = "SELECT schedule_id AS id, event_id, chef_id, plan_text, status " +
                "FROM ChefSchedules WHERE plan_text LIKE ? ORDER BY schedule_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + (q == null ? "" : q.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Schedule> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    // Optional: paged listing
    public List<Schedule> getSchedulesPaged(int page, int size) throws SQLException {
        int limit = Math.max(1, size);
        int offset = Math.max(0, (page - 1)) * limit;

        // SQL Server
        final String sql = "SELECT schedule_id AS id, event_id, chef_id, plan_text, status " +
                "FROM ChefSchedules ORDER BY schedule_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        // For MySQL/SQLite, use:
        // final String sql = "SELECT schedule_id AS id, event_id, chef_id, plan_text, status " +
        //         "FROM ChefSchedules ORDER BY schedule_id DESC LIMIT ? OFFSET ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Schedule> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    // Read: get schedule by id
    public Schedule getScheduleById(int id) throws SQLException {
        final String sql = "SELECT schedule_id AS id, event_id, chef_id, plan_text, status " +
                "FROM ChefSchedules WHERE schedule_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // Update: modify an existing schedule
    public boolean updateSchedule(int id, int eventId, int chefId, String planText, String status) throws SQLException {
        final String sql = "UPDATE ChefSchedules " +
                "SET event_id = ?, chef_id = ?, plan_text = ?, status = ? " +
                "WHERE schedule_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, chefId);
            ps.setString(3, safeText(planText));
            ps.setString(4, safeStatus(status));
            ps.setInt(5, id);
            return ps.executeUpdate() == 1;
        }
    }

    // Delete: remove a schedule by id
    public boolean deleteSchedule(int id) throws SQLException {
        final String sql = "DELETE FROM ChefSchedules WHERE schedule_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // Count (useful for pagination)
    public int countAll() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM ChefSchedules";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Mapper
    private Schedule mapRow(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setId(rs.getInt("id"));

        int event = rs.getInt("event_id");
        if (!rs.wasNull()) s.setEventId(event);

        int chef = rs.getInt("chef_id");
        if (!rs.wasNull()) s.setChefId(chef);

        s.setPlan(rs.getString("plan_text"));
        s.setStatus(rs.getString("status"));
        return s;
    }

    private String safeText(String v) {
        return v == null ? "" : v;
    }

    private String safeStatus(String v) {
        return (v == null || v.trim().isEmpty()) ? "draft" : v.trim();
    }
}
