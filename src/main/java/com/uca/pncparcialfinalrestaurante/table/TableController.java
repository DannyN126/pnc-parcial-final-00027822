package com.uca.pncparcialfinalrestaurante.table;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHIFT_MANAGER')")
public class TableController {

    private final TableService tableService;

    @GetMapping
    public List<RestaurantTable> findAll() {
        return tableService.findAll();
    }

    @GetMapping("/{id}")
    public RestaurantTable findById(@PathVariable Long id) {
        return tableService.findById(id);
    }

    @PostMapping
    public RestaurantTable create(@Valid @RequestBody TableRequest request) {
        return tableService.create(request);
    }

    @PutMapping("/{id}")
    public RestaurantTable update(
            @PathVariable Long id,
            @Valid @RequestBody TableRequest request
    ) {
        return tableService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tableService.delete(id);
    }
}