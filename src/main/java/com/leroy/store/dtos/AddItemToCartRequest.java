package com.leroy.store.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddItemToCartRequest {
    @NotNull(message = "Product Id must be provided")
    private UUID productId;
}
