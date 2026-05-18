package com.leroy.store.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CartProductDto {
    private UUID id;
    private String name;
    private BigDecimal price;
}
