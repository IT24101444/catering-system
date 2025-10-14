package com.example.catering_system.service;

import com.example.catering_system.model.Addon;
import com.example.catering_system.repository.AddonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddonService {
    private final AddonRepository repo;
    public AddonService(AddonRepository repo) { this.repo = repo; }
    public List<Addon> listActive() { return repo.findByActiveTrue(); }
    public Addon save(Addon a) { return repo.save(a); }
    public void delete(Long id) { repo.deleteById(id); }
}


