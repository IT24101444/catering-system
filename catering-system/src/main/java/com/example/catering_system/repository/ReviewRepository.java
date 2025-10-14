package com.example.catering_system.repository;

import com.example.catering_system.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop10ByOrderByCreatedAtDesc();
}


