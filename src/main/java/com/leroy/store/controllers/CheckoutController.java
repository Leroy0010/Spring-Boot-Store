package com.leroy.store.controllers;

import com.leroy.store.dtos.CheckoutRequest;
import com.leroy.store.dtos.CheckoutResponse;
import com.leroy.store.dtos.ErrorDto;
import com.leroy.store.exceptions.CartEmptyException;
import com.leroy.store.exceptions.CartNotFoundException;
import com.leroy.store.services.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        return ResponseEntity.ok(checkoutService.checkout(checkoutRequest));
    }



    @ExceptionHandler({CartNotFoundException.class, CartEmptyException.class})
    public ResponseEntity<ErrorDto> handleCheckoutException(Exception ex) {
        return ResponseEntity.badRequest().body(new  ErrorDto(ex.getMessage()));
    }
}
