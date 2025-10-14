package com.example.catering_system.controller;

import com.example.catering_system.model.Review;
import com.example.catering_system.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ReviewController {

    private final ReviewRepository repo;

    public ReviewController(ReviewRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/reviews/add")
    public String add(@RequestParam String name, @RequestParam Integer rating, @RequestParam String comment) {
        Review r = new Review();
        r.setName(name);
        r.setRating(rating);
        r.setComment(comment);
        repo.save(r);
        return "redirect:/";
    }

    @GetMapping("/reviews/recent")
    @ResponseBody
    public ResponseEntity<List<Review>> recent() {
        return ResponseEntity.ok(repo.findTop10ByOrderByCreatedAtDesc());
    }
}


