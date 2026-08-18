package com.dev.order_fulfillment.order;

import java.util.List;

public record CreateOrderRequest(List<OrderItemRequest> orderItemRequests) {
}
