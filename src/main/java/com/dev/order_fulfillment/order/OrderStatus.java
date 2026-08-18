package com.dev.order_fulfillment.order;

public enum OrderStatus {
    ORDER_PLACED,
    RESERVED,
    OUT_OF_STOCK,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    SHIPPED,
    CANCELLED
}
