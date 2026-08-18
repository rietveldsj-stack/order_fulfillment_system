package com.dev.order_fulfillment.event;

public record InventoryOutOfStockEvent(Long orderId,
                                       Long customerId) {
}
