package com.dev.order_fulfillment.event;

import java.util.List;

public record PaymentCompletedEvent(Long orderId,
                                    Long customerId,
                                    List<OrderPlacedItem> items) {
}
