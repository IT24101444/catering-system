package com.example.catering_system.controller;

import com.example.catering_system.model.MenuItem;
import com.example.catering_system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@Controller
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public String showMenu(Model model) {
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "menu";  // Ensure this is mapped to the menu.html template
    }

    @GetMapping("/menu-react")
    public String showMenuReact(Model model) {
        return "menu-react";
    }

    @PostMapping("/menu/add")
    public String addMenuItem(@RequestParam String name, @RequestParam String description,
                              @RequestParam double price,
                              @RequestParam(required = false) MultipartFile image,
                              @RequestParam String category,
                              @RequestParam(required = false) String cuisine,
                              @RequestParam(required = false) String eventType,
                              @RequestParam(required = false) String dishesIncluded) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(price);
        if (image != null && !image.isEmpty()) {
            String filename = System.currentTimeMillis() + "-" + StringUtils.cleanPath(image.getOriginalFilename());
            try {
                java.nio.file.Path uploadDir = java.nio.file.Paths.get("catering-system/src/main/resources/static/uploads");
                java.nio.file.Files.createDirectories(uploadDir);
                java.nio.file.Path dest = uploadDir.resolve(filename);
                image.transferTo(dest.toFile());
                menuItem.setImageUrl("/uploads/" + filename);
            } catch (Exception e) {
                menuItem.setImageUrl("");
            }
        }
        menuItem.setCategory(category);
        menuItem.setCuisine(cuisine);
        menuItem.setEventType(eventType);
        menuItem.setDishesIncluded(dishesIncluded);

        menuService.addMenuItem(menuItem);

        return "redirect:/menu";  // Redirect to /menu after adding the item
    }

    @PostMapping("/menu/{id}/edit")
    public String editMenuItem(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam double price,
                               @RequestParam(required = false) MultipartFile image,
                               @RequestParam String category,
                               @RequestParam(required = false) String cuisine,
                               @RequestParam(required = false) String eventType,
                               @RequestParam(required = false) String dishesIncluded) {
        menuService.getAllMenuItems().stream().filter(m -> m.getId().equals(id)).findFirst().ifPresent(m -> {
            m.setName(name);
            m.setDescription(description);
            m.setPrice(price);
            if (image != null && !image.isEmpty()) {
                String filename = System.currentTimeMillis() + "-" + StringUtils.cleanPath(image.getOriginalFilename());
                try {
                    java.nio.file.Path uploadDir = java.nio.file.Paths.get("catering-system/src/main/resources/static/uploads");
                    java.nio.file.Files.createDirectories(uploadDir);
                    java.nio.file.Path dest = uploadDir.resolve(filename);
                    image.transferTo(dest.toFile());
                    m.setImageUrl("/uploads/" + filename);
                } catch (Exception ignored) { }
            }
            m.setCategory(category);
            m.setCuisine(cuisine);
            m.setEventType(eventType);
            m.setDishesIncluded(dishesIncluded);
            menuService.addMenuItem(m);
        });
        return "redirect:/menu";
    }

    @PostMapping("/menu/{id}/delete")
    public String archiveMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return "redirect:/menu";
    }
}
