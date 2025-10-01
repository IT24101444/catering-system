package com.example.catering_system.controller;

import com.example.catering_system.model.Menu;
import com.example.catering_system.model.Schedule;
import com.example.catering_system.service.ChefService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }

    // ---------------- Menus (no class-level "/menus"; map each explicitly) ----------------

    // List menus (newest first)
    @GetMapping("/menus")
    public String listMenus(Model model) {
        try {
            List<Menu> menus = chefService.getAllMenusNewestFirst();
            model.addAttribute("menus", menus);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("menus", List.of());
        }
        model.addAttribute("activePage", "menu");
        model.addAttribute("pageTitle", "Menu Management");
        return "menus";
    }

    // Create menu (POST)
    @PostMapping("/menus")
    public String createMenu(@RequestParam String name,
                             @RequestParam String status,
                             @RequestParam(required = false, defaultValue = "0") Integer eventId) {
        try {
            chefService.createMenu(name, status, eventId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/menus";
    }

    // Menu details
    @GetMapping("/menus/{id}")
    public String getMenu(@PathVariable int id, Model model) {
        try {
            Menu menu = chefService.getMenuById(id);
            model.addAttribute("menu", menu);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("menu", null);
        }
        model.addAttribute("activePage", "menu");
        model.addAttribute("pageTitle", "Menu Details");
        // Optional guest count
        // try { model.addAttribute("guestCount", chefService.getGuestCountByMenu(id)); } catch (Exception ignore) {}
        return "menu-detail";
    }

    // Edit form
    @GetMapping("/menus/{id}/edit")
    public String editMenuForm(@PathVariable int id, Model model) {
        try {
            Menu menu = chefService.getMenuById(id);
            model.addAttribute("menu", menu);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("menu", null);
        }
        model.addAttribute("activePage", "menu");
        model.addAttribute("pageTitle", "Edit Menu");
        return "menu-edit";
    }

    // Update menu (POST) - FIX: include "/menus" in mapping to match form action
    @PostMapping("/menus/{id}/edit")
    public String updateMenu(@PathVariable int id,
                             @RequestParam String name,
                             @RequestParam String status) {
        try {
            chefService.updateMenu(id, name, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/menus";
    }

    // Delete menu (POST)
    @PostMapping("/menus/{id}/delete")
    public String deleteMenu(@PathVariable int id) {
        try {
            chefService.deleteMenu(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/menus";
    }

    // ---------------- Schedules ----------------

    @GetMapping("/schedule-list")
    public String scheduleList(Model model) {
        try {
            List<Schedule> schedules = chefService.getSchedules();
            model.addAttribute("schedules", schedules);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("schedules", Collections.emptyList());
        }
        model.addAttribute("activePage", "schedules");
        model.addAttribute("pageTitle", "Schedules");
        return "schedule-list";
    }

    // Create form
    @GetMapping("/schedule-form")
    public String newScheduleForm(Model model) {
        model.addAttribute("schedule", null); // create mode
        model.addAttribute("actionUrl", "/schedule-form");
        model.addAttribute("activePage", "schedules");
        model.addAttribute("pageTitle", "New Schedule");
        return "schedule-form";
    }

    // Edit form
    @GetMapping("/schedule-form/{id}")
    public String editScheduleForm(@PathVariable int id, Model model) {
        try {
            Schedule s = chefService.getScheduleById(id);
            model.addAttribute("schedule", s);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("schedule", null);
        }
        model.addAttribute("actionUrl", "/schedule-form/" + id);
        model.addAttribute("activePage", "schedules");
        model.addAttribute("pageTitle", "Edit Schedule");
        return "schedule-form";
    }

    // Create submit
    @PostMapping("/schedule-form")
    public String createSchedule(@RequestParam int eventId,
                                 @RequestParam int chefId,
                                 @RequestParam String plan,
                                 @RequestParam(required = false, defaultValue = "draft") String status,
                                 Model model) {
        try {
            chefService.saveSchedule(eventId, chefId, plan, status);
            return "redirect:/schedule-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to create schedule");
            model.addAttribute("schedule", null);
            model.addAttribute("actionUrl", "/schedule-form");
            model.addAttribute("activePage", "schedules");
            model.addAttribute("pageTitle", "New Schedule");
            return "schedule-form";
        }
    }

    // Update submit
    @PostMapping("/schedule-form/{id}")
    public String updateSchedule(@PathVariable int id,
                                 @RequestParam int eventId,
                                 @RequestParam int chefId,
                                 @RequestParam String plan,
                                 @RequestParam(required = false, defaultValue = "draft") String status,
                                 Model model) {
        try {
            chefService.updateSchedule(id, eventId, chefId, plan, status);
            return "redirect:/schedule-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to update schedule");
            try { model.addAttribute("schedule", chefService.getScheduleById(id)); } catch (Exception ignore) {}
            model.addAttribute("actionUrl", "/schedule-form/" + id);
            model.addAttribute("activePage", "schedules");
            model.addAttribute("pageTitle", "Edit Schedule");
            return "schedule-form";
        }
    }

    // Delete schedule (POST) - optional if used in list page
    @PostMapping("/schedule-delete/{id}")
    public String deleteSchedule(@PathVariable int id) {
        try {
            chefService.deleteSchedule(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/schedule-list";
    }
}
