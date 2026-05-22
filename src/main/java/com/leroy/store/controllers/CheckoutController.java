package com.leroy.store.controllers;

import com.leroy.store.payment.CheckoutRequest;
import com.leroy.store.payment.CheckoutResponse;
import com.leroy.store.dtos.ErrorDto;
import com.leroy.store.exceptions.CartEmptyException;
import com.leroy.store.exceptions.CartNotFoundException;
import com.leroy.store.payment.PaymentException;
import com.leroy.store.payment.CheckoutService;
import com.leroy.store.payment.WebhookRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;


    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        return ResponseEntity.ok(checkoutService.checkout(checkoutRequest));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload
    ){
        checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));
        return  ResponseEntity.noContent().build();
    }

    @ExceptionHandler({CartNotFoundException.class, CartEmptyException.class})
    public ResponseEntity<ErrorDto> handleCheckoutException(Exception ex) {
        return ResponseEntity.badRequest().body(new  ErrorDto(ex.getMessage()));
    }


    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto("Error creating checkout session"));
    }


}
