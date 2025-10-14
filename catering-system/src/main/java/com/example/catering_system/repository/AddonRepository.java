package com.example.catering_system.repository;

import com.example.catering_system.model.Addon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddonRepository extends JpaRepository<Addon, Long> {
    List<Addon> findByActiveTrue();
}


