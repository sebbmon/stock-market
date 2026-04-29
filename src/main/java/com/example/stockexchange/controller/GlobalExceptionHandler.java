package com.example.stockexchange.controller;

import com.example.stockexchange.exception.InsufficientStockException;
import com.example.stockexchange.exception.InsufficientWalletStockException;
import com.example.stockexchange.exception.StockNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<String> handleStockNotFound(StockNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler({InsufficientStockException.class, InsufficientWalletStockException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }
}
