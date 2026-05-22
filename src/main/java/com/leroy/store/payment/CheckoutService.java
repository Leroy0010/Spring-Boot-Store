package com.leroy.store.payment;

import com.leroy.store.entities.Order;
import com.leroy.store.exceptions.CartEmptyException;
import com.leroy.store.exceptions.CartNotFoundException;
import com.leroy.store.repositories.CartRepository;
import com.leroy.store.repositories.OrderRepository;
import com.leroy.store.services.AuthService;
import com.leroy.store.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;




    @Transactional
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

        try {
            var sessionUrl = paymentGateway.createCheckoutSession(saved).getCheckoutUrl();

            cartService.clearCart(cart.getId());

            return new CheckoutResponse(saved.getId(), sessionUrl);
        } catch (PaymentException e) {
            orderRepository.delete(saved);
            throw e;
        }
    }

    public void handleWebhookEvent(WebhookRequest webhookRequest) {
        paymentGateway
                .parseWebhookRequest(webhookRequest)
                .ifPresent(paymentResult -> {
                    var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
                    order.setStatus(paymentResult.getPaymentStatus());
                    orderRepository.save(order);
                });
    }

}
