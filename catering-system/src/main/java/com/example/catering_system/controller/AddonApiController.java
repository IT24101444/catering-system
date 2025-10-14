package com.example.catering_system.controller;

import com.example.catering_system.model.Addon;
import com.example.catering_system.service.AddonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
public class AddonApiController {
    private final AddonService service;
    public AddonApiController(AddonService service) { this.service = service; }
    @GetMapping
    public List<Addon> list() { return service.listActive(); }
}


