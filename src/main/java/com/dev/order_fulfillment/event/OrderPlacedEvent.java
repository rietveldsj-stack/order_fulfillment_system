package com.dev.order_fulfillment.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderPlacedEvent(Long orderId,
                               List<OrderPlacedItem> items,
                               BigDecimal totalAmount) {
}
