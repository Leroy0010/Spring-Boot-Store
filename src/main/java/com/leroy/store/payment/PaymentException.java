package com.leroy.store.payment;

public class PaymentException extends RuntimeException {
    public PaymentException() {
    }
    public PaymentException(String message) {
        super(message);
    }
}
