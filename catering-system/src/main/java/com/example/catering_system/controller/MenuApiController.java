package com.example.catering_system.controller;

import com.example.catering_system.model.MenuItem;
import com.example.catering_system.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuApiController {

    private final MenuService menuService;

    public MenuApiController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItem> list() { return menuService.getAllMenuItems(); }

    @GetMapping("/{id}")
    public MenuItem get(@PathVariable Long id) {
        return menuService.getAllMenuItems().stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }
}

@RestController
@RequestMapping("/api/menu-items")
class MenuItemsAliasController {
    private final MenuService menuService;
    public MenuItemsAliasController(MenuService menuService) { this.menuService = menuService; }
    @GetMapping
    public List<MenuItem> list() { return menuService.getAllMenuItems(); }
}


