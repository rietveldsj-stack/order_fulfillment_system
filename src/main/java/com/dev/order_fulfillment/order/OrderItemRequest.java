package com.dev.order_fulfillment.order;

public record OrderItemRequest(Long productId, Integer quantity) {
}
