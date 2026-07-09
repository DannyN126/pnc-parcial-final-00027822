package com.uca.pncparcialfinalrestaurante.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull
    private Long tableId;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;
}