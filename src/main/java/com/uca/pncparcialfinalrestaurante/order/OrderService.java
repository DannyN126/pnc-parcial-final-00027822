package com.uca.pncparcialfinalrestaurante.order;

import com.uca.pncparcialfinalrestaurante.common.enums.OrderStatus;
import com.uca.pncparcialfinalrestaurante.common.enums.Role;
import com.uca.pncparcialfinalrestaurante.common.enums.TableStatus;
import com.uca.pncparcialfinalrestaurante.product.Product;
import com.uca.pncparcialfinalrestaurante.product.ProductRepository;
import com.uca.pncparcialfinalrestaurante.security.AuthenticatedUserService;
import com.uca.pncparcialfinalrestaurante.table.RestaurantTable;
import com.uca.pncparcialfinalrestaurante.table.TableRepository;
import com.uca.pncparcialfinalrestaurante.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public List<Order> findAll() {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return orderRepository.findAll();
        }

        if (currentUser.getRole() == Role.SHIFT_MANAGER) {
            return orderRepository.findByRestaurantId(currentUser.getRestaurant().getId());
        }

        if (currentUser.getRole() == Role.CLIENT) {
            return orderRepository.findByClientId(currentUser.getId());
        }

        throw new SecurityException("No tiene permisos para ver pedidos");
    }

    public List<Order> findMyOrders() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return orderRepository.findByClientId(currentUser.getId());
    }

    public Order findById(Long id) {
        Order order = getOrder(id);
        validateOrderAccess(order);
        return order;
    }

    public Order create(CreateOrderRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() != Role.CLIENT) {
            throw new SecurityException("Solo los clientes pueden crear pedidos");
        }

        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        if (table.getStatus() == TableStatus.INACTIVE) {
            throw new IllegalArgumentException("La mesa no está disponible");
        }

        Order order = Order.builder()
                .client(currentUser)
                .table(table)
                .restaurant(table.getRestaurant())
                .status(OrderStatus.PENDING)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            if (!Boolean.TRUE.equals(product.getAvailable())) {
                throw new IllegalArgumentException("El producto no está disponible: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(item);
            total = total.add(subtotal);
        }

        order.setTotal(total);
        table.setStatus(TableStatus.OCCUPIED);

        return orderRepository.save(order);
    }

    public Order confirm(Long id) {
        Order order = getOrder(id);
        validateBranchForManager(order);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Solo se pueden confirmar pedidos pendientes");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    public Order complete(Long id) {
        Order order = getOrder(id);
        validateBranchForManager(order);

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Solo se pueden completar pedidos confirmados");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.getTable().setStatus(TableStatus.AVAILABLE);

        return orderRepository.save(order);
    }

    public Order cancel(Long id) {
        Order order = getOrder(id);
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == Role.CLIENT) {
            if (!order.getClient().getId().equals(currentUser.getId())) {
                throw new SecurityException("No puede cancelar pedidos de otro cliente");
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalArgumentException("El cliente solo puede cancelar pedidos pendientes");
            }
        } else {
            validateBranchForManager(order);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.getTable().setStatus(TableStatus.AVAILABLE);

        return orderRepository.save(order);
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    private void validateOrderAccess(Order order) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (currentUser.getRole() == Role.CLIENT) {
            if (!order.getClient().getId().equals(currentUser.getId())) {
                throw new SecurityException("No puede ver pedidos de otro cliente");
            }
            return;
        }

        if (currentUser.getRole() == Role.SHIFT_MANAGER) {
            validateBranchForManager(order);
            return;
        }

        throw new SecurityException("No tiene permisos para acceder al pedido");
    }

    /*
     * Regla de negocio no trivial:
     * Un encargado de turno solo puede gestionar pedidos de su propia sucursal.
     */
    private void validateBranchForManager(Order order) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (currentUser.getRole() != Role.SHIFT_MANAGER) {
            throw new SecurityException("No tiene permisos para gestionar este pedido");
        }

        Long managerRestaurantId = currentUser.getRestaurant().getId();
        Long orderRestaurantId = order.getRestaurant().getId();

        if (!managerRestaurantId.equals(orderRestaurantId)) {
            throw new SecurityException("No puede gestionar pedidos de otra sucursal");
        }
    }
}