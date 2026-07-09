package com.uca.pncparcialfinalrestaurante.table;

import com.uca.pncparcialfinalrestaurante.common.enums.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TableRequest {

    @NotNull
    private Integer number;

    @NotNull
    @Min(1)
    private Integer capacity;

    private TableStatus status;

    /*
     * ADMIN puede enviar restaurantId.
     * SHIFT_MANAGER usará automáticamente su sucursal.
     */
    private Long restaurantId;
}