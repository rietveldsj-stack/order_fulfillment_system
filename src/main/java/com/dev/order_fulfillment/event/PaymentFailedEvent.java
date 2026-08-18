package com.dev.order_fulfillment.event;

import java.util.List;

public record PaymentFailedEvent (Long orderId,
                                  Long customerId,
                                  List<OrderPlacedItem> items){
}
