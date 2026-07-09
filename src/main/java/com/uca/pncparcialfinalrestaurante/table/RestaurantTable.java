package com.uca.pncparcialfinalrestaurante.table;

import com.uca.pncparcialfinalrestaurante.common.enums.TableStatus;
import com.uca.pncparcialfinalrestaurante.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Número visible de la mesa dentro de la sucursal.
     */
    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}