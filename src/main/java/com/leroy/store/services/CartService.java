package com.leroy.store.services;

import com.leroy.store.dtos.CartDto;
import com.leroy.store.dtos.CartItemDto;
import com.leroy.store.dtos.UpdateCartItemRequest;
import com.leroy.store.entities.Cart;
import com.leroy.store.exceptions.CartNotFoundException;
import com.leroy.store.exceptions.ProductNotFoundException;
import com.leroy.store.mappers.CartMapper;
import com.leroy.store.repositories.CartRepository;
import com.leroy.store.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;

    public CartDto createCart(){
        var cart = new Cart();
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    public CartItemDto addItemToCart(UUID cartId, UUID productId){
        var cart =  cartRepository.findCartWithItems(cartId).orElse(null);
        if (cart == null)
            throw new CartNotFoundException("Cart not found");

        var product = productRepository.findById(productId).orElse(null);
        if (product == null)
            throw new ProductNotFoundException("Product not found");

        var cartItem = cart.addItem(product);

        cartRepository.save(cart);

        return cartMapper.toDto(cartItem);
    }

    public CartDto getCart(UUID cartId){
        var cart = cartRepository.findCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart not found");
        }
        return cartMapper.toDto(cart);
    }

    public CartItemDto updateCartItem(UUID cartId, UUID productId, UpdateCartItemRequest request){
        var cart = cartRepository.findCartWithItems(cartId).orElse(null);
        if (cart == null)
            throw new CartNotFoundException("Cart not found");

        var cartItem = cart.getCartItem(productId);

        if (cartItem == null)
            throw new ProductNotFoundException("Product not found");

        cartItem.setQuantity(request.getQuantity());

        cartRepository.save(cart);
        return cartMapper.toDto(cartItem);
    }

    public void removeItem(UUID cartId, UUID productId){
        var cart = cartRepository.findCartWithItems(cartId).orElse(null);
        if (cart == null)
            throw new CartNotFoundException("Cart not found");

        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public void clearCart(UUID cartId){
        var cart = cartRepository.findCartWithItems(cartId).orElse(null);
        if (cart == null)
            throw new CartNotFoundException("Cart not found");

        cart.clear();
        cartRepository.save(cart);
    }
}
