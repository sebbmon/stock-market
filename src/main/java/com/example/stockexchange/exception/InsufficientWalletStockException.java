package com.example.stockexchange.exception;

public class InsufficientWalletStockException extends RuntimeException {
    public InsufficientWalletStockException(String message) {
        super(message);
    }
}
