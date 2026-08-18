package com.dev.order_fulfillment.exceptionHandling;

public class NotFound extends RuntimeException {
    public NotFound(String message) {
        super(message);
    }
}
