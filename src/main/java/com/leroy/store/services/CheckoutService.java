package com.leroy.store.services;

import com.leroy.store.dtos.CheckoutRequest;
import com.leroy.store.dtos.CheckoutResponse;
import com.leroy.store.entities.Order;
import com.leroy.store.exceptions.CartEmptyException;
import com.leroy.store.exceptions.CartNotFoundException;
import com.leroy.store.repositories.CartRepository;
import com.leroy.store.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;

    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.findCartWithItems(request.getCartId()).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found");
        }

        if (cart.isEmpty()) {
            throw new CartEmptyException("Cart is empty");
        }

        var authenticatedUser = authService.getAuthenticatedUser();

        var order = Order.fromCart(cart, authenticatedUser);

        var saved = orderRepository.save(order);
        cartService.clearCart(cart.getId());
        return new CheckoutResponse(saved.getId());
    }


}
