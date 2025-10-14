package com.example.catering_system.service;

import com.example.catering_system.model.InventoryItem;
import com.example.catering_system.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository repository;

    public InventoryService(InventoryItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InventoryItem upsert(String name, Double quantity, String unit) {
        InventoryItem item = repository.findByNameIgnoreCase(name).orElseGet(InventoryItem::new);
        item.setName(name);
        if (quantity != null) item.setQuantity(quantity);
        if (unit != null) item.setUnit(unit);
        item.setLastUpdated(java.time.LocalDateTime.now());
        return repository.save(item);
    }

    @Transactional
    public void adjust(String name, double delta) {
        InventoryItem item = repository.findByNameIgnoreCase(name).orElseGet(() -> {
            InventoryItem i = new InventoryItem();
            i.setName(name);
            i.setQuantity(0.0);
            i.setUnit("pcs");
            return i;
        });
        double newQty = (item.getQuantity() != null ? item.getQuantity() : 0.0) + delta;
        item.setQuantity(newQty);
        item.setLastUpdated(java.time.LocalDateTime.now());
        repository.save(item);
    }

    public List<InventoryItem> list() { return repository.findAll(); }
    public void delete(Long id) { repository.deleteById(id); }
}


