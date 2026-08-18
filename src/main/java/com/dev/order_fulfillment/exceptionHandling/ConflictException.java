package com.dev.order_fulfillment.exceptionHandling;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
