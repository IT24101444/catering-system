package com.example.catering_system.service;

import com.example.catering_system.dao.MenuDAO;
import com.example.catering_system.dao.ScheduleDAO;
import com.example.catering_system.model.Menu;
import com.example.catering_system.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ChefService {

    private final DataSource dataSource;

    @Autowired
    public ChefService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ------------- Menus -------------

    public List<Menu> getAllMenusNewestFirst() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new MenuDAO(conn).getAllMenusNewestFirst();
        }
    }

    public List<Menu> getConfirmedMenus() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new MenuDAO(conn).getConfirmedMenus();
        }
    }

    public Menu getMenuById(int id) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new MenuDAO(conn).getMenuById(id);
        }
    }

    public int getGuestCountByMenu(int id) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new MenuDAO(conn).getGuestCountByMenu(id); // stubbed to 0 in DAO
        }
    }

    public boolean updateMenu(int id, String name, String status) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String safeStatus = safeStatus(status);
            String safeName = safeText(name);
            return new MenuDAO(conn).updateMenu(id, safeName, safeStatus);
        }
    }

    // Create with required name/status only (eventId defaults to 0 in DAO)
    public int createMenu(String name, String status) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String safeStatus = safeStatus(status);
            String safeName = safeText(name);
            return new MenuDAO(conn).insertMenu(safeName, safeStatus);
        }
    }

    // Overload to support eventId explicitly
    public int createMenu(String name, String status, Integer eventId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String safeStatus = safeStatus(status);
            String safeName = safeText(name);
            return new MenuDAO(conn).insertMenu(safeName, safeStatus, eventId);
        }
    }

    public boolean deleteMenu(int id) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new MenuDAO(conn).deleteMenu(id);
        }
    }

    public List<Menu> getConfirmedMenusFiltered(String category, String status, String query, String sort) throws Exception {
        String cat = trimOrNull(category);
        String stat = trimOrNull(status);
        String q = trimOrNull(query);
        String srt = trimOrNull(sort);

        try (Connection conn = dataSource.getConnection()) {
            MenuDAO menuDAO = new MenuDAO(conn);
            try {
                return menuDAO.getMenusFiltered(cat, stat, q, srt);
            } catch (Exception ex) {
                // Fallback: in-memory over confirmed menus (DB-first approach preferred)
                List<Menu> base = menuDAO.getConfirmedMenus();
                String ql = q == null ? "" : q.toLowerCase(Locale.ROOT);
                String sl = stat == null ? "" : stat.toLowerCase(Locale.ROOT);

                List<Menu> filtered = base.stream()
                        .filter(m -> sl.isEmpty() || (m.getStatus() != null && m.getStatus().toLowerCase(Locale.ROOT).equals(sl)))
                        .filter(m -> ql.isEmpty() || (m.getName() != null && m.getName().toLowerCase(Locale.ROOT).contains(ql)))
                        .collect(Collectors.toList());

                if ("name_asc".equalsIgnoreCase(srt)) {
                    filtered.sort(Comparator.comparing(m -> String.valueOf(m.getName()), String.CASE_INSENSITIVE_ORDER));
                } else {
                    filtered.sort((a, b) -> Integer.compare(
                            b.getId() == null ? 0 : b.getId(),
                            a.getId() == null ? 0 : a.getId())
                    ); // newest first
                }
                return filtered;
            }
        }
    }

    // ------------- Schedules -------------

    public boolean saveSchedule(int eventId, int chefId, String plan, String status) throws Exception {
        String safeStatus = safeStatus(status);
        try (Connection conn = dataSource.getConnection()) {
            return new ScheduleDAO(conn).saveSchedule(eventId, chefId, plan, safeStatus);
        }
    }

    public List<Schedule> getSchedules() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new ScheduleDAO(conn).getSchedules();
        }
    }

    public Schedule getScheduleById(int id) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new ScheduleDAO(conn).getScheduleById(id);
        }
    }

    public boolean updateSchedule(int id, int eventId, int chefId, String plan, String status) throws Exception {
        String safeStatus = safeStatus(status);
        try (Connection conn = dataSource.getConnection()) {
            return new ScheduleDAO(conn).updateSchedule(id, eventId, chefId, plan, safeStatus);
        }
    }

    public boolean deleteSchedule(int id) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            return new ScheduleDAO(conn).deleteSchedule(id);
        }
    }

    // ------------- Helpers -------------

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safeStatus(String v) {
        if (v == null || v.isBlank()) return "draft";
        return v.trim();
    }

    private static String safeText(String v) {
        return v == null ? "" : v;
    }
}
