package com.example.catering_system.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Menu") // Use exact table name as created in SQL Server (avoid pluralization surprises)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Identity is typical for SQL Server
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "Status", nullable = false, length = 50)
    private String status; // e.g., "confirmed", "planned", "in_progress"

    @Column(name = "EventId", nullable = false)
    private Integer eventId;

    // Optional: store items as a JSON-ish text or a join table.
    // For simplicity, persist as a single TEXT column with comma-separated values.
    @Column(name = "Items", columnDefinition = "NVARCHAR(MAX)")
    private String itemsCsv;

    // Transient list view for templates/forms
    @Transient
    private List<String> items = new ArrayList<>();

    public Menu() {}

    public Menu(Integer id, String name, String status, Integer eventId, List<String> items) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.eventId = eventId;
        setItems(items);
    }

    // Getters/setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public List<String> getItems() {
        if (items == null) items = new ArrayList<>();
        return items;
    }

    public void setItems(List<String> items) {
        this.items = (items == null) ? new ArrayList<>() : new ArrayList<>(items);
        // keep CSV in sync
        if (this.items.isEmpty()) {
            this.itemsCsv = null;
        } else {
            this.itemsCsv = String.join(",", this.items);
        }
    }

    public String getItemsCsv() { return itemsCsv; }
    public void setItemsCsv(String itemsCsv) {
        this.itemsCsv = (itemsCsv == null || itemsCsv.isBlank()) ? null : itemsCsv;
        // keep list in sync
        if (this.itemsCsv == null) {
            this.items = new ArrayList<>();
        } else {
            String[] parts = this.itemsCsv.split("\\s*,\\s*");
            List<String> list = new ArrayList<>();
            for (String p : parts) {
                if (!p.isBlank()) list.add(p);
            }
            this.items = list;
        }
    }

    // Convenience
    @Transient
    public boolean isConfirmed() {
        return this.status != null && "confirmed".equalsIgnoreCase(this.status);
    }
}
