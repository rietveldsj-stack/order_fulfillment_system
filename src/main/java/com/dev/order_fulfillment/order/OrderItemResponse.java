package com.dev.order_fulfillment.order;

import java.math.BigDecimal;

public record OrderItemResponse(String productName,
                                Integer quantity,
                                BigDecimal priceAtPurchase) {
}
