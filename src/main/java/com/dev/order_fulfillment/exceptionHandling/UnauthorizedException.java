package com.dev.order_fulfillment.exceptionHandling;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
