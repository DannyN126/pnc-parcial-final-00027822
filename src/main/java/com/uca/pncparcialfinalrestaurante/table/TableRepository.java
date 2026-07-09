package com.uca.pncparcialfinalrestaurante.table;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurantId(Long restaurantId);
}