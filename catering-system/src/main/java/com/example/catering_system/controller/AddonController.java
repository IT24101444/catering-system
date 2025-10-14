package com.example.catering_system.controller;

import com.example.catering_system.model.Addon;
import com.example.catering_system.service.AddonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/kitchen/addons")
public class AddonController {
    private final AddonService service;
    public AddonController(AddonService service) { this.service = service; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("addons", service.listActive());
        return "kitchen-addons";
    }

    @PostMapping("/add")
    public String add(@RequestParam String name,
                      @RequestParam(required = false) Double pricePerGuest,
                      @RequestParam(required = false) Double flatPrice) {
        Addon a = new Addon();
        a.setName(name);
        a.setPricePerGuest(pricePerGuest);
        a.setFlatPrice(flatPrice);
        a.setActive(true);
        service.save(a);
        return "redirect:/kitchen/addons";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/kitchen/addons";
    }
}


