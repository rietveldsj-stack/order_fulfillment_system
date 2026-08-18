package com.dev.order_fulfillment.event;

import java.math.BigDecimal;
import java.util.List;

public record InventoryReservedEvent(Long orderId,
                                     Long customerId,
                                     BigDecimal totalAmount,
                                     List<OrderPlacedItem> items) {
}
