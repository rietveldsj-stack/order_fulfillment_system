package com.dev.order_fulfillment.event;

public record OrderPlacedItem (Long productId,
                               Integer quantity){
}
