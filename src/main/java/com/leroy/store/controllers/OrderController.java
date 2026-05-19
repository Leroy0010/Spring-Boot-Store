package com.leroy.store.controllers;

import com.leroy.store.dtos.ErrorDto;
import com.leroy.store.dtos.OrderResponse;
import com.leroy.store.exceptions.OrderNotFoundException;
import com.leroy.store.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getOrdersByCustomer());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Void> handleOrderNotFoundException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(Exception e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDto(e.getMessage()));
    }
}
