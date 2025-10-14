package com.example.catering_system.controller;

import com.example.catering_system.model.Invoice;
import com.example.catering_system.model.MenuItem;
import com.example.catering_system.model.Order;
import com.example.catering_system.service.InvoiceService;
import com.example.catering_system.service.MenuService;
import com.example.catering_system.service.OrderService;
import com.example.catering_system.service.SessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {

    private final OrderService orderService;
    private final MenuService menuService;
    private final InvoiceService invoiceService;

    public BookingApiController(OrderService orderService, MenuService menuService, InvoiceService invoiceService) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<?> create(@CookieValue(value = "SESSION_ID", required = false) String sessionId,
                                    @RequestBody Map<String, Object> payload) {
        var user = SessionManager.getInstance().getUser(sessionId);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error","Login required"));

        try {
            @SuppressWarnings("unchecked")
            List<Integer> menuIds = (List<Integer>) payload.getOrDefault("menuIds", List.of());
            int guestCount = ((Number) payload.getOrDefault("guestCount", 0)).intValue();
            @SuppressWarnings("unchecked")
            List<Integer> addonIds = (List<Integer>) payload.getOrDefault("addonIds", List.of());
            String eventDate = String.valueOf(payload.get("eventDate"));
            String eventType = String.valueOf(payload.getOrDefault("eventType", ""));
            String location = String.valueOf(payload.getOrDefault("location", ""));
            String dietary = String.valueOf(payload.getOrDefault("dietaryRequirements", ""));

            List<MenuItem> selected = menuService.getAllMenuItems().stream()
                    .filter(m -> menuIds.contains(m.getId().intValue())).collect(Collectors.toList());
            double perPlate = selected.stream().mapToDouble(MenuItem::getPrice).sum();
            // compute addons
            double addonsFlat = 0.0;
            double addonsPerGuest = 0.0;
            try {
                var ctx = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
                if (ctx != null) {
                    var addonRepo = ctx.getBean(com.example.catering_system.repository.AddonRepository.class);
                    var addons = addonRepo.findAllById(addonIds.stream().map(Long::valueOf).toList());
                    for (var a : addons) {
                        if (a.getFlatPrice() != null) addonsFlat += a.getFlatPrice();
                        if (a.getPricePerGuest() != null) addonsPerGuest += a.getPricePerGuest();
                    }
                }
            } catch (Exception ignored) {}
            double totalAmount = perPlate * guestCount + addonsFlat + (addonsPerGuest * guestCount);

            Order o = new Order();
            o.setCustomerId(user.getId());
            o.setCustomerName(user.getFullName());
            o.setCustomerEmail(user.getEmail());
            o.setEventDate(new SimpleDateFormat("yyyy-MM-dd").parse(eventDate));
            o.setEventType(eventType);
            o.setLocation(location);
            o.setGuestCount(guestCount);
            o.setDietaryRequirements(dietary);
            o.setSelectedMenu(selected.stream().map(MenuItem::getName).collect(Collectors.joining(",")));
            o.setPerPlatePrice(perPlate);
            o.setTotalAmount(totalAmount);
            o.setStatus("Pending");
            orderService.save(o);

            Invoice inv = new Invoice();
            inv.setCustomerId(user.getId());
            inv.setAmount(totalAmount);
            inv.setIssueDate(new Date());
            inv.setDueDate(new Date(System.currentTimeMillis() + 7L*24*3600*1000));
            inv.setStatus("Unpaid");
            invoiceService.saveInvoice(inv);

            return ResponseEntity.ok(Map.of("orderId", o.getId(), "invoiceId", inv.getId(), "amount", totalAmount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}


