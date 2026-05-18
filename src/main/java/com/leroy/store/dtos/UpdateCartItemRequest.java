package com.leroy.store.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull(message = "Quantity mus be provided")
    @Min(value = 1, message = "Quantity must be greater than zero.")
    private Integer quantity;
}
