package com.leroy.store.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckoutRequest {
    @NotNull(message = "Cart ID must be provided")
    private UUID cartId;
}
