package com.leroy.store.services;

import com.leroy.store.dtos.OrderResponse;
import com.leroy.store.exceptions.OrderNotFoundException;
import com.leroy.store.mappers.OrderMapper;
import com.leroy.store.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer() {
        var user = authService.getAuthenticatedUser();
        return orderRepository.getAllWithItemsByCustomer(user)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        var order = orderRepository
                .getOrderWithItems(orderId).orElseThrow(() -> new OrderNotFoundException("Order not exception"));

        var user = authService.getAuthenticatedUser();

        if (!order.isPlacedBy(user)) {
            throw  new AccessDeniedException("You don't have access to this order");
        }
        return orderMapper.toOrderResponse(order);
    }
}
