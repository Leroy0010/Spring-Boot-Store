package com.leroy.store.mappers;

import com.leroy.store.dtos.CartDto;
import com.leroy.store.dtos.CartItemDto;
import com.leroy.store.entities.Cart;
import com.leroy.store.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);

    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    CartItemDto toDto(CartItem cartItem);
}
