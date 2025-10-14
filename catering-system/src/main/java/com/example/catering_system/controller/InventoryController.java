package com.example.catering_system.controller;

import com.example.catering_system.model.InventoryItem;
import com.example.catering_system.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/kitchen/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", inventoryService.list());
        return "kitchen-inventory";
    }

    @PostMapping("/upsert")
    public String upsert(@RequestParam String name, @RequestParam Double quantity, @RequestParam String unit) {
        inventoryService.upsert(name, quantity, unit);
        return "redirect:/kitchen/inventory";
    }

    @PostMapping("/adjust")
    public String adjust(@RequestParam String name, @RequestParam double delta) {
        inventoryService.adjust(name, delta);
        return "redirect:/kitchen/inventory";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return "redirect:/kitchen/inventory";
    }
}


