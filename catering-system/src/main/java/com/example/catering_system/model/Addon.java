package com.example.catering_system.model;

import jakarta.persistence.*;

@Entity
public class Addon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name; // e.g., Live Kitchen, Cocktail

    @Column
    private Double pricePerGuest; // optional per-guest price

    @Column
    private Double flatPrice; // optional flat price

    @Column
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPricePerGuest() { return pricePerGuest; }
    public void setPricePerGuest(Double pricePerGuest) { this.pricePerGuest = pricePerGuest; }
    public Double getFlatPrice() { return flatPrice; }
    public void setFlatPrice(Double flatPrice) { this.flatPrice = flatPrice; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}


