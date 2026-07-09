package com.uca.pncparcialfinalrestaurante.table;

import com.uca.pncparcialfinalrestaurante.common.enums.Role;
import com.uca.pncparcialfinalrestaurante.common.enums.TableStatus;
import com.uca.pncparcialfinalrestaurante.restaurant.Restaurant;
import com.uca.pncparcialfinalrestaurante.restaurant.RestaurantRepository;
import com.uca.pncparcialfinalrestaurante.security.AuthenticatedUserService;
import com.uca.pncparcialfinalrestaurante.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public List<RestaurantTable> findAll() {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return tableRepository.findAll();
        }

        if (currentUser.getRole() == Role.SHIFT_MANAGER) {
            return tableRepository.findByRestaurantId(currentUser.getRestaurant().getId());
        }

        throw new SecurityException("No tiene permisos para ver mesas");
    }

    public RestaurantTable findById(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        validateBranchAccess(table.getRestaurant().getId());

        return table;
    }

    public RestaurantTable create(TableRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Restaurant restaurant;

        if (currentUser.getRole() == Role.ADMIN) {
            if (request.getRestaurantId() == null) {
                throw new IllegalArgumentException("Debe indicar la sucursal");
            }

            restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
        } else if (currentUser.getRole() == Role.SHIFT_MANAGER) {
            restaurant = currentUser.getRestaurant();
        } else {
            throw new SecurityException("No tiene permisos para crear mesas");
        }

        RestaurantTable table = RestaurantTable.builder()
                .number(request.getNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : TableStatus.AVAILABLE)
                .restaurant(restaurant)
                .build();

        return tableRepository.save(table);
    }

    public RestaurantTable update(Long id, TableRequest request) {
        RestaurantTable table = findById(id);

        table.setNumber(request.getNumber());
        table.setCapacity(request.getCapacity());

        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }

        return tableRepository.save(table);
    }

    public void delete(Long id) {
        RestaurantTable table = findById(id);
        table.setStatus(TableStatus.INACTIVE);
        tableRepository.save(table);
    }

    private void validateBranchAccess(Long restaurantId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (currentUser.getRole() == Role.SHIFT_MANAGER) {
            Long userRestaurantId = currentUser.getRestaurant().getId();

            if (!userRestaurantId.equals(restaurantId)) {
                throw new SecurityException("No puede gestionar mesas de otra sucursal");
            }

            return;
        }

        throw new SecurityException("No tiene permisos para gestionar esta mesa");
    }
}