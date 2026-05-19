package com.leroy.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.MERGE, orphanRemoval = true,  fetch = FetchType.EAGER)
    private Set<CartItem> items = new LinkedHashSet<>();

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CartItem getCartItem(UUID productId) {
        return items.stream()
                .filter(item -> item.getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public CartItem addItem(Product product) {
        var cartItem = getCartItem(product.getId());

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setCart(this);
            getItems().add(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }

        return cartItem;
    }

    public void removeItem(UUID productId) {
        var cartItem = getCartItem(productId);
        if (cartItem != null) {
            items.remove(cartItem);
            cartItem.setCart(null);
        }
    }

    public void clear(){
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

}