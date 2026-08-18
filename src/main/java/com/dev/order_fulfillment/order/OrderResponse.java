package com.dev.order_fulfillment.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id,
                            OrderStatus orderStatus,
                            Long customerId,
                            List<OrderItemResponse> orderItems,
                            BigDecimal totalAmount,
                            LocalDateTime orderDate) {
}
