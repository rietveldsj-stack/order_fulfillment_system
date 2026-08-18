package com.dev.order_fulfillment.order;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order){

        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(orderItem ->
                    new OrderItemResponse(orderItem.getProduct().getName(), orderItem.getQuantity(), orderItem.getPriceAtPurchase()))
                .toList();

        return new OrderResponse(order.getId(),
                order.getOrderStatus(),
                order.getCustomer().getId(),
                orderItemResponses,
                order.getTotalAmount(),
                order.getOrderDate());
    }
}
