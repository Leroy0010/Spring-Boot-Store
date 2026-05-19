package com.leroy.store.mappers;

import com.leroy.store.dtos.OrderResponse;
import com.leroy.store.entities.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse  toOrderResponse(Order order);
}
