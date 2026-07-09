package com.uca.pncparcialfinalrestaurante.config;

import com.uca.pncparcialfinalrestaurante.common.enums.Role;
import com.uca.pncparcialfinalrestaurante.common.enums.TableStatus;
import com.uca.pncparcialfinalrestaurante.product.Product;
import com.uca.pncparcialfinalrestaurante.product.ProductRepository;
import com.uca.pncparcialfinalrestaurante.restaurant.Restaurant;
import com.uca.pncparcialfinalrestaurante.restaurant.RestaurantRepository;
import com.uca.pncparcialfinalrestaurante.table.RestaurantTable;
import com.uca.pncparcialfinalrestaurante.table.TableRepository;
import com.uca.pncparcialfinalrestaurante.user.User;
import com.uca.pncparcialfinalrestaurante.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final TableRepository tableRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Restaurant central = Restaurant.builder()
                .name("Sucursal Central")
                .address("San Salvador, Centro")
                .phone("2222-1111")
                .active(true)
                .build();

        Restaurant santaTecla = Restaurant.builder()
                .name("Sucursal Santa Tecla")
                .address("Santa Tecla, La Libertad")
                .phone("2222-2222")
                .active(true)
                .build();

        restaurantRepository.save(central);
        restaurantRepository.save(santaTecla);

        User admin = User.builder()
                .fullName("Administrador General")
                .username("admin")
                .email("admin@restaurante.com")
                .password(passwordEncoder.encode("Admin123"))
                .role(Role.ADMIN)
                .build();

        User managerCentral = User.builder()
                .fullName("Encargado Central")
                .username("manager.central")
                .email("manager.central@restaurante.com")
                .password(passwordEncoder.encode("Manager123"))
                .role(Role.SHIFT_MANAGER)
                .restaurant(central)
                .build();

        User managerSantaTecla = User.builder()
                .fullName("Encargado Santa Tecla")
                .username("manager.tecla")
                .email("manager.tecla@restaurante.com")
                .password(passwordEncoder.encode("Manager123"))
                .role(Role.SHIFT_MANAGER)
                .restaurant(santaTecla)
                .build();

        User client = User.builder()
                .fullName("Cliente de Prueba")
                .username("cliente")
                .email("cliente@restaurante.com")
                .password(passwordEncoder.encode("Cliente123"))
                .role(Role.CLIENT)
                .build();

        userRepository.save(admin);
        userRepository.save(managerCentral);
        userRepository.save(managerSantaTecla);
        userRepository.save(client);

        tableRepository.save(RestaurantTable.builder()
                .number(1)
                .capacity(4)
                .status(TableStatus.AVAILABLE)
                .restaurant(central)
                .build());

        tableRepository.save(RestaurantTable.builder()
                .number(2)
                .capacity(6)
                .status(TableStatus.AVAILABLE)
                .restaurant(central)
                .build());

        tableRepository.save(RestaurantTable.builder()
                .number(1)
                .capacity(4)
                .status(TableStatus.AVAILABLE)
                .restaurant(santaTecla)
                .build());

        tableRepository.save(RestaurantTable.builder()
                .number(2)
                .capacity(8)
                .status(TableStatus.AVAILABLE)
                .restaurant(santaTecla)
                .build());

        productRepository.save(Product.builder()
                .name("Hamburguesa clásica")
                .description("Hamburguesa con carne, queso, lechuga y tomate")
                .price(new BigDecimal("5.99"))
                .available(true)
                .build());

        productRepository.save(Product.builder()
                .name("Pizza personal")
                .description("Pizza individual de queso y pepperoni")
                .price(new BigDecimal("6.50"))
                .available(true)
                .build());

        productRepository.save(Product.builder()
                .name("Papas fritas")
                .description("Orden de papas fritas")
                .price(new BigDecimal("2.25"))
                .available(true)
                .build());

        productRepository.save(Product.builder()
                .name("Bebida natural")
                .description("Bebida natural del día")
                .price(new BigDecimal("1.75"))
                .available(true)
                .build());
    }
}