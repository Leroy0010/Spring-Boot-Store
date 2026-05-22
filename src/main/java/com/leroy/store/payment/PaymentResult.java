package com.leroy.store.payment;

import com.leroy.store.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PaymentResult {
    private UUID orderId;
    private PaymentStatus paymentStatus;
}
