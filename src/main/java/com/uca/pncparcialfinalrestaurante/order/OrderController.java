package com.uca.pncparcialfinalrestaurante.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHIFT_MANAGER', 'CLIENT')")
    public List<Order> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Order> findMyOrders() {
        return orderService.findMyOrders();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHIFT_MANAGER', 'CLIENT')")
    public Order findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public Order create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHIFT_MANAGER')")
    public Order confirm(@PathVariable Long id) {
        return orderService.confirm(id);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHIFT_MANAGER')")
    public Order complete(@PathVariable Long id) {
        return orderService.complete(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHIFT_MANAGER', 'CLIENT')")
    public Order cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }
}