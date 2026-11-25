package com.leroy.store.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest {

    private static Product product(String price) {
        var p = new Product();
        p.setId(UUID.randomUUID());
        p.setPrice(new BigDecimal(price));
        return p;
    }

    @Test
    void addingSameProductTwiceIncrementsQuantity() {
        var cart = new Cart();
        var p = product("10.00");

        cart.addItem(p);
        cart.addItem(p);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getCartItem(p.getId()).getQuantity()).isEqualTo(2);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void removeItemFindsItemByProductId() {
        var cart = new Cart();
        var p = product("5.00");
        cart.addItem(p);

        cart.removeItem(p.getId());

        assertThat(cart.isEmpty()).isTrue();
    }
}
