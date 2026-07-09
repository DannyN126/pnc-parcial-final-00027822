package com.uca.pncparcialfinalrestaurante.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
    }

    public Restaurant create(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .active(true)
                .build();

        return restaurantRepository.save(restaurant);
    }

    public Restaurant update(Long id, RestaurantRequest request) {
        Restaurant restaurant = findById(id);

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());

        return restaurantRepository.save(restaurant);
    }

    public void delete(Long id) {
        Restaurant restaurant = findById(id);
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }
}