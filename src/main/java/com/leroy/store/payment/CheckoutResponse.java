package com.leroy.store.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {
    private UUID checkoutId;
    private String checkoutUrl;

}
