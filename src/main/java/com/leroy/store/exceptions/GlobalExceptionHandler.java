package com.leroy.store.exceptions;

import com.leroy.store.dtos.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto("Invalid request body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex){
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDto> handleAuthenticationException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDto("User not authenticated"));
    }

}
